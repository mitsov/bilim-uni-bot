function dts_currentDate() {
    var time = currentDate();
    var ret = {};

    ret.day = dts_dayweekName(time.day());
    ret.month = dts_monthName(time.month()+1) ;
    //ret.date = time.year() + "-" + (time.month()+1) + "-" + time.date();
    ret.time = time.hour() + ":" + time.minute();
    ret.date = time.date();
    ret.where = "выбранном вами";

    return ret;
}

function dts_dayweekName(day) {
    switch(day) {
        case 1: return "понедельник";
        case 2: return "вторник";
        case 3: return "среда";
        case 4: return "четверг";
        case 5: return "пятница";
        case 6: return "суббота";
        case 7:
        case 0:
            return "воскресенье";
    }
}

function dts_monthName(month) {
    switch(month) {
        case 1: return "января";
        case 2: return "февраля";
        case 3: return "марта";
        case 4: return "апреля";
        case 5: return "мая";
        case 6: return "июня";
        case 7: return "июля";
        case 8: return "августа";
        case 9: return "сентября";
        case 10: return "октября";
        case 11: return "ноября";
        case 12: return "декабря";
    }
}

function dts_seasonName(day) {
    switch(day) {
        case 0: return "зима";
        case 1: return "весна";
        case 2: return "лето";
        case 3: return "осень";
    }
}

function dts_dayPeriodName(day) {
    switch(day) {
        case 0: return "ночь";
        case 1: return "утро";
        case 2: return "день";
        case 3: return "вечер";
    }
}

function comparisonTimezone(offset1, offset2) {
    var comparison = ((offset1 - offset2)/3600);
    var ru__hours = (Math.abs(comparison) % 10 === 1 && Math.abs(comparison) !== 11) ? 'час' : (Math.abs(comparison) % 10 > 1 && Math.abs(comparison) % 10 <= 4 && (Math.abs(comparison) < 11 || Math.abs(comparison) > 14)) ? 'часа' : 'часов'
    if (comparison == 0) {
        return 'нет разницы во времени';
    } else if (comparison > 0) {
        return '+ ' + comparison + ' ' + ru__hours;
    } else {
        return comparison + ' ' + ru__hours;
    }
}

function getDayPeriodForTime(dateTime) {
    var hours = parseInt(moment(dateTime).locale('ru').format('HH'));
    if (hours < 5) {
        return 0;
    } else if (hours < 12) {
        return 1;
    } else if (hours < 17) {
        return 2;
    } else {
        return 3;
    }
}

function yesOrNo(var1, var2) {
    return (var1 == var2) ?  'Да, ' : 'Нет, ';
}

function todayCheck(time, city) {
    var $session = $jsapi.context().session;
    var $temp = $jsapi.context().temp;
    var weekDay;
    var answerFact;
    var answerFactStart = (city == true) ? 'в городе ' + $session.Where.name + ' ' : '';
    if ($session.tree.DateAbsolute) {
        if ($session.tree.DateAbsolute[0].DateRelative) {
            if ($session.tree.DateAbsolute[0].DateRelative[0].DateRelativeDay) {
                if ($session.tree.DateAbsolute[0].DateRelative[0].DateRelativeDay[0].DateWeekday) {
                    weekDay = $session.tree.DateAbsolute[0].DateRelative[0].DateRelativeDay[0].DateWeekday[0].value;
                }
            }
        } else {
            weekDay = null;
        }
        if (weekDay) {
            answerFact = 'сегодня ' + answerFactStart + time.locale('ru').format('dddd');
            $temp.answer = yesOrNo(time.format('E'), weekDay) + answerFact;
        } else {
            answerFact = 'сегодня ' + answerFactStart + time.locale('ru').format('LL, dddd');
            $temp.answer = yesOrNo(time.locale('ru').format('L'), toMoment($session.tree.DateAbsolute[0]).locale('ru').format('L')) + answerFact;
        }
    } else if ($session.tree.DateMonth) {
        answerFact = 'сейчас ' + answerFactStart + time.locale('ru').format('MMMM');
        $temp.answer = yesOrNo(time.format('MM'), parseInt($session.tree.DateMonth[0].value)) + answerFact;
    } else if ($session.tree.Season) {
        var factSeason = getSeason(time);
        var yn = yesOrNo(factSeason, $session.tree.Season[0].value);
        $temp.answer = yn + 'сейчас ' + answerFactStart + dts_seasonName(factSeason);
    }
}


