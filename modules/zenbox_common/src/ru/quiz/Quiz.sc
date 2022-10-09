require: quiz.js

require: questions/quiz_football.csv
  name = QuizFootball
  var = $QuizFootball

require: number/number.sc
  module = zb_common

require: patterns.sc
  module = zb_common

patterns:
    $quizUruguay =  уругва*
    $quizGermany =  (герман*/фрг/немцы*/немецк*)
    $quizBrazil =  бразил*
    $quizArgentina =  аргентин*
    $quizItaly =  (италия/итальян*)
    $quizEngland =  (англи*/*британ*)

theme: /Quiz

    state: Quiz user initiates
        q!: * {[давай|хоч*] (поигра*|сыгра*|игра*|запусти*) * [в] (викторин*) *} * 
        q!: * {(давай|хоч*) [поигра*|сыгра*|игра*|запусти*] * [в] ((викторин*/вопрос*/тест) ([про/о] футбол*)) *} *
        q!: * {(давай|хоч*) [поигра*|сыгра*|игра*|запусти*] * [в] (([про/о] футбол*) (викторин*/вопрос*/тест)) *} *
        q!: [в] {(викторин*/вопрос*) ([про/о] футбол*)}
        script: 
            $session.questionsCounter = 0;
            $session.rightAnswersCounter = 0;
            $session.lastRoundResults = 0;
        if: $session.currentQuestionInfo
            #если в пределах текущей сессии уже играли
            go!: ../Quiz game/Get question
        else: 
            go!: ../Quiz welcome
    state: Quiz welcome
        a: Давайте сыграем в викторину! Правила просты: я задаю вопрос, предлагаю варианты ответа, а вы отвечаете. Викторина поделена на раунды по 5 вопросов. В конце каждого раунда подсчитываются результаты. Ну что же, приступим!
        go!: ../Quiz game/Get question

    state: Quiz game
        state: Get question
            if: $session.questionsCounter !==0 && $session.questionsCounter % 5 === 0 && !$temp.fromResults && !$temp.fromSkipQuestion
                go!: /Quiz/Results/Round

            script:
                $session.quizUndefinedCounter = 0;
                var success = getQuestion();
                if (success && $session.currentQuestionInfo) {
                    $session.questionsCounter++;
                    $reactions.transition("../Ask question");
                } else {
                    $reactions.answer("Упс! Кажется что-то сломалось...");
                    $reactions.transition('/');
                }
        state: Ask question
            a: {{ $session.currentQuestionInfo.question }}
            random:
                a: Варианты ответов:
                a: 
                a: 
                a: 
            a: {{ ($session.currentQuestionInfo.responseOptionsForQuestion) }}
            go: ../Get answer


        state: Get answer
            state: Number of answer
                # первый вариант; последний ответ; второй ответ и тд
                q: * {( $Number | (последн*):last) * (правильн*/вариант/ответ/решение/вердикт/выбор/выбира*/пусть [будет]/наверн*/номер/может [быть]/мб/скорее [всего])} *
                q: ( $Number | (последн*):last)

                if: $session.currentQuestionInfo.answerIsNumber && $parseTree.value != "last"
                    # если ответ - это число. Напр., вопр.:"Сколько раз бразилия становилась чемпионом?" отв.: 5
                    if: 1 <= Number($parseTree.value) && Number($parseTree.value) <= $session.currentQuestionInfo.responseOptions.length
                        script:
                            $temp.results = checkAnswer($parseTree.value, $session);
                        script:
                            $temp.maybeIndexOrAnswer = $session.currentQuestionInfo.responseOptions.reduce(function(res, el){
                               if (res) return true
                               var indexStart = el.search(/.\)\s/g)+2;
                               var subStr = el.substring(indexStart);
                               subStr = subStr.substring(subStr.search(/\d/g));

                               return Number($parseTree.value) == parseInt(subStr)
                            }, false);

                        if: $temp.maybeIndexOrAnswer
                            go!: /Quiz/Clarify misunderstandings

                        if: $session.currentQuestionInfo.rightAnswerNumber == $parseTree.value
                            go!: /Quiz/The answer is correct
                            
                        script:
                            $temp.indexIsWrong = true;
                    else:


                        script:
                            $temp.results = checkAnswer($parseTree.value, $session);
    
                        if: $temp.results == 'right' || $session.currentQuestionInfo.answerIsNumber ===  $parseTree.value
                            go!: /Quiz/The answer is correct
                    if: $temp.results == 'wrong' || $temp.indexIsWrong
                        go!: /Quiz/The answer is incorrect

                    a: Не понял. Выберите, пожалуйста, вариант ответа из предложенных.
                    go: ../../Get answer

                else:
                    if: $parseTree.value == "last" && ($session.currentQuestionInfo.rightAnswerNumber == $session.currentQuestionInfo.responseOptions.length)
                        # Пользователь:"последний вариант" И порядковый номер верного ответа РАВЕН кол-ву всех вариантов
                        go!: /Quiz/The answer is correct

                    if: $session.currentQuestionInfo.rightAnswerNumber === Number($parseTree.value)
                        go!: /Quiz/The answer is correct

                    go!: /Quiz/The answer is incorrect


            state: Probably answer
                q: *
                script:
                    $temp.results = checkAnswer($parseTree.text, $session);
                if: $temp.results == 'right'
                    go!: /Quiz/The answer is correct

                if: $temp.results == 'wrong'
                    go!: /Quiz/The answer is incorrect
                
                script: 
                    $session.quizUndefinedCounter++
                if: $session.quizUndefinedCounter == 1
                    a: Не понял. Назовите, пожалуйста вариант ответа.
                if: $session.quizUndefinedCounter == 2
                    a: Попробуйте выбрать ответ из предложенных:
                    a: {{ ($session.currentQuestionInfo.responseOptionsForQuestion) }}

                if: $session.quizUndefinedCounter == 3
                    go!: /Quiz/Skip question 
                go: ../../Get answer 



    state: Clarify misunderstandings
        a: Уточните пожалуйста: {{ $parseTree.value }} – это ответ или номер ответа?
        script:
            $session.memorizeQuizNum = Number($parseTree.value);
        state: Answer
            q: * ответ* *
            if: $session.currentQuestionInfo.answerIsNumber && Number($session.currentQuestionInfo.answerIsNumber) === $session.memorizeQuizNum
                go!: /Quiz/The answer is correct
            go!: /Quiz/The answer is incorrect

        state: Number of answer
            q: * номер* [ответ*] *
            if: $session.currentQuestionInfo.rightAnswerNumber == $session.memorizeQuizNum
                go!: /Quiz/The answer is correct
            go!: /Quiz/The answer is incorrect
                
    state: Skip question
        q: * || fromState = "/Quiz/Clarify misunderstandings"
        q: * {(пропусти*/следующ*/другой) вопрос*} * || fromState = /Quiz
        q: (пропусти*/следующ*) || fromState = /Quiz
        a: Правильный ответ: {{ $session.currentQuestionInfo.rightAnswer }}. Давайте перейдем к следующему вопросу!
        script:
            $temp.fromSkipQuestion = true;
            $session.questionsCounter--;
        go!: /Quiz/Quiz game/Get question

    state: The answer is correct
        random:
            a: Верно!
            a: Точно!
            a: Правильно!

        script: 
            $session.rightAnswersCounter++;

        go!: /Quiz/Quiz game/Get question


    state: The answer is incorrect
        if: $temp.indexIsWrong
            a: Ответ номер {{ $parseTree.value }} – неверный ответ.
        else:
            random:
                a: Не верно.
                a: К сожалению, вы ошиблись.
        if: $session.currentQuestionInfo.answerText
            a: {{ $session.currentQuestionInfo.answerText }}
        else:
            a: Правильный ответ: {{ $session.currentQuestionInfo.rightAnswer }}.

        go!: /Quiz/Quiz game/Get question

    state: Dont know
        q: * (не знаю/откуда (мне/я) * знать/хз ) * || fromState = "/Quiz"
        q: $dontKnow || fromState = "/Quiz"
        a: Попробуйте ответить наугад!
        go!: /Quiz/Quiz game/Get answer

    state: Repeat
        q: * {(повтори/какие/какой) * [вопрос/варианты]} * || fromState = "/Quiz"
        go!: /Quiz/Quiz game/Ask question

    state: Results

        state: Round
            a: Раунд завершен! {{ correctAnswersAmount($session.rightAnswersCounter - $session.lastRoundResults, 5) }}.
            script:
                $session.lastRoundResults = $session.rightAnswersCounter;

            a: Продолжаем ли мы викторину дальше?

            state: Continue the game
                q: * ($agree/продолж*) *
                script:
                    $temp.fromResults = true
                go!: /Quiz/Quiz game/Get question
            
        state: Whole game
            q: * сколько [у меня] * (балл*/правильн* ответ*) * || fromState = "/Quiz"
            q: * (сколько/количество/много) * (балл*/ответ* [правильн*]) * || fromState = "/Quiz"
            q: * результат* * || fromState = "/Quiz"
            go!: ./Somewhere in a game
            state: End of the game
                a: Результаты всей игры: {{ correctAnswersAmount($session.rightAnswersCounter, $session.questionsCounter) }}.
                go!: /

            state: Somewhere in a game
                a: Результаты всей игры: {{ correctAnswersAmount($session.rightAnswersCounter, $session.questionsCounter) }}.
                a: Продолжим викторину?

                state: Continue the game
                    q: * ($agree/продолж*) *
                    if: $session.questionsCounter !==0 && $session.questionsCounter % 5 === 0
                        script:
                            $temp.fromResults = true
                        go!: /Quiz/Quiz game/Get question

                    else:
                        go!: /Quiz/Quiz game/Get answer

                state: Finish the game
                    q: * ($disagree/не продолж*/ну нафиг) *
                    script:
                        $temp.fromResults = true
                    go!: /Quiz/Stop the game

        state: CatchAll local
            q: * || fromState = /Quiz/Results
            a: Не совсем вас понимаю. Мне задавать следующий вопрос?
            if: $session.lastRoundResults = $session.rightAnswersCounter;
                go: ../Round
            go: ../Whole game/Somewhere in a game

    state: Stop the game
        q: * ($stopGame/выключи* игр*) * || fromState = "/Quiz/Quiz game"
        q: * ($disagree/не продолж*/ну нафиг) * || fromState = "../Results/Round"
        a: Хорошо. Давайте закончим!
        if: !$temp.fromResults
            go!:  ../Results/Whole game/End of the game
        go!: /

    #state: Test
    #    q!: test $Number::QuestionID
    #    script:
    #        $session.questionsIDs = [Number($parseTree._QuestionID),Number($parseTree._QuestionID),Number($parseTree._QuestionID)]
    #    go!:  /Quiz/Quiz game/Get question