require: ../../common/common.sc
require: ../../common/reminder/Reminder.js
require: ../../common/alarmTimer/alarmTimer.js

require: dateTime/dateTime.sc
  module = zb_common

require: patterns.sc
  module = zb_common

require: ../main.js

require: answers.yaml
    var = AlarmCommonAnswers

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }
    $global.$AlarmAnswers = (typeof AlarmCustomAnswers != 'undefined') ? applyCustomAnswers(AlarmCommonAnswers, AlarmCustomAnswers) : AlarmCommonAnswers;

patterns:
    $TimeWeekEnd = (
        ([по|в|на] (будн*|рабоч*|учебн*) [дн*] |на неделе): 1 |
        ([по|в|на] ~выходной [дн*] | [по|в|кажд] ~суббота [и] ~воскресенье | [по|в|кажд] (нерабоч*|неучебн*) | [по|в|кажд] не (рабоч*|учебн*)): 2 |
        ((кажд*|по) (день|утр*|вечер*|ночь*|ночам|раз)|ежедневн*): 3
        ) || converter = $converters.numberConverterValue

    $recurrenceWeekdays = ((кажд*|по) $DateWeekday [и] [кажд*|по] [$DateWeekday] [и] [кажд*|по] [$DateWeekday] [и] [кажд*|по] [$DateWeekday] [и] [кажд*|по] [$DateWeekday]|(в|на) $DateWeekday [и] [в|на] $DateWeekday [и] [в|на] [$DateWeekday] [и] [в|на] [$DateWeekday] [и] [в|на] [$DateWeekday])

    $AMTimeAlarmPattern = (до полудня/~утро/сутра/рано/*раньш*/[час*] ~ночь/~полдень/$regexp<(0?[1-9]|1[0-2])>)
    $PMTimeAlarmPattern = (после полудня/~вечер/~вечерок/поздно/*позж*/[час*] ~день/~полночь/$regexp<(1[3-9]|2[0-4]|00?)>)

