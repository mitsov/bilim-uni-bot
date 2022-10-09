require: GeographyGame.js
require: ../../common/geographyGame/GeographyGame.js
require: ../../common/common.sc

require: ../Games.sc

require: where/whereEn.sc
  module = zb_common

require: patternsEn.sc
  module = zb_common

require: ../main.js

require: answers.yaml
  var = GeographyGameCommonAnswers

init:
    $global.themes = $global.themes || [];
    $global.themes.push("Geography quiz");
    $global.$GeographyGameAnswers = (typeof GeographyCustomAnswers != 'undefined') ? applyCustomAnswers(GeographyGameCommonAnswers, GeographyCustomAnswers) : GeographyGameCommonAnswers;

patterns:
    $Continent = (~australia:0/
                ~oceania:0/
                ~arctic:0/
                ~antarctic:0/
                ~eurasia:Eurasia/
                ~america:America/
                ~europe:Europe/
                ~asia:Asia/
                ~north america:North_America/
                ~south america:South_America/
                ~africa:Africa)
    $AmericaDescr = (~north:North_America/
                    ~south:South_America)                

theme: /Geography quiz
    state: Geography game user start
        q!: * [want|wanna|let 's|let us] play* * (geography/capitals/countries) [game|quiz] *
        q: * (~capital/~land/~country/~geography) * || fromState = "/PlayGames/Games/", onlyThisState = true
        script:
            GeographyGame.init();
            $response.action = 'ACTION_EXP_HAPPY';
        if: (!$session.again)
            a: {{selectRandomArg($GeographyGameAnswers["Geography game user start"])}} || question = true

        else:
            script:
                $session.again = false;  
        go!: ../Geography game pudding start


    state: Geography game suggest quiz
        q!: suggest a geography quiz
        script:
            $response.action = 'ACTION_EXP_HAPPY';
            GeographyGame.init();
        a: {{selectRandomArg($GeographyGameAnswers["Geography game suggest quiz"])}} || question = true
        go!: ../Geography game pudding start    

    state: Geography game pudding start        
        
        state: Geography game no
            q: * ($disagree|$notNow|$no help*) * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) * || fromState = "/PlayGames/Games/How to play Geography", onlyThisState = true 
            script:
                $response.action = 'ACTION_EXP_SAD';
            a: {{ selectRandomArg($GeographyGameAnswers["Geography game pudding start"]["Geography game no"])}}

        state: Geography game yes
            q: * ($agree|start*|come on|let 's do it|question*) [*game*] * || fromState = .., onlyThisState = true
            q: * ($agree|start*|come on|let 's do it|question*) * || fromState = "/PlayGames/Games/How to play Geography", onlyThisState = true
            q: * (~capital/~land/~country/~geography) * || fromState = /PlayGames/Games, onlyThisState = true
            q: * [$agree|$maybe] * (~capital/~country/~geography) * [$agree|$maybe] * || fromState = /PlayGames/Games, onlyThisState = true  
            script:
                $response.action = 'ACTION_EXP_HAPPY';
                GeographyGame.init();
            a: {{selectRandomArg($GeographyGameAnswers["Geography game pudding start"]["Geography game yes"])}} 
            go!: ../../Geography quiz get question    

        state: Geography game undefined start
            q: * || fromState = .., onlyThisState = true
            q: * || fromState = "/PlayGames/Games/How to play Geography", onlyThisState = true
            if: (!$temp.askedUnknown)
                script:
                    $session.askNow = true;
                    $temp.askedUnknown = true;
                    $response.action = 'ACTION_EXP_SAD';
                a: {{selectRandomArg($GeographyGameAnswers["Geography game pudding start"]["Geography game undefined start"]["noquestion"])}} 
                a: {{selectRandomArg($GeographyGameAnswers["Geography game pudding start"]["Geography game undefined start"]["question"])}} || question = true
            go: ../../Geography game pudding start

###############################################################################
###############                Генерация вопроса                 ##############
###############################################################################

    state: Geography quiz get question
        script:
            $response.action = 'ACTION_EXP_QUESTION';
            $session.questionType = ($session.questionType == 5) ? 1 : $session.questionType + 1;
        if: ($session.questionType == 1)
            if: ($session.gameLevelCounter >= 3)
                if: ($client.geographyLevel < 5)
                    script:
                        $client.geographyLevel += 1;
                    if: ($temp.rightAnswer)
                        a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["if"]["if"])}} 
                        a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["if"]["default"])}} 
            else:
                if: ($session.gameLevelCounter <= -3)
                    if: ($client.geographyLevel > 1)
                        script:
                            $client.geographyLevel -= 1;
                        a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["else"])}} 
            script:
                $session.gameLevelCounter = 0;
        script:
            eval('GeographyGame.getQuestionType' + $session.questionType)();
        a: {{$session.question}} || question = true

