require: ../../common/common.sc

require: ../../common/weather/Weather.js
require: Weather.js

require: where/where.sc
  module = zb_common
require: dateTime/dateTime.sc
  module = zb_common

require: ../main.js

require: params.yaml
   var = WeatherParams

patterns:
    $WeatherCondition = (
        (ясно|ясная|ясный|ясное|солн*|сух*): 1 |
        (дожд*|сыр*|мокр*|гроз*|~град|~изморось): 2 |
        (облак*|облач*|пасмурн*): 3 |
        (снеж*|снег*|скольз*|гололед*): 4 |
        (ветр*|ветер*|~буря|ураган*|~торнадо|смерч*|шторм*): 5 |
        (туман*): 6 
        ) || converter = $converters.numberConverterValue

    $TemperatureCondition = (
        (жар*|тепл*|пекл*): 1 |
        (хол*|прохл*|мороз*): 2
        ) || converter = $converters.numberConverterValue

    $TimePeriod = (с ($DateTime|$NumberSimple) по ($DateTime|$NumberSimple)|[в] $Weekend)
    $WeatherForecast = [с] [прогноз*] (погод*|прогноз*|метеопрогноз*|метео прогноз*|температур* [воздух*]|сколько [сейчас/щас] градусов|гисметео)

theme: /Weather

    state: Hint

    state: Ask current location for Current weather
        a: А в каком мы сейчас городе? || question = true

    state: Ask current location for Weather on a selected date 
        a: А в каком мы сейчас городе? || question = true

    state: Ask current location for Weather check today
        a: А в каком мы сейчас городе? || question = true  

    state: Ask current location for Weather check other day 
        a: А в каком мы сейчас городе? || question = true

    state: Current weather
        q!: * {[подска*|скажи*|покажи*|расска*|дай*] [~какой|как|что] [будет] * (на сегодня|сегодня|сейчас) $WeatherForecast}
        q!: * {[подска*|скажи*|покажи*|расска*|дай*|уточни*] (~какой|как|что) * $WeatherForecast}
        q!: * {(подска*|скажи*|покажи*|расска*|дай*|уточни*) [~какой|как|что] * $WeatherForecast} 
        q!: * {(на сегодня|сегодня|сейчас) прогноз погоды}     
        q!: температура         
        q: * [а] (сегодня|сейчас) || fromState  = "../Weather on a selected date"
        q: * [а] (дома|у нас|здесь) || fromState  = "../Weather check in location today"
        q: * $Where * || fromState  = "../Ask current location for Current weather"
        q: (ну/давай/сообщи) || fromState  = ../Hint
        script:
            if ($parseTree.Where) {
                $client.lat = $parseTree._Where.lat;
                $client.lon = $parseTree._Where.lon;
                $session.Where = $parseTree._Where;
            }
            $temp.isLocated = checkLocation();
        if: $temp.isLocated
            script:
                Weather.openWeatherMapCurrent("metric", "ru", $client.lat, $client.lon).then(function (res){
                    if (res.weather){
                        $response.action = tuneAnimation(res);
                        $reactions.answer("{Сегодня/Погода сегодня такая -} " + getWeatherCondition(res));
                    } else {
                        $reactions.answer(cantGetWeather());
                    }
                }).catch(function (err) {
                    $reactions.answer(cantGetWeather());
                });
        else:
            go!: ../Ask current location for Current weather

    state: Current weather in a location 
        q!: * {(~какой|как|что) [на|будет] (сегодня|сейчас|текущ*) $WeatherForecast [в|для] $Where} *
        q!: * {(~какой|как|что) [на|будет] (сегодня|сейчас|текущ*) $WeatherForecast [в|для] $Where} *
        q!: * {[подска*|скажи*|покажи*|расска*|дай*] (сегодня|сейчас|текущ*) $WeatherForecast [в|для] $Where} *
        q!: * {[на|будет] (сегодня|сейчас|текущ*) $WeatherForecast [в|для] $Where} *
        q!: * {[подска*|скажи*|покажи*|расска*|дай*] [~какой|как|что] [будет] $WeatherForecast} * [в|для] $Where *
        q!: * [подска*|скажи*|покажи*|расска*|дай*] * [~какой|как|что] [будет] [в|для] $Where $WeatherForecast *
        q!: * [~какой|как|что] [будет] $WeatherForecast * [в|для] $Where * [подска*|скажи*|покажи*|расска*] *
        q!: * [в] $Where [~какой|как|что] [на] [сейчас|текущ*] * [с] (погод*|метеопрогноз*|прогноз*)
        q: * [а] [в] $Where || fromState  = "../Current weather"
        q: * [а] [в] $Where || fromState  = "../Current weather in a location"         
        script:
            if ($parseTree.Where){
                $session.Where = $parseTree._Where;
            }
            Weather.openWeatherMapCurrent("metric", "ru", $session.Where.lat, $session.Where.lon).then(function (res){
                if (res && res.weather){
                    $response.action = tuneAnimation(res);
                    $reactions.answer("Сегодня в городе " + $session.Where.name + " " + getWeatherCondition(res));
                } else {
                    $reactions.answer(cantGetWeather());
                }
            }).catch(function (err) {
                $reactions.answer(cantGetWeather());
            });        

    state: Weather on a selected date 
        q!: * $DateTime * {[подска*|скажи*|покажи*|расска*|дай*] [~какой|как|что] * $WeatherForecast [будет] [на]} *
        q!: * [подска*|скажи*|покажи*|расска*|дай*] [~какой|как|что] * $WeatherForecast * {$DateTime [будет] [на]} *       
        q!: * [прогноз] (погод*|прогноз*) * [на] * $DateTime *
        q: * $DateTime * || fromState  = "../Weather on a selected date"
        q: * $DateTime * || fromState  = "../Weather on a datetime period in a location"
        q: * $DateTime * || fromState  = "../Current weather"
        q: * [а] * (здесь|дома|у нас) || fromState  = "../Weather on a selected date in a location"
        q: * $Where * || fromState  = "../Ask current location for Weather on a selected date"
        script:
            if ($parseTree.DateTime) {
                $session.when = $parseTree.DateTime[0];
            }         
            if ($parseTree.Where) {
                $client.lat = $parseTree._Where.lat;
                $client.lon = $parseTree._Where.lon;
                $session.Where = $parseTree._Where;
            }
            $temp.isLocated = checkLocation();
        if: $temp.isLocated 
            if: (toMoment($session.when).format('l') == currentDate().format('l'))
                go!: ../Current weather
            else:
                if: check15daysDateTime($session.when)
                    script:
                        Weather.openWeatherMapForecast(16, "metric", "ru", $client.lat, $client.lon).then(function (res){
                            if (res){
                                if (res.weather){
                                    $response.action = tuneAnimation(eaResult);
                                }
                                var weather = parseWeather(res.list, $session.when);
                                $reactions.answer(toMoment($session.when).locale('ru').format('Do MMMM') + " " + weather);
                            } else {
                                $reactions.answer(cantGetWeather());
                            }
                        }).catch(function (err) {
                            $reactions.answer(cantGetWeather());
                        });
                else:
                    a: Я, конечно, много чего умею, но земную погоду знаю только на 15 дней вперёд.
        else:
            go!: ../Ask current location for Weather on a selected date 
    
    state: Weather on a selected date in a location 
        q!: * [подска*|скажи*|покажи*|расска*|дай*] [~какой|как|что] * $DateTime * {$WeatherForecast [будет] [на] * [в|для] $Where} *
        q!: * [подска*|скажи*|покажи*|расска*|дай*] [~какой|как|что] * $WeatherForecast * {$DateTime * [будет] [на] * [в|для] $Where} *  
        q!: * [подска*|скажи*|покажи*|расска*|дай*] [~какой|как|что] * [в|для] $Where * {$DateTime * $WeatherForecast [будет] [на]} *                  
        q!: * [в|для] $Where * $DateTime  [~какой|как|что] [будет] * $WeatherForecast *
        q!: * $DateTime [в|для] $Where [~какой|как|что] [будет] * $WeatherForecast *
        q!: * [прогноз*|~какой|как] (погод*|прогноз*) * { [в|для] $Where * $DateTime} *
        q: * [а] [в|для] $Where || fromState  = "../Weather on a selected date"
        q: * [а] [в|для] $Where || fromState  = "../Weather on a selected date in a location"
        q: * [а] $DateTime * || fromState  = "../Current weather in a location"
        q: * [а] $DateTime * || fromState  = "../Weather on a selected date in a location"
        q: * [а] $DateTime * || fromState  = "../Weather on a datetime period in a location"
        q: * {[a] $DateTime * $Where } || fromState  = "../Weather on a selected date in a location"
        q: * {[a] $DateTime * $Where } || fromState  = "../Weather on a selected date"
        q: * {[a] $DateTime * $Where } || fromState  = "../Current weather in a location"
        q: * {[a] $DateTime * $Where } || fromState  = "../Current weather"
        q: * {[a] $DateTime * $WeatherCondition } * $weight<+0.5>  || fromState  = "../Current weather in a location"
        script:
            if ($parseTree.Where) {
                $session.Where = $parseTree._Where;
            }
            if ($parseTree.DateTime) {
                $session.when = $parseTree.DateTime[0];
            }     
        if: (toMoment($session.when).format('l') == currentDate().format('l'))
            go!: ../Current weather in a location
        else:
            if: check15daysDateTime($session.when)
                script:
                    Weather.openWeatherMapForecast(16, "metric", "ru", $session.Where.lat, $session.Where.lon).then(function (res){
                        if (res){
                            if (res.weather){
                                $response.action = tuneAnimation(res);
                            }
                            var weather = parseWeather(res.list, $session.when, null);
                            $reactions.answer(toMoment($session.when).locale('ru').format('Do MMMM') + " в городе " + $session.Where.name + " " + weather);
                        } else {
                            $reactions.answer(cantGetWeather());
                        }
                    }).catch(function (err) {
                        $reactions.answer(cantGetWeather());
                    });       
            else:       
                a: Я, конечно, много чего умею, но земную погоду знаю только на 15 дней вперёд.
            

    state: Weather on a datetime period in a location 
        q!: * {[[~какой|как|что] [будет]] * [[в|для] [$Where]] * $TimePeriod * $WeatherForecast} *
        q!: * $WeatherForecast * [в|для] [$Where] * $TimePeriod *
        q: * [а] [в|для] $Where || fromState  = "../Weather on a datetime period in a location"
        q: * [а] $TimePeriod * || fromState  = "../Current weather in a location"
        q: * [а] $TimePeriod * || fromState  = "../Weather on a selected date in a location"
        q: * {[a] $TimePeriod * $Where} || fromState  = "../Weather on a selected date in a location"
        q: * {[a] $TimePeriod * $Where} || fromState  = "../Weather on a selected date"
        q: * {[a] $TimePeriod * $Where} || fromState  = "../Current weather in a location"
        q: * {[a] $TimePeriod * $Where} || fromState  = "../Current weather"
        a: Я, конечно, много чего умею, но земную погоду знаю только на конкретный день.
        
       
    state: Weather check today 
        q!: * {(сейчас|днем|вечером|сегодня|будет|пойдет|ожида*) ($WeatherCondition|$TemperatureCondition) * [погода]}      
        q!: * ($WeatherCondition|$TemperatureCondition) (сейчас|днем|вечером|сегодня|будет|пойдет|ожида*)
        q: * [a] $WeatherCondition || fromState  = "../Weather check today"
        q: * $Where * || fromState  = "../Ask current location for Weather check today"
        q: * {[a] * (сегодня|сейчас|текущ*) * $WeatherCondition} * $weight<+0.9> || fromState  = "../Weather check in location other day"  
        if: $parseTree.TemperatureCondition
            go!: ../Current weather
        else:
            script:
                if ($parseTree.WeatherCondition) {
                    $session.weatherCondition = $parseTree._WeatherCondition;
                }
                if ($parseTree.DateTime) {
                    $session.when = $parseTree.DateTime[0];
                }         
                if ($parseTree.Where) {
                    $client.lat = $parseTree._Where.lat;
                    $client.lon = $parseTree._Where.lon;
                    $session.Where = $parseTree._Where;
                }
                $temp.isLocated = checkLocation();
            if: $temp.isLocated  
                script:
                    Weather.openWeatherMapCurrent("metric", "ru", $client.lat, $client.lon).then(function (res){
                        if (res && res.weather){
                            $response.action = tuneAnimation(res);
                            var react = checkWeatherCondition(res, $session.weatherCondition);
                            $reactions.answer(react + ", сегодня " + getWeatherCondition(res));
                        } else {
                            $reactions.answer(cantGetWeather());
                        }
                    }).catch(function (err) {
                        $reactions.answer(cantGetWeather());
                    });  
            else:
                go!: ../Ask current location for Weather check today
   
    state: Weather check other day 
        q!: * [[буд*|будет|ожида*|пойдет|пойдут] [ли]] * {$DateTime * ($WeatherCondition|$TemperatureCondition) } * [погода]   
        q!: * $DateTime [(будет|пойдет|ожида*) [ли]] * ($WeatherCondition|$TemperatureCondition) * [погода] 
        q!: * погода * $DateTime *  ($WeatherCondition|$TemperatureCondition) *
        q: * [а] $DateTime * || fromState  = "../Weather check today"
        q: * [а] $DateTime * || fromState  = "../Weather check other day"      
        q: * [а] (дом*|здесь|в (моем|нашем|этом) городе) || fromState  = "../Weather check in location other day"
        q: * [a] $WeatherCondition || fromState  = "../Weather check other day"
        q: * $Where * || fromState  = "../Ask current location for Weather check other day"
        script:
            if ($parseTree.DateTime) {
                $session.when = $parseTree.DateTime[0];
            }
        if: (toMoment($session.when).format('l') == currentDate().format('l'))
            go!: ../Weather check today
        else:
            if: $parseTree.TemperatureCondition
                go!: ../Weather on a selected date 
            else:
                script:
                    if ($parseTree.WeatherCondition) {
                        $session.weatherCondition = $parseTree._WeatherCondition;
                    }              
                    if ($parseTree.Where) {
                        $client.lat = $parseTree._Where.lat;
                        $client.lon = $parseTree._Where.lon;
                        $session.Where = $parseTree._Where;
                    }
                    $temp.isLocated = checkLocation();
                if: $temp.isLocated          
                    if: check15daysDateTime($session.when)
                        script:
                            Weather.openWeatherMapForecast(16, "metric", "ru", $client.lat, $client.lon).then(function (res){
                                if (res){
                                    if (res.weather){
                                        $response.action = tuneAnimation(res);
                                    }    
                                    var weather = parseWeather(res.list, $session.when, $session.weatherCondition);
                                    $reactions.answer($temp.react + ", " + toMoment($session.when).locale('ru').format('Do MMMM') + " " + weather);
                                } else {
                                    $reactions.answer(cantGetWeather());
                                }
                            }).catch(function (err) {
                                $reactions.answer(cantGetWeather());
                            });
                    else:
                        a: Я, конечно, много чего умею, но земную погоду знаю только на 15 дней вперёд. 
                else:
                    go!: ../Ask current location for Weather check other day     

    state: Weather check in location today
        q!: * [что] * {(сейчас|днем|вечером|сегодня) [(будет|пойдет|ожида*) [ли]] ($WeatherCondition|$TemperatureCondition) [погода] * $Where}    
        q!: * {[(будет|пойдет|ожида*) [ли]] ($WeatherCondition|$TemperatureCondition) * $Where * [погода]}
        q: * [а] [в] $Where || fromState  = "../Weather check today"
        q: * ([а] сегодня [в] $Where| [a] [в] [$Where] сегодня) || fromState  = "../Weather check in location other day"
        q: * [а] $WeatherCondition || fromState = "../Weather check in location today" 
        if: $parseTree.TemperatureCondition
            go!: ../Current weather in a location
        else:
            script:
                if ($parseTree.Where) {
                    $session.Where = $parseTree._Where;
                }
                if ($parseTree.WeatherCondition) {
                    $session.weatherCondition = $parseTree._WeatherCondition;
                } 
                Weather.openWeatherMapCurrent("metric", "ru", $session.Where.lat, $session.Where.lon).then(function (res){
                    if (res){
                        $response.action = tuneAnimation(res);
                        var react = checkWeatherCondition(res, $session.weatherCondition);
                        $reactions.answer(react + ", сегодня в городе " + $session.Where.name + " " + getWeatherCondition(res));  
                    } else {
                        $reactions.answer(cantGetWeather());
                    }
                }).catch(function (err) {
                    $reactions.answer(cantGetWeather());
                });               

    state: Weather check in location other day
        q!: * [в] $Where  [(будет|пойдет|ожида*) [ли]] ($WeatherCondition|$TemperatureCondition) * [погода] $DateTime * 
        q!: * $DateTime [в] $Where * ($WeatherCondition|$TemperatureCondition) * [погода] *
        q!: * ($WeatherCondition|$TemperatureCondition) [погода] * $DateTime * [в] $Where 
        q!: * $Where $DateTime [(будет|пойдет|ожида*) [ли]] ($WeatherCondition|$TemperatureCondition) *
        q!: * {$Where $DateTime ($WeatherCondition|$TemperatureCondition) *} *
        q: * ([а] [в] $Where|[а] [в] $DateTime|[а] $DateTime [в] $Where|[а] [в] $Where $DateTime|[а] $WeatherCondition) || fromState  = "../Weather check in location other day"
        q: * ([а] [в] $Where|[а] $DateTime [в] $Where|[а] [в] $Where $DateTime|[а] $WeatherCondition) || fromState  = "../Weather check other day"
        q: * ([а] [в] $Where|[а] [в] $DateTime|[а] $DateTime [в] $Where|[а] [в] $Where $DateTime|[а] $WeatherCondition) || fromState  = "../Weather check in location today"
        q: * {[a] * $DateTime * $WeatherCondition} * $weight<+0.5> || fromState  = "../Weather on a selected date in a location"  
        script:
            if ($parseTree.DateTime) {
                $session.when = $parseTree.DateTime[0];
            }     
        if: (toMoment($session.when).format('l') == currentDate().format('l'))
            go!: ../Weather check in location today 
        else:
            if: $parseTree.TemperatureCondition
                go!: ../Weather on a selected date in a location
            else:
                script:
                    if ($parseTree.Where) {
                        $session.Where = $parseTree._Where;
                    }
                    if ($parseTree.WeatherCondition) {
                        $session.weatherCondition = $parseTree._WeatherCondition;
                    }                 
                if: check15daysDateTime($session.when)
                    script:
                        Weather.openWeatherMapForecast(16, "metric", "ru", $session.Where.lat, $session.Where.lon).then(function (res){
                            if (res){
                                if (res.weather){
                                    $response.action = tuneAnimation(res);
                                }
                                var weather = parseWeather(res.list, $session.when, $session.weatherCondition);
                                $reactions.answer($temp.react + ", " + toMoment($session.when).locale('ru').format('Do MMMM') + " в городе " + $session.Where.name + " " + weather);    
                            } else {
                                $reactions.answer(cantGetWeather());
                            }
                        }).catch(function (err) {
                            $reactions.answer(cantGetWeather());
                        });   
                else:
                    a: Я, конечно, много чего умею, но земную погоду знаю только на 15 дней вперёд.            
                
    state: Exact city
        q: * [в|для] (~какой|как) * (~город|~населенный ~пункт) *
        q: [это] где
        if: $session.Where
            a: {{$session.Where.name}}
        else:
            a: Что-то я забыл, как назывался город...

    state: Water temperature
        q!: * какая * температура воды *
        random:
            a: Я не знаю температуру воды, только температуру воздуха.
            a: Я знаю только температуру воздуха.
            
    state: catchAll local || noContext=true
        q: * ясно * $weight<+0.5>
        random:
            a: В смысле?
            a: То есть?
            a: Продолжай!
            a: Что ты имеешь в виду?
            a: Интересно!
            a: Что тут скажешь...
            a: Есть над чем подумать.
            a: Отличный разговор!
            a: Нечего добавить.
            a: Разумеется.
            a: Такого мне ещё не говорили.
            a: Само собой.