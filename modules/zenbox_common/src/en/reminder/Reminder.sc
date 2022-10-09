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
    var = ReminderCommonAnswers
    
init:
    $global.$ReminderAnswers = (typeof ReminderCustomAnswers != 'undefined') ? applyCustomAnswers(ReminderCommonAnswers, ReminderCustomAnswers) : ReminderCommonAnswers;


patterns:

    $timeInterval = (
        [(in|at|for) [the]] morning: 1|
        [(in|at|for) [the]] (noon|afternoon): 2|
        [(in|at|for) [the]] (evening|night|tonight): 3|
        [in|at|for] (today|this day|present day): 4|
        ([in|at|for] [the] [this|present*] week*): 5|
        ([in|at|for] [the] next week*): 6)

    $setReminder =  ((remind [me]|[set [a]] reminder| to do [list]| task |(write|rememder|note|notification) * that [it ['s]|I ['m]|$AnyWord ['s]] [need/necessary/must/have [to]/has [to]]|([create|make|do|write|set|put|add|new] [me] [a/the/up] (reminder/task/note/to do|notification))) [* about] [* that [it ['s]|I ['m]|$AnyWord ['s]] [need/necessary/must/have [to]/has [to]]])   

    $severalDates = ($DateTime and $DateTime / (every/each/all [the]) $DateTime / {($DateRelative|$DateTimeRelative) * $DateAbsolute} / (every/each) day)

    $reminderAction = [to] (call/dial/meet/phone/connect/message/send) $Text

