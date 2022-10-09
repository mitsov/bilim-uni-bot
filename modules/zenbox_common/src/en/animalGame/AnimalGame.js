///////////////////////////////////////////////////////////////////////////////
///////////////        Генерация вопроса про животных            //////////////
///////////////////////////////////////////////////////////////////////////////

function getZooQuestion() {
    var $session = $jsapi.context().session;
    var res;
    var questionType;
    
    var questionTypes = ["savagery","class","numExtremities","parts","employment","coating","whoseParts"];
    if (testMode() && $session.AnimalGameTestNumber && questionTypes.length > $session.AnimalGameTestNumber) {
        questionType = questionTypes[$session.AnimalGameTestNumber];
    } else {
        questionType = getRandomQuestionType("savagery","class","numExtremities","parts","employment","coating","whoseParts"); 
    }

    
    if (questionType == "whoseParts") {
        questionType = selectRandomArg("wings","fins","horns","hoof","penny");
    }
    
    var randAnimal = getAnimal(questionType);
    var otherAnimal = getPairAnimal(randAnimal, questionType);
    var parts = false;
    switch(questionType) {
        case "savagery":
            res = "Is " + randAnimal.title + " a " + getPair(randAnimal, questionType, otherAnimal) + " animal?";
            break;
        case "class":
            res = "Is " + randAnimal.title + " a " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;
        case "numExtremities":
            res = "How many " + randAnimal.extremities + " does " + randAnimal.genTitle + " have?";
            break;
        case "parts":
            var returnedGetTwoExtremities = getTwoExtremities(randAnimal);
            parts = returnedGetTwoExtremities.parts;
            res = "What does " + randAnimal.genTitle + " have: " + returnedGetTwoExtremities.message + "?";
            break;
        case "employment":
            res = "Who " + randAnimal.employment + " - " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;
        case "coating":
            res = "Does " + randAnimal.genTitle + " have " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;
        case "wings":
            res = "Who has wings: " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;            
        case "fins":
            res = "Who has fins: " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;          
        case "horns":
            res = "Who has horns: " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;              
        case "hoof":
            res = "Who has hoofs: " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;              
        case "penny":
            res = "Who has a snout: " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;             
        case "trunk":
            res = "Who has a trunk: " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;           
        case "tusks":
            res = "Who has tusks: " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;                                            
    }

    var countQuestions = 1;
    if ($session.lastQuiz && $session.lastQuiz.numQuestions) {
        countQuestions = $session.lastQuiz.numQuestions + 1;
    }

    var countAnswers = 0;
    if ($session.lastQuiz && $session.lastQuiz.rightAnswers) {
        countAnswers = $session.lastQuiz.rightAnswers;
    }

    return {
        type: questionType,
        text: res,
        animal: randAnimal,
        numQuestions: countQuestions,
        rightAnswers: countAnswers,
        otherAnimal: otherAnimal,
        parts : parts
    };

}


function getRightAnswer(){
    var $session = $jsapi.context().session;
    var right;
    switch($session.lastQuiz.type){
        case "savagery":
            right = $session.lastQuiz.animal.savagery + ". ";
            break;
        case "class":
            right = $session.lastQuiz.animal.class + ". ";
            break;
        case "numExtremities":
            right = $session.lastQuiz.animal.numExtremities + ". ";
            break;
        case "parts":
            right = getCorrectPart() + ". ";
            break;
        case "employment":
            right = $session.lastQuiz.animal.title + ". ";
            break;
        case "coating":
            right = $session.lastQuiz.animal.coating + ". ";
            break;
        case "wings":
        case "fins":
        case "horns":
        case "hoof":
        case "penny":
        case "trunk":
        case "tusks":
            right = $session.lastQuiz.animal.genTitle + ". ";
            break;            
        
        }

    return right;
}