function checkWeekday(newMoment){
    var $parseTree = $jsapi.context().parseTree;
    var $temp = $jsapi.context().temp;
    if ($parseTree.DateRelative){
        if ($parseTree.DateRelative[0].DateRelativeDay) {
            if ($parseTree.DateRelative[0].DateRelativeDay[0].DateWeekday) {
                if (moment.unix(toPast($parseTree.DateRelative[0])).format('L') == toMoment($parseTree.DateAbsolute[0]).format('L') || $parseTree.Past){
                    newMoment = moment.unix(toPast($parseTree.DateRelative[0])).format('L');
                    $temp.answer  = moment.unix(toPast($parseTree.DateRelative[0])).locale('ru').format('dddd - это LL');
                } else  {
                    $temp.answer = toMoment($parseTree.DateRelative[0]).locale('ru').format('dddd - это LL');
                }
            }
        }
    } else if ($parseTree.DateAbsolute){
        if ($parseTree.DateAbsolute[0].DateRelative) {
            if ($parseTree.DateAbsolute[0].DateRelative[0].DateRelativeDay) {
                if ($parseTree.DateAbsolute[0].DateRelative[0].DateRelativeDay[0].DateWeekday) {
                    if ($parseTree.DateRelative){
                        $temp.answer = toMoment($parseTree.DateRelative[0]).locale('ru').format('LL - это dddd');
                    } else {
                        $temp.answer = toMoment($parseTree.DateTimeRelative[0]).locale('ru').format('LL - это dddd');
                    }
                }
            }
        }
    }
    return newMoment;
}


function dateCheckOtherDateAbsolute(){
    var $parseTree = $jsapi.context().parseTree;
    var $temp = $jsapi.context().temp;
    var newMoment;
    if ($parseTree.DateRelative) {
        newMoment = toMoment($parseTree.DateRelative[0]).format('L');
        $temp.answer = toMoment($parseTree.DateRelative[0]).locale('ru').format('LL, dddd');
    } else {
        newMoment = toMoment($parseTree.DateTimeRelative[0]).format('L');
        $temp.answer = toMoment($parseTree.DateTimeRelative[0]).locale('ru').format('LL, dddd');
    }
    //reaction for answer if needed
    if ($parseTree.DateAbsolute) {
        newMoment = checkWeekday(newMoment);
        $temp.answer = yesOrNo(newMoment, toMoment($parseTree.DateAbsolute[0]).format('L')) + $temp.answer;
    }
}

