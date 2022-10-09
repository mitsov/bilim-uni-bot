var $global = this;
$global.$converters = $global.$converters || {};
var cnv = $global.$converters;

// **********************************************************
//                  COMMON FUNCTIONS
// **********************************************************

function safeValue(tag, def) {
    if (typeof tag != 'undefined') {
        return tag[0].value;
    } else {
        return def;
    }
}

function currentDate() {
    var time = moment.utc($jsapi.currentTime());

    var $rd = $jsapi.context().request.data;
    if ($rd && typeof $rd.offset != 'undefined') {
        time = time.utcOffset($rd.offset);
    }
    return time;
}

function toPrettyString(obj) {
    var seen = [];
    return JSON.stringify(obj, function(key, val) {
        if (val != null && typeof val == "object") {
            if (seen.indexOf(val) >= 0) {
                return "cycle";
            }
            seen.push(val);
        }
        if (val == null) {
            return undefined;
        }
        return val;
    }, 2);
}

cnv.collectParseTreeValues = function(parseTree) {
    if (typeof parseTree.value != "undefined") {
        return [parseTree.value];
    }
    var values = [];
    for (var key in parseTree) {
        if (parseTree.hasOwnProperty(key)) {
            if (key == 'text' || key == 'value' || key == 'tag' || key == 'pattern' || key == 'words') {
                continue;
            }
            var tags = parseTree[key];
            for (var i = 0; i < tags.length; i++) {
                var innerValues = cnv.collectParseTreeValues(tags[i]);
                values = values.concat(innerValues);
            }
        }
    }
    return values;
};

cnv.propagateConverter = function(parseTree) {
    var values = cnv.collectParseTreeValues(parseTree);
    return values[0];
};

function parseHttpResponse(data, status, response) {
    return data;
}

function parseXmlHttpResponse(str) {
    var value = JSON.parse(JSON.stringify(str));
    try {
        value = parseXML(value);
    } catch (ex){}
    return value;
}

function parseJsonError(str) {
    var value = JSON.parse(str);
    return value;
}

function httpError(response, status, error) {
    return error;
}

function checkEnLetters(text) {
    return /.*[a-zA-Z]+.*/.test(text);
}

function resolveVariables(string) {
    return $reactions.template(string, $jsapi.context());
}

function resolveInlineDictionary(string){
    var result = "";
    if (string) {
        result = string.toString().replace(/\{([^{\/]*)(\/([^}\/]*))+?\}/g, inlineDictionaryReplacer);
    }
    return result;
}

function inlineDictionaryReplacer(match, p1, p2, p3, offset, string) {
    var alternatives = match.replace(/\{|\}/g,"").split("/");
    var index = testMode() ? 0 : randomInteger(0,alternatives.length-1);
    return alternatives[index];
}

function testMode() {
    if ($jsapi.context().testContext) {
        return true;
    } else {
        return false;
    }
}

//возвращает случайное число в интервале
function randomInteger(min, max) {
    if (testMode()) {
        return min;
    } else {
        var rand = min + Math.random() * (max + 1 - min);
        rand = Math.floor(rand);
        return rand;
    }  
}

function selectRandomArg() {
    var arg = arguments;
    if (Array.isArray(arg[0]) && arg.length === 1) {
        arg = arg[0];
    }
    var index;
    if (testMode()) {
        index = 0;
    } else {
        index = $reactions.random(arg.length);
    }
    return arg[index];
}

function toMoment(dateTime) {
    return moment($converters.absoluteDateTimeConverter(dateTime.value));
}

function dayStart(day) {
    return moment(day - (day % (3600000*24)));
}

function dayEnd(day) {
    return moment(dayStart(day)).add(1, 'days');
}

function toFuture(dateTime) {
    var year = dateTime.value.year;
    var month = dateTime.value.month;
    var day = dateTime.value.day;
    var hour = dateTime.value.hour;
    var minute = dateTime.value.minute;
    var today = new Date();
    var beforeNoon = ((dateTime.value.days === 0 || dateTime.value.day === today.getDate()) && hour && hour <= 12);

    var oldDateTime = toMoment(dateTime);
    var diff = oldDateTime - currentDate();
    var newDateTime;
    if (diff < 0) {
        if (beforeNoon === true) {
            newDateTime = oldDateTime.add(12, 'hour');
        } else if  ((year === undefined)  && (month !== undefined)){
            newDateTime = oldDateTime.add(1, 'years');  
        } else if ((month === undefined) && (day !== undefined)){
            newDateTime = oldDateTime.add(1, 'months'); 
        } else if ((day === undefined) && (hour !== undefined)){
            newDateTime = oldDateTime.add(1, 'days'); 
        } else if ((hour === undefined) && (minute !== undefined)){
            newDateTime = oldDateTime.add(1, 'hours');  
        } else {
            newDateTime = oldDateTime;
        }
    } else {
        newDateTime = oldDateTime;
    }
    var $temp = $jsapi.context().temp;
    $temp.interval = newDateTime.locale(global_locale).from(currentDate());

    return newDateTime.unix();
}

