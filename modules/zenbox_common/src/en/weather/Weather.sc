require: ../../common/common.sc

require: ../../common/weather/Weather.js
require: Weather.js

require: where/whereEn.sc
  module = zb_common
require: dateTime/dateTimeEn.sc
  module = zb_common

require: ../main.js

require: params.yaml
   var = WeatherParams

patterns:
    $WeatherCondition = (
        (dry*|sun*|fair|clear): 1 |
        (rain*|wet*|slush*|storm*): 2 |
        (cloud*|overcast|gloom*): 3 |
        (snow*|blizzard*): 4 |
        (wind*|breez*|gale): 5 |
        (mist*|fog|smok*): 6 
        ) || converter = $converters.numberConverterValue

    $TemperatureCondition = (
        (hot*|warm|heat*): 1 |
        (cold|cool|chilly): 2
        ) || converter = $converters.numberConverterValue

    $WeekendWeather = (this | on | (for/over/on) the)  (weekend*/holiday*)

    $TimePeriod = ((from/since) ($DateTime|$NumberSimple) (to/till) ($DateTime|$NumberSimple)| $WeekendWeather)
    $WeatherForecast = ((weather/meteorological) (forecast*/report*) | ([air] temperature*/temperature/temperature [of] [the] air) | [the] weather | how many degrees)