function getCorrectPart(){
    var $session = $jsapi.context().session;
    var res;

    if ($session.lastQuiz.animal.wings != "" && ($session.lastQuiz.text.indexOf("wings") != -1)){
        res = "wings";
    }
    if ($session.lastQuiz.animal.fins != "" && ($session.lastQuiz.text.indexOf("fins") != -1)){
        res = "fins";
    }
    if ($session.lastQuiz.animal.horns != "" && ($session.lastQuiz.text.indexOf("horns") != -1)){
        res = "horns";
    }
    if ($session.lastQuiz.animal.hoof != "" && ($session.lastQuiz.text.indexOf("hoofs") != -1)){
        res = "hoofs"; 
    }
    if ($session.lastQuiz.animal.penny != "" && ($session.lastQuiz.text.indexOf("snout") != -1)){
        res = "snout";
    }
    if ($session.lastQuiz.animal.trunk != "" && ($session.lastQuiz.text.indexOf("trunk") != -1)){
        res = "trunk";
    }
    if ($session.lastQuiz.animal.tusks != "" && ($session.lastQuiz.text.indexOf("tusks") != -1)){
        res = "tusks";
    }

    return res;
}



function getRandomQuestionType() {  
    var $session = $jsapi.context().session; 
    var types = [];
    if ($session.lastQuiz) {
        for (var j = 0; j < arguments.length; j++) {
            if ($session.lastQuiz.type != arguments[j]) {
                types.push(arguments[j]);
            }
        }
    } else {
        types = arguments;
    } 
    var index = randomIndex(types);
    return types[index];
}



///////////////////////////////////////////////////////////////////////////////
///////////////             Подбор данных для вопросов "или"     //////////////
///////////////////////////////////////////////////////////////////////////////


function getPair(animal, type, otherAnimal) {
    var res;
    var first;
    var second;

    switch(type) {

        case "savagery":
            first = animal.savagery;
            second = otherAnimal.savagery;
            break;
        case "class":
            first = animal.class;
            second = otherAnimal.class;
            break;
        case "numExtremities":
            first = animal.numExtremities;
            second = otherAnimal.numExtremities;
            break;  
        case "employment":
            first = animal.title;
            second = otherAnimal.title;
            break;   
        case "coating":
            first = animal.coating;
            second = otherAnimal.coating;
            break;    
        case "wings":
        case "fins":    
        case "horns":  
        case "hoof":  
        case "penny":
        case "trunk":  
        case "tusks":                                                                              
            first = animal.genTitle;
            second = otherAnimal.genTitle;     
            break;                        
    }

    var rand = selectRandomArg("1","2");  
    res = (rand == "1") ? first + " or a " + second : second + " or " + first;
    return res;
}

function getTwoExtremities(animal) {
    var res;
    var hasExtremities = [];
    var hasnotExtremities = [];

    if (animal.wings != "") {
        hasExtremities.push({title : "wings", value :1});
    } else {
        hasnotExtremities.push({title : "wings", value :1});
    }
    if (animal.fins != "") {
        hasExtremities.push({title : "fins", value :2});
    } else {
        hasnotExtremities.push({title : "fins", value :2});
    }
    if (animal.horns != "") {
        hasExtremities.push({title : "horns", value :3});
    } else {
        hasnotExtremities.push({title : "horns", value :3});
    }
    if (animal.hoof != "") {
        hasExtremities.push({title : "hoofs", value :4});
    } else {
        hasnotExtremities.push({title : "hoofs", value :4});
    }
    if (animal.penny != "") {
        hasExtremities.push({title : "snout", value :5});
    } else {
        hasnotExtremities.push({title : "snout", value :5});
    }
    if (animal.trunk != "") {
        hasExtremities.push({title : "trunk", value :6});
    } else {
        hasnotExtremities.push({title : "trunk", value :6});
    }
    if (animal.tusks != "") {
        hasExtremities.push({title : "tusks", value :7});
    } else {
        hasnotExtremities.push({title : "tusks", value :7});
    }                        

    var hasExtremity = hasExtremities[randomIndex(hasExtremities)];
    var hasnotExtremity = hasnotExtremities[randomIndex(hasnotExtremities)];

    var rand = selectRandomArg("1","2"); 
    res = (rand == "1") ? hasExtremity.title + " or " + hasnotExtremity.title : hasnotExtremity.title + " or " + hasExtremity.title;
    var ret =  {message : res,
                parts : {
                        right : hasExtremity,
                        wrong : hasnotExtremity
                        }
                }
    return ret;
}

