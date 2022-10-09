function askWhy(){
    var $session = $jsapi.context().session;
    $session.askedWhy = $session.askedWhy || {
        types: [],
        questions: [],
        question: '',
        answer: ''
    };

    var i, question, randNumber, numberOfQuestions;
    var questions = [];
    numberOfQuestions = Object.keys($WhyQuestions).length
    for (i=1; i<=numberOfQuestions; i++) {
        question = $WhyQuestions[i].value;
        if (!_.contains($session.askedWhy.types, question.type)) {
            questions.push([i, question]);
        }
    }
    if (questions.length == 0) {
        randNumber = randomInteger(1,numberOfQuestions);
        question = [randNumber, $WhyQuestions[randNumber].value];
    } else {
        question = questions[randomInteger(0,questions.length-1)];
    }
    $session.askedWhy.questions.push(question[0]);        
    $session.askedWhy.types.push(question[1].type);  
    $session.askedWhy.question = question[1].question; 
    $session.askedWhy.answer = question[1].answer;      
}