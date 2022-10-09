function setAlarm(action, locale, format) {
    /*
    This function sets the alarm via adding special fields to response;
    :param action: (addAlarm OR addReminder) - specifies type of action to be sent in response
    :param locale: specifies the way how moment.js will stringify date, e.g.:
        locale 'ru' will return "13 марта 2019 г."
        locale 'en' will return "March 13, 2019"
    :returns str: stringified time in specified locale to be answered to user
    */
    fillDict();
    checkOffset();
    var $response = $jsapi.context().response;
    var $session = $jsapi.context().session;
    var $client = $jsapi.context().client;
    $response.action = action;
    $response.id = $session.id;
    $response.timer = $client.id_to_info[$session.id][0];
    $response.text = $client.id_to_info[$session.id][1];
    $response.serverTime = moment().unix(); //$session.timer
    var sessionTimerMoment = moment.unix($session.timer).utcOffset($session.offset); // moment.js object
    var dateFormat = format || "LT, D MMMM";

    delete $session.relativeTimePeriods;
    delete $session.timePeriods;
    delete $session.dtModifier;
    delete $session.datesAreEqual;
    delete $session.usrWantedTimePassed;
    delete $session.alarmIsToday;
    delete $session.pmTimeIsAvailable;

    return sessionTimerMoment.locale(locale).format(dateFormat);
}

function getPrettySessionTimerMoment(){
    var $session = $jsapi.context().session;
    checkOffset();
    var sessionTimerMoment = moment.unix($session.timer).utcOffset($session.offset); // moment.js object
    var curDate = currentDate().utcOffset($session.offset);
    return sessionTimerMoment.locale('ru').format('LT');
}

function fillDict() {
    var $session = $jsapi.context().session;
    var $client = $jsapi.context().client;
    if ($client.id_to_info == undefined) {
        $client.id_to_info = {};
        $session.id = 0;
    } else {
        if (Object.keys($client.id_to_info).length > 0) {
            $session.id = Math.max.apply(null, Object.keys($client.id_to_info)) + 1;
        } else {
            $session.id = 0;
        }
    }

    $client.id_to_info[$session.id] = [$session.timer, $session.reminderSummary];
}

function removeAlarm(action, type, value, text1, text2) {
    var $response = $jsapi.context().response;
    var $client = $jsapi.context().client;
    var $temp = $jsapi.context().temp;
    var id = -1;
    var j = (type == 'summary') ? 1 : 0;
    if ($client.id_to_info) {
        for (var i = 0; i < Object.keys($client.id_to_info).length; i++) {
            var k = Object.keys($client.id_to_info)[i];
            if (value == $client.id_to_info[k][j]) {
                id = parseInt(k);
                break;
            }
        }
    }
    if (id > -1) {
        $response.action = action;
        $response.id = id;
        $temp.summary = $client.id_to_info[id][1];
        $reactions.answer(text1);
        delete $client.id_to_info[id];
    } else {
        $reactions.answer(text2);
    }
}

function checkNoon($parseTree) {
    var currentHour = currentDate().format("HH");
    if ($parseTree.text.indexOf("полдень") > -1 && $parseTree.DateTime[0].value.days === 0 && $parseTree.DateTime[0].value.hour < currentHour) {
        $jsapi.context().temp.isPast = true;
    }
}

function checkMidnight(date) {
    var $session = $jsapi.context().session;
    if (date.hour === 0 && (date.days === 1 || date.days === 2)) {
        $session.timer += 86400;
    }
}

function getNextLeapYear() {
    var nextLeapYear;
    for (var i = 1; i <= 4; i++) {
        if (currentDate().add(i, "years").isLeapYear()) {
            nextLeapYear = currentDate().add(i, "years").year();
            break;
        }
    }
    return nextLeapYear;
}

function checkLeapYear(date) {
    var $session = $jsapi.context().session;
    var currentYear = currentDate().year();
    if (date.day === 29 && date.month === 2) {
        if (date.year === currentYear && !(currentDate().isLeapYear() && currentDate().month() < 2)
            && !(currentDate().month() === 1 && currentDate().day() === 29)) {
            var year = getNextLeapYear();
            var newDate = {
                "year": year,
                "day": date.day,
                "month": date.month - 1,
                "hour": date.hour,
                "minute": date.minute
            };
            $session.timer = moment(newDate) / 1000;
        } else {
            $session.timer -= 86400;
        }
    }
}

$global.$converters = $global.$converters || {};
$global.$converters.nowDateTimeConverterNew = function() {
    return {
        days: 0
    };
};
