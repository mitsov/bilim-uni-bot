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

require: patternsEn.sc
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
    $global.themes.push("Extra game");
    $global.$OddWordGameAnswers = (typeof OddWordCustomAnswers != 'undefined') ? applyCustomAnswers(OddWordGameCommonAnswers, OddWordCustomAnswers) : OddWordGameCommonAnswers;

patterns:
    $oddWordGameElement = $entity<oddWordGameElements> || converter = $converters.oddWordGameElementTagConverter

    $oddWordGameOrder = (([[the] the] first/1 word):1|    
             ([the] second/[the] 2 word):2|
             ([the] third/[the] 3 word):3|
             ([the] fourth/[the] 4 word):4|
             ([the] fifth/[the] 5 word):5|
             ([the] last [one]):lst|
             ([the] (penultimate/penult/last but one)):blst)
    
    $oddWordGameStrictOrder = (1|2|3|4|5)
         
theme: /Odd word game

    state: Odd word game start
        q!: * [i] [want [to]|wanna|let 's|let us] play* * (odd word*|odd (one|1) out|odd [one]) [game] [with] [you|me] *
        q: * (odd (one|1) [out]) * || fromState=/PlayGames/Games, modal=true
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
            q: * ($disagree|$notNow|no help| 'll not help) * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) * || fromState = "/PlayGames/Games/How to play Extra game", onlyThisState = true
            a: {{selectRandomArg($OddWordGameAnswers["YesOrNo"]["No Odd Word game"])}}

        state: Yes Odd Word game
            q: * ($agree|start*|i will help*|go on|begin) * || fromState = .., onlyThisState = true
            q: * ($agree|start*) * || fromState = "/PlayGames/Games/How to play Extra game", onlyThisState = true
            q: * [$agree|$maybe] * (find odd*/odd (one|1) out) * [$agree|$maybe] * || fromState = /PlayGames/Games, onlyThisState = true        
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
        a: {{capitalize($session.oddWordGame.currentListText)}} || question = true

###############################################################################
###############                Возможные ответы                  ##############
###############################################################################


        state: Odd Word game get answer
            q: * [$maybe|$sure] * ($oddWordGameElement|$oddWordGameOrder) * [$oddWordGameElement|$oddWordGameOrder] * [$maybe|$sure] *
            q: [$maybe|$sure] ($oddWordGameStrictOrder::oddWordGameOrder) [$maybe|$sure]
            script:
                OddWordGame.processAnswer();
            
                           
        state: Odd Word game get answer dontknow
            q: * ($dontKnow/(do not/do n't) want/(tell me|what ['s|is]) [the|an] answer/next question) *
            q: * who knows *
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
            q: [repeat/say/read] [the] [list] [of] [words] [once/one more [time]] [again]
            q: {[repeat/say/read/come] [once/one more [time]] (again)} 
            q: * (*slower/(very/too) fast) *
            q: * what were the words *
            a: {{capitalize($session.oddWordGame.currentListText)}} || question = true
               

        state: Odd Word game what it is || noContext=true
            q: * (what/who) [the *] (is/are/was/were) * $oddWordGameElement *  || fromState = "../../Odd Word game ask question"
            a: {{selectRandomArg($OddWordGameAnswers["Odd Word game what it is"])}}


        state: Get hint
            q: * [$dontKnow] * {[repeat|one more time|* in other words:1|* different:1|again:1] * (hint|promt|help|tip)} *
            q: * [$dontKnow] * [repeat|one more time|* in other words:1|* different:1|again:1] * (hint|promt|help|tip):1 * || fromState = "../Get hint"
            q: * repeat * (hint|promt|help|tip) *
            if: $parseTree._Root == 1
                a: {{selectRandomArg($OddWordGameAnswers["Get hint"]["if"])}}
            else:
                a: {{selectRandomArg($OddWordGameAnswers["Get hint"]["else"])}} 


        state: Complexity level
            q: * (harder [~level|~question]|{~level (up|higher|next)}):1 *
            q: * (easier [~level|~question]|{~level (down|lower)}):2 *
            q: * (other/another) (quiz/question) * [too *] ((difficult|complicated|hard):2|(easy|easily):1) *
            q: * [more] ((harder/complicated/[still] easy):1/(easy/easier/[still] (hard/difficult/complicated)):2) *
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
            if ($session.oddWordGame.questionType == 3){
                $session.oddWordGame.questionType = 4;
            } else {
                $session.oddWordGame.questionType = 3;
                $session.oddWordGame.currentLevel += 1;
            }
        if: ($session.oddWordGame.currentLevel == 5)
            a: {{selectRandomArg($OddWordGameAnswers["RightAnswer"]["if"])}}  || question = true
            go: /Odd word game/YesOrNo
        else:
            a: {{selectRandomArg($OddWordGameAnswers["RightAnswer"]["else"])}} 
            go!: /Odd word game/Odd Word game ask question

    state: WrongAnswer
        script:
            $session.oddWordGame.answersCounter += 1;
            if ($session.oddWordGame.questionType == 3){
                if ($session.oddWordGame.currentLevel != 1){
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


    state: Extra game stop
        q: * ($stopGame/$shutUp) *
        q: * [i (need|want|wanna)|(let 's|let us) (have|make) ] * break *
        q: * (give|want to|wanna|need) * [have] [me|us] * rest *
        q: * (let 's|want to|wanna|need) * (end|stop|finish) [* game] *
        a: {{selectRandomArg($OddWordGameAnswers["Extra game stop"])}} 
            
    state: Extra game repeat
        q: * (let 's/want) * (play (more/again)/(another/new) game)  *
        go!: /Odd word game/YesOrNo/Yes Odd Word game