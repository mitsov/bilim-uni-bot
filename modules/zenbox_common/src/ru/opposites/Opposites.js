function getOpposite() {
    var $session = $jsapi.context().session;
    var i, j, word;
    var words = [], candidates = [];

    prepareOpposite();

    $session.opposites.turn += 1;

    //выбираем слово, для которого есть хорошая противоположность и которое не было использовано в сессии
    for (i=0; i<$session.opposites.dictionary.length; i++) {
        candidates = $session.opposites.dictionary[i].best.split(",");
        for (j=0; j<candidates.length; j++) {
            if (candidates[j] !== null && candidates[j] !== "" && !_.contains($session.opposites.used, candidates[j])) {
                words.push(candidates[j]);
            }            
        }
        //если все слова использованы, выбираем из всех
        if (i==($session.opposites.dictionary.length-1) && words.length == 0) {
            $session.opposites.used = [];
            i = 0;
        }
    }
    $session.opposites.last = words[randomInteger(0, words.length-1)];
    $session.opposites.used.push($session.opposites.last);  
}

function checkOpposite(test){
    var $session = $jsapi.context().session;
    var candidates= test.best.split(",").concat(test.possible.split(","));
    return _.contains(candidates, $session.opposites.last); 
}

function findOpposite(word){
    var $session = $jsapi.context().session;
    var i, j;
    var candidates = [];

    for (i=0; i<$session.opposites.dictionary.length; i++) {
        candidates = $session.opposites.dictionary[i].best.split(",");
        for (j=0; j<candidates.length; j++) {
            if (candidates[j] == word) {
                return $session.opposites.dictionary[i].word;
            }            
        }
    }

    for (i=0; i<$session.opposites.dictionary.length; i++) {
        candidates = $session.opposites.dictionary[i].possible.split(",");
        for (j=0; j<candidates.length; j++) {
            if (candidates[j] == word) {
                return $session.opposites.dictionary[i].word;
            }            
        }
    }

    return "";
}

function prepareOpposite() {
    var $session = $jsapi.context().session;
    var tempDictionary = [];
    if ($session.opposites === undefined || Object.keys($session.opposites).length === 0) {
        var countElements = Object.keys($Opposites).length;
        for (var i=1; i <= countElements; i++) {
            tempDictionary.push($Opposites[i].value);
        }
        $session.opposites = {
            used: [],
            dictionary: tempDictionary,
            turn: 0,
            correct: 0,
            last: ""
        };
    }
}