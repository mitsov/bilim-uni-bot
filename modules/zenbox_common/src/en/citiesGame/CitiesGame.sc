require: ../../common/common.sc

require: ../../common/citiesGame/CitiesGame.js
require: CitiesGame.js

require: where/whereEn.sc
  module = zb_common

require: ../Games.sc

require: ../main.js

require: patternsEn.sc
  module = zb_common

require: answers.yaml
  var = CitiesGameCommonAnswers

init:
    $global.themes = $global.themes || [];
    $global.themes.push("Gorodagame");
    $global.$CitiesGameAnswers = (typeof CitiesCustomAnswers != 'undefined') ? applyCustomAnswers(CitiesGameCommonAnswers, CitiesCustomAnswers) : CitiesGameCommonAnswers;

patterns:
    
    $lettersAll = ("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z")
theme: /Gorodagame
    state: Goroda game user start
        q!: * [want|wanna|let 's|let us] play * (cit*|word chain) [game] * 
        q: * (city|cities|word chain) * || fromState = /PlayGames/Games
        a: {{ selectRandomArg($CitiesGameAnswers["Goroda game user start"])}}
        go!: ../Goroda game intro
             
        state: Goroda game no
            q: * $disagree * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) * || fromState = "/PlayGames/Games/How to play Goroda", onlyThisState = true  
            script: 
                $response.action = 'ACTION_EXP_SAD'; 
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game no"]["disagree"])}}
            if: $parseTree.Text
                a: {{selectRandomArg($CitiesGameAnswers["Goroda game no"]["noandtext"])}} 


        state: Goroda game yes
            q: * $agree * || fromState = .., onlyThisState = true        
            q: * ($agree|start|begin*|$yes|i do) * || fromState = "/PlayGames/Games/How to play Goroda", onlyThisState = true        
            go!: ../../Goroda game intro

    state: Goroda game user starts
        
        state: Goroda game user starts with City
            q: * [$agree] $City * || fromState = "/PlayGames/Games/How to play Goroda", onlyThisState = true
            q: * [$agree] $City * || fromState = "../Goroda game user starts with CityCountry", onlyThisState = true 
            q: * [$agree] $City * || fromState = "../../Goroda game user starts", onlyThisState = true
            script:
                $session.gameCities = [];
                $session.gameCities.push($City.name);
            a: {{getCity()}} || question = true
            go!: ../../Goroda game question  

        state: Goroda game user starts with CityCountry
            q: * [$agree] $Country * || fromState = "/PlayGames/Games/How to play Goroda", onlyThisState = true 
            q: * [$agree] $Country * || fromState = "../Goroda game user starts with CityCountry", onlyThisState = true 
            q: * [$agree] $Country * || fromState = "../../Goroda game user starts", onlyThisState = true
            if: (isCityCountry($parseTree.Country[0].text))
                script:
                    $session.gameCities = [];
                    $session.gameCities.push($Country.name);
                a: {{getCity()}} || question = true
                go!: ../../Goroda game question  
            else:
                a: {{ selectRandomArg($CitiesGameAnswers["Goroda game user starts with CityCountry"])}} || question = true
                       
            state: Goroda game user starts with CityCountry not true yes
                q: * ($agree|[will|'ll] try) * || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["agree"])}} || question = true
                go!: ../../../Goroda game user starts

            state: Goroda game user starts with CityCountry not true no
                q: * ($disagree|$stopGame) * || fromState = .., onlyThisState = true
                go!: ../../../Goroda game end

    state: Goroda game intro
        script:
            $session.gameCities = [];
            $temp.nextCity = getCity();
        if: ($temp.nextCity != '')
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game intro"]['intro'])}} || question = true
            go!: ../Goroda game question 
        else:
            script:
                $response.action = 'ACTION_EXP_DIZZY';
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game intro"]["forgotAll"])}}
             
    state: Goroda game intro iterate
        script:
            $session.gameCities = [];
            $temp.nextCity = getCity();
        if: ($temp.nextCity != '')
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game intro iterate"])}} || question = true
            go!: ../Goroda game question 
        else:
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game intro"]["forgotAll"])}}
            
    state: Goroda game question
        
        state: DisagreeWithRules
            q: $disagree
            go!: ../../Goroda game end  

        state: AgreeWithRules
            q: $agree
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["AgreeWithRules"])}} || question = true
            go!: ../../Goroda game question        
           
        state: Remind user a letter
            q: * {(what|remind|tell|which|forgot) * (letter|word|city) } *
            q: * {(what|remind|tell|which|forgot) [letter|word|city] * (was|previous|said|told|named)} *
            q: (what/{repeat [again] [[one/once] more [time]]})
            script:
                $temp.lastCity = $session.gameCities[$session.gameCities.length - 1];
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Remind user a letter"])}} || question = true
            go!: ../../Goroda game question        

        state: Goroda game answer true
            q: * $City [$City::City2] * || fromState = "../../Goroda game question", onlyThisState = true
            q: * $City [$City::City2] * || fromState = "../Goroda game answer undefined", onlyThisState = true
            q: * $City [$City::City2] * || fromState = "../Goroda game answer city not true", onlyThisState = true
            q: * $City [$City::City2] * || fromState = "../Goroda game giving up", onlyThisState = true

            if: $parseTree.City2
                a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["severalCities"])}}
                go!: ../../Goroda game question  
            else:
                if: $parseTree.text !== $parseTree.City[0].text
                    a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["getCityFromText"])}}
                
                if: checkCity($parseTree.City[0])
                    if: isNewCity($parseTree._City.name)
                        if: my_capitalize($nlp.parseMorph($parseTree.City[0].text).normalForm) !== $parseTree._City.name
                            script:
                                $temp.variantCityName = my_capitalize($parseTree.City[0].text);
                                $temp.officialCityName = $parseTree._City.name;

                            if: !alternateWriting($temp.variantCityName, $temp.officialCityName)
                                script:
                                    $temp.variantFirstLetter = $temp.variantCityName.charAt(0);
                                    $temp.officialFirstLetter = $temp.officialCityName.charAt(0);
                                if: $temp.variantFirstLetter !== $temp.officialFirstLetter
                                    a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["wrongNameVariant"])}}
                                    go!: ../../Goroda game question
                                else: 
                                    a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["correctNameVariant"])}}
                        script:
                            $session.gameCities.push($parseTree._City.name);
                        a: {{getCity()}} || question = true
                        go!: ../../Goroda game question   
                    else:
                        script:
                            $temp.userCity = $parseTree._City.name;
                            $temp.userText =  my_capitalize($parseTree.City[0].text);
                            $temp.isCity = $temp.userCity == $temp.userText;
                            $temp.isAlterCity = alternateWriting($temp.userText, $temp.userCity);
                        if: ($temp.isCity || $temp.isAlterCity)
                            a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["oldCity"])}} || question = true
                        else:
                            a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["oldCityText"])}} || question = true
                        go!: ../Goroda game answer city not true
                else:
                    if: getGameProgress()
                        script:
                            $temp.userCity = $parseTree._City.name;
                            $temp.firstLetter = capitalize($temp.userCity.substring(0,1));
                            $temp.lastLetter = getCityLetter();
                            $temp.lastCity = $session.gameCities[$session.gameCities.length - 1];
                        a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["notACity"])}} || question = true
                        go!: ../Goroda game answer city not true           
                    else:
                        a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["forgotCity"])}}  || question = true 
                        go!: ../../Goroda game intro

        state: Goroda game answer country
            q: * $Country * || fromState = "../../Goroda game question", onlyThisState = true
            q: * $Country * || fromState = "../Goroda game answer undefined", onlyThisState = true
            q: * $Country * || fromState = "../Goroda game answer city not true", onlyThisState = true
            q: * $Country * || fromState = "../Goroda game giving up", onlyThisState = true
            if: (isCityCountry($parseTree.Country[0].text))
                if: checkCity($parseTree.Country[0]) 
                    if: isNewCity($Country.name)
                        script:
                            $session.gameCities.push($parseTree._Country.name);
                        a: {{getCity()}} || question = true
                        go!: ../../Goroda game question 
                    else:
                        script:
                            $temp.userCity =  $Country.name;
                            $temp.userText =  my_capitalize($parseTree.Country[0].text);
                            $temp.isCity = $temp.userCity == $temp.userText;
                            $temp.isAlterCity = alternateWriting($temp.userText, $temp.userCity);
                        if: ($temp.isCity || $temp.isAlterCity)
                            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["oldCity"])}}  || question = true
                        else:
                            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["oldCityText"])}} || question = true
                        go!: ../Goroda game answer city not true  
                else:
                    if: getGameProgress()
                        script:
                            $temp.userCity =  $Country.name;
                            $temp.firstLetter = capitalize($temp.userCity.substring(0,1));
                            $temp.lastLetter = getCityLetter();
                            $temp.lastCity = $session.gameCities[$session.gameCities.length - 1];
                        a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["notACity"])}} || question = true
                        go!: ../../Goroda game answer city not true           
                    else:
                        a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["forgotCity"])}}  || question = true
                        go!: ../../Goroda game intro
            else:
                a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["country"])}} || question = true
                go!: ../Goroda game answer city not true

        state: Goroda game answer undefined
            q: * $AnyWord * || fromState = "../../Goroda game question", onlyThisState = true
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game answer undefined"])}} || question = true
            go!: ../Goroda game answer city not true

        state: Goroda game answer city not true
            
            state: Goroda game answer city not true yes
                q: * ($agree|try|go on|continue) * || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["agree"])}} || question = true
                go!: ../../../Goroda game question

            state: Goroda game answer city not true no
                q: * $disagree * || fromState = .., onlyThisState = true
                go!: ../../../Goroda game end 

            state: Goroda game answer undefined stop
                q: * $stopGame * || fromState = .., onlyThisState = true
                go!: ../../../Goroda game end

            state: Goroda game answer undefined undefined
                q: * $Text::AnyWord * || fromState = .., onlyThisState = true
                go!: ../../Goroda game answer undefined

        state: Goroda game do not know answer
            q: * (can n't| do n't|does n't) (know|remind|remember|recall|[give [an]] answer|name|tell|come up [with]) [city|cities|answer|word] * [letter] * [$lettersAll] * 
            q: * [letter] $lettersAll * (can n't| do n't|does n't) (know|remind|remember|recall|[give [an]] answer|name|tell|come up [with]) * (city|cities|answer|word|nothing) * 
            q: * ([there is] no|none) * [any] * (city|cities|anything) * [letter] * [$lettersAll] * || fromState = "../../Goroda game question"
            q: * [letter] $lettersAll * no * (city|cities|anything) *  || fromState = "../../Goroda game question"
            go!: ../../Goroda game end

        state: Goroda game user does not know the city
            q: * { [i/we] [even] * ((did/do/does) n't | dont) [even] (know|knew) * [such|this|the] (cities|city) } * 
            q: * { [i/we] [even] * ((did/do/does) n't | dont) [even] (know|knew) * (such|this|the) [cities|city] } * 
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game user does not know the city"])}} || question = true
            go!: ../Goroda game answer city not true

        state: Goroda game thank you for repeat
            q: * thank* [you] *
            script:
                $response.action = 'ACTION_EXP_HAPPY';
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game thank you for repeat"])}}
            go!: ../../Goroda game question 

        state: Goroda game giving up
            q: * ( hint/help [me]/[give] [a/some/*] [tip/tips] | ([you] tell/what [is]) * [the] (answer/city/cities/word) | * do n't know ) *
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game giving up"]["notByRules"])}}
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game giving up"]["giveUp"])}} || question = true
            
            state: Goroda game giving up yes
                q: * {($maybe|$sure|$agree) [$maybe|$sure|$agree|$disagree] }* 
                go!: ../../../Goroda game end

            state: Goroda game giving up no
                q: * $disagree *
                a: {{selectRandomArg($CitiesGameAnswers["Goroda game giving up no"])}} 
                go!: ../../Remind user a letter

    state: Goroda game end
        q: [i/we] (give up/gave up)
        q: * $stopGame *
        q: * $stopGame * || fromState = "../Goroda game question"
        q: * (let 's|want to|wanna|need) * (end|stop|finish) [* game] *
        q: * (give|want to|wanna|need) * [have] [me|us] * rest *
        q: * [i (need|want|wanna)|(let 's|let us) (have|make) ] * break *
        a: {{selectRandomArg($CitiesGameAnswers["Goroda game end"])}} 
        a: {{getResultString()}}
        script:
            $session.gameCities = [];
            $session.lastQuiz = undefined;   
            $client.sortedCitiesByLetter = {};
         
    state: Goroda game new iteration
        q: * {[want|let (us/'s)] (restart|start/replay) [from * begin*] * [city/cities]} * || fromState = "../Goroda game question", onlyThisState = true
        a: {{selectRandomArg($CitiesGameAnswers["Goroda game new iteration"])}} 
        a: {{getResultString()}}
        script:
            $session.gameCities = [];
            $session.lastQuiz = undefined;   
            $client.sortedCitiesByLetter = {};
        go!: ../Goroda game intro iterate    

    state: Goroda game User asks game progress
        q: * { * [show/say/tell] (how (long/many)|what is)  [our/my] * (word* [chain]|result*|city/cities) [get|got|already] } *
        a: {{getResultString()}}
        a: {{ selectRandomArg($CitiesGameAnswers["Goroda game User asks game progress"])}} || question = true
        go!: ../Goroda game question/Goroda game answer city not true


    state: Goroda WantMore
        q: * [want|let 's|let us] * {([once] again|more | continue [* game] | [from [the]] start * [again]) * (city/cities/word chain)} * [time] * || fromState = "../Goroda game end", onlyThisState = true
        q: * (want|let 's|let us) * (replay/play/restart) [again/* start] * [(city/cities/word chain)] * [time] * || fromState = "../Goroda game end", onlyThisState = true
        a: {{selectRandomArg($CitiesGameAnswers["Goroda WantMore"])}} 
        go!: ../Goroda game intro iterate