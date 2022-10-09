function cloneArr(arr){
    var newArr = [];
    for (var inx = 0; inx<arr.length; ++inx) {
        newArr.push(arr[inx]);
    }
    return newArr;
}
function shuffleArr(arr) {
    if ($jsapi.context().testContext) {
        return arr
    }

    var y, x;
    for (var i = arr.length - 1; i > 0; i--) {
        y = Math.floor(Math.random() * (i + 1));
        x = arr[i];
        arr[i] = arr[y];
        arr[y] = x;
    }
    return arr;
}
function getQuestionIDs() {
    var $session = $jsapi.context().session;
    var IDs = Object.keys($QuizFootball);
    var shuffledIDs = shuffleArr(IDs);
    if ($session.currentQuestionInfo && $session.currentQuestionInfo.id) {
        var idxLastQuestion = shuffledIDs.indexOf($session.currentQuestionInfo.id);
        shuffledIDs.splice(idxLastQuestion, 1);
        shuffledIDs.push($session.currentQuestionInfo.id);
    }
    return shuffledIDs
}
function getQuestion() {
    var $session = $jsapi.context().session;
    var id;
    var ret = true;
    $session.questionsIDs = $session.questionsIDs || getQuestionIDs();

    if ($session.questionsIDs.length > 1) {
        id = $session.questionsIDs.splice(0, 1);
    } else if ($session.questionsIDs.length === 1) {
        id = $session.questionsIDs[0];
        $session.questionsIDs = false;
    } else {
        ret = false;
    }

    if (ret) {
        var value = $QuizFootball[id].value;
        var responseOptions = cloneArr(value.wrongAnswer);
        var answerIsNumber = value.answerIsNumber ? true : false;
        responseOptions.push(value.rightAnswer);
        responseOptionsForQuestion = shuffleArr(responseOptions);


        var rightAnswerNumber = responseOptionsForQuestion.indexOf(value.rightAnswer) + 1;
        for (var n = 0; n < responseOptionsForQuestion.length; ++n) {
            responseOptionsForQuestion[n] = String(n + 1) + ") " + responseOptionsForQuestion[n] + ";";
        }
        var responseOptionsForQuestion = responseOptionsForQuestion.join("\n");

        $session.currentQuestionInfo = {
            'id': id,
            'question': value.question,
            'responseOptions': responseOptions,
            'responseOptionsForQuestion': responseOptionsForQuestion,
            'rightAnswerNumber': rightAnswerNumber,
            'rightAnswer': value.rightAnswer,
            'answerText': value.answerText,
            'rightAnswerPatterns': value.rightAnswerPatterns,
            'wrongAnswerPatterns': value.wrongAnswerPatterns,
            'answerIsNumber' : answerIsNumber
        }
    }
    return ret
}
function checkAnswer(textToCheck, $session) {
    var rightAnswer = $session.currentQuestionInfo.rightAnswerPatterns;
    var wrongAnswer = $session.currentQuestionInfo.wrongAnswerPatterns;
    var isRight = $nlp.matchPatterns(textToCheck, rightAnswer);
    var isWrong = $nlp.matchPatterns(textToCheck, wrongAnswer);

    var ret = false;
    if (isRight && isWrong) {
        ret =  isRight.score > isWrong.score ? 'right' : 'wrong';
    } else if (isRight && isWrong == null){
        ret = 'right';
    } else if (isRight == null && isWrong){
        ret = 'wrong';
    }
    return ret
}
function correctAnswersAmount(correct, all) {
    var ru__answers = correct % 10 === 1 && correct % 100 !== 11 ? 'правильный ответ' : (correct % 10 >= 2 && correct % 10 <= 4 && (correct % 100 < 10 || correct % 100 >= 20) ? 'правильных ответа' : 'правильных ответов');
    var ret = correct + ' ' + ru__answers + ' из ' + all;
    return ret

}