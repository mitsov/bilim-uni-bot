require: ../../common/common.sc
require: ../../common/reminder/Reminder.js

require: dateTime.sc

require: patterns.sc
  module = zb_common

require: ../main.js

require: answers.yaml
    var = ReminderCommonAnswers
    
init:
    $global.$ReminderAnswers = (typeof ReminderCustomAnswers !== "undefined") ? applyCustomAnswers(ReminderCommonAnswers, ReminderCustomAnswers) : ReminderCommonAnswers;

patterns:

    $timeInterval = (
        (утром|утречком|на (утро|утречко)): 1|
        (днём|днем|на день): 2|
        (вечером|на вечер): 3|
        (сегодня|в этот день|[на|в] ближайший день): 4|
        (на [этой|ближ*] недел*): 5|
        (на (той|след*|буд*) недел*): 6)

    $setReminder =  ((напомни* [мне]|(запиши|помни) * что [я/мне] [нужно/надо/пора/необходимо/должен/должна]|([созда*|сдела*|запиши|записать|поставь|постави*|добав*|новое|новая] [мне] напомин*)) [* про] [* что [я/мне] [нужно/надо/пора/необходимо/должен/должна]])   

    $severalDates = ($DateTime и $DateTime / ~каждый $DateTime / по $DateTime/ {($DateRelative|$DateTimeRelative) * $DateAbsolute} / каждый день / ежедневно)

    $reminderAction = ((позвонить/набрать/связаться) $Text/[$Text] (одно/один/одна/одни) $Text/$Text (одно/один/одна/одни) [$Text])

    $at = (в/на)

