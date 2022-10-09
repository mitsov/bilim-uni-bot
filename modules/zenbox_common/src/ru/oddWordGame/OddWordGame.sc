require: OddWordGame.js
require: ../../common/oddWordGame/OddWordGame.js
require: ../../common/common.sc

require: ../Games.sc
require: odd-word-game-classes.csv
  name = oddWordGameClasses
  var = $oddWordGameClasses

require: odd-word-game-elements.csv
  name = oddWordGameElements
  var = $oddWordGameElements

require: answers.yaml
  var = OddWordGameCommonAnswers

require: ../main.js

require: patterns.sc
  module = zb_common

require: common.js
  module = zb_common

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .oddWordGameElementTagConverter = function(parseTree) {
            var id = parseTree.oddWordGameElements[0].value;
            return $oddWordGameElements[id].value;
        };

    $global.$converters
        .oddWordGameClassTagConverter = function(parseTree) {
            var id = parseTree.oddWordGameClasses[0].value;
            return $oddWordGameClasses[id].value;
        };

    $global.themes = $global.themes || [];
    $global.themes.push("Odd word game");
    $global.$OddWordGameAnswers = (typeof OddWordCustomAnswers != 'undefined') ? applyCustomAnswers(OddWordGameCommonAnswers, OddWordCustomAnswers) : OddWordGameCommonAnswers;

patterns:
    $oddWordGameElement = $entity<oddWordGameElements> || converter = $converters.oddWordGameElementTagConverter

    $oddWordGameOrder = ((~первый/1 слово):1|
             (~второй/2 слово):2|
             (~третий/3 слово/третье):3|
             (~четвертый/4 слово):4|
             (~пятый/5 слово):5|
             (~последний):lst|
             (предпоследний/предпоследняя/предпоследнее):blst)
    
    $oddWordGameStrictOrder = (1|2|3|4|5)
         
theme: /Odd word game

    state: Odd word game start
        q!: * [хочу/давай] (~играть|~сыграть|~поиграть|~игра) * [в/про] ([найди/найти] лишнее) *
        q!: * [хочу/давай] * [в/про] * (лишнее слово|найди лишнее)
        q: * (лишнее/найди) * || fromState=/PlayGames/Games, modal=true
        script:
            oddWordGameInit();
        if: (!$session.oddWordGame.again)
            a: {{selectRandomArg($OddWordGameAnswers["Odd word game start"])}} || question = true
        else:
            script:
                $session.oddWordGame.again = false;
        go: ../YesOrNo

    state: YesOrNo       
        
        state: No Odd Word game
            q: * ($disagree|$notNow|не помогу) * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) * || fromState = "/PlayGames/Games/How to play Odd word game", onlyThisState = true
            a: {{selectRandomArg($OddWordGameAnswers["YesOrNo"]["No Odd Word game"])}}

        state: Yes Odd Word game
            q: * ($agree|начин*|начн*|помогу|говори) * || fromState = .., onlyThisState = true
            q: * ($agree|начин*|начн*|говори) * || fromState = "/PlayGames/Games/How to play Odd word game", onlyThisState = true
            q: * [$agree|$maybe|$sure] * ((найди/найти) лишнее) * [$agree|$maybe|$sure] * || fromState = /PlayGames/Games, onlyThisState = true        
            script:
                oddWordGameInit();
                $temp.start = true;
            if: $temp.repeat
                a: {{selectRandomArg($OddWordGameAnswers["YesOrNo"]["Yes Odd Word game"]["if"])}}
            else:
                a: {{selectRandomArg($OddWordGameAnswers["YesOrNo"]["Yes Odd Word game"]["else"])}}
            go!: ../../Odd Word game ask question   

###############################################################################
###############                Генерация вопроса                 ##############
###############################################################################

    state: Odd Word game ask question
        script:
            OddWordGame.getWordChain();
        a: {{$session.oddWordGame.currentListText}} || question = true

