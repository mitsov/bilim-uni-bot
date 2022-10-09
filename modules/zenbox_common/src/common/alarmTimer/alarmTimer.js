function initAlarm($parseTree) {
    /*
    This function is immediately called upon entering the "Set an alarm" state.
    :creates $session.alarmTime: DateTime object merged from $parseTree
    :creates $temp.alarmTime: temporary copy of $session.alarmTime
    */
    var $session = $jsapi.context().session;
    var $temp = $jsapi.context().temp;

    // Clean up previous alarm-related session data
    delete $session.relativeTimePeriods;
    delete $session.timePeriods;
    delete $session.dtModifier;
    delete $session.datesAreEqual;
    delete $session.usrWantedTimePassed;
    delete $session.alarmIsToday;
    delete $session.pmTimeIsAvailable;

    // If coming from the "WhatTime" state, we merge what we have said before and now
    // and return from func in order not to overwrite any previously entered info
    if ($session.alarmTime && $session.alarmCheck && $parseTree.DateTime) {
        $session.alarmTime = mergeTwoDateTime($parseTree.DateTime[0], $session.alarmTime);
        $temp.alarmTime = $session.alarmTime;
        delete $session.alarmCheck;
        return;
    }

    // If $DateTime matched several times, we merge the two objects into one
    if ($parseTree.DateTime && $parseTree.DateTime[1]) {
        $parseTree.DateTime[0] = mergeTwoDateTime($parseTree.DateTime[0], $parseTree.DateTime[1]);
    }

    // Save all info into persistent and temporary structures
    if ($parseTree.DateTime) {
        $session.alarmTime = $parseTree.DateTime[0];
        $temp.alarmTime = $session.alarmTime;
    }
}

function parseAlarm($parseTree) {
    /*
    This function parses $temp.alarmTime and saves some of its properties into $session
    */

    function getRelativeTimePeriods(usrAbsHour){
        return usrAbsHour < 5 ? ['день', 'ночь'] : (usrAbsHour < 12 ? ['вечер','утро'] : (usrAbsHour < 18 ? ['день', 'ночь'] : ['вечер','утро']))
    }

    var $session = $jsapi.context().session;
    var $temp = $jsapi.context().temp;
    checkOffset();
    var today = currentDate();
    var momentAlarmTime = toMoment($temp.alarmTime);
    log('today: ' + today.format('DD.MM HH:mm'));
    log('usrTime: ' + momentAlarmTime.format('DD.MM HH:mm'));

    $session.relativeTimePeriods = getRelativeTimePeriods($parseTree._DateTimeAbsolute.hour);
    $session.timePeriods = [momentAlarmTime.format('HH:mm')];
    $session.dtModifier = $parseTree._TimeHoursModifier
    $session.datesAreEqual = today.isSame(momentAlarmTime, 'day')
    $session.usrWantedTimePassed = today.isAfter(momentAlarmTime)
    $session.alarmIsToday = momentAlarmTime.isSame(today, 'day')
    $session.pmTimeIsAvailable = momentAlarmTime.add(12, 'hours').isSame(today, 'day') && !today.isAfter(momentAlarmTime)
    // ^ momentAlarmTime is already +12 hours when second condition applied 
    $session.timePeriods.push(momentAlarmTime.format('HH:mm'));

    log('datesAreEqual: ' + $session.datesAreEqual)
    log('UsrWantedTimePassed: ' + $session.usrWantedTimePassed)
    log('pmTimeIsAvailable: ' + $session.pmTimeIsAvailable)
    log('dtModifier: ' + $session.dtModifier)
}
