function getCity() {
    var $session = $jsapi.context().session;
    var $client = $jsapi.context().client;
    var city = '';
    var letter = getCityLetter();
    if (letter == '') {
        letter = selectRandomArg("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z");
    }
    var citiesNumber = Object.keys($Cities).length;
    var sortedCityNames = findCities(letter, citiesNumber);   
    if (sortedCityNames.length != 0) {
        city = chooseCity(sortedCityNames);
        if (!$session.gameCities) {
            $session.gameCities = [];
        }
        $session.gameCities.push(city);
    }
    var id = $client.sortedCitiesByLetter[letter].indexOf(city);
    $client.sortedCitiesByLetter[letter].slice(id, 1);
    return city;
}

function findCities(letter, citiesNumber) {
    var $client = $jsapi.context().client;
    var cities = [];
    var nextCity, score;

    if (!$client.sortedCitiesByLetter){
        $client.sortedCitiesByLetter = {};
    } 

    if (!(letter in $client.sortedCitiesByLetter)){
        for (var i = 1; i <= citiesNumber; i++) {
            nextCity = $Cities[i].value;
            if (nextCity.name.substr(0,1) == letter) {
                score = 1/(nextCity.population * (nextCity.capital == "true" && nextCity.continent != "Africa" ? 20 : 1) * (nextCity.continent == "Europe" && nextCity.country != "RU" && nextCity.country != "TR"? 20:1) );
                cities.push({title: nextCity.name, score: score });
            }
        }
        if (cities.length > 0) {
            cities.sort(compareScore);
        }
        $client.sortedCitiesByLetter[letter] = cities;
    }

    return $client.sortedCitiesByLetter[letter];
}

function my_capitalize(s){
    return s.toLowerCase().replace( /\b./g, function(a){ return a.toUpperCase(); } );
};

function alternateWriting(text, city){
   return ((text.indexOf(' ')==city.indexOf('-') && text.length == city.length) || 
   (text.indexOf('е')==city.indexOf('ё') && text.length == city.length) || 
   (capitalize(text)==capitalize(city.replace(/-/g, '')) && text.length == city.replace(/-/g, '').length));
}

function isCityCountry(text){
   var res = false;
   var citiesNumber = Object.keys($Cities).length;
   var city;
    if (citiesNumber) {
        for (var i = 1; i < citiesNumber; i++) {
            city = $Cities[i].value;
            if (city.name == text) {
                res = true;
                break;
            }
        }
    }
    return res; 
}

function getResultString(){
    var $session = $jsapi.context().session;
    $session.chain = 0;
    if ($session.gameCities) {
            $session.chain = $session.gameCities.length;
        }
    $session.ru__words = $session.chain % 10 === 1 && $session.chain % 100 !== 11 ? 'word' : ($session.chain % 10 >= 2 && $session.chain % 10 <= 4 && ($session.chain % 100 < 10 || $session.chain % 100 >= 20) ? 'words' : 'words')
    var result = $CitiesGameAnswers["getResultString"];
    return result;
}