function dts_currentDate() {
    var time = moment(currentDate());
    var ret = {};
    
    ret.day = dts_dayweekName(time.day());
    ret.month = dts_monthName(time.month()+1) ;    
    ret.date = time.year() + "-" + (time.month()+1) + "-" + time.date(); 
    ret.time = time.hour() + ":" + time.minute();
    ret.date = time.date();
    ret.where = "of your choice";
     
    return ret;
}

function calculateDate(dateTime, Root) {
    if (Root == 1) {
        return moment(dateTime).locale('en').format('MMMM D, YYYY, dddd');
    } else if (Root == 2) {
        return moment(dateTime).locale('en').format('MMMM');
    } else if (Root == 3) {
        return moment(dateTime).locale('en').format('YYYY');
    } else {
        return dts_seasonName(getSeason(moment(dateTime)));
    }
}

function calculateDateTime(dateTime, flag) {
    var oldDateTime = 0;
    if (flag == "future") {
        oldDateTime = moment.unix(toFuture(dateTime));
    } else {
        oldDateTime = moment.unix(toPast(dateTime));
    }
    return oldDateTime.locale('en').from(currentDate());
}
  
function dts_dayweekName(day) {
    switch(day) {
        case 0: return "Sunday";
        case 1: return "Monday";
        case 2: return "Tuesday";
        case 3: return "Wednesday";
        case 4: return "Thursday";
        case 5: return "Friday";
        case 6: return "Saturday";
    }
}     

function dts_monthName(month) {
    switch(month) {
        case 1: return "January";
        case 2: return "February";
        case 3: return "March";
        case 4: return "April";
        case 5: return "May";
        case 6: return "June";
        case 7: return "July";
        case 8: return "August";
        case 9: return "September";
        case 10: return "October";
        case 11: return "November";
        case 12: return "December";
    }
}

function dts_seasonName(day) {
    switch(day) {
        case 0: return "winter";
        case 1: return "spring";
        case 2: return "summer";
        case 3: return "autumn";
    }
}    

function getVerb(Root) {
    if (Root == "1") {
        return "is";
    } else {
        return "was";
    }
}

function comparisonTimezone(offset1, offset2) {
    var comparison = ((offset1 - offset2)/3600);
    var en__hours = Math.abs(comparison) % 10 === 1 && Math.abs(comparison) % 100 !== 11 ? 'hour' : 'hours';
    if (comparison == 0) {
        return 'No time difference';
    } else if (comparison > 0) {
        return '+ ' + comparison + ' ' + en__hours;
    } else {
        return comparison + ' ' + en__hours; 
    }    
}

/*

$temp.check = dts_currentTrue();

function dts_currentTime() {
    var time = moment(currentTime());
    var ret = {};
    
    ret.time = time.hours() + ":" time.minutes() ;    
    ret.where = "выбранном вами";

    return ret;
} 

   */

function yesOrNo(var1, var2) {
    return (var1 == var2) ?  'Yes, ': 'No, ';
}


function todayCheck(time, city) {
    var $session = $jsapi.context().session;
    var $temp = $jsapi.context().temp;
    var yn;
    var weekDay;
    var answerFact;
    var answerFactStart;
    var answerFactEnd = (city==true) ? ' in ' + $session.Where.name + ' ' : '';
    if ($session.tree.DateAbsolute) {
        if ($session.tree.DateAbsolute[0].DateRelativeDay) {
           
            if ($session.tree.DateAbsolute[0].DateRelativeDay[0].DateWeekday) {
                weekDay = $session.tree.DateAbsolute[0].DateRelativeDay[0].DateWeekday[0].value;
                answerFact = 'today is ' + time.locale('en').format('dddd');
                $temp.answer = yesOrNo(time.format('E'), weekDay) + answerFact;
            }
            
        } else {
            if ($session.tree.DateAbsolute[0].DateYearNumeric) {
                if (!$session.tree.DateAbsolute[0].DateDayNumeric) {
                    yn = yesOrNo(time.format('YYYY'), toMoment($session.tree.DateAbsolute[0]).format('YYYY'));
                } else {
                    yn = "No, ";
                }            
                answerFactStart = 'now is ' + time.locale('en').format('YYYY') + ' year';                                              
            } else {
                answerFactStart = 'today is ' + time.locale('en').format('LL, dddd');
                yn = yesOrNo(time.format('L'), toMoment($session.tree.DateAbsolute[0]).format('L'));
            }
            $temp.answer = yn + answerFactStart + answerFactEnd;
        }
    } else if ($session.tree.DateMonth) {
        yn = yesOrNo(time.locale('en').format('MMMM'), dts_monthName($session.tree.DateMonth[0].value));
        $temp.answer = yn + 'now it\'s ' + time.locale('en').format('MMMM') + answerFactEnd;
    } else if ($session.tree.Season) {
        var factSeason = getSeason(time);
        yn = yesOrNo(factSeason, $session.tree.Season[0].value);
        $temp.answer = yn + 'now it\'s ' + dts_seasonName(factSeason) + answerFactEnd;    
    } else {
        $temp.answer = yesOrNo(time.locale('en').format('L'), toMoment($session.tree.DateAbsolute[0]).locale('en').format('L')) + answerFact;
        answerFact = 'today is ' + time.locale('en').format('LL, dddd') + answerFactStart;
    }
}