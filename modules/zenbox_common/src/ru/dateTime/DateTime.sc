require: ../../common/common.sc

require: ../../common/dateTime/DateTime.js
require: DateTime.js

require: dateTime/dateTime.sc
  module = zb_common

require: patterns.sc
  module = zb_common
require: where/where.sc
  module = zb_common

require: ../main.js

require: answers.yaml
    var = DateTimeCommonAnswers

init:
    $global.$DateTimeAnswers = (typeof DateTimeCustomAnswers != 'undefined') ? applyCustomAnswers(DateTimeCommonAnswers, DateTimeCustomAnswers) : DateTimeCommonAnswers;

    bind("postProcess", function($context) {
        // Постобработчик для корректировки формата даты-времени для синтеза
        function replaceDateTime(text) {
            var monthRegex = /\b(\d+)(?=\s+(янв|фев|мар|апр|мая|июн|июл|авг|сен|окт|ноя|дек))/g;
            var monthMatch = monthRegex.exec(text);
            var monthDigit;

            if (monthMatch) {
                // У порядковых прилагательных на 3 (кроме 13) местоименный тип склонения, для ТТС нужно -е
                // У всех остальных адъективный, им нужно -ое (если просто -е, то будет читаться как -ые)
                monthDigit = parseInt(monthMatch[0], 10);
                text = text.replace(monthRegex, (monthDigit % 10 === 3 && monthDigit !== 13) ? "$1-е" : "$1-ое");
            }

            return text.replace(/\b(\d+)\s+г\./g, "$1-го года");
        }

        var $response = $context.response;

        if ($response.replies && $response.replies.length > 0) {
            for (var idx = 0, len = $response.replies.length; idx < len; idx += 1) {
                if ($response.replies[idx].state && $response.replies[idx].state.indexOf("/Date and Time") > -1 && $response.replies[idx].text) {
                    $response.replies[idx].text = replaceDateTime($response.replies[idx].text);
                }
            }
        }
    });

patterns:
    $Home = (у нас|[с] нами|[в] наш* (~город|~край|~края|~местоположени*))
    $InCity = (в/во) [городе]
    $Location = ([$InCity] $Where / $InCity $AnyWord)
    $Past = (был*)

    $curTime = [текущее] время
    $dayPeriod = время суток
    $timeZone = ~часовой ~пояс
    $weekDay = день недели
    $whatTheCurTime = ((который|какой|какое/ско*) {[сейчас|щас] (час|время/часов/времени/$dayPeriod)}/(что/сколько) * {[сейчас|щас] (с|со|на) (часах|часами|временем)}/[*скажи] ($curTime/$dayPeriod))
    $whatTheCurDate = {(какой|какое|какая|назови|скажи|что) * [(сегодня*|сейчас|щас)] [по|на] (день|число|дат*|~календарь)}
    $afterAbsoluteInterval = {сколько [времени|время] прошло}
    $tillAbsoluteInterval = (когда|через сколько [дней]|как скоро|скоро (ли|ль))