theme: /Reminder

    state: Set a reminder
        q!: * (remind * [me]|[create|make|put|set] * reminder*) * [for me] * [to|about|of|for] $Text [for [a] [date]|at] $DateTime * 
        q!: * (remind * [me]|[create|make|put|set] * reminder*) * [for [a] [date]|at] $DateTime * [for me] * [to|about|of] $Text
        q!: * $DateTime * (remind * [me]|[create|make|put|set] * reminder*) * [for me] * [to|about|of|for] $Text
        q!: * {$setReminder * $DateTime} [[that] [me/I ['m]] ['ll/will] (going/must|(have/has) to|need)|about] ($Text/$reminderAction::Text)
        q!: * $setReminder [[that] [me/I ['m]] ['ll/will] (going/must|(have/has) to|need)|about] ($Text/$reminderAction::Text) [at/on/in] [the] $DateTime *
        q!: * plan* [me/my/i] {($Text/$reminderAction::Text) ([at/on/in] [the] $DateTime)} *
        q!: * [at/on/in] [the] $DateTime plan* [me/my/i] ($Text/$reminderAction::Text) *
        q!: * $DateTime tell [me] [that] ($Text/$reminderAction::Text)
        script:
            if ($parseTree.DateTime) {
                $session.timer = toFuture($parseTree.DateTime[0]);
                $temp.isPast = isPast($parseTree.DateTime[0]);
            }
            if ($parseTree.Text) {
                $session.reminderSummary = $parseTree.Text[0].text;
            }
        if: $temp.isPast
            script:
                $session.reaction = selectRandomArg($ReminderAnswers["Set a reminder"]["inPast"]);
            go!: ../Set a reminder no time
        else:
            script:
                $temp.time = setAlarm('addReminder','en','h:mm a','h:mm a, LL');
            a: {{ selectRandomArg($ReminderAnswers["Set a reminder"]["correctlySet"]) }}
            script:
                delete $session.timer;
                delete $session.reminderSummary;

        state: Cancel the reminder
            q: * (cancel|delete|switch off|remove|wrong) * [remind*] [please]|| fromState = "../../Set a reminder"
            script:
                delete $client.id_to_info[$session.id];
                $response.action = "deleteReminder";
                $response.id = $session.id;
            a: {{ selectRandomArg($ReminderAnswers["Cancel the reminder"]) }}
            go!: /Reminder

        state: Fix the reminder
            q: * [no] (no|not|wrong|do n't|stop|wait|false) [please] *|| fromState ="../../Set a reminder"
            a: {{ selectRandomArg($ReminderAnswers["Fix the reminder"]) }}
            go!: /Reminder 

    state: Thanks
        q: * thank* [you] *
        a: {{ selectRandomArg($ReminderAnswers["Thanks"]) }}

    state: Edit the reminder
        q: * [no|not|wrong|cancel|do n't|stop|wait|false] * (no|not|wrong|change|incorrect|(n't/not) correct|modify|edit) ($Text/* $DateTime */remind*/* )
        q: * {($Text/* $DateTime */remind*/(time/date)) (cancel|stop|wrong|change|incorrect|(n't/not) correct|modify|edit)} *
        q!: * (reset/postpone/delay) [reminder] at * $DateTime *
        a: {{ selectRandomArg($ReminderAnswers["Edit the reminder"]) }}

    state: Set a reminder no todo no time
        q!: $setReminder 
        q!: * (remind * [me]|[create|make|put|set] * reminder*) * [for me] *
        a: {{ selectRandomArg($ReminderAnswers["Set a reminder no todo no time"]) }}

    state: Set a reminder no time 
        q!: * $setReminder ($Text/$reminderAction::Text)
        q!: * (remind * [me]|[create|make|put|set] * reminder*) * [to|about|of|for] $Text
        script:
            if ($session.reaction) {
            } else {
                $session.reaction = "";
            }
            $session.reminderSummary = $parseTree.Text[0].text;
            $response.text = $session.reminderSummary;
        a: {{ selectRandomArg($ReminderAnswers["Set a reminder no time"]) }}

        state: Set reminder time
            q: * ($DateTime/$severalDates) * || fromState ="../../Set a reminder no time"
            script:
                if ($parseTree.severalDates) {
                    $temp.nextState = "../../Recurring reminder"
                } else {
                    $temp.nextState = "../../Set a reminder"
                }
            go!: {{ $temp.nextState }}

        state: Set reminder time cancel
            q: * $disagree * || fromState ="../../Set a reminder no time", onlyThisState = true
            a: {{ selectRandomArg($ReminderAnswers["Set reminder time cancel"]) }}

        state: Set reminder time error
            q: * || fromState ="../../Set a reminder no time"
            a: {{ selectRandomArg($ReminderAnswers["Set reminder time error"]) }}

    state: Set a reminder no todo || modal = true
        q!: * $setReminder * $DateTime
        q!: * create a reminder for * $DateTime
        script:
            checkOffset();
            $session.timer = toFuture($parseTree.DateTime[0]);
        a: {{ selectRandomArg($ReminderAnswers["Set a reminder no todo"]) }}
        
        state: Set reminder todo
            state: Set reminder todo set
                q: ([* (to|about|of)] $Text) [$DateTime *] || fromState = ../../, onlyThisState = true
                q: ([* (to|about|of)] $Text) [$DateTime *] || fromState = "/Reminder/Set a reminder no todo no time", onlyThisState = true
                q: [* $DateTime] $Text  || fromState = ../../, onlyThisState = true
                q: [* $DateTime] $Text  || fromState = "/Reminder/Set a reminder no todo no time", onlyThisState = true
                script:
                    $session.reminderSummary = $parseTree.Text[0].value;
                if: ($session.timer || $parseTree.DateTime)
                    go!: ../../../Set a reminder
                else:
                    go!: ../../../Set a reminder no time        

            state: Set reminder todo cancel
                q: * nothing * || fromState = ../../, onlyThisState = true
                q: * nothing * || fromState = "/Reminder/Set a reminder no todo no time", onlyThisState = true
                q: $disagree || fromState = ../../, onlyThisState = true
                q: $disagree || fromState = "/Reminder/Set a reminder no todo no time", onlyThisState = true
                a: {{ selectRandomArg($ReminderAnswers["Set reminder todo cancel"]) }}
                script:
                    delete $session.timer;
                go!: /Reminder

    state: Delete a reminder no time no summary
        q!: * (remove/delete/cancel/turn off/disable/ i (do n't/dont/do not/would n't) (need/want)) [the] [my/this] (reminder|task|notification) [please]
        q: * (remove/delete/cancel/turn off/disable/ i (do n't/dont/do not/would n't) (need/want)) [the] [my/this] (reminder|task|notification) [please] || fromState = "../Set a reminder"
        q: * (remove/delete/cancel/turn off/disable/ i (do n't/dont/do not/would n't) (need/want)) [the] * || fromState="/Reminder/Edit the reminder"
        if: ($client.id_to_info && (Object.keys($client.id_to_info).length != 0))
            if: (Object.keys($client.id_to_info).length == 1)
                script:
                    $response.action = "deleteReminder";
                    $response.id = parseInt(Object.keys($client.id_to_info)[0]);
                a: {{ selectRandomArg($ReminderAnswers["Delete a reminder no time no summary"]["cancelTheReminder"]) }}
                script:    
                    delete $client.id_to_info[$response.id];
                go!: /Reminder
            else:
                a: {{ selectRandomArg($ReminderAnswers["Delete a reminder no time no summary"]["cancelAReminder"]) }}
        else:
            a: {{ selectRandomArg($ReminderAnswers["Delete a reminder no time no summary"]["doesNotFoundTheReminder"]) }}
            
    state: Delete a reminder by time
        q!: * (remove/delete/cancel/turn off/disable/ i (do n't/dont/do not/would n't) (need/want)) [the] [my] * [this] (reminder|reminders|task*|notification*) [about] * [for/at] $DateTime *  
        q: * $DateTime * || fromState = "../Delete a reminder no time no summary"
        script:
            var timer = toFuture($parseTree.DateTime[0]).toString();
            removeAlarm('deleteReminder', 'timer', timer, selectRandomArg($ReminderAnswers["Delete a reminder by time"]["success"]) , selectRandomArg($ReminderAnswers["Delete a reminder by time"]["fail"]) );

    state: Delete a reminder by summary
        q!: * (remove/delete/cancel/turn off/disable) [the] [my] * [this] (reminder|reminders|task*|notification*) [about] * ($Text/$reminderAction::Text) 
        q!: * do (not/n't) need * (reminder|task|notification) ($Text/$reminderAction::Text)
        q: $Text [$DateTime] || fromState = "../Delete a reminder no time no summary"
        q: $Text [$DateTime]
        script:
            var text = $parseTree.Text[0].value;
            removeAlarm('deleteReminder', 'summary', text, selectRandomArg($ReminderAnswers["Delete a reminder by summary"]["success"]) , selectRandomArg($ReminderAnswers["Delete a reminder by summary"]["fail"]) );

    state: Delete all reminders
        q!: * {(remove/delete/cancel/turn off/disable/ i (do n't/dont/do not/would n't) (need/want)) [all] [my] [the] [this] (reminders|tasks|notifications) * [$DateTime] } *
        a: {{ selectRandomArg($ReminderAnswers["Delete all reminders"]) }}
    
    state: Reminders list
        q!: * [what*| show [me]] reminder* * (do (you|i) have|are (there|set|on)) *
        q!: * how [do] [i|you] (put|set|create|make) * [my] reminder* *
        q!: * (list|name|check|tell [me]|what| show [me]) * [about] [my] * [current] reminder*
        q!: * what [do] [i|you] (have|need|should|do) * [to] (do|remind * [me] [about]) *
        q!: * [my] (to do list|reminders) *
        q!: * (what|what's| show [me]) [is|are] [my] * (plan|plans) *[do] [i] * [have] *
        q!: * (do i have|are there|what are) * [any|my] * reminder* *
        q!: * [what*] reminder* * do (you|i) have * [for] ($DateTime|$timeInterval) *
        q!: * (list|name|check| show [me]) * [my] * reminder* [for] ($DateTime|$timeInterval) *
        q!: * what [do] [i|you] (have|need|should|do) * [to] (do|remind * [me] [about]) * ($DateTime|$timeInterval) *
        q!: * [my] (to do [list]|reminder*) * [for] ($DateTime|$timeInterval) *
        q!: * what* * [my] (to do [list]|reminder*) * [for] ($DateTime|$timeInterval) *
        q!: * (what|what's) [is|are] [my] * (plan|plans) *[do] * [i] * [have] * [for] ($DateTime|$timeInterval) *
        q!: * (do i have|are there) * [any] * reminder* * [for] ($DateTime|$timeInterval) *
        q: * what [do] [i|you] (have|need|should|do) * [to] (do|remind * [me] [about]) * ($DateTime|$timeInterval) *|| fromState = " ../Set a reminder no time"
        q: * (what|what's) [is|are] [my] * (plan|plans) *[do] * [i] * [have] * [for] ($DateTime|$timeInterval) *|| fromState = " ../Set a reminder no time"
        a: {{ selectRandomArg($ReminderAnswers["Reminders list"]) }}

    state: Snooze a reminder
        q!: * (snooze|pause|delay|postpone|put off) * [reminder] * [for] * {$DateTime [$Text]} *
        a: {{ selectRandomArg($ReminderAnswers["Snooze a reminder"]) }}

    state: Recurring reminder
        q!: * $setReminder * $severalDates [[that] [me/I ['m]] ['ll/will] (going/must|(have/has) to|need)|about] $Text
        q!: * $setReminder [[that] [me/I ['m]] ['ll/will] (going/must|(have/has) to|need)|about] $Text [at/in/on] [the] $severalDates *
        q!: * {$setReminder * $severalDates} [[that] [me/I ['m]] ['ll/will] (going/must|(have/has) to|need)|about] $Text
        q!: * [at/in/on] [the] $severalDates * plan* [me/my/i* *] $Text *
        a: {{ selectRandomArg($ReminderAnswers["Recurring reminder"]) }}