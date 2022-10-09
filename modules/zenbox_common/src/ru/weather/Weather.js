function cantGetWeather() {
    return selectRandomArg("Что-то земной Интернет барахлит. Не могу ничего сказать о погоде.",
        "Напрягаюсь изо всех сил, но не могу получить данных о погоде. Наверное, что-то с Интернетом.",
        "Не могу сказать. Данные о земной погоде мне сейчас почему-то недоступны.",
        "Тут какие-то помехи, не могу получить данные о погоде на Земле.",
        "Как тут говорят, главное - погода в доме, а к остальной информации у меня сейчас почему-то нет доступа.");
}

function convertTemperature(value) {
    var temp = Math.round(value);  
    var absTemp = Math.abs(temp);  
    var ru__grads = absTemp % 10 === 1 && absTemp % 100 !== 11 ? 'градус' : (absTemp % 10 >= 2 && absTemp % 10 <= 4 && (absTemp % 100 < 10 || absTemp % 100 >= 20) ? 'градуса' : 'градусов');
    if (temp > 0) {
        return '+' + temp + ' ' + ru__grads;
    } else {
        return temp + ' ' + ru__grads;
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
                temp = 'неизвестная температура';
            }
            var condition = res.weather[0].description;
            if (typeof condition == 'undefined') {
                condition = 'неизвестные погодные условия';
            }       
            if (weatherCondition) {              
                $temp.react = checkWeatherCondition(res, weatherCondition);
            }
            break;
        }
    }
    return checkEnLetters(condition) ? temp : (condition + ", " + temp);
}

function getWeatherCondition(eaResult) {
    return checkEnLetters(eaResult.weather[0].description) ? convertTemperature(eaResult.main.temp) : (eaResult.weather[0].description + ", " + convertTemperature(eaResult.main.temp));
}

function checkWeatherCondition(result, condition) {
    var currentCondition = checkCurrentWeatherCondition(result);
    return currentCondition == condition ? "Да" : "Нет";
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