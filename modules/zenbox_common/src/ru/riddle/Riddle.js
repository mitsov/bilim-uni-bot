function getRiddle(){
    var $session = $jsapi.context().session;
    $session.usedRiddles = $session.usedRiddles || {
        questions: [],
        id: null,        
        question: '',
        answer: ''
    };

    var i, question, randNumber, numberOfQuestions;
    var questions = [];
    numberOfQuestions = Object.keys($Riddles).length;
    for (i=1; i<=numberOfQuestions; i++) {
        question = $Riddles[i].value;
        if (!_.contains($session.usedRiddles.questions, i) && question.question != "") {
            questions.push([i, question]);
        }
    }
    if (questions.length == 0) {
        $session.usedRiddles = {
            questions: [],
            id: null,            
            question: '',
            answer: ''
        };
        getRiddle();
    } else {
        question = questions[randomIndex(questions)];
        $session.usedRiddles.questions.push(question[0]);
        $session.usedRiddles.id = question[0];        
        $session.usedRiddles.question = question[1].question; 
        $session.usedRiddles.answer = question[1].answer; 
    }     
}