require: ../patterns.sc
require: emotionClassifier.sc
require: catchAllClassifier.js
require: checkSameAnswer.js
require: switch.sc

theme: /
    init:
        $global.catchAll = {
            //количество попаданий в CatchAll, после которых бот предлагает оператора или говорит, что вопрос не в компетенции.
            giveUpRepetition: $injector.giveUpRepetition || 2,

            //темы, которые озвучивает бот при попадании в CatchAll
            topics: $injector.topics,

            //нужно ли просить переформулировать, если бот хочет снова ответить то же самое.
            CheckSameAnswer: $injector.CheckSameAnswer || false,

            //есть ли перевод на оператора
            withOperator: $injector.withOperator || false,

            //стейт, в который переходит бот после того, как завершился чат с оператором            
            livechatFinished: $injector.livechatFinished,

            //массив фраз, по которым выходит из диалога с оператором
            closeChatPhrases: $injector.closeChatPhrases || ["/close"],

            //группа операторов, которым будут приходить сообщения. По умолчанию приходит всем.
            operatorGroup: $injector.operatorGroup
        };

    state: CatchAll         || noContext = true
        q!: $catchAll
        script:
            $session.catchAll = $session.catchAll || {};

            //Начинаем считать попадания в кэчол с нуля, когда предыдущий стейт не кэчол.
            if ($session.lastState && !$session.lastState.startsWith("/CatchAll")) {
                $session.catchAll.repetition = 0;
            } else{
                $session.catchAll.repetition = $session.catchAll.repetition || 0;
            }

            // увеличиваем счётчик входов в catchAll
            $session.catchAll.repetition += 1;

            // определяем класс
            var clazz = catchAllClassifier.check($parseTree);

            $reactions.transition(clazz);

        state: SeemsMeaningful || noContext = true 
            if: $session.catchAll.repetition <= catchAll.giveUpRepetition
                if: catchAll.topics
                    script:
                        $reactions.answer(getAnswer('SeemsMeaningfulWithTopics'));
                else: 
                    script:
                        $reactions.answer(getAnswer('SeemsMeaningful'));
            else:
                go!: ../OutOfScope

        #Этот стейт нужен, чтобы не переводить бессмыслицу на оператора.
        state: Nonsense || noContext = true 
            if: catchAll.topics
                script:
                    $reactions.answer(getAnswer('NonsenseWithTopics'));
            else: 
                script:
                    $reactions.answer(getAnswer('Nonsense'));

        state: Transliteration
            script:
                $reactions.answer(getAnswer('Transliteration'));

            state: Yes
                q: $agree       || onlyThisState = true
                go!: {{ $session.catchAll.transliterationState }}

            state: NoUnknown
                q: $disagree    || onlyThisState = true
                go!: /CatchAll/AskAgain?


        state: NegativeEmotion  || noContext = true 
            if: catchAll.topics
                script:
                    $reactions.answer(getAnswer('NegativeEmotionWithTopics'));
            else: 
                script:
                    $reactions.answer(getAnswer('NegativeEmotion'));

        state: SameAnswer || noContext = true
            script:
                $reactions.answer(getAnswer('SameAnswer')); 


        state: AskAgain?
            script:
                $reactions.answer(getAnswer('AskAgain?')); 

        state: OutOfScope  || noContext = true
            script:
                $reactions.answer(getAnswer('OutOfScope')); 
            if: (catchAll.withOperator && hasOperatorsOnline(catchAll.operatorGroup)) || (catchAll.withOperator && testMode())
                go!: /Switch/DoYouWannaSwitch?