function toPast(dateTime) {
    var year = dateTime.value.year;
    var month = dateTime.value.month;
    var day = dateTime.value.day;
    var hour = dateTime.value.hour;
    var minute = dateTime.value.minute;

    var oldDateTime = toMoment(dateTime);
    var newDateTime;
    var diff = oldDateTime - currentDate();
    if (diff > 0) {
        if  ((year == undefined)  && (month!=undefined)){
            newDateTime = oldDateTime.subtract(1, 'years');  
        } else if ((month == undefined) && (day!=undefined)){
            newDateTime = oldDateTime.subtract(1, 'months'); 
        } else if ((day == undefined) && (hour!=undefined)){
            newDateTime = oldDateTime.subtract(1, 'days'); 
        } else if ((hour == undefined) && (minute!=undefined)){
            newDateTime = oldDateTime.subtract(1, 'hours');  
        } else if (dateTime.value.days) {
            newDateTime = oldDateTime.subtract(7, 'days'); 
        } else {
            newDateTime = oldDateTime;
        }
    }   
    else {
        newDateTime = oldDateTime;
    }
    return newDateTime.unix();
}

function isPast(dateTime) {
    var dt = 0;
    if (dateTime.DateTimeAbsolute) {
        dt = dateTime.DateTimeAbsolute[0].value;
    } else if (dateTime.DateTimeRelative) {
        dt = dateTime.DateTimeRelative[0].value;
    }
    if (dt != 0) {

        if (dt.seconds) {
            if (dt.seconds < 0) {
                return true;
            }
        }                
        if (dt.minutes) {
            if (dt.minutes < 0) {
                return true;
            }
        }                    
        if (dt.hours) {
            if (dt.hours < 0) {
                return true;
            }
        }                   
        if (dt.days) {
            if (dt.days < 0) {
                return true;
            }
        }    
        if (dt.months) {
            if (dt.months < 0) {
                return true;
            }
        }    
        if (dt.years) {
            if (dt.years < 0) {
                return true;
            }
        }   
    }
    return false;
}

function checkOffset(){
    var $rd = $jsapi.context().request.data;
    var $session = $jsapi.context().session;
    if ($rd.offset == undefined){
        $session.offset = 0;
    } else{
        $session.offset = $rd.offset;
    }
}

