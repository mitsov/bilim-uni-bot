require: GeographyGame.js
require: ../../common/geographyGame/GeographyGame.js
require: ../../common/common.sc

require: ../Games.sc

require: where/where.sc
  module = zb_common

require: patterns.sc
  module = zb_common

require: ../main.js

require: answers.yaml
  var = GeographyGameCommonAnswers

init:
    $global.themes = $global.themes || [];
    $global.themes.push("Geography quiz");
    $global.$GeographyGameAnswers = (typeof GeographyCustomAnswers != 'undefined') ? applyCustomAnswers(GeographyGameCommonAnswers, GeographyCustomAnswers) : GeographyGameCommonAnswers;

patterns:
    $Continent = (~австралия:0/
                ~океания:0/
                ~арктика:0/
                ~антарктида:0/
                ~евразия:Евразия/
                ~америка:Америка/
                ~европа:Европа/
                ~азия:Азия/
                ~северный ~америка:Северная_Америка/
                ~южный ~америка:Южная_Америка/
                ~африка:Африка)     
    $AmericaDescr = (северн*:Северная_Америка/
                    южн*:Южная_Америка)                

theme: /Geography quiz
    state: Geography game user start
        q!: * [хочу/давай] * (~играть|~сыграть|~поиграть|~игра|*играем) * [в/про] * (~столица/~страна/~география) *
        q!: * (хочу/давай) [~играть|~сыграть|~поиграть|~игра] * (в/про) * (~столица/~страна/~география) *
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
        q!: предложи викторину по географии
        script:
            $response.action = 'ACTION_EXP_HAPPY';
            GeographyGame.init();
        a: {{selectRandomArg($GeographyGameAnswers["Geography game suggest quiz"])}} || question = true
        go!: ../Geography game pudding start    

    state: Geography game pudding start        
        
        state: Geography game no
            q: * ($disagree|$notNow|не помогу) * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) * || fromState = "/PlayGames/Games/How to play Geography", onlyThisState = true 
            script:
                $response.action = 'ACTION_EXP_SAD';
            a: {{ selectRandomArg($GeographyGameAnswers["Geography game pudding start"]["Geography game no"])}}

        state: Geography game yes
            q: * ($agree|начин*|начн*|помогу|поехали|вопрос*|задавай) [*игра*] * || fromState = .., onlyThisState = true
            q: * ($agree|начин*|начн*|поехали|вопрос*|задавай) * || fromState = "/PlayGames/Games/How to play Geography", onlyThisState = true
            q: * (~столица/~страна/~география/*графи*) * || fromState = /PlayGames/Games, onlyThisState = true
            q: * [$agree|$maybe|$dontKnow|$sure] * (~столица/~страна/~география) * [$agree|$maybe|$dontKnow|$sure] * || fromState = /PlayGames/Games, onlyThisState = true  
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
            if($session.questionType === undefined) {
                $session.questionType = 1;
            }
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
            eval('GeographyGame.getQuestionType' + $session.questionType + '()');
        a: {{$session.question}} || question = true

###############################################################################
###############                Возможные ответы                  ##############
###############################################################################


        state: Geography quiz get question answer definite Country
            q: * [$maybe|$dontKnow|$sure|$agree] * {[$Capital] * (($Country) * [$Country])} * [$maybe|$dontKnow|$sure|$agree] *
            q: * [$maybe|$dontKnow|$sure|$agree] * {[$Capital] [это]  [столица] * (($Country) * [$Country])} * [$maybe|$dontKnow|$sure|$agree] *
            q: * [$maybe|$dontKnow|$sure|$agree] * $Country * [$maybe|$dontKnow|$sure|$agree] *
            script:
                GeographyGame.getAnswerCountry($parseTree);
            if: ($session.askNow)
            else:        
                go!: ../../{{$session.nextState}}
                 
        state: Geography quiz get question answer definite Capital
            q: * [$maybe|$dontKnow|$sure|$agree] * (($Capital/($City * [~столица])) * [$Capital::City2/$City::City2]) * [$maybe|$dontKnow|$sure|$agree] *
            q: * [$maybe|$dontKnow|$sure|$agree]  * { [столица] $Capital * [это] $Country * } * [$maybe|$dontKnow|$sure|$agree] *
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
            q: * ($dontKnow/$agree/$disagree/не хочу/не буду/не надо/подскажи/подсказк*/помоги/уже спрашивал/повторяешься/(скажи/какой/какая/назови/ответь/отвечай) * (ответ/сам/ты/город/страна/континент)/(сам/ты/ну) (ответь/отвечай/скажи))  *
            q: * [давай] (дальше|след*|ещё|еще|игра*|поехали|задавай|другой|пропустим) [дальше|след*|ещё|еще|игра*|поехали|задавай|другой] [этот] [вопрос|вопросик] *
            q: * [в/на] (~какой) 
            script:
                $response.action = 'ACTION_EXP_SPEECHLESS';
                $temp.unknownAnswer = true;
            a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["Geography quiz get question answer dontknow"])}} 
            go!: ../../WrongAnswer

        state: Geography quiz get question answer badGeographyKnowledge
            q: ({[я] плохо знаю географию}|(мое знание географии оставляет желать лучшего))
            a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["Geography quiz get question answer badGeographyKnowledge"]["default"])}}
            a: {{selectRandomArg($GeographyGameAnswers["Geography quiz get question"]["Geography quiz get question answer badGeographyKnowledge"]["rand"])}} .

        state: Geography quiz get question answer repeatPlease
            q: * {(повтори*|скажи снова) [вопрос]} *
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
        q: * ($stopGame/(не хочу/не буду) * играть/надоело/отстань/спасибо за игру) *
        q: * (хочу отдохнуть/я устал*/давай * перерыв) *
        script:
            $response.action = 'ACTION_EXP_CUTE';
        a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["default"])}}
        script:
            $temp.right = $session.gameRightCounter;
            $temp.all_questions = $session.gameCounter;
            
            $temp.ru__answers = $temp.right % 10 === 1 && $temp.right % 100 !== 11 ? 'вопрос' : ($temp.right % 10 >= 2 && $temp.right % 10 <= 4 && ($temp.right % 100 < 10 || $temp.right % 100 >= 20) ? 'вопроса' : 'вопросов');
            $temp.ru_questions = $temp.all_questions % 10 === 1 && $temp.all_questions % 100 !== 11 ? 'вопрос' : ($temp.all_questions % 10 >= 2 && $temp.all_questions % 10 <= 4 && ($temp.all_questions % 100 < 10 || $temp.all_questions % 100 >= 20) ? 'вопроса' : 'вопросов');
        if: ($temp.right == 0)
            if: ($temp.all_questions == 1)
                a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["if"]["if"])}}
            else:
                a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["if"]["else"])}}
                a: {{selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["if"]["else2"])}} 
        else:
            if: ($temp.right == $temp.all_questions)
                if: ($temp.all_questions == 1)
                    a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["if"]["if"])}}
                else:
                    a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["if"]["else"])}}
                    a: {{selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["if"]["else2"])}} 
            else:
                a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["else"])}}
                a: {{selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["else"]["else2"])}} 

        state: Thanks
            q: * ({мне понравилось [с тобой] играть}/{мне понравилась [$AnyWord] игра}) *
            q: $thanks [за игру]
            a: {{ selectRandomArg($GeographyGameAnswers["Geography quiz stop"]["Thanks"])}}

    state: Geography quiz repeat
        q: * { [давай] * (еще/ещё/снова/опять/сначала) [раз] * [сыгр*|игр*|поигр*] } *
        go!: /Geography quiz/Geography game pudding start/Geography game yes