function cantGetWeather() {
    return selectRandomArg("It seems the internet isn't working. I can't get any information about the weather.",
        "I do my best, but I don't have any data. Something must be wrong with the internet.",
        "I can't say. The data isn't available.",
        "The connection isn't working. I can't get any data on the weather.",
        "We must be thankful together whatever the weather. Especially since I can't get any weather data now.");
}

function convertTemperature(value) {
    var temp = Math.round(value);  
    var absTemp = Math.abs(temp);  
    if (temp > 0) {
        return '+' + temp + ' degrees';
    } else {
        return temp + ' degrees';
    }
}

function parseWeather(result, dateTime, weatherCondition) {
    var $request = $jsapi.context().request;
    var $temp = $jsapi.context().temp;
    var when;
    var offset = $request.offset ? $request.offset/3600000 : 4;
    var when = dayStart(toMoment(dateTime)).add((12-offset), 'hours')/1000;
    var res;
    for (var i = 0; i < result.length; i++) {
        res = result[i];    
        if (Math.abs(when - res.dt) < 43200) {
            var temp = convertTemperature(res.temp.max);
            if (typeof temp == 'undefined') {
                temp = 'temperature unknown';
            }
            var condition = res.weather[0].description;
            if (typeof condition == 'undefined') {
                condition = 'weather conditions unknown';
            }       
            if (weatherCondition) {              
                $temp.react = checkWeatherCondition(res, weatherCondition);
            }
            break;
        } 
    }
    if (!condition && !$temp.react) {
        return false
    } else {
        return condition.match(/ is /) ? temp + " and " + condition : temp + " with " + condition;
    }

}

function getWeatherCondition(eaResult) {
    return eaResult.weather[0].description ? convertTemperature(eaResult.main.temp) + " with " + eaResult.weather[0].description : convertTemperature(eaResult.main.temp);
}

function checkWeatherCondition(result, condition) {
    var currentCondition = checkCurrentWeatherCondition(result);
    return currentCondition == condition ? "Yes" : "No";
}


function tuneAnimation(result){
    var currentCondition = checkCurrentWeatherCondition(result);
    switch(currentCondition){
        case 1:
            return "ACTION_FINE";
        case 2:
            return "ACTION_RAIN";
        case 3:
        case 6:
            return "ACTION_CLOUD";    
        case 4:
            return "ACTION_SNOW";
        case 5:
            return "ACTION_WIND";            
    }
}    