theme: /Weather

    state: Hint

    state: Ask current location for Current weather
        a: What city are we in? || question = true

    state: Ask current location for Weather on a selected date 
        a: What city are we in? || question = true

    state: Ask current location for Weather check today
        a: What city are we in? || question = true  

    state: Ask current location for Weather check other day 
        a: What city are we in? || question = true

    state: Current weather
        q!: * [do] you know $WeatherForecast *
        q!: * (how/what) ('s/is) [[the] current] $WeatherForecast [today/now] [like] *
        q!: * [tell|what*] [me] [about] * (weather [forecast] |forecast|temperature) *
        q!: * is it (nice|fine) [weather] outside *
        q!: $WeatherForecast
        q: * [and] * [today|now]|| fromState = "../Weather on a selected date"
        q: * [at/in/this/that] (home|here|town|city) || fromState = "../Weather check in location today"
        q: * $Where * || fromState = "../Ask current location for Current weather"
        q: (ok/go on/tell/inform/say/let me know) || fromState = "../Hint"
        script:
            if ($parseTree.Where) {
                $client.lat = $parseTree._Where.lat;
                $client.lon = $parseTree._Where.lon;
                $session.Where = $parseTree._Where;
            }
            $temp.isLocated = checkLocation();
        if: $temp.isLocated
            script:
                Weather.openWeatherMapCurrent("imperial", "en", $client.lat, $client.lon).then(function (res){
                    if (res.weather){
                        $response.action = tuneAnimation(res);
                        $reactions.answer("It's " + getWeatherCondition(res) + ".");
                    } else {
                        $reactions.answer(cantGetWeather());
                    }
                }).catch(function (err) {
                    $reactions.answer(cantGetWeather());
                });
        else:
            go!: ../Ask current location for Current weather

    state: Current weather in a location 
        q!: * (how ('s/is) | what ['s/is]) [[the] current] $WeatherForecast [today/now] [is like] in $Where [today/now] *
        q!: * $WeatherForecast [in] $Where *
        q!: * $Where $WeatherForecast *
        q!: * [tell|what*] * [current]  (weather [forecast] |forecast|temperature) * [in|at|for] $Where *
        q!: * [in|at|for] $Where [current]  (weather [forecast] |forecast|temperature) *
        q: * [and] [what about] $Where || fromState = "../Current weather"
        q: * [and] [what about] $Where || fromState = "../Current weather in a location"
        q: * [and] [what about] (now|today|at * moment|at present|currently) || fromState = "../Weather on a selected date in a location"
        script:
            if ($parseTree.Where){
                $session.Where = $parseTree._Where;
            }
            Weather.openWeatherMapCurrent("imperial", "en", $session.Where.lat, $session.Where.lon).then(function (res){
                if (res && res.weather){
                    $response.action = tuneAnimation(res);
                    $reactions.answer("In " + $session.Where.name + " it's " + getWeatherCondition(res) + ".");
                } else {
                    $reactions.answer(cantGetWeather());
                }
            }).catch(function (err) {
                $reactions.answer(cantGetWeather());
            });        

    state: Weather on a selected date 
        q!: * {[tell|what*] * $DateTime * $WeatherForecast} *
        q: * $DateTime * || fromState = "../Weather on a selected date"
        q: * $DateTime * || fromState = "../Weather on a datetime period in a location"
        q: * $DateTime * || fromState = "../Current weather"
        q: * [and] [what about] (here|this place| my (city|town|location| place)) || fromState = "../Weather on a selected date in a location"
        q: * $Where * || fromState = "../Ask current location for Weather on a selected date"
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
                        Weather.openWeatherMapForecast(16, "imperial", "en", $client.lat, $client.lon).then(function (res){
                            if (res){
                                if (res.weather){
                                    $response.action = tuneAnimation(eaResult);
                                }
                                var weather = parseWeather(res.list, $session.when);
                                if (weather === false) {
                                    $reactions.answer(cantGetWeather());
                                } else {
                                    $reactions.answer(toMoment($session.when).locale('en').format('Do MMMM') + " " + weather + ".");
                                }
                            } else {
                                $reactions.answer(cantGetWeather());
                            }
                        }).catch(function (err) {
                            $reactions.answer(cantGetWeather());
                        });
                else:
                    a: Of course I am a very intelligent robot, but as for the weather on Earth, I know it only for the next 15 days.
        else:
            go!: ../Ask current location for Weather on a selected date 
    
    state: Weather on a selected date in a location
        q!: *  [tell|what*]  * $WeatherForecast * [in|at|for] * $Where * [on|for] * $DateTime * 
        q!: *  [tell|what*]  * $WeatherForecast [on|for] * $DateTime * [in|at|for] $Where *
        q!: *  [tell|what*]  * $Where $WeatherForecast [on|for] * $DateTime *
        q!: *  { $Where * $DateTime} * $WeatherForecast *
        q: * [and] [what about] * $Where || fromState = "../Weather on a selected date"
        q: * [and] [what about] * $Where || fromState = "../Weather on a selected date in a location"
        q: * [and] [what about] * $DateTime * || fromState = "../Current weather in a location"
        q: * [and] [what about] * $DateTime * || fromState = "../Weather on a selected date in a location"
        q: * [and] [what about] * $DateTime * || fromState = "../Weather on a datetime period in a location"
        q: * {$DateTime * $Where } || fromState = "../Weather on a selected date in a location"
        q: * {$DateTime * $Where } || fromState = "../Weather on a selected date"
        q: * {$DateTime * $Where } || fromState = "../Current weather in a location"
        q: * {$DateTime * $Where } || fromState = "../Current weather"
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
                    Weather.openWeatherMapForecast(16, "imperial", "en", $session.Where.lat, $session.Where.lon).then(function (res){
                        if (res){
                            if (res.weather){
                                $response.action = tuneAnimation(res);
                            }
                            var weather = parseWeather(res.list, $session.when, null);
                            if (weather === false) {
                                $reactions.answer(cantGetWeather());
                            } else {
                                $reactions.answer("On " + toMoment($session.when).locale('en').format('Do MMMM') + " in " + $session.Where.name + " it's " + weather + ".");
                            }
                        } else {
                            $reactions.answer(cantGetWeather());
                        }
                    }).catch(function (err) {
                        $reactions.answer(cantGetWeather());
                    });       
            else:       
                a: Of course I am a very intelligent robot, but as for the weather on Earth, I know it only for the next 15 days.
            

    state: Weather on a datetime period in a location 
        q!: * {[$Where] * $TimePeriod * $WeatherForecast} *
        q: * [and] [what about] * $Where || fromState = "../Weather on a datetime period in a location"
        q: * [and] [what about] * $TimePeriod * || fromState = "../Current weather in a location"
        q: * [and] [what about] * $TimePeriod * || fromState = "../Weather on a selected date in a location"
        q: * [and] [what about] * {$TimePeriod * $Where} || fromState = "../Weather on a selected date in a location"
        q: * [and] [what about] * {$TimePeriod * $Where} || fromState = "../Weather on a selected date"
        q: * [and] [what about] * {$TimePeriod * $Where} || fromState = "../Current weather in a location"
        q: * [and] [what about] * {$TimePeriod * $Where} || fromState = "../Current weather"
        a: Of course I am a very intelligent robot, but as for the weather on Earth, I know it only on a particular day.
        
       
    state: Weather check today
        q!: * (is it|isn't it|it's|is * weather|any|will it) * [going] [to] [be] * ($WeatherCondition|$TemperatureCondition) * [$WeatherForecast] [now|this day] * 
        q: * [and] [what about] * (today|now) * || fromState = "../Weather check other day"
        q: * [and] [what about] $WeatherCondition || fromState = "../Weather check today"
        q: * [and] [what about] (here|this place| my (city|town|location| place)) || fromState = "../Weather check in location today"
        q!: * {(now|day time|noon|afternoon|evening|today|this day|morning|night) ($WeatherCondition|$TemperatureCondition) * [$WeatherForecast]}      
        q!: * ($WeatherCondition|$TemperatureCondition) (now|day time|noon|afternoon|evening|today|this day|morning|night)
        q: * $Where * || fromState = "../Ask current location for Weather check today"
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
                    Weather.openWeatherMapCurrent("imperial", "en", $client.lat, $client.lon).then(function (res){
                        if (res && res.weather){
                            $response.action = tuneAnimation(res);
                            var react = checkWeatherCondition(res, $session.weatherCondition);
                            $reactions.answer(react + ", it's " + getWeatherCondition(res) + ".");
                        } else {
                            $reactions.answer(cantGetWeather());
                        }
                    }).catch(function (err) {
                        $reactions.answer(cantGetWeather());
                    });  
            else:
                go!: ../Ask current location for Weather check today
   
    state: Weather check other day 
        q!: * (is it|isn't it|it's|is * weather|will it) * [going] [to] [be] * ($WeatherCondition|$TemperatureCondition) * [$WeatherForecast] $DateTime * 
        q!: *  (is it|isn't it|it's|(is|will) * $WeatherForecast|any) * [going] [to] [be]  * ($WeatherCondition|$TemperatureCondition) [$WeatherForecast]  * [on] *  $DateTime *   
        q!: * $DateTime  * ($WeatherCondition|$TemperatureCondition) * [$WeatherForecast] * 
        q!: * [$WeatherForecast] ($WeatherCondition|$TemperatureCondition) [$WeatherForecast] * $DateTime *
        q!: * {$DateTime * ($WeatherCondition|$TemperatureCondition) } * [$WeatherForecast]   
        q!: * $DateTime * ($WeatherCondition|$TemperatureCondition) * [$WeatherForecast] 
        q!: * $WeatherForecast * $DateTime *  ($WeatherCondition|$TemperatureCondition) *
        q: * [а] $DateTime * || fromState = "../Weather check today"
        q: * [а] $DateTime * || fromState = "../Weather check other day"      
        q: * [at/in] [this/that/my]  (home|here|town|city) || fromState = "../Weather check in location other day"
        q: * $WeatherCondition || fromState = "../Weather check other day"
        q: * $Where * || fromState = "../Ask current location for Weather check other day"
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
                            Weather.openWeatherMapForecast(16, "imperial", "en", $client.lat, $client.lon).then(function (res){
                                if (res){
                                    if (res.weather){
                                        $response.action = tuneAnimation(res);
                                    }    
                                    var weather = parseWeather(res.list, $session.when, $session.weatherCondition);
                                    if (weather === false) {
                                        $reactions.answer(cantGetWeather());
                                    } else {
                                        $reactions.answer($temp.react + ", " + toMoment($session.when).locale('en').format('Do MMMM') + " it's " + weather + ".");
                                    }
                                } else {
                                    $reactions.answer(cantGetWeather());
                                }
                            }).catch(function (err) {
                                $reactions.answer(cantGetWeather());
                            });
                    else:
                        a: Of course I am a very intelligent robot, but as for the weather on Earth, I know it only for the next 15 days. 
                else:
                    go!: ../Ask current location for Weather check other day     

    state: Weather check in location today
        q!: *  (is it|isn't it|it's|(is|will) * weather|any) * [going] [to] [be]  * ($WeatherCondition|$TemperatureCondition) * [weather] [in|at] $Where * 
        q!: *  (is it|isn't it|it's|(is|will) * weather|any) * $Where *[going] [to] [be]  * ($WeatherCondition|$TemperatureCondition) * [weather] 
        q!: * $Where *  ($WeatherCondition|$TemperatureCondition) *
        q: * {(now/today/current*) * $Where} * || fromState = "../Weather check in location other day"
        q: * [and] [what about] * $Where || fromState = "../Weather check today"
        q: * [and] [what about] * $Where || fromState = "../Weather check in location today"
        q: * $WeatherCondition *  || fromState = "../Weather check in location today"
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
                Weather.openWeatherMapCurrent("imperial", "en", $session.Where.lat, $session.Where.lon).then(function (res){
                    if (res){
                        $response.action = tuneAnimation(res);
                        var react = checkWeatherCondition(res, $session.weatherCondition);
                        $reactions.answer(react + ", today in " + $session.Where.name + " it's " + getWeatherCondition(res) + ".");  
                    } else {
                        $reactions.answer(cantGetWeather());
                    }
                }).catch(function (err) {
                    $reactions.answer(cantGetWeather());
                });               

    state: Weather check in location other day
        q!: * *  ((is|will) it|isn't it|it's|(is|will) * weather|any) * [going] [to] [be]  * ($WeatherCondition|$TemperatureCondition) * [weather] {[in|at] $Where * [on|at] $DateTime} * 
        q!: * *  ((is|will) it|isn't it|it's|(is|will) * weather|any) *  [in|at] $Where * [going] [to] [be]  * ($WeatherCondition|$TemperatureCondition) * [weather] * [on|at] $DateTime * 
        q!: * *  ((is|will) it|isn't it|it's|(is|will) * weather|any) * [going] [to] [be]  * ($WeatherCondition|$TemperatureCondition) * [weather] * [on|at] * $DateTime * [in|at] $Where *
        q!: * $DateTime (is it|isn't it|it's|is * weather|any) * [going] [to] [be]  * ($WeatherCondition|$TemperatureCondition) * [weather] [in|at] $Where
        q!: * {$DateTime *  $Where} * (is it|isn't it|it's|is * weather|any) * [going] [to] [be]  * ($WeatherCondition|$TemperatureCondition) *
        q: * ([and] [what about] $Where|[and] [what about] $DateTime|[and] [what about] * $DateTime * $Where | [and] [what about] $Where * $DateTime| [and] [what about] * $WeatherCondition) || fromState = "../Weather check in location other day"
        q: * ([and] [what about] * $Where|[and] [what about] $DateTime * $Where|[and] [what about] * $Where * $DateTime|[and] [what about] * $WeatherCondition) || fromState = "../Weather check other day"
        q: * ([and] [what about] $Where|[and] [what about] $DateTime|[and] [what about] $DateTime [in|at] * $Where|[and] [what about] $Where $DateTime|[а] $WeatherCondition) || fromState = "../Weather check in location today"
        q: ([and] [what about] $Where|{$DateTime * $Where}| $WeatherCondition) || fromState = "../Weather check today"
        q: * [a] $WeatherCondition || fromState = "../Weather on a selected date in a location"        
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
                        Weather.openWeatherMapForecast(16, "imperial", "en", $session.Where.lat, $session.Where.lon).then(function (res){
                            if (res){
                                if (res.weather){
                                    $response.action = tuneAnimation(res);
                                }
                                var weather = parseWeather(res.list, $session.when, $session.weatherCondition);
                                if (weather === false) {
                                    $reactions.answer("It looks like there is some problems with dates. Couldn't get weather forecast");
                                } else {
                                    $reactions.answer($temp.react + ", " + toMoment($session.when).locale('en').format('Do MMMM') + " in " + $session.Where.name + " it's " + weather + ".");    
                                }
                            } else {
                                $reactions.answer(cantGetWeather());
                            }
                        }).catch(function (err) {
                            $reactions.answer(cantGetWeather());
                        });   
                else:
                    a: Of course I am a very intelligent robot, but as for the weather on Earth, I know it only for the next 15 days.            
                
    state: Exact city
        q: * [for] (which|what) * (place|city|town|location) * 
        if: $session.Where
            a: {{$session.Where.name}}.
        else:
            a: I forgot what it's called....

    state: Water temperature
        q!: * (water temperature/temperature of [the] water) *
        random:
            a: I don't know water temperature, only air temperature.
            a: I know only the temperature of air.