theme: /Alarm and Timer

    state: Set an alarm
        q!: * $DateTime * ((разбуди*|подними*|поднять) [меня]|[меня] (разбуди*|подними*|поднять)|(хочу|[мне] надо) (проснуться|встать)|надо [меня] будить|буди* меня) * $DateTime *             
        q!: * {((разбуди*|подними*|поднять) [меня]|[меня] (разбуди*|подними*|поднять)|(хочу|[мне] надо) (проснуться|встать)|надо [меня] будить|буди* меня) * [$DateTime]} *
        q!: * {(созда*|поставь|постави*|поставл*|настро*|заведи|установи*|включи*|завести) [мне] [свой] будильник [на] * [$DateTime]} *
        q!: * [*став*] будильник* [на] * [$DateTime] *
        q!: * хочу (что|чтоб*) * [меня] (будил*|разбудил*) * [$DateTime] *
        q!: * (смени*|измени*|поменя*) * [время] [на] будильник* [на] * $DateTime *  
        q: * $DateTime * || fromState = "../Set an alarm"
        q: * $DateTime * || fromState = "../Set an alarm/WhatTime"
        q: * $DateTime * || fromState = "../Change an alarm"
        script:
            initAlarm($parseTree);
        # Если пользователь просто сказал "поставь будильник", уточняем дату/время
        # Если пользователь назвал только дату, уточняем время (дата уже сохранена)
        if: !$parseTree.DateTime || ($temp.alarmTime == undefined || ($temp.alarmTime.value.hours == undefined && $temp.alarmTime.value.minutes == undefined && $temp.alarmTime.value.hour == undefined))
            script:
                $session.alarmCheck = true;
            go!: ../Set an alarm/WhatTime
        script:
            parseAlarm($parseTree);
        # Пользователь назвал (за один шаг или два) дату и время
        script:
            $session.timer = toMoment($temp.alarmTime).unix();
            $session.reminderSummary = selectRandomArg($AlarmAnswers["alarm text"]);
        if: toMoment($temp.alarmTime).isBefore(currentDate(), 'day')
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["inPast"]) }}

        # S1: Половина дня не указана пользователем. Текущее время - день/вечер. 
        # S1: time: 22:00; query: поставь будильник на 9: Сегодня не предлагать. 
        elseif: $session.usrWantedTimePassed && !$session.pmTimeIsAvailable && !$session.dtModifier
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"].NoonOrAfternoon.tomorrow) }}
            go: ../NoonOrAfternoon

        # S2: Половина дня не указана пользователем. Текущее время - ночь/утро. 
        # S2: time: 10:00; query: поставь будильник на 3: Предложить сегодня на вечер или завтра на утро
        elseif: $session.usrWantedTimePassed && $session.pmTimeIsAvailable && !$session.dtModifier
            script:
                var parse = $nlp.parseMorph($session.relativeTimePeriods[1]);
                var gender = _.find((_.isArray(parse)) ? parse[0].tags : parse.tags, function(item) {
                    return _.contains(["masc", "femn", "neut"], item);
                });
                $temp.nextPeriod = $nlp.inflect($nlp.inflect("следующий", gender), "accs") + " " + $session.relativeTimePeriods[1];
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"].TodayOrTomorrow) }}
            go: ../TodayOrTomorrow

        # S3: Установка будильника на определенное время на завтра, если определена половина дня.
        # S3: time: 10:00; query: поставь будильник на 3 утра: Ставим завтра на утро.
        # S3: time: 18:00; query: поставь будильник на 5 вечера: Ставим завтра на вечер.
        elseif: $session.usrWantedTimePassed && $session.dtModifier
            script: 
                $session.timer = $session.usrWantedTimePassed
                    ? moment.unix($session.timer).utcOffset($session.offset).add(1, 'days').unix()
                    : $session.timer;
                $temp.time = setAlarm('addAlarm','ru');                    
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["successfully set"]) }}

        # S4: Установка будильника на вторую половину дня сегодня.
        # S4: time: 18:00; query: поставь будильник на девятнадцать ноль ноль: Ставим завтра на вечер.
        elseif: !$session.usrWantedTimePassed && !$session.pmTimeIsAvailable && $session.alarmIsToday
            script: 
                $temp.time = setAlarm('addAlarm','ru');                    
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["successfully set"]) }}

        # S5: Установка будильника на неопределенное время дня. Задача - определить, на утро или на вечер ставить.
        # S5: query: Поставь завтра на десять => На 10 утра или вечера?
        elseif: !$session.dtModifier
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"].NoonOrAfternoon[($session.datesAreEqual) ? "today" : "sometime"]) }}
            go: ../NoonOrAfternoon

        # S6: Ограничивающих условий нет, вся информация предоставлена. Ставим.
        else:
            script: 
                $temp.time = setAlarm('addAlarm','ru');                    
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["successfully set"]) }}

            
        state: Remove the current alarm
            q: * [нет] (нет|не надо|неправильно|не правильно/неверно|отмен*|отмени*|не то|удали*|убери|стой|стоп|погоди*|подожди*) * [будильник] || fromState ="../../Set an alarm"
            script:
                delete $client.id_to_info[$session.id];
                $response.action = "deleteAlarm";
                $response.id = $session.id;
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["Remove the current alarm"]) }}
            
            
        state: WhatTime
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["WhatTime"]) }} || question = true

            state: Unknown time for an alarm || noContext = true
                q: [в|на] *
                script:
                    $session.alarmTime = undefined;
                a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["Unknown time for an alarm"]) }} || question = true


    state: NoonOrAfternoon
        #  Можно поставить сегодня/завтра на HH:mm или на HH+12:mm.

        state: GetAMTime
            q: * $AMTimeAlarmPattern *
            script:
                $session.timer = $session.usrWantedTimePassed 
                    ? moment.unix($session.timer).utcOffset($session.offset).add(1, 'days').unix() 
                    : $session.timer; 
                $temp.time = setAlarm('addAlarm','ru');
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["successfully set"]) }}
            go: /Alarm and Timer/Set an alarm


        state: GetPMTime
            q: * $PMTimeAlarmPattern *
            script: 
                $session.timer = $session.usrWantedTimePassed 
                    ? moment.unix($session.timer).utcOffset($session.offset).add(36, 'hours').unix() 
                    : moment.unix($session.timer).utcOffset($session.offset).add(12, 'hours').unix(); 
                $temp.time = setAlarm('addAlarm','ru');
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["successfully set"]) }}
            go: /Alarm and Timer/Set an alarm

        state: LocalCatchAll
            q: * 
            a: Назови, пожалуйста, точное время, когда ставить будильник.

    state: TodayOrTomorrow
        # Можно поставить на HH+12:mm или на следующее утро/день/вечер/ночь на HH+24:mm.

        state: Today
            q: * (сегодн*/сеня/седня/~день/днем) *
            q: * $PMTimeAlarmPattern *
            script: 
                $session.timer = moment.unix($session.timer).utcOffset($session.offset).add(12, 'hours').unix();
                $temp.time = setAlarm('addAlarm','ru');                  
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["successfully set"]) }}
            go: /Alarm and Timer/Set an alarm

        state: Tomorrow
            q: * завтр* *
            q: * $AMTimeAlarmPattern * 
            script: 
                $session.timer = moment.unix($session.timer).utcOffset($session.offset).add(1, 'days').unix() 
                $temp.time = setAlarm('addAlarm','ru');     
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["successfully set"]) }}
            go: /Alarm and Timer/Set an alarm

        state: LocalCatchAll
            q: * 
            a: Назови, пожалуйста, точное время, когда ставить будильник.

    state: Change an alarm
        q!: * (смени*|измени*|поменя*) * [время] [на] будильник* *
        q: * (смени*|измени*|поменя*) * || fromState = "../Set an alarm"
        if: $session.timer
            script:
                $temp.time = moment.unix($session.timer).utcOffset($session.offset).locale('ru').format('LT, D MMMM');
            a: {{ selectRandomArg($AlarmAnswers["Change an alarm"]) }}
        else:
            a: {{ selectRandomArg($AlarmAnswers["Remove an alarm"]["no alarms"]) }}

    state: Remove an alarm
        q!: * {(отмени*|отключи*|выключи*|выруби*|забуд*|удали*|не нужен) * будильник* } * [$DateTime]
        q!: * { не буди [меня] * [$DateTime]}
        q!: * { { (не (надо|нужно)) [меня] будить } * [$DateTime]}
        q!: * не хочу (что|чтоб*) * [меня] будил* * [$DateTime]
        q: * $DateTime * || fromState = "../Remove an alarm/WhatTime"
        q: отменен на $DateTime
        if: ($parseTree.DateTime)
            script:
                var timer = toFuture($parseTree.DateTime[0]).toString();
                removeAlarm('deleteAlarm', 'alarm', timer, selectRandomArg($AlarmAnswers["Remove an alarm"]["successfully removed"]), selectRandomArg($AlarmAnswers["Remove an alarm"]["failed to remove"]));
        else:
            if: ($client.id_to_info && (Object.keys($client.id_to_info).length != 0))
                if: (Object.keys($client.id_to_info).length == 1)
                    script:
                        $response.action = "deleteAlarm";
                        $response.id = parseInt(Object.keys($client.id_to_info)[0]);
                        delete $client.id_to_info[$response.id];
                    a: {{ selectRandomArg($AlarmAnswers["Remove an alarm"]["successfully removed"]) }}
                else: 
                    go!: ../Remove an alarm/WhatTime            
            else:
                a: {{ selectRandomArg($AlarmAnswers["Remove an alarm"]["no alarms"]) }}
        
        state: WhatTime
            a: {{ selectRandomArg($AlarmAnswers["Remove an alarm"]["WhatTime"]) }} || question = true

            state: Unknown time for an alarm || noContext = true
                q: [в|на] *
                script:
                    $session.alarmTime = undefined;
                a: {{ selectRandomArg($AlarmAnswers["Remove an alarm"]["Unknown time for an alarm"]) }} || question = true 

    state: Remove all alarms
        q!: * {(отмени*|отключи*|выключи*|выруби*|забуд*|удали*|не нужны) * все будильник* } *
        a: {{ selectRandomArg($AlarmAnswers["Remove all alarms"]) }}
        
    state: Thanks
        q: (спасибо|отлично|супер|ок|окей|оки)
        q!: спасибо что разбудил
        a: {{ selectRandomArg($AlarmAnswers["Thanks"]) }}

    state: Set a recurring alarm 
        q!: * ((разбуди*/буди*) [меня]|(хочу|[мне] надо) (просыпат*|встать|вставать)) * [$TimeAbsolute] * ($TimeWeekEnd/$recurrenceWeekdays) *
        q!: * ($TimeWeekEnd/$recurrenceWeekdays) * ((разбуди*/буди*) * [меня]|(хочу|[мне] надо) (просыпат*|встать|вставать)) * [$TimeAbsolute] *
        q!: * ((разбуди*/буди*) [меня]|надо [меня] буди*|(хочу|[мне] надо) (просыпат*|встать|вставать)) * ($TimeWeekEnd/$recurrenceWeekdays) * [$TimeAbsolute] *
        q!: * (созда*|поставь|постави*|поставл*|заведи|установи*|включи*) * {будильник [на|в] [$TimeAbsolute] [кажд*|по|в] ($TimeWeekEnd/$recurrenceWeekdays) }
        a: {{ selectRandomArg($AlarmAnswers["Set a recurring alarm"]["a1"]) }}
        a: {{ selectRandomArg($AlarmAnswers["Set a recurring alarm"]["random"]) }}


    state: Check an alarm
        q!: * {[как*|насколько] будильник* [(у меня|у тебя)] (поставл*|установл*|задан*|работают|работает|стоит|стоят) }
        q!: * (перечисли*|провер*|назови|список|скажи|покажи|найди|найти|открой|открыть|$tellMe) * [(поставл*|активн*|установл*|задан*|мой|мои*)] * будильник* *
        q!: * {(во сколько/в какое время) ты [$AnyWord] меня разбудишь} * 
        q: * активн* * будильник*|| fromState = "../Check an alarm"
        q: * активн* * будильник*|| fromState = "../Set an alarm"
        a: {{ selectRandomArg($AlarmAnswers["Check an alarm"]) }}

    state: Set a timer
        q!: * {[(поставь|[можешь] постави*|поставл*|заряди*|запусти*|заведи|включи*|засечь|засеки|засечешь|установи*)] * [$pls] таймер [на] [$DateTimeRelative::DateTime]} *
        q!: * {(отсчита*|замер*|засеки*|засечь|засечешь) [$pls] $DateTimeRelative::DateTime}
        q!: * {[*скажи*] [мне] (когда (пройдут|пройдет)) [$pls] $DateTimeRelative::DateTime}
        q: * $DateTimeRelative::DateTime *|| fromState = "../Set a timer"
        if: ($parseTree.DateTime)
            script:
                $session.timer = toFuture($parseTree.DateTime[0]);
                $session.reminderSummary = selectRandomArg($AlarmAnswers["timer text"]);
            if: ($session.timer < currentDate().format('X'))
                a: {{ selectRandomArg($AlarmAnswers["Set a timer"]["inPast"]) }}
            else:
                script:
                    setAlarm('addAlarm','ru','LT','LT, D MMMM');
                    delete $session.timer;
                    delete $session.reminderSummary;
                a: {{ selectRandomArg($AlarmAnswers["Set a timer"]["successfully set"]) }}
        else:   
            a: {{ selectRandomArg($AlarmAnswers["Set a timer"]["undefined time"]) }} || question = true
   

        state: Remove the current timer
            q: * [нет] (нет|не надо|неправильно|не правильно/неверно|отмен*|не то|удали*|убери|стоп|стой|прекрати|останови*|отмени*|выруб*) * [таймер]|| fromState ="../../Set a timer"
            script:
                delete $client.id_to_info[$session.id];
                $response.action = "deleteAlarm";
                $response.id = $session.id;
            a: {{ selectRandomArg($AlarmAnswers["Set a timer"]["Remove the current timer"]) }}
            
    state: Check timers
        q!: * { [как*] таймер* * (поставл*|установл*|задан*|работают|заведены|стоят) [у тебя|у меня] }
        q!: * { ((через|во) сколько [времени]|когда) * [будет|должен] (сработа*|срабат*|заработа*|запустит*|запускать*|прозвонит*) таймер* }
        q!: * (перечисли|назови|провер*|список|{у меня есть}) * [(поставл*|установл*|задан*|актуальн*|мои*|работающ*|завед*)] таймер*
        a: {{ selectRandomArg($AlarmAnswers["Check timers"]) }}

    state: Remove a timer    
        q!: * {(убери|убирай|убрать|удал*|выключ*|отключ*|прекрати|остан*|выруб*|не нужен|отмен*) * [$pls] таймер*} *
        if: ($client.id_to_info && (Object.keys($client.id_to_info).length != 0))
            if: (Object.keys($client.id_to_info).length == 1)
                script:
                    $response.action = "deleteAlarm";
                    $response.id = parseInt(Object.keys($client.id_to_info)[0]);
                    delete $client.id_to_info[$response.id];
                a: {{ selectRandomArg($AlarmAnswers["Remove a timer"]["successfully removed"]) }}
            else:
                a: {{ selectRandomArg($AlarmAnswers["Remove a timer"]["remove in app"]) }}
        else:
            a: {{ selectRandomArg($AlarmAnswers["Remove a timer"]["no timers"]) }}
        

    state: Set a stopper
        q!: * { [включи*|запусти*|поставь*|постави*|заряди*|заведи|завести] * секундомер* } *
        q!: * {(засеки*|засечь) * время} *
        a: {{ selectRandomArg($AlarmAnswers["Set a stopper"]) }}

    state: Stop a stopper
        q!: * { [выключи*|отключи*|убери|убирай|убрать|удал*|отмен*|выруби*|прекрати|сбрось|сбросить|не нужен|останови*] * секундомер* } *
        a: {{ selectRandomArg($AlarmAnswers["Stop a stopper"]) }}

    state: Why did not alarm
        q!: * (почему/что же) * ты * не разбуди* *
        a: {{ selectRandomArg($AlarmAnswers["Why did not alarm"]) }}

    state: Stop
        q: * ($stopGame/передумал*) * || fromState = "../NoonOrAfternoon"
        q: * ($stopGame/передумал*) * || fromState = "../TodayOrTomorrow"
        q: * ($stopGame/передумал*) * || fromState = "../Set an alarm/WhatTime"
        a: {{selectRandomArg($AlarmAnswers["Stop"])}}
