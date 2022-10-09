function learnAnswer(memory, question, answer) {
    var $client = $jsapi.context().client;
    $client[memory] = $client[memory] || [];
    var newQuestion = true;
    for (var i=0; i<$client[memory].length; i++) {
        if ($client[memory][i][0] == question.toLowerCase()) {
            newQuestion = false;
            $client[memory][i][1] = answer;
        }
    }
    if (newQuestion) {
        $client[memory].push([question.toLowerCase(), answer]);
    }
 }

function isQuestionLearned(memory, question) {
    var $client = $jsapi.context().client;
    $client[memory] = $client[memory] || [];
    var original = question.toLowerCase();
    if (question.toLowerCase().substring(0,6) == 'емеля ') {
        var cleaned = question.toLowerCase().substring(6);
    }
    for (var i=0; i<$client[memory].length; i++) {
        if ($client[memory][i][0] == original || (typeof cleaned != 'undefined' && $client[memory][i][0] == cleaned)) {
            return true;
        }
    }
    return false;
}

function getAnswerLearned(memory, question) {
    var $client = $jsapi.context().client;
    $client[memory] = $client[memory] || [];
    var result = "";
    var original = question.toLowerCase();
    if (question.toLowerCase().substring(0,6) == 'емеля ') {
        var cleaned = question.toLowerCase().substring(6);
    }    
    for (var i=0; i<$client[memory].length; i++) {
        if ($client[memory][i][0] == original || (typeof cleaned != 'undefined' && $client[memory][i][0] == cleaned)) {
            result = $client[memory][i][1];
            break;
        }
    }
    return result;
}

function checkLearnedAnswers() {
    var $parseTree = $jsapi.context().parseTree;
    var $temp = $jsapi.context().temp;    
    if ($parseTree && $parseTree.text && isQuestionLearned("answersLearned", $parseTree.text)) {
        $temp.targetState =  "/LearnAnswer/SayAnswer";
    }
}

bind("preProcess", checkLearnedAnswers);