theme: /Date and Time

    state: Current time
        q!: * {$whatTheCurTime [([$InCity] $Where / $InCity $AnyWord)]} *
        q!: * {(сейчас/щас) времени} [([$InCity] $Where / $InCity $AnyWord)]
        q: * [а] время * || fromState = "../Current date"
        q!: * ско* * [$InCity] $Where * времени *
        q: {[а] (([$InCity] $Where / $InCity $AnyWord)) [(сколько/какое) [сейчас]]} || fromState = "../Current time"
        q!: (ско*/сейчас/щас) часов
        script:
            $response.action = 'time';
            $temp.time = currentDate().locale('ru').format('LT');
            if ($parseTree.whatTheCurTime && $parseTree.whatTheCurTime[0].dayPeriod) {
                $session.hasDayPeriod = true;
                $temp.dayPeriod = dts_dayPeriodName(getDayPeriodForTime(currentDate()));
            } else {
                $session.hasDayPeriod = false;
            }
        if: $parseTree.Where
            script:
                $session.Where = $parseTree.Where[0].value;
                DateTime.timezoneSearch($parseTree._Where.lat, $parseTree._Where.lon).then(function (res){
                    if (res && res.status === "OK"){
                        if ($session.hasDayPeriod) {
                            $temp.dayPeriod = dts_dayPeriodName(getDayPeriodForTime(res.timestamp*1000));
                            $reactions.answer(selectRandomArg($DateTimeAnswers["Current time"]["gotCity"]["hasDayPeriod"]));
                            delete $session.hasDayPeriod;
                        } else {
                            $temp.time = moment(res.timestamp*1000).locale('ru').format('LT');
                            toTimeAnswer(selectRandomArg($DateTimeAnswers["Current time"]["gotCity"]["hasNotDayPeriod"]), res.gmtOffset/60);
                        }   
                    } else {
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Current time"]["gotCity"]["error"]));
                    }
                }).catch(function (err) {
                    $reactions.answer(selectRandomArg($DateTimeAnswers["Current time"]["gotCity"]["error"]));
                });
        else:
            script:
                if ($parseTree.AnyWord) {
                    $reactions.answer(selectRandomArg($DateTimeAnswers["Current time"]["noCity"]));
                } else if ($parseTree.whatTheCurTime && $parseTree.whatTheCurTime[0].dayPeriod) {
                    $reactions.answer($temp.dayPeriod);
                    delete $session.hasDayPeriod;
                } else {
                    toTimeAnswer($temp.time, $request.data.offset);
                }        

    state: Current date
        q!: * {$whatTheCurDate [(([$InCity] $Where / $InCity $AnyWord))]} *
        q: * [а] (число|дата|день) * || fromState = "../Current time"
        q: {[а] (([$InCity] $Where / $InCity $AnyWord)) [какая]} || fromState = "../Current date"
        q: {[а] (([$InCity] $Where / $InCity $AnyWord)) [какая]} || fromState = "../Date check today"
        script:
            $temp.date = currentDate().locale('ru').format('LL, dddd');
        if: $parseTree.Where
            script:
                $session.Where = $parseTree.Where[0].value
                DateTime.timezoneSearch($parseTree._Where.lat, $parseTree._Where.lon).then(function (res){
                    if (res && res.status === "OK"){
                        $temp.date = moment(res.timestamp*1000).locale('ru').format('dddd, LL');
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Current date"]["gotCity"]["succeed"]));  
                    } else {
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Current date"]["gotCity"]["failed"]));
                    }
                }).catch(function (err) {
                    $reactions.answer(selectRandomArg($DateTimeAnswers["Current date"]["gotCity"]["failed"]));
                }); 
        else:
            if: ($parseTree.AnyWord)
                a: {{selectRandomArg($DateTimeAnswers["Current date"]["noCity"]["haveAnyWord"])}}
            else:
                a: {{selectRandomArg($DateTimeAnswers["Current date"]["noCity"]["haveNothing"])}}
                  
    state: Time check
        q!: * {сейчас [([$InCity] $Where / $InCity $AnyWord)] * $TimeAbsolute} *
        q!: * { ([(([$InCity] $Where / $InCity $AnyWord))] (сейчас|уже|еще)|([$InCity] $Where / $InCity $AnyWord) [сейчас|уже|еще]) (поздно|рано|утро|вечер|день|ночь)}
        q: {[а] (([$InCity] $Where / $InCity $AnyWord))} || fromState = "../Time check"
        script:
            $response.action = 'time';
            $temp.time = currentDate().locale('ru').format('LT');
        if: $parseTree.Where
            script:
                $session.Where = $parseTree.Where[0].value;
                DateTime.timezoneSearch($parseTree._Where.lat, $parseTree._Where.lon).then(function (res){
                    if (res && res.status === "OK"){
                        $temp.time = moment(res.timestamp*1000).locale('ru').format('LT');
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Time check"]["gotCity"]["succeed"]));
                    } else {
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Time check"]["gotCity"]["failed"]));
                    }
                }).catch(function (err) {
                    $reactions.answer(selectRandomArg($DateTimeAnswers["Time check"]["gotCity"]["failed"]));
                });
        else:
            if: ($parseTree.AnyWord)
                a: {{selectRandomArg($DateTimeAnswers["Time check"]["noCity"]["haveAnyWord"])}}
            else:
                a: {{selectRandomArg($DateTimeAnswers["Time check"]["noCity"]["haveNothing"])}}

    state: Date check today
        q!: * {(сейчас|сегодня) [([$InCity] $Where/ $InCity $AnyWord)] ($DateAbsolute|$DateRelative::DateAbsolute|$DateMonth|$Season)} * 
        script:
            $session.tree = $parseTree;
            $temp.date = currentDate().locale('ru').format('LL, dddd');
        if: $parseTree.Where
            script:
                $session.Where = $parseTree.Where[0].value;
                DateTime.timezoneSearch($parseTree._Where.lat, $parseTree._Where.lon).then(function (res){
                    if (res && res.status === "OK"){
                        todayCheck(moment(res.timestamp*1000), 1); 
                        $reactions.answer($temp.answer); 
                    } else {
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Date check today"]["gotCity"]["failed"]));
                    }
                }).catch(function (err) {
                    $reactions.answer(selectRandomArg($DateTimeAnswers["Date check today"]["gotCity"]["failed"]));
                });  
        else:
            if: ($parseTree.AnyWord)
                a: {{selectRandomArg($DateTimeAnswers["Date check today"]["noCity"]["haveAnyWord"])}}
            else:
                script:
                    todayCheck(currentDate(),0);
                a: {{$temp.answer}}
        
    state: Date check other DateWeekday
        q!: * ($DateRelative::DateAbsolute|$DateAbsolute) * [(это|будет|наступит):1|был*:2] $DateWeekday *
        q: * [а] * ($DateRelative::DateAbsolute|$DateAbsolute) * || fromState = "../Date check other DateWeekday"
        if: ($parseTree.DateWeekday)
            script:
                if ($parseTree._Root != "2"){
                    $temp.future = moment.unix(toFuture($parseTree.DateAbsolute[0]));
                    $temp.yn = yesOrNo(dts_dayweekName($parseTree.DateWeekday[0].value), $temp.future.locale("ru").format('dddd'));
                    $temp.weekDay = $temp.future.locale('ru').format('dddd');
                    if ($temp.weekDay != "Invalid date"){
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Date check other DateWeekday"]["future"]["succeed"]));
                    } else {
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Date check other DateWeekday"]["failed"]));
                    }                 
                } else {
                    $temp.past =  moment.unix(toPast($parseTree.DateAbsolute[0]));
                    $temp.yn = yesOrNo(dts_dayweekName($parseTree.DateWeekday[0].value), $temp.past.locale("ru").format('dddd'));
                    $temp.weekDay = $temp.past.locale('ru').format('dddd');
                    if ($temp.weekDay != "Invalid date"){
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Date check other DateWeekday"]["past"]["succeed"]));
                    } else {
                        $reactions.answer(selectRandomArg($DateTimeAnswers["Date check other DateWeekday"]["failed"]));
                    }   
                }
        else:
            if: (dts_dayweekName($parseTree.DateAbsolute[0].value) != undefined)
                a: {{ selectRandomArg($DateTimeAnswers["Date check other DateWeekday"]["wholeDate"]) }}
            else:
                a: {{ selectRandomArg($DateTimeAnswers["Date check other DateWeekday"]["failed"]) }}

    state: Date check other DateAbsolute
        q!: * ($DateRelative|$DateTimeRelative) * $DateAbsolute
        q: * [а] * $DateRelative * || fromState = "../Current date"
        q: * [а] * $DateRelative * || fromState = "../Date plus"
        q: * [а] * $DateRelative * || fromState = "../Date minus"
        q: * [а] * $DateRelative * || fromState = "../Date check today"
        q: * [а] * $DateRelative * || fromState = "../Date check other DateAbsolute"
        script:
            if ($parseTree.DateTimeRelative) {
                if ($parseTree.DateTimeRelative[0].DateTimeNow)
                    $temp.today = 1;
            }
        if: ($temp.today == 1)
            script:
                $temp.today = 0;
            go!: ../Date check today
        else:
            script: 
                dateCheckOtherDateAbsolute();
            a: {{$temp.answer}}
        
    state: Day of the week
        q!: * {какой [сегодня|сейчас] $weekDay [$DateAbsolute]}
        q!: * {какой [это:0|(будет|наступит|выпадет/выпадает):1|$Past] ($weekDay|день) [-/на] ($DateRelative::DateAbsolute|$DateAbsolute)}
        q: [а] * ($DateRelative::DateAbsolute) || fromState = ./
        q: [а] * ($DateAbsolute) || fromState = ./
        script:
            // Если явно говорим, что нужен день недели, а не день, то не называем полную дату
            var format = ($parseTree.weekDay) ? "dddd" : "LL, dddd";

            if ($parseTree.DateAbsolute) {
                $session.date = $parseTree.DateAbsolute[0];

                // Если сами спрашиваем про дату (а не вчера/послезавтра), то тоже её не дублируем
                if ($session.date.pattern === "DateAbsolute") {
                    format = "dddd";
                }

                if ($parseTree._Root == "1") {
                    $temp.weekDay = moment.unix(toFuture($session.date)).locale("ru").format(format);
                } else if ($parseTree.Past) {
                    $temp.weekDay = moment.unix(toPast($session.date)).locale("ru").format(format);
                } else {
                    $temp.weekDay = (currentDate().format("X") - toPast($session.date) > 2592000)
                        ? moment.unix(toPast($session.date)).locale("ru").format(format)
                        : moment.unix(toFuture($session.date)).locale("ru").format(format);
                }
                if ($temp.weekDay != "Invalid date") {
                    $reactions.answer(capitalize($temp.weekDay));
                } else {
                    $reactions.answer(selectRandomArg($DateTimeAnswers["Day of the week"]["failed"]));
                }
            } else {
                $temp.weekDay = currentDate().locale("ru").format(format);
                $reactions.answer(selectRandomArg($DateTimeAnswers["Day of the week"]["succeed"]));
            }

    state: Time plus
        q!: * ({сколько будет [врем*]}|{какое будет врем*}) * ($DateTimeRelative|$TimeRelative) *
        q!: ($DateTimeRelative|$TimeRelative) * ({сколько будет [врем*]}|{какое будет врем*})            
        q: [а] * ($DateTimeRelative|$TimeRelative) || fromState = "../Current time"
        q: [а] * ($DateTimeRelative|$TimeRelative) || fromState = "../Time plus"
        q: [а] * ($DateTimeRelative|$TimeRelative) || fromState = "../Time minus"            
        script: 
            if ($parseTree.TimeRelative) {
                $reactions.answer(toMoment($parseTree.TimeRelative[0]).locale('ru').format('LT'));                
            } else {
                $reactions.answer(toMoment($parseTree.DateTimeRelative[0]).locale('ru').format('LT'));   
            }

    state: Date plus
        q!: * {(как* (день|дата|~число) [будет|наступит]|это когда) * ($DateTimeRelative|$DateRelative)} *
        q!: * {как* (день|дата|~число) [будет|наступит] ($DateTimeRelative|$DateRelative)} *        
        q: [а] * ($DateTimeRelative|$DateRelative) || fromState = "../Current date"
        q: [а] * ($DateTimeRelative|$DateRelative) || fromState = "../Date plus"
        q: [а] * {(будет|наступит) ($DateTimeRelative|$DateRelative)} || fromState = "../Day of the week"
        q: [а] * {[будет|наступит] ($DateTimeRelative|$DateRelative)} || fromState = "../Date minus"
        q: [а] {(день|дата|число) [как*]} * || fromState = "../Day of the week"
        script:
            if ($parseTree.DateTimeRelative) {
                $reactions.answer(toMoment($parseTree.DateTimeRelative[0]).locale('ru').format('LL, dddd'));
            } else if ($parseTree.DateRelative) {
                $reactions.answer(toMoment($parseTree.DateRelative[0]).locale('ru').format('LL, dddd'));
            } else {
                if(!$session.date) {
                    $session.date = currentDate().locale('ru').format('LL, dddd');
                    $reactions.answer($session.date);
                } else {
                    $reactions.answer(toMoment($session.date).locale('ru').format('LL, dddd'));
                }
                delete $session.date;
            }           

    state: Year plus
        q!: * {како* год [будет|наступит] * ($DateTimeRelative|$DateRelative)} *
        q: [а] * ($DateTimeRelative|$DateRelative) || fromState = "../Year plus"
        if: $parseTree.DateTimeRelative
            a: {{toMoment($parseTree.DateTimeRelative[0]).locale('ru').format('YYYY')}}-й
        else:
            a: {{toMoment($parseTree.DateRelative[0]).locale('ru').format('YYYY')}}-й

    state: Month plus
        q!: * {како* месяц [будет|наступит] * ($DateTimeRelative|$DateRelative)} *
        q: [а] * ($DateTimeRelative|$DateRelative) || fromState = "../Month plus"
        if: $parseTree.DateTimeRelative
            a: {{toMoment($parseTree.DateTimeRelative[0]).locale('ru').format('MMMM')}}
        else:
            a: {{toMoment($parseTree.DateRelative[0]).locale('ru').format('MMMM')}}

    state: Time minus
        q!: * (сколько {было [времени|время]}|{како[й|е] * врем* был* }) * ($DateTimeRelative|$TimeRelative) *
        q!: * ($DateTimeRelative|$TimeRelative) [назад] * (сколько {было [времени|время]}|{како* * врем* был* })
        q: [а] * ($DateTimeRelative|$TimeRelative) * || fromState = "../Time minus"
        if: ($parseTree.TimeRelative)
            a: {{toMoment($parseTree.TimeRelative[0]).locale('ru').format('LT')}}
        else:
            a: {{toMoment($parseTree.DateTimeRelative[0]).locale('ru').format('LT')}}
    
    state: Date minus
        q!: * како* (день|число|год|месяц) был* * ($DateTimeRelative|$DateRelative) *
        q!: * ($DateTimeRelative|$DateRelative) * како* (день|число|год|месяц) был* *
        q: [а] * ($DateTimeRelative|$DateRelative) * || fromState = "../Date minus"
        q: [а] * {был* ($DateTimeRelative|$DateRelative)} * || fromState = "../Date plus"
        q: [а] {(день|дата|число) [как*]} * || fromState = "../Day of the week"
        if: ($parseTree.DateTimeRelative)
            a: {{moment.unix(toPast($parseTree.DateTimeRelative[0])).locale('ru').format('LL, dddd')}}
        else:
            if: ($parseTree.DateRelative)
                a: {{moment.unix(toPast($parseTree.DateRelative[0])).locale('ru').format('LL, dddd')}}
            else:
                a: {{moment.unix(toPast($session.date)).locale('ru').format('LL, dddd')}}
                script:
                    delete $session.date;        

    state: Time till
        q!: * (сколько|как скоро) [врем*|дней|час*] [осталось [ждать]] до [~начало] ($DateTimeAbsolute|$DateMonth|$Season|$Weekend)
        q!: * $tillAbsoluteInterval [будет|будут|наступит|наступят|настанет|настанут|[осталось] ждать|ожидать|начнется|начнётся] [~начало] ($DateTimeAbsolute|$DateMonth|$Season|$Weekend) *
        q: [а] * ($DateTimeAbsolute|$DateMonth|$Season|$Weekend) * || fromState = "../Time till"
        script:
            if ($parseTree.DateTimeAbsolute) {
                timeInterval("dateTime", true);
            } else if ($parseTree.Season) {
                timeInterval("season", $parseTree.Season[0], false);
            } else if ($parseTree.DateMonth) {
                timeInterval("month", $parseTree.DateMonth[0], false);
            } else {
                timeInterval("weekEnd");
            }

    state: Time till end of month or season
        q!: * (сколько|как скоро) [врем*|дней|час*] [осталось [ждать]] до (конца|окончания) ($DateMonth|$Season)
        q!: * $tillAbsoluteInterval [будет|будут|наступит|настанет|[осталось] ждать|ожидать|начнется|начнётся] {(~конец|~окончание|*кончится|заканчивается) ($DateMonth|$Season)} *
        q: [а] * ($DateMonth|$Season) * || fromState = "../Time till end of month or season"
        q: [а] * (законч*|~конец) * || fromState = "../Time till"
        if: ($parseTree.Season)
            script:
                timeInterval("season", $parseTree.Season[0], true);
        else:
            if: ($parseTree.DateMonth)
                script:
                    timeInterval("month", $parseTree.DateMonth[0], true);
            else:
                a: {{ selectRandomArg($DateTimeAnswers["Time till end of month or season"]["repeat plz"]) }} || question = true

    state: Time after
        q!: * $afterAbsoluteInterval (с|после) * $DateTimeAbsolute
        q!: * (как давно|когда|(сколько|какое) (врем*|дней|часов|минут) назад) был* * $DateTimeAbsolute
        q!: * (сколько * назад|как давно) * было * $DateTimeAbsolute *     
        q: [а] * $DateTimeAbsolute * || fromState = "../Time after"
        script:
            timeInterval("dateTime", false);

    state: Time zone
        q!: * {(какой|каком|что) * [у нас|наш|[у|в] $Where] $timeZone} *
        a: {{selectRandomArg($DateTimeAnswers["Time zone"]["a1"])}}
        a: {{selectRandomArg($DateTimeAnswers["Time zone"]["a2"])}}
        a: {{selectRandomArg($DateTimeAnswers["Time zone"]["random"])}}

    state: Timezone comparison
        q!: * [(сколько|какая|какова)] * разниц* [в*|по] [времени|время|часах|часам|часовы* пояс*] между ($Where|$AnyWord|$Home) * ($Where|$AnyWord|$Home) [в*|по] [времени|время|часах|часам|часовы* пояс*]
        q!: * [(сколько|какая|какова)] * {разниц* [в*|по] [времени|время|часах|часам|часовы* пояс*] [у] ($Where|$AnyWord|$Home)} * [с|и у] ($Where|$AnyWord|$Home) [в*|по] [времени|время|часах|часам|часовы* пояс*]
        q: * $Where * || fromState = "../Ask current location for Timezone comparison", onlyThisState = true
        if: $parseTree.Home
            script:
                $session.isHome = true
                $session.Where2 = $parseTree.Where[0].value;
            go!: ../Ask current location for Timezone comparison
        else:
            if: $parseTree.Where
                if: Object.keys($parseTree.Where).length == 2 || $session.isHome && $parseTree.Where
                    script:
                        if ($session.isHome) {
                            $session.Where1 = $parseTree.Where[0].value;
                            delete $session.isHome;
                        } else {
                            $session.Where1 = $parseTree.Where[0].value;
                            $session.Where2 = $parseTree.Where[1].value;
                        }
                        DateTime.timezoneSearch($session.Where1.lat, $session.Where1.lon).then(function (res){
                            if (res && res.status === "OK"){
                                $session.gmtOffset1 = res.gmtOffset;
                                DateTime.timezoneSearch($session.Where2.lat, $session.Where2.lon).then(function (res){
                                    if (res && res.status === "OK"){
                                        $session.gmtOffset2 = res.gmtOffset;
                                        $temp.comparison = comparisonTimezone($session.gmtOffset1, $session.gmtOffset2);
                                        $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone comparison"]["succeed"]));
                                    } else {
                                        $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone comparison"]["failed"]));
                                    }
                                }).catch(function (err) {
                                    $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone comparison"]["failed"]));
                                });  
                            } else {
                                $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone comparison"]["failed"]));
                            }
                        }).catch(function (err) {
                            $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone comparison"]["failed"]));
                        });  
                else:
                    a: {{ selectRandomArg($DateTimeAnswers["Timezone comparison"]["dontKnowCities"]) }}
            else:
                a: {{ selectRandomArg($DateTimeAnswers["Timezone comparison"]["dontKnowCities"]) }}

    state: Timezone calculator
        q!: * (сколько|какое) {{[врем*] [будет]} ([$InCity] $Where::Where2|$InCity $AnyWord|$Home)} (если|когда|в то время как) [в|на|во] ([$InCity] $Where::Where1|$InCity $AnyWord|$Home) [будет] $TimeAbsolute
        q!: * [(если|когда|в то время как)] ([$InCity] $Where::Where1|$InCity $AnyWord|$Home) [будет] $TimeAbsolute [то] (сколько|какое) {{[будет] [врем*]} ([$InCity] $Where::Where2|$InCity $AnyWord|$Home)}
        q: * $Where * || fromState = "../Ask current location for Timezone calculator", onlyThisState = true
        if: $parseTree.Home
            script:
                $session.Where1 = null;
                $session.Where2 = null;
                $session.isHome = true
                if ($parseTree.Where1) {
                    $session.Where1 = $parseTree.Where1[0].value;
                } else if ($parseTree.Where2) {
                    $session.Where2 = $parseTree.Where2[0].value;
                }
                $session.timeAbsolute = $parseTree.TimeAbsolute[0];
            go!: ../Ask current location for Timezone calculator
        else:
            if: $parseTree.Where1 || $session.isHome
                if: $parseTree.Where2 || $session.isHome
                    script:
                        if ($session.isHome && $parseTree.Where) {
                            if ($session.Where1) {
                                $session.Where2 = $parseTree.Where[0].value;
                            } else if ($session.Where2) {
                                $session.Where1 = $parseTree.Where[0].value;
                            }
                            $temp.timeQuery = $session.timeAbsolute;

                            delete $session.isHome;
                            delete $session.timeAbsolute;
                        } else {
                            $session.Where1 = $parseTree.Where1[0].value;
                            $session.Where2 = $parseTree.Where2[0].value;
                            $temp.timeQuery = $parseTree.TimeAbsolute[0];
                        }
                        
                        $session.time1 = calculatorTimezone(0, $request.data.offset*60, toMoment($temp.timeQuery));

                        DateTime.timezoneSearch($session.Where1.lat, $session.Where1.lon).then(function (res){
                            if (res && res.status === "OK") {
                                $session.gmtOffset1 = res.gmtOffset;                    
                                DateTime.timezoneSearch($session.Where2.lat, $session.Where2.lon).then(function (res){
                                    if (res && res.status === "OK") {
                                        $session.gmtOffset2 = res.gmtOffset;
                                        $session.time2 = calculatorTimezone($session.gmtOffset1, $session.gmtOffset2, $session.time1);
                                        $temp.time1f = moment($session.time1).locale('ru').format('LT');
                                        $temp.time2f = moment($session.time2).locale('ru').format('LT');
                                        $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone calculator"]["succeed"]));
                                    } else {
                                        $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone calculator"]["failed"]));
                                    }
                                }).catch(function (err) {
                                    $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone calculator"]["failed"]));
                                });
                            } else {
                                $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone calculator"]["failed"]));
                            }
                        }).catch(function (err) {
                            $reactions.answer(selectRandomArg($DateTimeAnswers["Timezone calculator"]["failed"]));
                        });      
                else:
                    a: {{ selectRandomArg($DateTimeAnswers["Timezone calculator"]["dontKnowCities"]) }}
            else:
                a: {{ selectRandomArg($DateTimeAnswers["Timezone calculator"]["dontKnowCities"]) }}
            
    state: Current year
        q!: * {какой * (сейчас|сегодня|нынче|теперь) год} *
        q: * [а] год * || fromState = "/Date and Time/Current time"
        q: * [а] год * || fromState = "../Current month"
        q: * [а] год * || fromState = "../Current season"
        a: {{currentDate().locale('ru').format('YYYY')}}-й

    state: Current month
        q!: * {какой * (сейчас|сегодня|нынче|теперь) месяц} *
        q: * [а] месяц * || fromState = "/Date and Time/Current time"
        q: * [а] месяц * || fromState = "../Current year"
        q: * [а] месяц * || fromState = "../Current season"
        a: {{currentDate().locale('ru').format('MMMM')}}

    state: Current season
        q!:  * {(како*|назови*) * (сейчас|сегодня|нынче|теперь|текущ*) (время года|сезон)} *
        q: * время года * || fromState = "../Current date"
        q: * [а] время года * || fromState = "/Date and Time/Current time"
        q: * [а] время года * || fromState = "../Current year"
        q: * [а] время года * || fromState = "../Current month"
        q: * [а] время года * || fromState = "../Current season"
        a: {{dts_seasonName(getSeason(currentDate()))}}

    state: Sunset Daybreak
        q!: * {(восколько|во сколько|когда) * (закат|рассвет|заря|зорька)} *
        q: * [а] (закат|рассвет|заря|зорька) * || fromState = "../Sunset Daybreak"
        a: {{ selectRandomArg($DateTimeAnswers["Sunset Daybreak"]) }}

    state: Eclipse
        q!: * {(восколько|во сколько|когда) * [солнечн*] затмен*} *
        a: {{ selectRandomArg($DateTimeAnswers["Eclipse"]) }}

    state: Ask current location for Timezone comparison
        a: {{ selectRandomArg($DateTimeAnswers["Ask current location for Timezone comparison"]) }} || question = true

    state: Ask current location for Timezone calculator
        a: {{ selectRandomArg($DateTimeAnswers["Ask current location for Timezone calculator"]) }} || question = true