var Weather = (function(){
    var apiKey = WeatherParams["apiKey"];

    function openWeatherMapCurrent(units, lang, lat, lon){
        return $http.get("http://api.openweathermap.org/data/2.5/weather?APPID=${APPID}&units=${units}&lang=${lang}&lat=${lat}&lon=${lon}", {
            timeout: 10000,
            query:{
                APPID: apiKey,
                units: units,
                lang: lang,
                lat: lat,
                lon: lon
            }
        }).then(parseHttpResponse).catch(httpError);
    };

    function openWeatherMapForecast(ctn, units, lang, lat, lon){
        return $http.get("http://api.openweathermap.org/data/2.5/forecast/daily?cnt=${ctn}&APPID=${APPID}&units=${units}&lang=${lang}&lat=${lat}&lon=${lon}", {
            timeout: 10000,
            query:{
                APPID: apiKey,
                ctn: ctn,
                units: units,
                lang: lang,
                lat: lat,
                lon: lon
            }
        }).then(parseHttpResponse).catch(httpError);
    };

    return {
        openWeatherMapCurrent : openWeatherMapCurrent,
        openWeatherMapForecast : openWeatherMapForecast
    };

})();

function check15daysDateTime(dateTime) {
    var today = dayStart(currentDate());
    var request = dayStart(toMoment(dateTime));
    var diff = request.diff(today, 'days');
    return (diff >= 0 && diff <= 15);
}

function checkCurrentWeatherCondition(result){
    var currentCondition = result.weather[0].id;
    if (currentCondition == 800) {
        currentCondition = 1; //ясно
    } else {
        currentCondition = Math.floor(currentCondition/100);
        switch(currentCondition) {
            case 2:
            case 3:
            case 5:
                currentCondition = 2; //дождь
                break;
            case 6:
                currentCondition = 4; //снег
                break;
            case 7:
                currentCondition = 6; //туман
                break;
            case 8:
                currentCondition = 3; //облачно
                break;
            default:
                currentCondition = 5; //ветер
        }
    }
    return currentCondition
}

function checkLocation() {
    var $request = $jsapi.context().request;
    var $client = $jsapi.context().client;
    if ($request && $request.data) {
        if ($request.data.lat && $request.data.lon) {
            $client.lat = $request.data.lat;
            $client.lon = $request.data.lon;
            return true;
        }
    }
    if ($client) {
        if ($client.lat && $client.lon) {
            return true;
        }
    } 
    return false;
}
