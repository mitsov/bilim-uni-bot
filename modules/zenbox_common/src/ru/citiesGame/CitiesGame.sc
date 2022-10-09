require: ../../common/common.sc

require: ../../common/citiesGame/CitiesGame.js
require: CitiesGame.js

require: where/where.sc
  module = zb_common

require: ../Games.sc

require: ../main.js

require: patterns.sc
  module = zb_common

require: answers.yaml
  var = CitiesGameCommonAnswers

init:
    $global.themes = $global.themes || [];
    $global.themes.push("Gorodagame");
    $global.$CitiesGameAnswers = (typeof CitiesCustomAnswers != 'undefined') ? applyCustomAnswers(CitiesGameCommonAnswers, CitiesCustomAnswers) : CitiesGameCommonAnswers;

patterns:
    
    $lettersAll = (а|б|в|г|д|е|ё|ж|з|и|к|л|м|н|о|п|р|с|т|у|ф|х|ц|ч|ш|щ|э|ю|я)
    $letterNoCity = (ъ|ы|ь)

theme: /Gorodagame
    state: Goroda game user start
        q!: * {[давай|хоч*] [поигра*|сыгра*|игра*|~игра] * [в] [снова/опять/еще] (город*|горада|града|градов|гораздо|дорадо|гора да|рада|коррадо|гроза)} *
        q: * {(город*|горада|града|дорадо|градов|гораздо|гора да|рада|коррадо|гроза) * [*умеешь|*можешь|*могешь|*играешь|*ыграешь]} * || fromState = /PlayGames/Games
        a: {{ selectRandomArg($CitiesGameAnswers["Goroda game user start"])}} || question = true
        go!: ../Goroda game intro
             
        state: Goroda game no
            q: * $disagree [* (о|об) $Text] * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) * || fromState = "/PlayGames/Games/How to play Goroda", onlyThisState = true  
            script: 
                $response.action = 'ACTION_EXP_SAD'; 
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game no"]["disagree"])}}
            if: $parseTree.Text
                a: {{selectRandomArg($CitiesGameAnswers["Goroda game no"]["noandtext"])}} 


        state: Goroda game yes
            q: * ($agree|начин*|начн*|поехали) * || fromState = "/PlayGames/Games/How to play Goroda", onlyThisState = true        
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
                q: * ($agree|попробую|продолжаем) * || fromState = .., onlyThisState = true
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
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game intro"]["forgotAll"])}} || question = true
             
    state: Goroda game intro iterate
        script:
            $session.gameCities = [];
            $temp.nextCity = getCity();
        if: ($temp.nextCity != '')
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game intro iterate"])}} || question = true
            go!: ../Goroda game question 
        else:
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game intro"]["forgotAll"])}} || question = true
            
    state: Goroda game question
        
        state: DisagreeWithRules
            q: $disagree
            go!: ../../Goroda game end  

        state: AgreeWithRules
            q: $agree
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["AgreeWithRules"])}} || question = true
            go!: ../../Goroda game question        
           
        state: Remind user a letter
            q: * {(как*|напом*|*скажи*|назови*) * (букв*|слов*|город*) } *
            q: * {(что|как*) [букв*|слов*|город*] * (был*|предшествовал*|прозвучал*|говорил*|сказал*)} *
            q: * (что [ты] [сказал] [это] [за]/кто/{повтори [ещё] [раз*]}/какой/чего) *
            script:
                $temp.lastCity = $session.gameCities[$session.gameCities.length - 1];
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game question"]["Remind user a letter"])}} || question = true
            go!: ../../Goroda game question        

        state: Goroda game answer true
            q: * $City [или] [$City::City2] * || fromState = "../../Goroda game question", onlyThisState = true
            q: * $City [или] [$City::City2] * || fromState = "../Goroda game answer undefined", onlyThisState = true
            q: * $City [или] [$City::City2] * || fromState = "../Goroda game answer city not true", onlyThisState = true
            q: * $City [или] [$City::City2] * || fromState = "../Goroda game giving up", onlyThisState = true
            if: ($parseTree.City2 && ($parseTree._City2.name !== "Великий Новгород") && ($parseTree._City.name !== "Нижний Новгород"))
                a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["severalCities"])}} || question = true
                go!: ../../Goroda game question  
            else:
                if: ($parseTree.text !== $parseTree.City[0].text)
                    if: ($parseTree.City2 && ($parseTree._City2.name !== "Великий Новгород") && ($parseTree._City.name !== "Нижний Новгород"))
                        a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["getCityFromText"])}} || question = true            
                if: checkCity($parseTree.City[0])
                    if: isNewCity($parseTree._City.name)
                        script:
                            if ($parseTree.City2 && ($parseTree._City2.name === "Великий Новгород") && ($parseTree._City.name === "Нижний Новгород")) {
                                    $parseTree.City[0].text = "Нижний Новгород";
                                }
                        if: (my_capitalize($nlp.parseMorph($parseTree.City[0].text).normalForm) !== $parseTree._City.name)
                            script:
                                $temp.variantCityName = my_capitalize($parseTree.City[0].text);
                                $temp.officialCityName = $parseTree._City.name;
                            if: !alternateWriting($temp.variantCityName, $temp.officialCityName)
                                script:
                                    $temp.variantFirstLetter = $temp.variantCityName.charAt(0);
                                    $temp.officialFirstLetter = $temp.officialCityName.charAt(0);
                                if: $temp.variantFirstLetter !== $temp.officialFirstLetter
                                    a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["wrongNameVariant"])}} || question = true
                                    go!: ../../Goroda game question
                                else:
                                    a: {{selectRandomArg($CitiesGameAnswers["Goroda game question"]["Goroda game answer"]["correctNameVariant"])}} || question = true
                        script:
                            $session.gameCities.push($parseTree._City.name);
                        a: {{getCity()}} || question = true
                        go!: ../../Goroda game question   
                    else:
                        script:
                            if ($parseTree.City2 && ($parseTree._City2.name === "Великий Новгород") && ($parseTree._City.name === "Нижний Новгород")) {
                                    $temp.userCity = "Нижний Новгород";
                                    $temp.userText = "Нижний Новгород";
                            }
                            else {
                                $temp.userCity = $parseTree._City.name;
                                $temp.userText =  my_capitalize($parseTree.City[0].text);
                            }
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
                q: * ($agree|попробую|продолжаем) * || fromState = .., onlyThisState = true
                go!: ../../Remind user a letter

            state: Goroda game answer city not true no
                q: * $dontKnow * || fromState = .., onlyThisState = true
                script:
                    $temp.lastCity = $session.gameCities[$session.gameCities.length - 1];
                a: {{ $CitiesGameAnswers["Goroda game answer city not true no"] }}
                go!: ../../../Goroda game question

            state: Goroda game answer undefined stop
                q: * ($disagree|$stopGame) * || fromState = .., onlyThisState = true
                go!: ../../../Goroda game end

            state: Goroda game answer undefined undefined
                q: * $Text::AnyWord * || fromState = .., onlyThisState = true
                go!: ../../Goroda game answer undefined

        state: Goroda game do not know answer
            q: * не (знаю|могу) [ответить|ответ*|назвать|придумать] [на [букву] $lettersAll] * 
            q: * не (знаю|могу) [ответить|ответ*|назвать|придумать|дать|сказать] (город|города|ответ) на [букву] $lettersAll * 
            q: * на [букву] $lettersAll * не (знаю|могу) [ответить|ответ*|назвать|придумать|дать|сказать] * (город|города|ответ|ничего) * 
            q: * (нет|нету) * (города|городов|ничего) * на [букву] $lettersAll * || fromState = "../../Goroda game question"
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game do not know answer"])}}
            go!: ../Goroda game answer city not true

        state: Goroda game letterNoCity answer
            q: * не (знаю|могу) [ответить|ответ*|назвать|придумать|дать|сказать] * (город|города|ответ|ничего) * на [букву] $letterNoCity * || fromState = "../../Goroda game question"
            q: * (нет|нету) * (города|городов|ничего) * на [букву] $letterNoCity * || fromState = "../../Goroda game question"
            q: * на [букву] $letterNoCity * не (знаю|могу) [ответить|ответ*|назвать|придумать|дать|сказать] * (город|города|ответ|ничего) * || fromState = "../../Goroda game question"
            q: * на [букву] $letterNoCity * (нет|нету) * (города|городов|ничего) *  || fromState = "../../Goroda game question"
            script:
                $temp.letter = $parseTree._letterNoCity;
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game letterNoCity answer"])}} || question = true
            go!: ../../Goroda game question  
            

        state: Goroda game user does not know the city
            q: * { [я] [даже] [и] (не знаю|не знал*) [этот|этого|такой|такого] (город|города) } * 
            q: * { [я] [даже] [и] (не знаю|не знал*) * (этот|этого|такой|такого) [город|города] } * 
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game user does not know the city"])}} || question = true
            go!: ../Goroda game answer city not true

        state: Goroda game thank you for repeat
            q: * (спасибо|благодар*|понятн*) *
            script:
                $response.action = 'ACTION_EXP_HAPPY';
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game thank you for repeat"])}} || question = true
            go!: ../../Goroda game question 

        state: Goroda game giving up
            q: * (подскажи/помоги/подсказать/(скажи/какой/назови) * ответ/сам (ответь/отвечай)) * [не знаю] *
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game giving up"]["notByRules"])}} || question = true
            a: {{ selectRandomArg($CitiesGameAnswers["Goroda game giving up"]["giveUp"])}} || question = true
            
            state: Goroda game giving up yes
                q: * {($maybe|$sure|$agree) [$maybe|$sure|$agree|$disagree] }* 
                go!: ../../../Goroda game end

            state: Goroda game giving up no
                q: * $disagree *
                a: {{selectRandomArg($CitiesGameAnswers["Goroda game giving up no"])}}  || question = true
                go!: ../../Remind user a letter

    state: Goroda game end
        q: [я] (устал*/сдаюсь/сдаёмся)
        q: * $stopGame *
        q: * $stopGame * || fromState = "../Goroda game question"
        q: * { (хоч*|давай*) (всё|все) } *
        a: {{selectRandomArg($CitiesGameAnswers["Goroda game end"]["a1"])}}
        a: {{selectRandomArg($CitiesGameAnswers["Goroda game end"]["a2"])}}
        a: {{getResultString()}}
        script:
            $session.gameCities = [];
            $session.lastQuiz = undefined;   
            $client.sortedCitiesByLetter = {};
        go!: /

    state: Goroda game new iteration
        q: * {[хоч*|давай*] (еще|ещё|снова|заново) [раз] [сыгра*|*игра*] [в] [город*]} * || fromState = "../Goroda game question", onlyThisState = true
        q: * {(хоч*|давай*) (сначала|[c] начала) [сыгра*|*игра*] [в] [город*] } * || fromState = "../Goroda game question", onlyThisState = true
        a: {{selectRandomArg($CitiesGameAnswers["Goroda game new iteration"])}} || question = true
        a: {{getResultString()}}
        script:
            $session.gameCities = [];
            $session.lastQuiz = undefined;   
            $client.sortedCitiesByLetter = {};
        go!: ../Goroda game intro iterate    

    state: Goroda game User asks game progress
        q: * { * (сколько|какой) [у нас] (слов|результат|город*) [набрал*|получилось] } *
        a: {{getResultString()}}
        a: {{ selectRandomArg($CitiesGameAnswers["Goroda game User asks game progress"])}} || question = true
        go!: ../Goroda game question/Goroda game answer city not true


    state: Goroda WantMore
        q: * {[хоч*|давай*] (еще|ещё|снова|заново) [раз] [сыгра*|*игра*] [в] [город*] [дальше]} * || fromState = "../Goroda game end", onlyThisState = true
        q: * {[(хоч*|давай*)] (сначала|[c] начала|продолжим|продолжить|дальше) [сыгра*|*игра*] [в] [город*] } * || fromState = "../Goroda game end", onlyThisState = true
        a: {{selectRandomArg($CitiesGameAnswers["Goroda WantMore"])}}  || question = true
        go!: ../Goroda game intro iterate