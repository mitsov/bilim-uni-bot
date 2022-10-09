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
            res = capitalize(randAnimal.title) + " - " + getPair(randAnimal, questionType, otherAnimal) + " животное?";
            break;
        case "class":
            res = capitalize(randAnimal.title) + " - это " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;
        case "numExtremities":
            res = "Сколько " + randAnimal.extremities + " у " + randAnimal.genTitle + "?";
            break;
        case "parts":
            var returnedGetTwoExtremities = getTwoExtremities(randAnimal);
            parts = returnedGetTwoExtremities.parts;
            res = "Что есть у " + randAnimal.genTitle + ": " + returnedGetTwoExtremities.message + "?";
            break;
        case "employment":
            res = "Кто " + randAnimal.employment + " - " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;
        case "coating":
            res = "У " + randAnimal.genTitle + " " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;
        case "wings":
            res = "У кого есть крылья: у " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;            
        case "fins":
            res = "У кого есть плавники: у " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;          
        case "horns":
            res = "У кого есть рога: у " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;              
        case "hoof":
            res = "У кого есть копыта: у " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;              
        case "penny":
            res = "У кого есть пятачок: у " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;             
        case "trunk":
            res = "У кого есть хобот: у " + getPair(randAnimal, questionType, otherAnimal) + "?";
            break;           
        case "tusks":
            res = "У кого есть бивни: у " + getPair(randAnimal, questionType, otherAnimal) + "?";
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
            right = "у " + $session.lastQuiz.animal.genTitle + ". ";
            break;            
        
        }

    return right;
}

function getCorrectPart(){
    var $session = $jsapi.context().session;
    var res;

    if ($session.lastQuiz.animal.wings != "" && ($session.lastQuiz.text.indexOf("крылья") != -1)){
        res = "крылья";
    }
    if ($session.lastQuiz.animal.fins != "" && ($session.lastQuiz.text.indexOf("плавники") != -1)){
        res = "плавники";
    }
    if ($session.lastQuiz.animal.horns != "" && ($session.lastQuiz.text.indexOf("рога") != -1)){
        res = "рога";
    }
    if ($session.lastQuiz.animal.hoof != "" && ($session.lastQuiz.text.indexOf("копыта") != -1)){
        res = "копыта"; 
    }
    if ($session.lastQuiz.animal.penny != "" && ($session.lastQuiz.text.indexOf("пятачок") != -1)){
        res = "пятачок";
    }
    if ($session.lastQuiz.animal.trunk != "" && ($session.lastQuiz.text.indexOf("хобот") != -1)){
        res = "хобот";
    }
    if ($session.lastQuiz.animal.tusks != "" && ($session.lastQuiz.text.indexOf("бивни") != -1)){
        res = "бивни";
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
    res = (rand == "1") ? first + " или " + second : second + " или " + first;
    return res;
}

function getTwoExtremities(animal) {
    var res;
    var hasExtremities = [];
    var hasnotExtremities = [];

    if (animal.wings != "") {
        hasExtremities.push({title : "крылья", value :1});
    } else {
        hasnotExtremities.push({title : "крылья", value :1});
    }
    if (animal.fins != "") {
        hasExtremities.push({title : "плавники", value :2});
    } else {
        hasnotExtremities.push({title : "плавники", value :2});
    }
    if (animal.horns != "") {
        hasExtremities.push({title : "рога", value :3});
    } else {
        hasnotExtremities.push({title : "рога", value :3});
    }
    if (animal.hoof != "") {
        hasExtremities.push({title : "копыта", value :4});
    } else {
        hasnotExtremities.push({title : "копыта", value :4});
    }
    if (animal.penny != "") {
        hasExtremities.push({title : "пятачок", value :5});
    } else {
        hasnotExtremities.push({title : "пятачок", value :5});
    }
    if (animal.trunk != "") {
        hasExtremities.push({title : "хобот", value :6});
    } else {
        hasnotExtremities.push({title : "хобот", value :6});
    }
    if (animal.tusks != "") {
        hasExtremities.push({title : "бивни", value :7});
    } else {
        hasnotExtremities.push({title : "бивни", value :7});
    }                        

    var hasExtremity = hasExtremities[randomIndex(hasExtremities)];
    var hasnotExtremity = hasnotExtremities[randomIndex(hasnotExtremities)];

    var rand = selectRandomArg("1","2"); 
    res = (rand == "1") ? hasExtremity.title + " или " + hasnotExtremity.title : hasnotExtremity.title + " или " + hasExtremity.title;
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
        case "шерсть":
            res = "1";
            break;
        case "перья":
            res = "2";
            break;
        case "панцирь":
            res = "3";
            break;
        case "кожа":
            res = "4";
            break;   
        case "чешуя":
            res = "5";
            break; 
        case "иголки":
            res = "6";
            break;                                              
    }
    return res;
}

function getClassCode(animal) {
    var res = "0";
    switch(animal.class) {
        case "зверь":
            res = "1";
            break;
        case "птица":
            res = "2";
            break;
        case "насекомое":
            res = "3";
            break;
        case "рыба":
            res = "4";
            break;                        
    }
    return res;
}

function getSavageryCode(animal) {
    var res = "0";
    switch(animal.savagery) {
        case "домашнее":
            res = "1";
            break;
        case "дикое":
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