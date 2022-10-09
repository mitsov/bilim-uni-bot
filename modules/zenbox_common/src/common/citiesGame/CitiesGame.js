function checkCity(city) {
    var firstLetter = capitalize(city.text.substring(0,1));
    var lastLetter = getCityLetter();
    return (firstLetter == lastLetter);
}

function compareScore(cityA, cityB) {
    return cityA.score - cityB.score;
}

function chooseCity(cities) {
    var city = '';
    var start = (cities.length > 3) ? selectRandomArg(0, 1, 2) : 0;
    for (var i = start; i < cities.length; i++) {
        if (isNewCity(cities[i].title)) {
            city = cities[i].title;
            break;  
        }
    }
    return city;
}

function isNewCity(title) {
    var $session = $jsapi.context().session;
    var res = true;
    if ($session.gameCities) {
        for (var i = 0; i < $session.gameCities.length; i++) {
            if ($session.gameCities[i] == title) {
                res = false;
                break;
            }
        }
    }
    return res;
}

function getCityLetter() {
    var $session = $jsapi.context().session;
    var letter = '';
    var letters;
    if ($session.gameCities && $session.gameCities[$session.gameCities.length - 1]) {
        letters = $session.gameCities[$session.gameCities.length - 1].substring($session.gameCities[$session.gameCities.length - 1].length - 2);
        letter = validateLetters(letters);
    }
    return letter;
}

function getGameProgress(){
    var $session = $jsapi.context().session;
    var r =  $session.gameCities.length;
    return r;
}

function validateLetters(letters) {
    var letter = letters.substring(1);
    switch(letter) {
        case "ё":
            letter = "е";
            break;          
        case "ы":
        case "ь":
            letter = letters.substring(0, 1);
            break;
    }
    return letter.toUpperCase();
}