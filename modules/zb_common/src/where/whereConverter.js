$global.$converters = $global.$converters || {};
var cnv = $global.$converters;

cnv.geographyConverter = function(parseTree) {
    var id = parseTree.Geography[0].value;
    return $Geography[id].value;
};

cnv.countryConverter = function(parseTree) {
    var id = parseTree.Countries[0].value;
    return $Countries[id].value;
};

cnv.country2CapitalConverter = function(parseTree) {
    return country2CapitalConverter(parseTree);
};

function country2CapitalConverter(parseTree){
    var id, country, countElements, city, capital, i;
    id = parseTree.Countries[0].value;
    country = $Countries[id].value;
    countElements = Object.keys($Geography).length;
    var capitalFound = false;
    for (i = 1; i <=countElements; i++) {
        capital = $Geography[i].value;
        if (capital.country == country.name) {
            capitalFound = true;
            break;
        }
    }
    if (capitalFound) {
        countElements = Object.keys($Cities).length;
        for (i = 1; i <=countElements; i++) {
            city = $Cities[i].value;
            if (city.name == capital.name) {
                return city;
            }
        }
        return {country: country.name, error: "unknown location"};
    } else {
        return {country: country.name, error: "unknown location"};
    }
}