###############################################################################
###############                Возможные ответы                  ##############
###############################################################################


        state: Odd Word game get answer
            q: * [$maybe|$sure] * ($oddWordGameElement|$oddWordGameOrder) * [$oddWordGameElement|$oddWordGameOrder] * [$maybe|$sure] *
            q: [$maybe|$sure] ($oddWordGameStrictOrder::oddWordGameOrder) [$maybe|$sure]
            script:
                OddWordGame.processAnswer();

        state: Odd Word game get answer dontknow
            q: * ($dontKnow/не хочу/не буду/не надо/(скажи/какой) * ответ/{сам (ответь/отвечай)}/[давай] (дальше/(~следующий/~другой) (вопрос/~загадка))/хз) *
            q: * кто (его/это) * (знать/знает) *
            a: {{selectRandomArg($OddWordGameAnswers["Odd Word game get answer dontknow"])}}
            script:
                $temp.unknownAnswer = true;
            go!: ../../WrongAnswer

        state: Odd Word game get answer other
            q: * 
            if: !$temp.askedUnknown
                script:
                    $temp.askNow = true;
                    $temp.askedUnknown = true;
                a: {{selectRandomArg($OddWordGameAnswers["Odd Word game get answer other"])}} || question = true
            if: !$temp.askNow             
                go!: ../../WrongAnswer

        state: Odd Word game repeat list || noContext=true
            q: * {[повтори/скажи/прочитай/какие были/повтори-ка] [еще] [раз] (список/запрос/вопрос/варианты/слова/список слов/что [там/в списке] было)} *
            q: * {(повтори/скажи/прочитай/повтори-ка) [еще] [раз]}
            q: [* повтори/повтори-ка] еще раз
            q: * (*медленнее/(очень/слишком) быстро) *
            q: * из чего * (выбрать/выбирать) *
            a: {{capitalize($session.oddWordGame.currentListText)}} || question = true
               

        state: Odd Word game what it is || noContext=true
            q: * (что/кто) (такая/такое/такой) * $oddWordGameElement *  || fromState = "../../Odd Word game ask question"
            a: {{selectRandomArg($OddWordGameAnswers["Odd Word game what it is"])}}


        state: Get hint
            q: * [не знаю] * {[повтори|еще раз|~другой:1|по-другому:1|еще:1] * (подскажи|намекни|помоги|~подсказка)} *
            q: * [не знаю] * [еще раз|~другой|по-другому|еще:1] * (подскажи|намекни|помоги|подсказка|подсказку):1 * || fromState = "../Get hint"
            q: * повтори * (~подсказка|~подсказывать) *
            if: $parseTree._Root == 1
                a: {{selectRandomArg($OddWordGameAnswers["Get hint"]["if"])}}
            else:
                a: {{selectRandomArg($OddWordGameAnswers["Get hint"]["else"])}} 


        state: Complexity level
            q: * (посложнее [~уровень|~вопрос]|{~уровень (выше|повыше|следующий)}):1 *
            q: * ((попроще|полегче) [~уровень|~вопрос]|{~уровень (ниже|пониже)}):2 *
            q: * (~другой) (~загадка/вопрос) * [слишком] ((~сложный|сложно):2|(~легкий|легко):1) *
            q: * [еще] ((сложнее/посложнее/легко):1/(легче/полегче/сложно):2) *
            if: $parseTree._Root == 1
                if: $session.oddWordGame.currentLevel == 4
                    a: {{selectRandomArg($OddWordGameAnswers["Complexity level"]["if"]["if"])}}  || question = true
                else:
                    script:
                        $session.oddWordGame.currentLevel += 1;
                    a: {{selectRandomArg($OddWordGameAnswers["Complexity level"]["if"]["else"])}} 
                    go!: ../
            if: $parseTree._Root == 2
                if: $session.oddWordGame.currentLevel == 1
                    script:
                        $session.askEasy = true;
                    a: {{selectRandomArg($OddWordGameAnswers["Complexity level"]["else"]["if"])}}  || question = true
                else:
                    script:
                        $session.oddWordGame.currentLevel -= 1;
                    a: {{selectRandomArg($OddWordGameAnswers["Complexity level"]["else"]["else"])}} 
                    go!: ../

###############################################################################
###############   Реакция на правильный/неправильный ответы      ##############
###############################################################################

    state: RightAnswer
        script:
            $session.oddWordGame.answersCounter += 1;
            $session.oddWordGame.rightAnswersCounter += 1;
            if ($session.oddWordGame.questionType === 3) {
                $session.oddWordGame.questionType = 4;
            } else {
                $session.oddWordGame.questionType = 3;
                $session.oddWordGame.currentLevel += 1;
            }
            $session.oddWordGame.answers = $session.oddWordGame.rightAnswersCounter % 10 === 1 && $session.oddWordGame.rightAnswersCounter % 100 !== 11 ? 'вопрос' : ($session.oddWordGame.rightAnswersCounter % 10 >= 2 && $session.oddWordGame.rightAnswersCounter % 10 <= 4 && ($session.oddWordGame.rightAnswersCounter % 100 < 10 || $session.oddWordGame.rightAnswersCounter % 100 >= 20) ? 'вопроса' : 'вопросов');
        if: ($session.oddWordGame.currentLevel == 5)
            a: {{selectRandomArg($OddWordGameAnswers["RightAnswer"]["if"])}}  || question = true
            go: /Odd word game/YesOrNo
        else:
            a: {{selectRandomArg($OddWordGameAnswers["RightAnswer"]["else"])}} 
            go!: /Odd word game/Odd Word game ask question

    state: WrongAnswer
        script:
            $session.oddWordGame.answersCounter += 1;
            if ($session.oddWordGame.questionType === 3) {
                if ($session.oddWordGame.currentLevel !== 1) {
                    $session.oddWordGame.questionType = 4;
                    $session.oddWordGame.currentLevel -= 1;
                }
            } else {
                $session.oddWordGame.questionType = 3;
            }
        if: !$temp.unknownAnswer
            a: {{selectRandomArg($OddWordGameAnswers["WrongAnswer"]["if"])}}     
        a: {{$session.oddWordGame.rightAnswerText}} 
        go!: ../Odd Word game ask question  

###############################################################################
###############     Конец, остановка или повторение игры     ##################
###############################################################################


    state: Odd word game stop
        q: * ($stopGame/(не хочу/надоело/не буду) * играть/отстань) *
        a: {{selectRandomArg($OddWordGameAnswers["Odd word game stop"])}} 
            
    state: Odd word game repeat
        q: * давай * (еще/ещё/снова/опять/сначала)  * [сыгр*|игр*|поигр*] *
        go!: /Odd word game/YesOrNo/Yes Odd Word game