function mergeTwoDateTime(dt1, dt2) {
    var mergedYears = null;
    if (dt1.value.years) {
        mergedYears = dt1.value.years; 
    } else if (dt2.value.years) {
        mergedYears = dt2.value.years; 
    }
    var mergedMonths = null;
    if (dt1.value.months) {
        mergedMonths = dt1.value.months; 
    } else if (dt2.value.months) {
        mergedMonths = dt2.value.months; 
    }  
    var mergedDays = null;
    if (dt1.value.days) {
        mergedDays = dt1.value.days; 
    } else if (dt2.value.days) {
        mergedDays = dt2.value.days; 
    }
    var mergedHours = null;
    if (dt1.value.hours) {
        mergedHours = dt1.value.hours; 
    } else if (dt2.value.hours) {
        mergedHours = dt2.value.hours; 
    }  
    var mergedMinutes = null;
    if (dt1.value.minutes) {
        mergedMinutes = dt1.value.minutes; 
    } else if (dt2.value.minutes) {
        mergedMinutes = dt2.value.minutes; 
    }
    var mergedSeconds = null;
    if (dt1.value.seconds) {
        mergedSeconds = dt1.value.seconds; 
    } else if (dt2.value.seconds) {
        mergedSeconds = dt2.value.seconds; 
    }  
    var mergedYear = null;
    if (dt1.value.year) {
        mergedYear = dt1.value.year; 
    } else if (dt2.value.year) {
        mergedYear = dt2.value.year; 
    }
    var mergedMonth = null;
    if (dt1.value.month) {
        mergedMonth = dt1.value.month; 
    } else if (dt2.value.month) {
        mergedMonth = dt2.value.month; 
    }  
    var mergedDay = null;
    if (dt1.value.day) {
        mergedDay = dt1.value.day; 
    } else if (dt2.value.day) {
        mergedDay = dt2.value.day; 
    }
    var mergedWeekDay = null;
    if (dt1.value.weekDay) {
        mergedWeekDay = dt1.value.weekDay; 
    } else if (dt2.value.weekDay) {
        mergedWeekDay = dt2.value.weekDay; 
    }    
    var mergedHour = null;
    if (dt1.value.hour) {
        mergedHour = dt1.value.hour; 
    } else if (dt2.value.hour) {
        mergedHour = dt2.value.hour; 
    }  
    var mergedMinute = null;
    if (dt1.value.minute) {
        mergedMinute = dt1.value.minute; 
    } else if (dt2.value.minute) {
        mergedMinute = dt2.value.minute; 
    }
    var mergedSecond = null;
    if (dt1.value.second) {
        mergedSecond = dt1.value.second; 
    } else if (dt2.value.second) {
        mergedSecond = dt2.value.second; 
    }  
    var mergedPeriod = null;
    if (dt1.value.period) {
        mergedPeriod = dt1.value.period; 
    } else if (dt2.value.period) {
        mergedPeriod = dt2.value.period; 
    }                      
    return {
        "tag": "DateTime",
        "value": {
            years:  mergedYears,
            months: mergedMonths,
            days:   mergedDays,
            hours:  mergedHours,
            minutes: mergedMinutes,
            seconds: mergedSeconds,
            year:   mergedYear,
            month:  mergedMonth,
            day:    mergedDay,
            weekDay: mergedWeekDay,
            hour:   mergedHour,
            minute: mergedMinute,
            second: mergedSecond,
            period: mergedPeriod        
        }
    };
}

function themeExists(theme){
    return _.contains($global.themes, theme);
}

function randomIndex(array) {
    var index;
    if (testMode()) {
        index = 0;
    } else {
        index = Math.floor(Math.random()*array.length);
    }
    return index;    
}

function getRandomElement(dict) {
    return dict[randomInteger(1,Object.keys(dict).length)].value; 
}

function random(upperBound) {
    var $session = $jsapi.context().session;
    var $temp = $jsapi.context().temp;
    if (typeof upperBound != 'number') {
        throw new Error("upperBound must be a number for function random(upperBound)");
    }
    if (upperBound == 1) {
        return 0;    
    }
    
    var id = currentState() + "_" + upperBound;
    var inStateId = $temp[id] || 0;

    $session.smartRandom = $session.smartRandom || {};
    $session.smartRandom[id] = $session.smartRandom[id] || Array.apply(null, new Array(upperBound)).map(Number.prototype.valueOf, 0);

    var index = Math.floor(Math.random()*upperBound);
    var currentIndex = index;

    while ($session.smartRandom[id][currentIndex] != 0) { 
        if (currentIndex >= upperBound - 1) {
            currentIndex = 0;
        } else {
            currentIndex++;
        }

        if (currentIndex == index) {
            fillWithZero(id);
            break;
        }
    }

    $session.smartRandom[id][currentIndex] = 1;  
    return currentIndex;  
}


function fillWithZero(id){
    var $session = $jsapi.context().session;
    for (var i=0; i<$session.smartRandom[id].length; i++) {
        $session.smartRandom[id][i] = 0;
    }
}


function currentState() {
    return $jsapi.context().currentState;
}

function hasDateRelative(date){
    return date.DateRelative;
}

function applyCustomAnswers(common, custom) {
    if (typeof custom !== 'undefined') {
        var merged = {};
        for (var key in common) {
            if (Object.prototype.toString.call(common[key]) === '[object Object]') {
                merged[key] = applyCustomAnswers(common[key], custom[key]);
            } else if (custom[key]) {
                merged[key] = custom[key];
            } else {
                merged[key] = common[key];
            }
        }
        if (merged){
            return merged;
        } else {
            return {"1":"2"};
        }
        
    }
    return common;
}

function isMale() {
    var $client = $jsapi.context().client;
    return ($client.gender && $client.gender == 0);    
}

function isFemale() {
    var $client = $jsapi.context().client;
    return ($client.gender && $client.gender == 1);    
}

function toGender(text1, text2, text3) {
    return isMale() ? text1 : (isFemale() ? text2 : text3);
}