###############################################################################
###############                Возможные ответы                  ##############
###############################################################################


        state: Geography quiz get question answer definite Country
            q: * [$maybe|$dontKnow|$sure|$agree] * {[$Capital] * (($Country) * [$Country])} * [$maybe|$dontKnow|$sure|$agree] *
            q: * [$maybe|$dontKnow|$sure|$agree] * {[$Capital] [is] [a/the]  [capital] * (($Country) * [$Country])} * [$maybe|$dontKnow|$sure|$agree] *
            q: * [$maybe|$dontKnow|$sure|$agree] * $Country * [$maybe|$dontKnow|$sure|$agree] *
            script:
                GeographyGame.getAnswerCountry($parseTree);
            if: ($session.askNow)
            else:        
                go!: ../../{{$session.nextState}}
                 
        state: Geography quiz get question answer definite Capital
            q: * [$maybe|$dontKnow|$sure|$agree] * (($Capital/($City * [~столица])) * [$Capital::City2/$City::City2]) * [$maybe|$dontKnow|$sure|$agree] *
            q: * [$maybe|$dontKnow|$sure|$agree]  * { [is] [a/the] * [capital] $Capital * $Country * } * [$maybe|$dontKnow|$sure|$agree] *
            q: * [$maybe|$dontKnow|$sure|$agree] * $Capital * [$maybe|$dontKnow|$sure|$agree] *
            script:
                GeographyGame.getAnswerCapital($parseTree);
            if: ($session.askNow)
            else:        
                go!: ../../{{ $session.nextState }}

        state: Geography quiz get question answer definite Continent
            q: * [$maybe|$dontKnow|$sure|$agree] * {[$Country] * ($Continent * [$Continent])} * [$maybe|$dontKnow|$sure|$agree] *
            q: * [$maybe|$dontKnow|$sure] * $AmericaDescr * [$maybe|$dontKnow|$sure] *
            script:
                GeographyGame.getAnswerContinent($parseTree);
            if: (!$session.askNow)       
                go!: ../../{{ $session.nextState }}

        state: Geography quiz get question answer dontknow
            q: * (($dontKnow/$agree/$disagree/next/another/different/else/other/already asked/[$you] tell me/stupid/skip/[$me] [need] [some] help * country/continent) / {you (tell/say/answer)})  *
            q: * (next/another/different/other) question *
            q: * (what/which/where) * 
            q: * {[$me] [need/give] [some] (help/tip/hint)} * 
            script:
                $response.action = 'ACTION_EXP_SPEECHLESS';
                $temp.unknownAnswer = true;
            a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["Geography quiz get question answer dontknow"])}} 
            go!: ../../WrongAnswer

        state: Geography quiz get question answer badGeographyKnowledge
            q: ({[i ['m]] (bad*/poor/awful) [know/knowledge] [at/in] geography}|(my knowledge of geography leaves much to be desired))
            a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["Geography quiz get question answer badGeographyKnowledge"]["default"])}}
            a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["Geography quiz get question answer badGeographyKnowledge"]["rand"])}} .

        state: Geography quiz get question answer repeatPlease
            q: * {(repeat*|(tell/ask) [me] again) [question]} *
            a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["Geography quiz get question answer repeatPlease"])}} 
            a: {{$session.question}} 

        state: Geography quiz get question answer other
            q: * 
            if: (!$temp.askedUnknown)
                script:
                    $session.askNow = true;
                    $temp.askedUnknown = true;
                    $response.action = 'ACTION_EXP_SAD';
                a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["Geography quiz get question answer other"])}} 
            if: ($session.askNow)
            else:        
                go!: ../../WrongAnswer

###############################################################################
###############   Реакция на правильный/неправильный ответы      ##############
###############################################################################

    state: RightAnswer
        script:
            $temp.rightAnswer = true;
            $session.gameCounter += 1;
            $session.gameRightCounter += 1;
            $session.gameLevelCounter += 1;
        a: {{selectRandomArg($GeographyGameAnswers["RightAnswer"])}}
        go!: ../Geography quiz get question 

    state: WrongAnswer
        script:
            $session.gameCounter += 1;
            $session.gameLevelCounter -= 1;
        if: (!$temp.unknownAnswer)
            a: {{selectRandomArg($GeographyGameAnswers["WrongAnswer"])}} 
        a: {{$session.rightAnswerText}}
        if: ($temp.wrongEurasiaPart)
            a: {{$temp.wrongEurasiaPart}}
        go!: ../Geography quiz get question    

###############################################################################
###############         Остановка или повторение игры        ##################
###############################################################################

    state: Geography quiz stop
        q: * ($stopGame/$shutUp) *
        q: * (i (am/'m) tired/(need/let 's/want/wanna) * (break/finish)) *
        q: * (let 's|want to|wanna|need) * (end|stop|finish) [* game] *
        q: * (give|want to|wanna|need) * [have] [me|us] * rest *
        script:
            $response.action = 'ACTION_EXP_CUTE';
        a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["default"])}}
        script:
            $temp.right = $session.gameRightCounter;
            $temp.all_questions = $session.gameCounter;
            
            $temp.ru__answers = $temp.right % 10 === 1 && $temp.right % 100 !== 11 ? 'question' : ($temp.right % 10 >= 2 && $temp.right % 10 <= 4 && ($temp.right % 100 < 10 || $temp.right % 100 >= 20) ? 'questions' : 'questions');
            $temp.ru_questions = $temp.all_questions % 10 === 1 && $temp.all_questions % 100 !== 11 ? 'question' : ($temp.all_questions % 10 >= 2 && $temp.all_questions % 10 <= 4 && ($temp.all_questions % 100 < 10 || $temp.all_questions % 100 >= 20) ? 'questions' : 'questions');
        if: ($temp.right == 0)
            if: ($temp.all_questions == 1)
                a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["if"]["if"])}}
            else:
                a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["if"]["else"])}}
                a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["if"]["else2"])}} 
        else:
            if: ($temp.right == $temp.all_questions)
                if: ($temp.all_questions == 1)
                    a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["if"]["if"])}}
                else:
                    a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["if"]["else"])}}
                    a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["if"]["else2"])}} 
            else:
                a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["else"])}}
                a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["else2"])}} 

        state: Thanks
            q: * ({[i am/ i 'm/ me/ i] [really] * [enjoy*/like*/love/good] [feel*/felt] [with you] * play}/{i liked [$AnyWord] game}) *
            q: $thanks [за игру]
            a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["Thanks"])}}

    state: Geography quiz repeat
        q: * [let 's/let us/i want [to]] * [play] (again/another game/one more game) *
        go!: /Geography quiz/Geography game pudding start/Geography game yes