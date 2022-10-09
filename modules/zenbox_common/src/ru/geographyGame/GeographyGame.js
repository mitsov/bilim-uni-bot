var GeographyGame = (function() {
    // инициализация необходимых переменных
    function init(){
        var $session = $jsapi.context().session;
        var $client = $jsapi.context().client;
    
        $client.levels = {}; //словарь городов по уровням
        $session.questionType = 0; //текущий тип вопроса
        $session.gameCounter = 0; //количество ответов
        $session.gameLevelCounter = 0; //разность правильных и неправильных ответов
        $session.gameRightCounter = 0; //количество правильных ответов
        $client.geographyLevel = ($client.geographyLevel) ? $client.geographyLevel : 1; //текущий уровень пользователя
        var i, j, complexity;
        for (i=1; i<Object.keys($Geography).length; i++){
            complexity = $Geography[i].value.complexity;
            if (!(complexity in $client.levels)){
                $client.levels[complexity] = {1:[], 2:[], 3:[], 4:[], 5:[]};
            } 
            for (j=1;j<5;j++){    
                $client.levels[complexity][j].push(i);
            } 
            if ($Geography[i].value.continent != "Австралия и Океания"){
                $client.levels[complexity][5].push(i);
            }
        }
        // из словаря levels будут удаляться использованные значения, и будут пополняться из словаря levelsConst при опустении.
        $client.levelsConst = $client.levels; 
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    ///////////////        Генерация вопроса по географии            //////////////
    ///////////////////////////////////////////////////////////////////////////////
    
    
    
    //вопрос типа В какой стране находится город Лондон?
    function getQuestionType1(){
        var $session = $jsapi.context().session;
        var $client = $jsapi.context().client;    
        check_dict($client.geographyLevel);
        var value = getInfo($client.geographyLevel);
    
        $session.capital = value.name;
        $session.genCountry = value.genCountry;
        var country = value.country;
        $session.fact = (value.facts.length) ? (selectRandomArg.apply(this, value.facts) + $GeographyGameAnswers["getQuestion"]) : "";
        
        $session.question = $GeographyGameAnswers["getQuestionType1"]["question"];
        $session.rightAnswer = country;
        $session.rightAnswerText = $GeographyGameAnswers["getQuestionType1"]["rightAnswerText"];
    } 
    
    //вопрос типа Какой город является столицей Великобритании?
    function getQuestionType2(){
        var $client = $jsapi.context().client;    
        var $session = $jsapi.context().session;
        check_dict($client.geographyLevel);
        var value = getInfo($client.geographyLevel);
    
        $session.capital = value.name;
        $session.genCountry = value.genCountry;
        $session.fact = (value.facts.length) ? (selectRandomArg.apply(this, value.facts) + $GeographyGameAnswers["getQuestion"]) : "";
    
        $session.question = $GeographyGameAnswers["getQuestionType2"]["question"];
        $session.rightAnswer = $session.capital;
        $session.rightAnswerText = $GeographyGameAnswers["getQuestionType2"]["rightAnswerText"];
        
    }
    
    //вопрос типа Какой из городов является столицей Великобритании: Лондон или Париж?
    function getQuestionType3(){ 
        var $client = $jsapi.context().client;    
        var $session = $jsapi.context().session; 
        check_dict($client.geographyLevel);
        var values = getInfo($client.geographyLevel + 1, true)
        var value = values[0];
        var value2 = values[1];
    
        $session.capital = value.name;
        $session.genCountry = value.genCountry;
        $session.capitalFalse = value2.name;
        $session.fact = (value.facts.length) ? (selectRandomArg.apply(this, value.facts) + $GeographyGameAnswers["getQuestion"]): "";
        $session.firstCapital = selectRandomArg($session.capital, $session.capitalFalse);
        $session.secondCapital = ($session.firstCapital == $session.capital) ? $session.capitalFalse : $session.capital;
        $session.question = $GeographyGameAnswers["getQuestionType3"]["question"];   
        $session.rightAnswer = $session.capital;
        $session.rightAnswerText = $GeographyGameAnswers["getQuestionType3"]["rightAnswerText"];
    
    }
    
    //вопрос типа В какой стране находится город Лондон: Великобритания или Франция?
    function getQuestionType4(){
        var $client = $jsapi.context().client;    
        var $session = $jsapi.context().session;
        check_dict($client.geographyLevel + 1);
        var values = getInfo($client.geographyLevel + 1, true)
        var value = values[0];
        var value2 = values[1];
    
        $session.capital = value.name;
        var country = value.country;
        $session.genCountry = value.genCountry;
        var country2 = value2.country;
        $session.fact = (value.facts.length) ? (selectRandomArg.apply(this, value.facts) + $GeographyGameAnswers["getQuestion"]) : "";
    
        $session.firstCountry = selectRandomArg(country, country2);
        $session.secondCountry = ($session.firstCountry == country) ? country2 : country;
        $session.question = $GeographyGameAnswers["getQuestionType4"]["question"];  
        $session.rightAnswer = value.country;
        $session.rightAnswerText = $GeographyGameAnswers["getQuestionType4"]["rightAnswerText"];
    
    }
    
    //вопрос типа На каком континенте находится страна Великобритания?
    function getQuestionType5(){
        var $client = $jsapi.context().client;    
        var $session = $jsapi.context().session;
        check_dict($client.geographyLevel);
        var value = getInfo($client.geographyLevel);
    
        $session.country = value.country;
        $session.continent = value.continent;
    
        $session.question = $GeographyGameAnswers["getQuestionType5"]["question"];
        $session.rightAnswer = $session.continent;
        $session.rightAnswerText = $GeographyGameAnswers["getQuestionType5"]["rightAnswerText"];
        if (value.continent_part) {
            $session.continent_part = value.continent_part;
            if ($session.continent_part === 'Европа') {
                $session.rightAnswerTextAboutEurasia = $GeographyGameAnswers["getQuestionType5"]["inEurope"];
            } else {
                $session.rightAnswerTextAboutEurasia = $GeographyGameAnswers["getQuestionType5"]["inAsia"];
            }
        }
    }
    
    function getAnswerCapital($parseTree){
        var $session = $jsapi.context().session;
        $session.nextState = undefined; 
        $session.askNow = false;
        if ($parseTree.City2) {
            if ($parseTree.City) {
                if ($parseTree.City[0].value.name != $parseTree.City2[0].value.name){
                    $session.askNow = true;
                    $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerCapital"]["notOneCity"]));
                } else {
                    $session.nextState = "WrongAnswer";
                }
            } else {
                if ($parseTree.Capital[0].value.name != $parseTree.City2[0].value.name){
                    $session.askNow = true;
                    $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerCapital"]["notOneCity"]));
                } else {
                    var condition = ($parseTree.Capital[0].value.name == $session.rightAnswer);  
                    $session.nextState = (condition) ? "RightAnswer" : "WrongAnswer"; 
                }
            }
        } else if ($parseTree.City){
            var condition = ($parseTree.City[0].value.name == $session.rightAnswer);      
            $session.nextState = (condition) ? "RightAnswer" : "WrongAnswer";
        } else if ($session.questionType == 1 || $session.questionType == 4){
            if ($parseTree.Capital[0].value.duplicateName == "false"){
                $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerCapital"]["notCountry"]));
                $session.askNow = true; 
            } else {
                $session.nextState = "Geography quiz get question/Geography quiz get question answer definite Country";
            }       
        } else if ($session.questionType == 2 || $session.questionType == 3) {
            if ($parseTree.Capital){
                var condition = ($parseTree.Capital[0].value.name == $session.rightAnswer);      
            } else {
                var condition = ($parseTree.Country[0].value.name == $session.rightAnswer);      
            }
            $session.nextState = (condition) ? "RightAnswer" : "WrongAnswer"; 
        } else {
            $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerCapital"]["notContinent"]));
            $session.askNow = true;
        }
    
    }
    
    function getAnswerCountry($parseTree){
        var $session = $jsapi.context().session;
        $session.nextState = undefined; 
        $session.askNow = false;
        var condition;
        if ($parseTree.Country && $parseTree.Country[1] && ($parseTree.Country[0].value.name != $parseTree.Country[1].value.name)) {
            $session.askNow = true;
            $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerCountry"]["notOneCountry"]));
        } else if ($session.questionType == 1 || $session.questionType == 4){
            if ($parseTree.Country){
                condition = ($parseTree.Country[0].value.name == $session.rightAnswer);  
            } else {
                condition = ($parseTree.Capital[0].value.name == $session.rightAnswer);  
            }    
            $session.nextState = (condition) ? "RightAnswer" : "WrongAnswer"; 
        } else if ($session.questionType == 2 || $session.questionType == 3) {
            if ($parseTree.Capital){
                $session.nextState = "Geography quiz get question/Geography quiz get question answer definite Capital";
            } else {
                if ($parseTree.Country[0].value.duplicateName == "false"){
                    $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerCountry"]["notCity"]));
                    $session.askNow = true;
                } else {
                    $session.nextState = "Geography quiz get question/Geography quiz get question answer definite Capital";
                }    
            }    
        } else {
            if ($parseTree.Country[0].text.toLowerCase() == "америка") {
                if ($session.rightAnswer == "Африка" || $session.rightAnswer == "Евразия") {
                    $session.nextState = "WrongAnswer";
                } else {
                    $reactions.answer(selectRandomArg($GeographyGameAnswers["NorthSouth"]));
                    $session.askNow = true; 
                } 
            } else {
                    if ($parseTree.Country[0].text.toLowerCase() == "австралия") {
                        if ($session.rightAnswer == "Австралия") 
                            $session.nextState = "RightAnswer";
                        else
                            $session.nextState = "WrongAnswer";
                    }
                    else {
                        $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerCountry"]["notContinent"]));
                        $session.askNow = true; 
                    }
                
            }
        }
    }
    
    function getAnswerContinent($parseTree){
        var $session = $jsapi.context().session;
        var $temp = $jsapi.context().temp;

        $session.nextState = undefined;
        $session.askNow = false;
        var condition;
        if ($parseTree.Continent && $parseTree.Continent[1]) {
            $session.askNow = true;
            $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerContinent"]["notOneContinent"]));
        } else if ($parseTree.Continent && $parseTree.Continent[0].value == 0){
            condition = false;
        } else if ($session.questionType == 1 || $session.questionType == 4){
            $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerContinent"]["notCountry"]));
            $session.askNow = true;
        } else if ($session.questionType == 2 || $session.questionType == 3) {
            $reactions.answer(selectRandomArg($GeographyGameAnswers["getAnswerContinent"]["notCity"]));
            $session.askNow = true;
        } else {
            if ($session.rightAnswer == "Африка"){
                condition = ($parseTree.Continent[0].value == $session.rightAnswer);
            } else if ($session.rightAnswer == "Евразия"){
                if ($parseTree.Continent[0].value === 'Европа' || $parseTree.Continent[0].value === 'Азия') {
                    if ($parseTree.Continent[0].value === $session.continent_part) {
                        condition = true;
                    } else {
                        $temp.wrongEurasiaPart = $session.rightAnswerTextAboutEurasia;
                    }
                } else {
                    condition = ($parseTree.Continent[0].value == $session.rightAnswer);   
                }
            } else {
                if ($parseTree.Continent && $parseTree.Continent[0].value == "Америка") {
                    $session.askNow = true;
                    $reactions.answer(selectRandomArg($GeographyGameAnswers["NorthSouth"]));
                } else if ($parseTree.AmericaDescr){
                    var answ;
                    if($parseTree.AmericaDescr[0].value == "Северная_Америка"){
                        answ = "Северная Америка";
                    }
                    else{
                        answ = "Южная Америка";
                    }
                    condition = (answ == $session.rightAnswer);
                } else if ($parseTree.Continent){
                    var answ;
                    if($parseTree.Continent[0].value == "Северная_Америка"){
                        answ = "Северная Америка";
                    }
                    else{
                        answ = "Южная Америка";
                    }
                    condition = (answ == $session.rightAnswer);
                } else {
                    condition = ("Северная Америка" == $session.rightAnswer);  
                }
            } 
        }
        $session.nextState = (condition) ? "RightAnswer" : "WrongAnswer";    
    }

    return {
        init: init,
        getQuestionType1: getQuestionType1,
        getQuestionType2: getQuestionType2,
        getQuestionType3: getQuestionType3,
        getQuestionType4: getQuestionType4,
        getQuestionType5: getQuestionType5,
        getAnswerCapital: getAnswerCapital,
        getAnswerCountry: getAnswerCountry,
        getAnswerContinent: getAnswerContinent
    };
})();
        