// 3 -> "три" или "третий"
// Категории: 'cardinal' (по умолчанию), 'ordinal' 
// Максимальное количественное числительное 999 999 999 999
// Максимальное порядковое числительное 1 000 000
var numberToString = (function() {

    var arr_numbers = new Array();
    arr_numbers[1] = new Array('', 'один', 'два', 'три', 'четыре', 'пять', 'шесть', 'семь', 'восемь', 'девять', 'десять', 'одиннадцать', 'двенадцать', 'тринадцать', 'четырнадцать', 'пятнадцать', 'шестнадцать', 'семнадцать', 'восемнадцать', 'девятнадцать');
    arr_numbers[2] = new Array('', '', 'двадцать', 'тридцать', 'сорок', 'пятьдесят', 'шестьдесят', 'семьдесят', 'восемьдесят', 'девяносто');
    arr_numbers[3] = new Array('', 'сто', 'двести', 'триста', 'четыреста', 'пятьсот', 'шестьсот', 'семьсот', 'восемьсот', 'девятьсот');

    function parseNumber(num, desc) {
        var string = '';
        var num_hundred = '';
        var source_num = num;
        if (num.length == 3) {
            num_hundred = num.substr(0, 1);
            num = num.substr(1, 3);
            string = arr_numbers[3][num_hundred] + ' ';
        }
        if (num < 20 && num[0] != '0') {
            string += arr_numbers[1][num] + ' ';
        }
        else {
            var first_num = num.substr(0, 1);
            var second_num = num.substr(1, 2);
            string += arr_numbers[2][first_num] + ' ' + arr_numbers[1][second_num] + ' ';
        }             
        switch (desc){
            case 1:
                if (source_num == '000') {
                    break;
                }
                var last_num = num.substr(-1);
                if (last_num == 1) {
                    string += 'тысяча ';
                }
                else if (last_num > 1 && last_num < 5) {
                    string += 'тысячи ';
                }
                else {
                    string += 'тысяч ';
                }
                string = string.replace('один ', 'одна ');
                string = string.replace('два ', 'две ');
                break;
            case 2:
                if (source_num == '000') {
                    break;
                }
                var last_num = num.substr(-1);
                if (last_num == 1) {
                    string += 'миллион ';
                }
                else if (last_num > 1 && last_num < 5) {
                    string += 'миллиона ';
                }
                else {
                    string += 'миллионов ';
                }
                break;
            case 3:
                var last_num = num.substr(-1);
                if (last_num == 1) {
                    string += 'миллиард ';
                }
                else if (last_num > 1 && last_num < 5) {
                    string += 'миллиарда ';
                }
                else {
                    string += 'миллиардов ';
                }
                break;
        }
        string = string.replace('  ', ' ');
        return string;
    }

    return function(number, category) {
        if (category && !_.contains(["cardinal", "ordinal"], category)) {
            throw new Error("Unknown number category: " + category);
        }

        if (number === 0) {
            return (category === 'ordinal') ? "нулевой" : "ноль";
        }

        var numStr = number.toString();

        if (!number || typeof(number) !== 'number' || numStr.indexOf('.') > -1) {
            throw new Error("Invalid input, integer required: " + number);
        }

        if (numStr.length > 12) {
            throw new Error("Number too large: " + numStr);
        }

        if (category === 'ordinal' && numStr.length >= 7 && numStr !== '1000000') {
            throw new Error("Number too large to get ordinal category: " + numStr);
        }

        var number_length = numStr.length;
        var string = '';
        var num_parser = '';
        var count = 0;
        for (var p = (number_length - 1); p >= 0; p--) {
            var num_digit = numStr.substr(p, 1);
            num_parser = num_digit + num_parser;
            if ((num_parser.length == 3 || p == 0) && !isNaN(num_parser)) {
                string = parseNumber(num_parser, count) + string;
                num_parser = '';
                count++;
            }
        }

        string = string.trim();

        if (category === 'ordinal') {
            var num_parts = string.split(' ');
            var ordinal_part = num_parts.pop();

            if (ordinal_part == 'сто') {
                string = 'сотый'
            }
            else if (ordinal_part == 'тысяча') {
                string = 'тысячный';
            }
            else if (ordinal_part == 'миллион') {
                string = 'миллионный';
            }
            else if (string.indexOf('миллион') == -1 &&
                     ordinal_part.indexOf('тысяч') > -1) {
                string = '';
                for (var i = 0; i < num_parts.length; i++) {
                    string += $nlp.inflect(num_parts[i], "gent");
                }
                string += "тысячный";
            }
            else {
                ordinal_part = $nlp.inflect(ordinal_part, "Anum");
                string = num_parts.join(' ') + ' '  + ordinal_part; 
            }
        }

        return string;
    }
} ());