///////////////////////////////////////////////////////////////////////////////
///////////////      Получение кодов покрытий, классов и т.п.    //////////////
///////////////////////////////////////////////////////////////////////////////

function getCoatingCode(animal) {
    var res = "0";
    switch(animal.coating) {
        case "fur":
            res = "1";
            break;
        case "feathers":
            res = "2";
            break;
        case "carapace":
            res = "3";
            break;
        case "skin":
            res = "4";
            break;   
        case "scales":
            res = "5";
            break; 
        case "spines":
            res = "6";
            break;                                              
    }
    return res;
}

function getClassCode(animal) {
    var res = "0";
    switch(animal.class) {
        case "mammal":
            res = "1";
            break;
        case "bird":
            res = "2";
            break;
        case "insect":
            res = "3";
            break;
        case "fish":
            res = "4";
            break;                        
    }
    return res;
}

function getSavageryCode(animal) {
    var res = "0";
    switch(animal.savagery) {
        case "domestic":
            res = "1";
            break;
        case "wild":
            res = "2";
            break;
    }
    return res;
}

function operateWithAnswer_AnimalGame(answerWithNo, answer, anotherAnswer, rightAnswer, wrongAnswer, AnswerIsAnimal) {
//answerWithNo - ответ с НЕ (не слон); 
//answer - просто ответ (слон);
//anotherAnswer - еще один просто ответ; 
//rightAnswer - правильный вариант ответа на вопрос из предложенных;
//wrongAnswer - НЕ правильный вариант ответа на вопрос из предложенных;
//AnswerIsAnimal - true если в ответ должен принять название животного;
    var $temp = $jsapi.context().temp;
    if (answerWithNo && !answer && !answerWithNo[1]) {
        if (AnswerIsAnimal ? formCondition_when_AnswerIsAnimal(answerWithNo[0], wrongAnswer) : wrongAnswer == Number(answerWithNo[0].value)) {
            $temp.nextState = 'RightAnswer';

        } else if ( AnswerIsAnimal ? formCondition_when_AnswerIsAnimal(answerWithNo[0], rightAnswer) : rightAnswer == Number(answerWithNo[0].value)) {
            $temp.nextState = 'WrongAnswer';
        } else {
            $temp.entryReason = 'User answered not stated variant';
            $temp.nextState = 'Animal game question Negative';
        }
    } else if (answer && !anotherAnswer ) {
        if (AnswerIsAnimal ? formCondition_when_AnswerIsAnimal(answer, rightAnswer)  : rightAnswer == Number(answer)){
            $temp.nextState = 'RightAnswer';
        } else if (AnswerIsAnimal ? formCondition_when_AnswerIsAnimal(answer, wrongAnswer) : wrongAnswer == Number(answer)){
            $temp.nextState = 'WrongAnswer';
        } else {
            $temp.entryReason = 'User answered not stated variant';
            $temp.nextState = 'Animal game question Negative';
        }
    } else {
        $temp.entryReason = 'User answered more then one answer';
        $temp.nextState = 'Animal game question Negative';
    }
}
function formCondition_when_AnswerIsAnimal(whatToCompare, compareWithWhat) {
//сравнивает введенное название животного с различными его вариациями, которые прописаны в csv
//возвращает true, если находит животное в csv
 
    var ret =   compareWithWhat == whatToCompare.value.title  || 
                compareWithWhat == whatToCompare.value.male   || 
                compareWithWhat == whatToCompare.value.female || 
                (whatToCompare.value.otherTitles).indexOf(compareWithWhat) != -1;
    return ret
}