require: ../../common/common.sc

require: ../../common/dateTime/DateTime.js
require: DateTime.js


require: dateTime/dateTimeEn.sc
  module = zb_common

require: patternsEn.sc
  module = zb_common
require: where/whereEn.sc
  module = zb_common

require: ../main.js

patterns:

    $KindOfDate = (
        (date|day):1 |
        month:2 |
        year:3 |
        (season of [the|this] year|[year] season):4)
    $Season = (winter: 0|spring: 1|summer: 2|autumn: 3)
    
    #timezone calculator
    $itIs = ({it is} | it 's | its | it's)
    $whatTheTime = ((how (much|early|late) [time|$itIs]) | (what|whats|what's|what 's) [is] [the] time)
    $when = (when | if) 

    #current time 
    $curTime = ([the] [current] time)
    $whatTheCurTime = ((how (much time|early|late) [$itIs] [now]) | (what [is]|whats|what's|what 's) $curTime)
    $tell = ([(can|could) you] (tell|give) [me] | what about | want to (know|get|receive))

    $here = (here/us/(our/my) city)


theme: /Date and Time

    state: Current time
        q!: [current] time
        q!: $curTime
        q!: * $whatTheCurTime * [is it] * [([in] $Where | in $AnyWord)] *
        q!: * $tell * $curTime * [([in] $Where | in $AnyWord)] *
        q!: * ([in] $Where | in $AnyWord) * $curTime *
        q: * [and|what about] * time * || fromState = "../Current date"
        q: * [and] * ([in] $Where |in $AnyWord) * || fromState = "../Current time"
        script:
            $response.action = 'time';
            $temp.time = currentDate().locale('en').format('h:mm a');                  
        if: $parseTree.Where
            script:
                $session.Where = $parseTree.Where[0].value;
                DateTime.timezoneSearch($session.Where.lat, $session.Where.lon).then(function (res){
                    if (res && res.status === "OK") {
                        $temp.time = moment(res.timestamp*1000).locale('en').format('h:mm a');
                        toTimeAnswer("It's " + $temp.time + " in " + $session.Where.name + " now", res.gmtOffset/60);     
                    } else {
                        $reactions.answer($session.Where.name + "? Hmm. I don't know this city. But it's " + $temp.time +" in our city now");
                    }
                }).catch(function (err) {                    
                    $reactions.answer($session.Where.name + "? Hmm. I don't know this city. But it's " + $temp.time +" in our city now");
                });
        else:
            script:
                if ($parseTree.AnyWord){
                    $reactions.answer(capitalize($parseTree._AnyWord) + "? Hmm. I don't know this city. But it's " + $temp.time +" in our city now");
                } else {
                    toTimeAnswer("It's " + $temp.time, $request.data.offset);
                }       
    
    state: Current date
        q!: [current] date
        q!: * (tell|give) me the (date|day) *
        q!: * (what is|what 's) * [the] (date|day) * [today] *
        q!: * [can you] [tell] [me] [the] current * [is] * $KindOfDate * [([in] $Where | in $AnyWord)] *
        q!: * what * $KindOfDate [is] [it] today * [([in] $Where | in $AnyWord)] *
        q!: * what * $KindOfDate is [it] * [([in] $Where | in $AnyWord)] *
        q: * [and] $KindOfDate * || fromState = "../Current time"
        q: * [and] $KindOfDate * || fromState = "../Current date"
        q: * [and] ([in] $Where |in $AnyWord) || fromState = "../Current date"
        q: * [and] ([in] $Where |in $AnyWord) || fromState = "../Date check today"
        q!: * what * [is] {[the] $KindOfDate * today*} * [([in] $Where | in $AnyWord)] *
        script:
            $session.$parseTree = $parseTree;
            if ($parseTree.KindOfDate){
                $temp.kindOfDate = calculateDate(currentDate(), $session.$parseTree.KindOfDate[0].value);
            }
            $temp.date = moment(currentDate()).locale('en').format('LL, dddd');
        if: $parseTree.Where
            script:
                $session.Where = $parseTree.Where[0].value
                DateTime.timezoneSearch($session.Where.lat, $session.Where.lon).then(function (res){
                    if (res && res.status === "OK"){
                        if ($parseTree.KindOfDate) {
                            $temp.date = calculateDate(moment(res.timestamp*1000), $session.$parseTree.KindOfDate[0].value);
                        } else {
                            $temp.date = moment(res.timestamp*1000).locale('en').format('dddd, LL');
                        }
                        $reactions.answer("It's " + $temp.date + " in " + $session.Where.name + " now");   
                    } else {
                        $temp.date = ($parseTree.KindOfDate) ? $temp.kindOfDate : $temp.date;
                        $reactions.answer($session.Where.name + "? Hmm. I don't know this city. But it's " + $temp.date + " in our city now");
                    }
                }).catch(function (err) {
                    $temp.date = ($parseTree.KindOfDate) ? $temp.kindOfDate : $temp.date;
                    $reactions.answer($session.Where.name + "? Hmm. I don't know this city. But it's " + $temp.date + " in our city now");  
                }); 
        else:
            script:
                if ($parseTree.AnyWord) {
                    $temp.date = ($parseTree.KindOfDate) ? $temp.kindOfDate : $temp.date;
                    $reactions.answer(capitalize($parseTree._AnyWord) + "? Hmm. I don't know this city. But it's " + $temp.date + " in our city now"); 
                } else {
                    $temp.date = ($parseTree.KindOfDate) ? $temp.kindOfDate : $temp.date;
                    $reactions.answer("It's " + $temp.date);    
                }
                
                   
    state: Time check
        q!: * is it $TimeAbsolute * [([in] $Where | in $AnyWord)] *
        q!: * { [([in] $Where | in $AnyWord)] * [now|already|still|at the moment] * (daytime|night*|morning|evening|late|early)}
        q: * [and] *  ([in] $Where |in $AnyWord) * || fromState = "../Time check"
        script:
            $response.action = 'time';
            $temp.time = moment(currentDate()).locale('en').format('h:mm a');
        if: $parseTree.Where
            script:
                $session.Where = $parseTree.Where[0].value;
                DateTime.timezoneSearch($session.Where.lat, $session.Where.lon).then(function (res){
                    if (res && res.status === "OK"){
                        $temp.time = moment(res.timestamp*1000).locale('en').format('h:mm a');
                        $reactions.answer("Exact time in " + $session.Where.name + " is " + $temp.time);  
                    } else {
                        $reactions.answer($session.Where.name + "? Hmm. I don't know this city. But it's " + $temp.time +" in our city now");
                    }
                }).catch(function (err) {
                    $reactions.answer($session.Where.name + "? Hmm. I don't know this city. But it's " + $temp.time +" in our city now"); 
                });
        else:
            script:
                if ($parseTree.AnyWord){
                    $reactions.answer(capitalize($parseTree._AnyWord) + "? Hmm. I don't know this city. But it's " + $temp.time +" in our city now");
                } else {
                    $reactions.answer("Exact time is " + $temp.time);
                }    
        

    state: Date check today 
        q!: * [today] (it 's|it is|is (it/today)|is) * ($DateAbsolute|$DateRelative::DateAbsolute|$DateMonth|$Season) [coming] * 
        q!: * [today] (it 's|it is|is (it/today)|is) * ($DateAbsolute|$DateRelative::DateAbsolute|$DateMonth|$Season) [coming] {[([in] $Where | in $AnyWord)] [today|now]} *
        q!: * [today] (it 's|it is|is (it/today)|is) * ($DateAbsolute|$DateRelative::DateAbsolute|$DateMonth|$Season) [coming] {[([in] $Where | in $AnyWord)] (now|today|at the moment|is it [the])} *
        script:
            $session.tree = $parseTree;
            $temp.date = moment(currentDate()).locale('en').format('LL, dddd');
        if: $parseTree.Where
            script:            
                $session.Where = $parseTree.Where[0].value;
                DateTime.timezoneSearch($session.Where.lat, $session.Where.lon).then(function (res){
                    if (res && res.status === "OK"){
                        todayCheck(moment(res.timestamp*1000), 1); 
                        if($temp.answer) {
                            $reactions.answer($temp.answer)
                        }
                    } else {
                        $reactions.answer($session.Where.name + "? Hmm. I don\'t know this city. But it\'s " + $temp.date + ' in our city now.');
                    }
                }).catch(function (err) {
                    $reactions.answer($session.Where.name + "? Hmm. I don\'t know this city. But it\'s " + $temp.date + ' in our city now.');
                });  
        else:
            script:  
                if ($parseTree.AnyWord){
                    $reactions.answer(capitalize($parseTree._AnyWord) + "? Hmm. I don\'t know this city. But it\'s " + $temp.date + ' in our city now.');
                } else {
                    todayCheck(currentDate(),0);
                    $reactions.answer($temp.answer);
                }


                
    state: Date check other DateAbsolute
        q!: * ((is|will [be]):1|(was):2) * [it] * $DateRelative * $DateAbsolute *
        q!: * ((is|will [be]):1|(was):2) * [it] * $DateAbsolute * $DateRelative *
        q!: * $DateRelative * ((is|will [be]):1|(was):2) * $DateAbsolute *
        q!: * ((is|will [be]):1|(was):2) $DateAbsolute * ((is|will [be]):1|(was):2) * $DateRelative *
        q: * [and] * $DateRelative * || fromState = "../Current date"
        q: * [and] * $DateRelative * || fromState = "../Date plus"
        q: * [and] * $DateRelative * || fromState = "../Date minus"
        q: * [and] * $DateRelative * || fromState = "../Date check today"
        q: * [and] * $DateRelative * || fromState = "../Date check other DateAbsolute"
        if: $parseTree.DateRelative && $parseTree.DateRelative[0].DateRelativeDay[0].DateWeekday
            script:
                $temp.DateWeekday = $parseTree.DateRelative[0].DateRelativeDay[0].DateWeekday[0].value;
            go!: ../Date check other DateWeekday
        elseif: ($parseTree.DateAbsolute && $parseTree.DateAbsolute[0].DateRelative && $parseTree.DateAbsolute[0].DateRelative[0].DateRelativeDay[0].value === 0) || ($parseTree.DateRelative && $parseTree.DateRelative[0].DateRelativeDay[0].value === 0)
            go!: ../Date check today
        else:
            script:
                $temp.date = toMoment($parseTree.DateRelative[0]).locale('en').format('dddd, LL');
                if ($parseTree.DateAbsolute) {
                    var is = getVerb($parseTree._Root);
                    var yn = yesOrNo($temp.date, toMoment($parseTree.DateAbsolute[0]).format('dddd, LL'));
                    $reactions.answer(yn + $parseTree.DateRelative[0].text.toLowerCase() + ' ' + is + ' ' + $temp.date);
                } else {
                    $reactions.answer($temp.date);
                }


    state: Date check other DateWeekday
        q!: * ((is|will [be]):1|(was):2) * [it] * ($DateRelative::DateAbsolute|$DateAbsolute) * $DateWeekday
        q!: * ((is|will [be]):1|(was):2) * [it] $DateWeekday * ($DateRelative::DateAbsolute|$DateAbsolute) *
        q!: * $DateWeekday *  ((is|will [be]):1|(was):2) * ($DateRelative::DateAbsolute|$DateAbsolute) *
        q!: * ($DateRelative::DateAbsolute|$DateAbsolute) * ((is|will [be]):1|(was):2) * [a] $DateWeekday
        q: * [and] * ($DateRelative::DateAbsolute|$DateAbsolute) * || fromState = "../Date check other DateWeekday"
        script:
            if ($parseTree.DateWeekday || $temp.DateWeekday) {
                var weekDay = $parseTree.DateWeekday ? $parseTree.DateWeekday[0].value : $temp.DateWeekday;
                var is = getVerb($parseTree._Root);
                if ($parseTree._Root == "1"){
                    var future = moment.unix(toFuture($parseTree.DateAbsolute[0]));
                    var yn = yesOrNo(dts_dayweekName(weekDay), future.format('dddd'));
                    $temp.weekDay = future.locale('en').format('dddd');
                    $reactions.answer(yn + $parseTree.DateAbsolute[0].text.toLowerCase() + ' ' + is + ' ' + $temp.weekDay);  
                } else {
                    var past =  moment.unix(toPast($parseTree.DateAbsolute[0]));
                    var yn = yesOrNo(dts_dayweekName(weekDay), past.format('dddd'));
                    $temp.weekDay = past.locale('en').format('dddd');
                    $reactions.answer(yn + $parseTree.DateAbsolute[0].text.toLowerCase() + ' ' + is + ' ' + $temp.weekDay);
                }
            } else {
                $temp.answer = 'It is ' + dts_dayweekName($parseTree.DateAbsolute[0].value);
                $reactions.answer($temp.answer);
            }

    state: Day of the week today
        q!: * (what's|what [is]) * ((day of * week)|weekday) * [is it] *
        script:       
            $temp.weekDay = moment(currentDate()).locale('en').format('dddd');
            $reactions.answer("Today is " + $temp.weekDay);
    
    state: Day of the week other day 
        q!: * (what|name) * [the] ((day of * week)|weekday) * [is|will be|falls] * [in|on] * $DateTime *
        script:
            $temp.weekDay = moment.unix(toFuture($parseTree.DateTime[0])).locale('en').format('dddd');
            $reactions.answer($parseTree.DateTime[0].text.charAt(0).toUpperCase() + $parseTree.DateTime[0].text.slice(1) + " is " + $temp.weekDay);


    state: Day of the week past 
        q!: * (what|name) * [the] ((day of * week)|weekday) * was * [in|on] * $DateTime *
        script:
            $temp.weekDay = moment.unix(toPast($parseTree.DateTime[0])).locale('en').format('dddd');
            $reactions.answer($parseTree.DateTime[0].text.charAt(0).toUpperCase() + $parseTree.DateTime[0].text.slice(1) + " was " + $temp.weekDay);

    state: Time plus
        q!: * $whatTheTime * [is|will * be|it 'll be] * ($TimeRelative|$TimeAbsolute::TimeRelative) * [from now] *
        q!: * (what|what 's) * (is|will * be|it 'll be) * [time] * ($TimeRelative|$TimeAbsolute::TimeRelative) * [from now] * 
        q: * [аnd] * ($TimeRelative|$TimeAbsolute::TimeRelative) || fromState = "../Current time"
        q: * [аnd] * ($TimeRelative|$TimeAbsolute::TimeRelative) || fromState = "../Time plus"
        q: [а] * ($DateTimeRelative|$TimeRelative) || fromState = "../Time minus"
        script: 
            $reactions.answer(toMoment($parseTree.TimeRelative[0]).locale('en').format('h:mm a'));

    state: Date plus
        q!: * what {[the] $KindOfDate (is|'s|will * be|(it 'll|it will|it'll) be) * ($DateRelative::DateTimeRelative|$DateTimeRelative)} *
        q!: * what {[the] $KindOfDate [is|'s|will * be|(it 'll|it will|it'll) be] * in * ($DateRelative::DateTimeRelative|$DateTimeRelative)} *
        script:
            if ($parseTree.DateTimeRelative && $parseTree.DateTimeRelative[0].value && $parseTree.DateTimeRelative[0].value.days === 0) {
                $reactions.transition('../Current date');
            } else {
                $temp.date = calculateDate(moment.unix(toFuture($parseTree.DateTimeRelative[0])), $parseTree.KindOfDate[0].value);
                $reactions.answer("It'll be " + $temp.date);
            } 

    state: Time minus
        q!: * $whatTheTime * (is|was) * $DateTimeRelative * [(before:-1)] [[from] (now|present)] *
        script: 
            $reactions.answer(toMoment($parseTree.DateTimeRelative[0]).locale('en').format('h:mm a'));

    state: Date minus
        q!: * what * $KindOfDate was * $DateRelative * [(before:-1)] [[from] (now|present)] *
        script: 
            $reactions.answer("It was " + calculateDate(moment.unix(toPast($parseTree.DateRelative[0])), $parseTree.KindOfDate[0].value));

    state: Time till 
        q!: * (how much|how long|how many) * [time|it] * [do * have] (is|it 's|(will be):1) [left] [till|until|before] * $DateTimeAbsolute * 
        q!: * (how much|how long|how many) * [time|it] * [do * have] [is|it 's|(will be):1] [left] (till|until|before) * [$DateTimeAbsolute] *   
        script:
            if ($parseTree.DateTimeAbsolute){
                $temp.interval = calculateDateTime($parseTree.DateTimeAbsolute[0], "future");
                if ($parseTree._Root != "1") {
                    $temp.interval = $temp.interval.replace("in","");
                }
                $reactions.answer("About " +  $temp.interval);
            } else {
                $reactions.answer("Sorry, I don't know"); 
            }    
    
    state: Time after
        q!: * (how (much|long)) * [it 's|it is|time] [has] [it] [been] [passed] (since:1|after:1) * [$DateTimeAbsolute] *
        q!: * how long * ago * was * [$DateTimeAbsolute] *
        script:
            if ($parseTree.DateTimeAbsolute){
                $temp.interval = calculateDateTime($parseTree.DateTimeAbsolute[0], "past");
                if ($parseTree._Root == "1") {
                    $temp.interval = $temp.interval.replace("ago","");
                }
                $reactions.answer("About " + $temp.interval);
            } else {
                $reactions.answer("Sorry, I don't know");
            }    

    state: Timezone comparison
        q!: * [what is|what 's|name|how big] * time difference [between] * ($Where|$AnyWord) [and|to] * ($Where|$AnyWord)
        q!: * difference * time * ($Where|$AnyWord) * ($Where|$AnyWord)
        q: * [and] * (between|difference) ($Where|$AnyWord) * ($Where|$AnyWord) || fromState = "../Timezone comparison"
        if: $parseTree.Where
            if: Object.keys($parseTree.Where).length == 2        
                script:
                    $session.Where1 = $parseTree.Where[0].value;
                    $session.Where2 = $parseTree.Where[1].value;   
                    DateTime.timezoneSearch($session.Where1.lat, $session.Where1.lon).then(function (res){
                        if (res && res.status === "OK"){
                            $session.gmtOffset1 = res.gmtOffset;
                            DateTime.timezoneSearch($session.Where2.lat, $session.Where2.lon).then(function (res){
                                if (res && res.status === "OK"){
                                    $session.gmtOffset2 = res.gmtOffset;
                                    $temp.comparison = comparisonTimezone($session.gmtOffset2, $session.gmtOffset1);
                                    if ($temp.comparison != "No time difference") {
                                        $reactions.answer("Time difference between " + $session.Where1.name + " and " + $session.Where2.name + " is " + $temp.comparison);
                                    } else {
                                        $reactions.answer($temp.comparison);
                                    }
                                } else {
                                    $reactions.answer("I don't know.");
                                }
                            }).catch(function (err) {
                                $reactions.answer("I don't know.");
                            });  
                        } else {
                            $reactions.answer("I don't know.");
                        }
                    }).catch(function (err) {
                        $reactions.answer("I don't know.");
                    });  
            else:
                script:
                    $reactions.answer("Sorry, I can't recognize cities' names ");
        else:
            script:
                $reactions.answer("Sorry, I can't recognize cities' names");           

    state: Timezone calculator
        q!: * $whatTheTime * ([in] $Where::Where2|in $AnyWord) $when * {([$itIs|is] $TimeAbsolute) ([in] $Where::Where1|in $AnyWord)} *
        q!: * [$when] * {([$itIs|is] $TimeAbsolute) ([in] $Where::Where1|in $AnyWord)} * $whatTheTime * ([in] $Where::Where2|in $AnyWord) *
        if: $parseTree.Where1 
            if: $parseTree.Where2
                script:
                    $session.Where1 = $parseTree.Where1[0].value;
                    $session.Where2 = $parseTree.Where2[0].value;
                    $session.time1 = toMoment($parseTree.TimeAbsolute[0]);    
                    DateTime.timezoneSearch($session.Where1.lat, $session.Where1.lon).then(function (res){
                        if (res && res.status === "OK"){
                            $session.gmtOffset1 = res.gmtOffset;                    
                            DateTime.timezoneSearch($session.Where2.lat, $session.Where2.lon).then(function (res){
                                if (res && res.status === "OK"){
                                    $session.gmtOffset2 = res.gmtOffset;
                                    $session.time2 = calculatorTimezone($session.gmtOffset1, $session.gmtOffset2, $session.time1);
                                    var format1 = (moment($session.time1).get('minute') == 0) ? 'h a' : 'h:mm a'
                                    var format2 = (moment($session.time2).get('minute') == 0) ? 'h a' : 'h:mm a'
                                    $temp.time1f = moment($session.time1).locale('en').format(format1);
                                    $temp.time2f = moment($session.time2).locale('en').format(format2);
                                    $reactions.answer("When it's " + $temp.time1f + " in " + $session.Where1.name + ", it's " + $temp.time2f + " in " + $session.Where2.name);         
                                } else {
                                    $reactions.answer("I don't know.");
                                }
                            }).catch(function (err) {
                                $reactions.answer("I don't know.");
                            });
                        } else {
                            $reactions.answer("I don't know.");
                        }
                    }).catch(function (err) {
                        $reactions.answer("I don't know.");
                    });
            else:
                script:
                    $reactions.answer("Sorry, I can't recognize cities' names ");
        else:
            script:
                $reactions.answer("Sorry, I can't recognize cities' names");   

    state: City
        q: $City
        script:
            $reactions.answer("This city is located in " + $City[0].value.country);