theme: /Reminder

    state: Set a reminder
        q!: * $setReminder * [$at] $DateTime [[(что|чтобы)] [мне] (нужно|надо будет|надо)|о том что] ($Text/$reminderAction::Text)
        q!: * $setReminder [[(что|чтобы)] [мне] (нужно|надо будет|надо)|о том что] ($Text/$reminderAction::Text) [$at] $DateTime *
        q!: * $setReminder * [$at] $DateTime [про] [что] [[мне] (нужно|надо будет|надо)] ($Text/$reminderAction::Text)
        q!: * [$at] $DateTime * $setReminder [про] [что] [[мне] (нужно|надо будет|надо)] ($Text/$reminderAction::Text)
        q!: * (*планируй*|*планировать) [мне] ($Text/$reminderAction::Text) [$at] $DateTime *
        q!: * (*планируй*|*планировать) [мне] [в|на] $DateTime ($Text/$reminderAction::Text) *
        q!: * [$at] $DateTime (*планируй*|*планировать) [мне] ($Text/$reminderAction::Text) *
        q!: * [$at] $DateTime скажи [что] ($Text/$reminderAction::Text)
        script:
            if ($parseTree.DateTime) {
                $session.timer = toFuture($parseTree.DateTime[0]);
                checkMidnight($parseTree.DateTime[0].value);
                checkLeapYear($parseTree.DateTime[0].value);
                $temp.isPast = isPast($parseTree.DateTime[0]);
                checkNoon($parseTree);
            }
            if ($parseTree.Text) {
                $session.reminderSummary = $parseTree.Text[0].text;
            }
        if: !$session.timer
            a: {{ selectRandomArg($ReminderAnswers["Set a reminder"]["invalidDate"]) }}
            go!: ../Set a reminder no time
        elseif: $temp.isPast
            a: {{ selectRandomArg($ReminderAnswers["Set a reminder"]["inPast"]) }}
            go!: ../Set a reminder no time
        else:
            script:
                var year = moment($session.timer * 1000).year();
                var currentYear = currentDate().year();
                var format = year === currentYear ? "LT, D MMMM" : "LT, D MMMM YYYY";
                $temp.time = setAlarm("addReminder", "ru", format);
            a: {{ selectRandomArg($ReminderAnswers["Set a reminder"]["correctlySet"]) }}
            script:
                delete $session.timer;
                delete $session.reminderSummary;

        state: Cancel the reminder
            q: * [нет|не надо|не то|неправильно|не правильно/неверно|стой|погоди] (не надо|отмен*|удали*|убери) * [напоминание/напоминал*] || fromState ="../../Set a reminder"
            script:
                delete $client.id_to_info[$session.id];
                $response.action = "deleteReminder";
                $response.id = $session.id;
            a: {{ selectRandomArg($ReminderAnswers["Cancel the reminder"]) }}
            go!: /Reminder

        state: Fix the reminder
            q: * [нет] (нет|неправильно|не правильно|неверно|не то|стой|стоп|ой|погоди*|подожди*) * || fromState ="../../Set a reminder"
            a: {{ selectRandomArg($ReminderAnswers["Fix the reminder"]) }}
            go!: /Reminder 

    state: Thanks
        q: * $thanks *
        a: {{ selectRandomArg($ReminderAnswers["Thanks"]) }}

    state: Edit the reminder
        q: * {время (неправильно*|не правильно*|неверно*|не то)} *
        q: * [нет|не|неправильно|неверно|стой|стоп|ой|погоди|подожди] * (лучше [в]|*правь|*правим|поправил*|исправил*|измени*|*меняй|ошиб*) * $DateTime *
        q: * [нет|не|неправильно|неверно|стой|стоп|ой|погоди|подожди] * (лучше [в]|*правь|*правим|поправил*|исправил*|измени*|*меняй|ошиб*) $Text
        q: * (нет [не]|не|неправильно|неверно|стой|стоп|ой|погоди|подожди) * [лучше [в]|*правь|*правим|поправил*|исправил*|измени*|напомни*|*меняй] * $DateTime *
        q: * (нет [не]|не|неправильно|неверно|стой|стоп|ой|погоди|подожди) * [лучше [в]|*правь|*правим|поправил*|исправил*|измени*|напомни*|*меняй] $Text
        q!: * отложи [напоминание] на * $DateTime *
        a: {{ selectRandomArg($ReminderAnswers["Edit the reminder"]) }}

    state: Set a reminder no todo no time
        q!: $setReminder 
        script:
            delete $session.timer;
            delete $session.reminderSummary;
        a: {{ selectRandomArg($ReminderAnswers["Set a reminder no todo no time"]) }}

    state: Set a reminder no time 
        q!: * $setReminder ($Text/$reminderAction::Text)
        if: $parseTree.Text !== undefined || $session.reminderSummary
            script:
                $session.reminderSummary = $parseTree.Text ? $parseTree.Text[0].text : $session.reminderSummary;
                $response.text = $session.reminderSummary;
        else:
            go!: ../Set a reminder no todo no time
        a: {{ selectRandomArg($ReminderAnswers["Set a reminder no time"]) }}

        state: Set reminder time
            q: * ($DateTime/$severalDates) * || fromState ="../../Set a reminder no time"
            script:
                if ($parseTree.severalDates) {
                    if ($parseTree.severalDates[0].DateTimeRelative) {
                        $reactions.answer(selectRandomArg($ReminderAnswers["Set a reminder"]["invalidDate"]));
                        $temp.nextState = "../../Set a reminder no time";
                    } else {
                        $temp.nextState = "../../Recurring reminder";
                    }
                } else {
                    $temp.nextState = "../../Set a reminder";
                }
            go!: {{ $temp.nextState }}

        state: Set reminder time cancel
            q: * ($disagree/ни в какое/передумал*/забудь) * || fromState ="../../Set a reminder no time", onlyThisState = true
            a: {{ selectRandomArg($ReminderAnswers["Set reminder time cancel"]) }}

        state: Set reminder time error
            q: * || fromState ="../../Set a reminder no time"
            a: {{ selectRandomArg($ReminderAnswers["Set reminder time error"]) }}

    state: Set a reminder no todo || modal = true
        q!: * $setReminder * [$at] $DateTime
        script:
            checkOffset();
            $session.timer = toFuture($parseTree.DateTime[0]);
            $temp.format = toMoment($parseTree.DateTime[0]).diff(currentDate(), "days") > 0 ? "LT, D MMMM" : "LT";
        if: $session.timer
            a: {{ selectRandomArg($ReminderAnswers["Set a reminder no todo"]) }}
        else:
            a: {{ selectRandomArg($ReminderAnswers["Set a reminder"]["invalidDate"]) }}
            go!: ../Set a reminder no todo no time
        
        state: Set reminder todo
            state: Set reminder todo set
                q: [$setReminder] [$Text/$reminderAction::Text] [$at] [$DateTime] * $weight<+0.4> || fromState = ../../, onlyThisState = true
                q: [$setReminder] [$Text/$reminderAction::Text] [$at] [$DateTime] * $weight<+0.4> || fromState = "/Reminder/Set a reminder no todo no time", onlyThisState = true
                q: [$setReminder] [$at] [$DateTime] [$Text/$reminderAction::Text] $weight<+0.4> || fromState = ../../, onlyThisState = true
                q: [$setReminder] [$at] [$DateTime] [$Text/$reminderAction::Text] $weight<+0.4> || fromState = "/Reminder/Set a reminder no todo no time", onlyThisState = true
                if: $parseTree.Text
                    script:
                        $session.reminderSummary = $parseTree.Text[0].value;
                    if: $session.timer || $parseTree.DateTime
                        go!: ../../../Set a reminder
                    else:
                        go!: ../../../Set a reminder no time
                elseif: $parseTree.DateTime
                    go!: ../../../Set a reminder no todo
                else:
                    go!: ../../../Set a reminder no todo no time

            state: Set reminder todo cancel
                q: * (ни о чем/передумал*/забудь) * || fromState = ../../, onlyThisState = true
                q: * (ни о чем/передумал*/забудь) * || fromState = "/Reminder/Set a reminder no todo no time", onlyThisState = true
                q: * $disagree * || fromState = ../../, onlyThisState = true
                q: * $disagree * || fromState = "/Reminder/Set a reminder no todo no time", onlyThisState = true
                a: {{ selectRandomArg($ReminderAnswers["Set reminder todo cancel"]) }}
                script:
                    delete $session.timer;

    state: Delete a reminder no time no summary
        q!:  * (удали*|отмени*|выключи) [это] (напоминание|напоминатель|напоминалк*) [пожалуйста]
        q: * (удали*|отмени*|выключи) * || fromState="/Reminder/Edit the reminder"
        if: ($client.id_to_info && (Object.keys($client.id_to_info).length != 0))
            if: (Object.keys($client.id_to_info).length == 1)
                script:
                    $response.action = "deleteReminder";
                    $response.id = parseInt(Object.keys($client.id_to_info)[0]);
                a: {{ selectRandomArg($ReminderAnswers["Delete a reminder no time no summary"]["cancelTheReminder"]) }}
                script:
                    delete $client.id_to_info[$response.id];
            else:
                a: {{ selectRandomArg($ReminderAnswers["Delete a reminder no time no summary"]["cancelAReminder"]) }}
        else:
            a: {{ selectRandomArg($ReminderAnswers["Delete a reminder no time no summary"]["doesNotFoundTheReminder"]) }}
            
    state: Delete a reminder by time
        q!: * (удали*|отмени*) [это] (напоминание|напоминатель|напоминалк*) * $DateTime *  
        q: * $DateTime * || fromState = "../Delete a reminder no time no summary"
        script:
            var timer = toFuture($parseTree.DateTime[0]).toString();
            removeAlarm("deleteReminder", "timer", timer, selectRandomArg($ReminderAnswers["Delete a reminder by time"]["success"]), selectRandomArg($ReminderAnswers["Delete a reminder by time"]["fail"]) );

    state: Delete a reminder by summary
        q!: * (удали*|отмени*) [это] (напомина*) ($Text/$reminderAction::Text) 
        q!: * (не [надо|нужно]|хватит [мне]) * напомина* ($Text/$reminderAction::Text)
        q: $Text [$DateTime] || fromState = "../Delete a reminder no time no summary"
        script:
            var text = $parseTree.Text[0].value;
            removeAlarm("deleteReminder", "summary", text, selectRandomArg($ReminderAnswers["Delete a reminder by summary"]["success"]), selectRandomArg($ReminderAnswers["Delete a reminder by summary"]["fail"]) );

    state: Delete all reminders
        q!: * {(отмени*|отключи*|выключи*|выруби*|забуд*|удали*|не нужны) * все (напоминания|напоминатели|напоминалки) [$DateTime] } *
        a: {{ selectRandomArg($ReminderAnswers["Delete all reminders"]) }}
    
    state: Reminders list
        q!: [~мой] ~план 
        q!: ~план [$preposition] [$morph<П>] ($DateTime|$timeInterval)
        q!: * (~мой/у меня) [какие-нибудь|что-нибудь] (~план|~дело|запланирован*) * ($DateTime|$timeInterval) *
        q!: * (~план|запланирован*) * (у меня) * ($DateTime|$timeInterval) *
        q!: * {[как*] (напомин*|план*|дела|делишки) [поставл*|установл*|задан*|есть] у меня [$DateTime|$timeInterval]} *
        q!: * {[как*] напомин* [поставл*|установл*|задан*|есть] [$DateTime|$timeInterval]} *
        q!: * (перечисли|назови|покаж*|показ*|провер*|список) * [(поставл*|установл*|задан*|актуальн*|мои*)] напомин*
        q!: * {(о (чём|чем)|что) * ты должен * [мне] напомн*}
        q!: * {что * [мне|я] (надо|нужно|пора|необходимо|должен|должна) * сделать ($DateTime|$timeInterval)}
        q!: * { (как*|что) напомин* * [поставл*|установл*|задан*|есть] [у тебя|у меня|у нас] * ($DateTime|$timeInterval) } *
        q!: * (перечисли|назови|покаж*|показ*|провер*|список/напомни) * [(поставл*|установл*|задан*|актуальн*|мои*)] (напомин*|напоминания/~план) * [$DateTime|$timeInterval] *
        q!: * {(о (чём|чем)|что) * ты должен * [мне] напомн*} [$DateTime|$timeInterval] *
        q!: * (поставл*|установл*|задан*|актуальн*|мои*) (напомин*|напоминания) * [$DateTime|$timeInterval] *
        q: * { (как*|что) напомин* * [поставл*|установл*|задан*|есть] [у тебя|у меня|у нас] * ($DateTime|$timeInterval) } *
        a: {{ selectRandomArg($ReminderAnswers["Reminders list"]) }}

    state: Snooze a reminder
        q!: * (отложи/перенеси/поменяй) * [напоминание/напоминалку] * {$Text $DateTime} *
        a: {{ selectRandomArg($ReminderAnswers["Snooze a reminder"]) }}

    state: Recurring reminder
        q!: * $setReminder * $severalDates [[(что|чтобы)] [мне] (нужно|надо будет|надо)] $Text
        q!: * $setReminder [[(что|чтобы)] [мне] (нужно|надо будет|надо)] $Text [в|на] $severalDates *
        q!: * $setReminder * $severalDates [про] [что] [[мне] (нужно|надо будет|надо)] $Text
        q!: * $severalDates * $setReminder [про] [что] [[мне] (нужно|надо будет|надо)] $Text
        q!: * (*планируй*|*планировать) [мне] $Text [в|на] $severalDates *
        q!: * (*планируй*|*планировать) [мне] [в|на] $severalDates $Text *
        q!: * [в|на] $severalDates (*планируй*|*планировать) [мне] $Text *
        a: {{ selectRandomArg($ReminderAnswers["Recurring reminder"]) }}