function timeInterval(mode, param1, param2) {
    var $parseTree = $jsapi.context().parseTree;
    var $temp = $jsapi.context().temp;
    var $request = $jsapi.context().request;
    switch(mode) {
        case "dateTime":
            var future = param1;

            // бинарная переменная future указывает, находится ли вторая точка временного интервала в будущем.
            var dateTimeAbs = JSON.parse(JSON.stringify($parseTree.DateTimeAbsolute[0]));
            var year = dateTimeAbs.value.year;
            var month = dateTimeAbs.value.month;
            var day = dateTimeAbs.value.day;
            var hour = dateTimeAbs.value.hour;
            var minute = dateTimeAbs.value.minute;

            var queryDate = toMoment($parseTree.DateTimeAbsolute[0]);
            var diff = (future === true) ? (queryDate - currentDate()) : (currentDate() - queryDate);
            var newDateTime;


            if (diff < 0) {
                if ((year == null)  && (month != null)) {
                    newDateTime = (future === true) ? queryDate.add(1, 'years') : queryDate.subtract(1, 'years');
                } else if ((month == null) && (day != null)) {
                    newDateTime = (future === true) ? queryDate.add(1, 'months') : queryDate.subtract(1, 'months');
                } else if ((day == null) && (hour != null)) {
                    newDateTime = (future === true) ? queryDate.add(1, 'days') : queryDate.subtract(1, 'days');
                } else if ((hour == null) && (minute != null)) {
                    newDateTime = (future === true) ? queryDate.add(1, 'hours') : queryDate.subtract(1, 'hours');
                } else {
                    newDateTime = queryDate;
                }
            } else {
                newDateTime = queryDate;
            }

            $temp.interval = newDateTime.locale('ru').from(currentDate());

            if ((!$parseTree.afterAbsoluteInterval && !$parseTree.tillAbsoluteInterval) && future === true) {
                $temp.interval = $temp.interval.replace("через ", "");
            } else if (($parseTree.afterAbsoluteInterval || $parseTree.tillAbsoluteInterval) && future === false) {
                $temp.interval = $temp.interval.replace("назад", "");
            }

            if ($temp.interval != "Invalid date"){
                $reactions.answer("Примерно " + $temp.interval);
            } else {
                $reactions.answer("Такой даты не существует");
            }
            break;
        case "season":
            var season = param1;
            var end = param2;
            $temp.season = season.value;
            var currentSeason = getSeason(currentDate());
            var hours = currentDate().locale('ru').format('HH');
            var minutes = currentDate().locale('ru').format('mm');
            var seconds = currentDate().locale('ru').format('ss');

            var year = '';
            var year_add = 0;
            var value = '';
            if (currentSeason == $temp.season && end == 0) {
                $reactions.answer(capitalize(dts_seasonName(currentSeason)) + " уже на дворе.");
            } else {
                if ((end == 1 && $temp.season == 0) || !($temp.season == 0 || currentSeason <= $temp.season)) {
                    year_add = 1;
                }

                year = parseInt(currentDate().locale('ru').format('YYYY')) + year_add;
                var lat = $request.data.lat ? $request.data.lat : 60;
                var month = '';

                if ($temp.season == 0) {
                    month = (end == 0 ? (lat >= 0 ? 11 : 5) : (lat >= 0 ? 2 : 8));
                } else if ($temp.season == 1) {
                    month = (end == 0 ? (lat >= 0 ? 2 : 8) : (lat >= 0 ? 5 : 11));
                } else if ($temp.season == 2) {
                    month = (end == 0 ? (lat >= 0 ? 5 : 11) : (lat >= 0 ? 8 : 2));
                } else {
                    month = (end == 0 ? (lat >= 0 ? 8 : 2) : (lat >= 0 ? 11 : 5));
                }
                value = [year, month, 1, hours, minutes, seconds];
                $temp.date = moment(value).locale('ru').from(currentDate());
                var season = capitalize(dts_seasonName(parseInt($temp.season)));
                $reactions.answer(season + " " + (end == 0 ? "начнётся" : "закончится") + " примерно " + $temp.date + ".",
                    season + " " + (end == 0 ? "наступит" : "закончится") + " примерно " + $temp.date + ".",
                    season + " " + (end == 0 ? "ожидается" : "заканчивается") + " примерно " + $temp.date + ".");
            }
            break;
        case "month":
            var month = param1;
            var end = param2;
            $temp.month = month.value;
            var currentMonth = currentDate().locale('ru').format('M');
            var hours = currentDate().locale('ru').format('HH');
            var minutes = currentDate().locale('ru').format('mm');
            var seconds = currentDate().locale('ru').format('ss');

            var year = '';
            var year_add = 0;
            if (currentMonth == $temp.month && end == 0) {
                $reactions.answer(capitalize(currentDate().locale('ru').format('MMMM')) + " уж наступил.");
            } else {
                if (currentMonth > $temp.month) {
                    year_add = 1;
                }

                year = parseInt(currentDate().locale('ru').format('YYYY')) + year_add;

                var value = [year, $temp.month - (end == 0 ? 1 : 0), 1, hours, minutes, seconds];
                $temp.date = moment(value).locale('ru').from(currentDate(), true);
                var month = dts_monthName(parseInt($temp.month));
                $reactions.answer("До " + (end == 1 ? "конца " : "") + month + " осталось примерно " + $temp.date + ".",
                    "До " + (end == 1 ? "конца " : "") + month + " остаётся примерно " + $temp.date + ".");
            }
            break;
        case "weekEnd":
            var currentWeekday = currentDate().day();
            if (currentWeekday == 0 || currentWeekday == 6) {
                $reactions.answer("Выходные уже наступили. Сегодня " + dts_dayweekName(currentWeekday) + ".");
            } else {
                var diff = Math.abs(currentWeekday - 6);
                if (diff == 1) {
                    $reactions.answer("До выходных остался 1 день.", "До выходных остаётся 1 день.");
                } else if (diff == 2 || diff == 3 || diff == 4) {
                    $reactions.answer("До выходных осталось " + diff + " дня.", "До выходных остаётся " + diff + " дня.");
                } else {
                    $reactions.answer("До выходных осталось " + diff + " дней.", "До выходных остаётся " + diff + " дней.");
                }
            }
            break;
    }
}