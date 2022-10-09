function getMathQuestion() {
    var $client = $jsapi.context().client;
    var question, questionLevel, questionType;

    //определяем уровень игрока: если не было никакого уровня, назначаем 0-й
    //определяем, нужно ли повышение/понижение уровня по рез-там игры
    defineMathLevel();

    //определяем уровень сложности очередного вопроса
    questionLevel = $client.mathLevel.testIsNeeded ? $client.mathLevel.level + 1 : $client.mathLevel.level;

    //получаем тип вопроса по уровню сложности
    questionType = getMathQuestionType(questionLevel);

    //получаем вопрос нужного типа, нужной сложности
    question = getMathQuestionByType(questionType, questionLevel);

    return {
        type: questionType,
        text: question.text,
        result: question.result
    };

}

//определяем вопрос нужного типа, нужного уровня
function getMathQuestionByType(type, level) {
    switch(type) {
        case "missingNumber":
            return getMissingNumberQuestion(level);
        case "moreLess":
            return getMoreLessQuestion();
        case "betweenNumber":
            return getBetweenNumberQuestion();
        case "add":
        case "sub":
        case "mul":
        case "div":
        case "addSub":                   
            return getMathFuncQuestion(type, level);
        case "mathAnimals":
            return getMathAnimalsQuestion(level);                                                
    }
}

//определяем тип очередного вопроса
function getMathQuestionType(level) {
    switch(level) {
        case 0:
            return getRandomQuestionType("missingNumber", "moreLess", "betweenNumber");
        case 1:
            return getRandomQuestionType("missingNumber", "add", "sub", "mathAnimals");
        case 2:
        case 3:        
            return getRandomQuestionType("add", "sub", "addSub", "mul", "div", "mathAnimals");
    }
}