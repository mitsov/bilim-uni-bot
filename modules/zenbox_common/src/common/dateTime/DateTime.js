var DateTime = (function(){

    function timezoneSearch(lat, lon){
        return $http.get("http://vip.timezonedb.com/v2.1/get-time-zone?key=${key}&format=json&by=position&lat=${lat}&lng=${lon}", {
            timeout: 10000,
            query:{
                key: "ZGKA3SJBWHDV",
                lat: lat,
                lon: lon
            }
        }).then(parseHttpResponse).catch(httpError);
    };

    return {
        timezoneSearch : timezoneSearch
    };

})();

function getSeason(dateTime) {
    var $request = $jsapi.context().request;
    var month = parseInt(moment(dateTime).format('M'));
    var lat;
    lat = $request.data.lat ? $request.data.lat : 60;

    if (lat >= 0) {
        if (month == 12 || month == 1 || month == 2) {
            return 0;
        } else if (month == 3 || month == 4 || month == 5) {
            return 1;
        } else if (month == 6 || month == 7 || month == 8) {
            return 2;
        } else {
            return 3;
        }
    } else {
        if (month == 12 || month == 1 || month == 2) {
            return 2;
        } else if (month == 3 || month == 4 || month == 5) {
            return 3
        } else if (month == 6 || month == 7 || month == 8) {
            return 0;
        } else {
            return 1;
        }
    }
}

function calculatorTimezone(offset1, offset2, time1) {
    var comparison = (offset2 - offset1);
    if (comparison >= 0) {
        return moment(time1).add(comparison, 'seconds');
    } else {
        return moment(time1).subtract(Math.abs(comparison), 'seconds');
    }
}

function toTimeAnswer(text, offset) {
    var $response = $jsapi.context().response;
    $reactions.answer(text);
    $response.action = "time";
    $response.time = Math.floor(new Date().getTime() / 1000);
    
    var offsetHours = Math.floor(Math.abs(offset)/60);
    var offsetMinutes = Math.abs(offset)%60;
    if (offsetMinutes == 0) {
        offsetMinutes = "";
    } else {
        if (offsetMinutes >= 10) {
            offsetMinutes = ":" + offsetMinutes;
        } else {
            offsetMinutes = ":0" + offsetMinutes;
        }
    }
    var sign = offset >= 0 ? "+" : "-";

    $response.timezone = "GMT" + sign + offsetHours + offsetMinutes;
}