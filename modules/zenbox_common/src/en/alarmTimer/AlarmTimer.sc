require: ../../common/common.sc
require: ../../common/reminder/Reminder.js

require: common.js
  module = zb_common

require: dateTime/dateTimeEn.sc
  module = zb_common

require: patternsEn.sc
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
        ([at|on|every] weekday* [days]|[at|on|every] week day*): 1 |
        ([at|on|every] weekend* [days]|[at|on|every] week end*): 2 |
        (every (day|morning|evening|afternoon)|daily): 3
        ) || converter = $converters.numberConverterValue

    $recurrenceWeekdays = ((every/on) $DateWeekday [and] [every*/on] [$DateWeekday] | [every/on] $DateWeekday [and] [every*/on] $DateWeekday | on $DateWeekdays::DateWeekday) [and] [every*/on] [$DateWeekday] [and] [every*/on] [$DateWeekday] [and] [every*/on] [$DateWeekday]

theme: /Alarm and Timer

    state: Set an alarm
        q!: * $DateTime * [remind [me] [about|to]] (wake|get) [me] up * [in|at|on] $DateTime *
        q!: * [remind [me] [about|to]|can you] wake [me] * [up] [in|at|on] * [$DateTime] *
        q!: * [remind [me] [about|to]] get [me] up * [in|at|on] * [$DateTime] *
        q!: * [create|set|put|(turn|switch|put) on] * (alarm*|(wakeup|wake up) call) [at|for] * [$DateTime] *
        q!: * alarm [create|set|put] * [at|for] * [$DateTime] *
        q!: * [in|at|on] [$DateTime] * wake [me] * [up] *
        q!: * [at|for] [$DateTime] * [create|set|put|(turn|switch|put) on] * alarm *
        q: * $DateTime || fromState = "../Set an alarm"
        q: * $DateTime * || fromState = "../Set an alarm/WhatTime"
        script:
            if ($session.alarmTime && $session.alarmCheck && $parseTree.DateTime) {
                $temp.alarmTime = mergeTwoDateTime($session.alarmTime, $parseTree.DateTime[0]);
                delete $session.alarmTime;
                delete $session.alarmCheck;
            }
            if ($parseTree.DateTime && $parseTree.DateTime[1]){
                $parseTree.DateTime[0] = mergeTwoDateTime($parseTree.DateTime[0], $parseTree.DateTime[1]);
            }
            if ($parseTree.DateTime) {
                $session.alarmTime = $parseTree.DateTime[0];
                if (!$temp.alarmTime) {$temp.alarmTime = $session.alarmTime;}
            }
        if: ($temp.alarmTime != undefined && ($temp.alarmTime.value.hours != undefined || $temp.alarmTime.value.minutes != undefined || $temp.alarmTime.value.hour != undefined))
            script:
                $session.timer = toFuture($temp.alarmTime);
                $temp.isPast = isPast($temp.alarmTime);
                $session.reminderSummary = selectRandomArg($AlarmAnswers["alarm text"]);
            if:   $temp.isPast 
                a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["inPast"]) }}
            else:
                script: 
                    $temp.time = setAlarm('addAlarm','en','h:mm a','h:mm a, LL');
                    delete $session.timer;
                    delete $session.reminderSummary;                    
                a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["successfully set"]) }}
        else:
            script:
                $session.alarmCheck = true;
            go!: ../Set an alarm/WhatTime
            
            
        state: Remove the current alarm
            q: * [no|not|wrong|do n't|stop|wait|false] (cancel|delete|switch off|remove) * [alarm] || fromState ="../../Set an alarm"
            script:
                delete $client.id_to_info[$session.id];
                $response.action = "deleteAlarm";
                $response.id = $session.id;
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["Remove the current alarm"]) }}
            
            
        state: WhatTime
            a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["WhatTime"]) }} || question = true

            state: Unknown time for an alarm || noContext = true
                q: [in|at|on] *
                script:
                    $session.alarmTime = undefined;
                a: {{ selectRandomArg($AlarmAnswers["Set an alarm"]["Unknown time for an alarm"]) }} || question = true

    state: Remove an alarm
        q!: * (remove*|cancel*|delete*|disable*|disconnect*|depose|(turn|switch|put) off| i (do n't|dont|do not|would n't) (need|want)) * {(alarm*|wake up call) * [$DateTime]} *
        q!: * (do not|do n't|dont|would n't|depose) * wake [me] up * [$DateTime] *
        q!: * (turn|switch|put) * [my|the] alarm* (off|out|down) *
        q: * $DateTime * || fromState = "../Remove an alarm/WhatTime"
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
                q: [in|at|on] *
                script:
                    $session.alarmTime = undefined;
                a: {{ selectRandomArg($AlarmAnswers["Remove an alarm"]["Unknown time for an alarm"]) }} || question = true 

    state: Remove all alarms
        q!: * {(cancel*|switch off|turn off|remove|don't need) * all alarms } *
        a: {{ selectRandomArg($AlarmAnswers["Remove all alarms"]) }}
        
    state: Thanks
        q: (thank*|ok|okay|o'kay)
        q!: thank* [you] for waking * up [this morning] *
        a: {{ selectRandomArg($AlarmAnswers["Thanks"]) }}

    state: Set a recurring alarm 
        q!: * (wake|get) * [me] [up] * [at|for] [($TimeAbsolute|$Number::TimeAbsolute)] * ($recurrenceWeekdays/$TimeWeekEnd) *
        q!: * ($recurrenceWeekdays/$TimeWeekEnd) [on] (wake|get) * [me] [up] * [at|for] [($TimeAbsolute|$Number::TimeAbsolute)] *
        q!: * (wake|get) * [me] * ($recurrenceWeekdays/$TimeWeekEnd) * [at|for] [($TimeAbsolute|$Number::TimeAbsolute)] *
        q!: * (create|set|put) * alarm [at|for] [($TimeAbsolute|$Number::TimeAbsolute)] * ($recurrenceWeekdays/[at|on|every] $TimeWeekEnd) *
        q!: * (create|set|put) * daily alarm [at|for] [($TimeAbsolute|$Number::TimeAbsolute)] *
        a: {{ selectRandomArg($AlarmAnswers["Set a recurring alarm"]["a1"]) }}
        a: {{ selectRandomArg($AlarmAnswers["Set a recurring alarm"]["random"]) }}


    state: Check an alarm
        q!: * [what] alarm* * ((do you|do i) * have |are (there|set|on)) *
        q!: * what time* * ((do|should) you|(do|shall|should) i|are you going) * (wake|get) [me] * [up] *
        q!: * what time* * is * [my] (alarm|(wake|get) up call) * [set|put|(turn|switch|put) on] *
        q!: * (name|check|list|tell [me]|what) * [about] [all] [my] [current] * alarm* * 
        q: * active* * alarm*|| fromState = "../Check an alarm"
        q: * active* * alarm*|| fromState = "../Set an alarm"
        a: {{ selectRandomArg($AlarmAnswers["Check an alarm"]) }}

    state: Set a timer
        q!: * (put|set|start) * timer [for|on] [$DateTimeRelative::DateTime] *
        q!: * (put|set|start) * [$DateTimeRelative::DateTime] * timer *
        q: * $DateTimeRelative::DateTime *|| fromState = "../Set a timer"
        if: ($parseTree.DateTime)
            script:
                $session.timer = toFuture($parseTree.DateTime[0]);
                $session.reminderSummary = selectRandomArg($AlarmAnswers["timer text"]);
            if: ($session.timer < currentDate().format('X'))
                a: {{ selectRandomArg($AlarmAnswers["Set a timer"]["inPast"]) }}
            else:
                script:
                    setAlarm('addAlarm','en','h:mm a','h:mm a, LL');
                    delete $session.timer;
                    delete $session.reminderSummary;
                a: {{ selectRandomArg($AlarmAnswers["Set a timer"]["successfully set"]) }}
        else:   
            a: {{ selectRandomArg($AlarmAnswers["Set a timer"]["undefined time"]) }} || question = true
   

        state: Remove the current timer
            q: * [no] (no|not|wrong|do n't|stop|wait|false|cancel|delete|switch off|remove) * [timer] || fromState ="../../Set a timer"
            script:
                delete $client.id_to_info[$session.id];
                $response.action = "deleteAlarm";
                $response.id = $session.id;
            a: {{ selectRandomArg($AlarmAnswers["Set a timer"]["Remove the current timer"]) }}
            
    state: Check timers
        q!: * [what] timer* * ((do you|do i) have |are (there|set|on)) *
        q!: * ([do] you have|((you've|have (you|i)) got)) * timer* *
        q!: * (name|check|list|tell|any) [all] [my] * timer* *
        a: {{ selectRandomArg($AlarmAnswers["Check timers"]) }}

    state: Remove a timer    
        q!: * (remove|cancel|delete|(turn|switch|put) off|stop) * [my|the] timer*  *
        q!: * (turn|switch|put) * [my|the] timer* (off|out|down) *
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
        q!: * (put|set|start) * stopwatch *
        q!: * note [the] time *
        a: {{ selectRandomArg($AlarmAnswers["Set a stopper"]) }}

    state: Stop a stopper
        q!: * (remove|cancel|delete|(turn|switch|put) off|stop) * [my|the] stopwatch*  *
        q!: * (turn|switch|put) * [my|the] stopwatch* (off|out|down) *
        a: {{ selectRandomArg($AlarmAnswers["Stop a stopper"]) }}

    state: Why did not alarm
        q!: * why * {$you * (did/do) (not/n't)} (wake/woke) me [up]*
        q!: * [why] {alarm * [$DateTime] * ((did n't|did not|didnt) go off)} *
        q!: * why * {$you * (did|do) (not|n't)} (create|set|put|(turn|switch|put) on) * (alarm*|(wakeup|wake up) call) *
        a: {{ selectRandomArg($AlarmAnswers["Why did not alarm"]) }}