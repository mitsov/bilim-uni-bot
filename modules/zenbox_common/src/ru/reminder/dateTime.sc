require: number/number.sc
    module = zb_common
require: dateTime/moment.min.js
    module = zb_common
require: dateTime/moment-business.js
    module = zb_common
require: dateTime/dateTimeConverter.js
    module = zb_common


patterns:

    $Season = ((~зима/~зимний): 0|(~весна/~весенний): 1|(~лето/~летний): 2|(~осень/осенний): 3)

    $Weekend = (выходн*|уикенд*|викенд*|уикэнд*|викэнд*)

    $TimeHhMmSs = $regexp<((0)?\d|1\d|2(0|1|2|3|4))(:|\.|-)((0|1|2|3|4|5)\d)((:|\.|-)((0|1|2|3|4|5)\d))?> ||  converter = $converters.timeConverterHhMmSs

    $TimeHhMmWithoutSeparator = $regexp<((0)?\d|1\d|2(0|1|2|3|4))(0|1|2|3|4|5)\d> ||  converter = $converters.timeConverterHhMmWithoutSeparator

    $TimeHourDigit = $regexp<((0)?\d|1\d|2(0|1|2|3|4))> ||  converter = $converters.numberConverterDigit

    $NumberTwoDigitTime = (
        $NumberTwoDigit | 
        (двадцат*|twenty):20 |
        (двадцат* ~один|twenty one):21 |
        (двадцат* ~два|twenty two):22 |
        (двадцат* ~три|twenty three):23) || converter = function(pt) {return pt.NumberTwoDigit ? pt.NumberTwoDigit[0].value : parseInt(pt.value);};
    $TimeHourNumber = ( $NumberDozen [$NumberOrdinalDigit | $NumberOneDigit] | $NumberOrdinalDigit | $NumberOneDigit | $NumberTwoDigitTime | $TimeHourDigit) ||  converter = $converters.numberConverterSum;

    $TimeMinuteDigit = $regexp<(0|1|2|3|4|5)?\d> ||  converter = $converters.numberConverterDigit

    $TimeMinuteNumber = ( $NumberDozen [$TimeMinuteDigit|$NumberOneDigit] | $TimeMinuteDigit | $NumberTwoDigit | ноль [$NumberOneDigit] ) ||  converter = $converters.numberConverterSum

    $DateDayDigit = $regexp<\d+(-(е|го|его|ого))?> ||  converter = $converters.numberConverterDigit

    $DateDayAndMonthDigit = $regexp<\d+((\.|\/)((0)?(0|1|2|3|4|5|6|7|8|9)|1(0|1|2)))(\.|/)?((1|2)?\d?\d\d)?>[г] || converter = $converters.timeConverterDateMonth

    $DateDayNumber = ( $NumberDozen [$NumberOrdinalDigit | $NumberNumeric] | ($NumberOrdinalDigit | $NumberNumeric) | $DateDayDigit) ||  converter = $converters.numberConverterSum

    $DateDayOrderNumber = (
        (в [самом] начале|в [самых] первых числах):1 |
        (десятое|десятого):10 | 
        (одиннадцатое|одиннадцатого):11 | 
        (двенадцатое|двенадцатого):12 | 
        (тринадцатое|тринадцатого):13 | 
        (четырнадцатое|четырнадцатого):14 |
        (в середине):15 | 
        (пятнадцатое|пятнадцатого):15 | 
        (шестнадцатое|шестнадцатого):16 | 
        (семнадцатое|семнадцатого):17 | 
        (восемнадцатое|восемнадцатого):18 | 
        (девятнадцатое|девятнадцатого):19 |
        (в конце):28 ) ||  converter = $converters.numberConverterValue

    $TimeHhMm = $TimeHourNumber $TimeMinuteNumber ||  converter = $converters.timeConverterHhMm

    $TimeHoursModifier = (
        (утра|утро|утром|утречком|ночи|ночью): am |
        (дня|днем|вечера|вечер*|пополудни|после полудня): pm ) ||  converter = $converters.propagateConverter

    # TODO : move утро/вечер/день into a separate pattern
    # TODO : add period modifier for час дня/час ночи
    $TimeHoursSpecial = (
        час дня: 13 |
        час ночи: 1 |
        час: 1 |
# maybe it's better to introduce special fields for morning and evening hours
        ([с|на] утр*|поутру): 8 |
        [с|на] вечер*: 18 |
        [с|на] ночь*: 0 |
        (ноч*|полночь|полночи|полуноч*): 0 |
        (полдень|полудн*): 12 
        ) ||  converter = $converters.numberConverterValue


    $NumberNumericTime = ($NumberOneDigit | $NumberTwoDigitTime) || converter = $converters.propagateConverter;
    $TimeHoursNumeric = (
        $TimeHoursModifier [в|на] ($NumberNumericTime::Number|$TimeHourDigit::Number) [час*] |
        ($NumberNumericTime::Number|$TimeHourDigit::Number) ([час*] $TimeHoursModifier| (час|часа|часов|часиков) [$TimeHoursModifier]) |
        $TimeHoursSpecial
        ) ||  converter = $converters.timeHoursConverter

    $TimeMinutesModifier = (
        без $Number [минут] |
        без четверти:15 |
        [в] четверть:45                   
        ) ||  converter = $converters.numberConverterSum

    $TimeRelativeHoursHalf = (
        $Number с половин* час* | 
        $Number час* с половин* ) ||  converter = $converters.timeRelativeHoursConverter
    $TimeRelativeMinutesHalf = (
        $Number с половин* (минуты|минут|минуточек|минуточки) | 
        $Number (минуты|минут|минуточек|минуточки) с половин* ) ||  converter = $converters.timeRelativeMinutesConverter

    $TimeRelativeHours = ( [один] (час|часик|часок) :1 | $Number час* ) ||  converter = $converters.numberConverterSum
    $TimeRelativeMinutes = (
        [одну] минут* :1 | 
        [одна] минута:1 |
        четверт* часа:15 |
        (полчас*|пол (часа|часика)):30 | 
        (полтора час*):90 | 
        ($Number|$TimeMinuteDigit) (минуты|минут|минуточек|минуточки|минуту)
        ) ||  converter = $converters.numberConverterSum

    $TimeRelativeSeconds = (
        [одну] секунд* :1 |
        (полминут*|пол минут*):30 |
        $Number секунд*
        ) ||  converter = $converters.numberConverterSum

    $TimeRelative = (
        через $TimeRelativeHours [и] [$TimeRelativeMinutes] |
        (через|на) $TimeRelativeMinutes |
        через ($TimeHourNumber::TimeRelativeHours|$TimeRelativeHours) $TimeMinuteNumber::TimeRelativeMinutes |
        час* через $Number::TimeRelativeHours |
        минут* через $Number::TimeRelativeMinutes |
        $TimeRelativeHoursHalf |
        $TimeRelativeMinutesHalf
        ) ||  converter = $converters.relativeDateTimeConverter

    $TimeHalfAnHourSpecial = (
        (полпервого | (пол|половин*) (первого|1)): 1 |
        (полвторого | (пол|половин*) (второго|2)): 2 |
        (полтретьего | (пол|половин*) (третьего|3)): 3 |
        (полчетвёртого | (пол|половин*) (четвёртого|4)): 4 |
        (полпятого | (пол|половин*) (пятого|5)): 5 |
        (полшестого | (пол|половин*) (шестого|6)): 6 |
        (полседьмого | (пол|половин*) (седьмого|7)): 7 |
        (полвосьмого | (пол|половин*) (восьмого|8)): 8 |
        (полдевятого | (пол|половин*) (девятого|9)): 9 |
        (полдесятого | (пол|половин*) (десятого|10)): 10 |
        (полодинадцатого | (пол|половин*) (одинадцатого|11)): 11 |
        (полдвенадцатого | (пол|половин*) (двенадцатого|12)): 12
        ) ||  converter = $converters.timeHalfAnHourConverter

    $TimeMinutesAndHours = [в] ($NumberOneDigit|$TimeMinuteNumber) (мин|минут*) ||  converter = $converters.propagateConverter

    $TimeMinutesNumeric = ([в] $TimeMinuteNumber| и $NumberOneDigit) [мин|минут*] ||  converter = $converters.propagateConverter
    $TimeSecondsNumeric = $Number [сек|секунд*] ||  converter = $converters.propagateConverter
    $TimeAbsolute = [в/на] (
        $TimeHhMmSs::Time |
        (на|в) $TimeHhMmWithoutSeparator::Time [$TimeHoursModifier] |
        [на|в] $TimeHhMmWithoutSeparator::Time $TimeHoursModifier |
        $TimeHalfAnHourSpecial::Time [$TimeHoursModifier] |
        [на|в] ($TimeHourNumber::TimeHoursNumeric|$TimeHourDigit::TimeHoursNumeric) [$TimeHoursModifier] |
        $TimeHourNumber::TimeHoursNumeric [ноль|00] $TimeMinutesNumeric [$TimeHoursModifier] |
        $TimeMinutesModifier ($Number::TimeHoursNumeric | $TimeHoursNumeric) |
        $TimeHoursSpecial::TimeHoursNumeric [$TimeMinuteNumber::TimeMinutesNumeric] |
        [на|в] ($TimeHourNumber::TimeHoursNumeric [час*|часа|часов] [и] $TimeMinutesNumeric) [$TimeHoursModifier] |
        [[на] $TimeHoursModifier] [на|в] ($TimeHourNumber::TimeHoursNumeric [час*|часа|часов] [и] $TimeMinutesNumeric) |
        $TimeHoursNumeric |
        [на] $TimeMinutesAndHours ($Number::TimeHoursNumeric | $TimeHoursNumeric) |
        $TimeRelative
        ) ||  converter = $converters.absoluteTimeConverter

    # Converts any date representation into absolute Date value
    $DateMonth = (
        (~январь|январ*|янв|january|jan):1 |
        (~февраль|феврал*|фев|february|feb):2 |
        (~март|март*|march|mar):3 |
        (~апрель|апрел*|апр|april|apr):4 |
        (~май|май|мая|мае|may):5 |
        (~июнь|июнь|июня|июне|june|jun):6 |
        (~июль|июль|июля|июле|july|jul):7 |
        (~август|август*|august|aug):8 |
        (~сентябрь|сентябр*|сент|september|sep):9 |
        (~октябрь|октябр*|окт|october|oct):10 |
        (~ноябрь|ноябр*|november|nov):11 |
        (~декабрь|декабр*|december|dec):12 ) ||  converter = $converters.numberConverterValue

    $DateWeekday = (
        ~понедельник:1 | ~вторник:2 | ~среда:3 | ~четверг:4 | ~пятница:5 | (~суббота|субот*):6 | (~воскресенье|воскресение):7 
        ) ||  converter = $converters.numberConverterValue

    $DateFuturePastModifier = (
        (в (этом|эту|текущем) | этого | сего | текущего|ближайш* ):0 | 
        (следующ*) :1 |
        прошл* :-1 |
        позапрошл* :-2 |
        через (~один|$Number):1 
        ) ||  converter = $converters.numberConverterMultiply

    $DateRelativeDay = (
        [$DateFuturePastModifier] [недел*] [в|во|на] $DateWeekday |
        [в|во] $DateWeekday [$DateFuturePastModifier] |
        $DateFuturePastModifier (дня|дней|днем|день|сутки|суток) |
        через [одни] сутки:1 |
        сегодн*:0 |
        (вчера*:-1|позавчера*:-2|(поза позавчера*|позапозавчера*):-3|завтра*:1|(послезавтра*|после завтра*):2|(послепослезавтра*|после послезавтра*|после после завтра*):3) |
        [$Number] (дня|дней|день) [тому] назад:-1
        ) ||  converter = $converters.relativeDayConverter

    $DateRelativeMonth = (
        $DateFuturePastModifier месяц* |
        [$Number] месяц* назад:-1
        ) ||  converter = $converters.relativeMonthConverter

    $DateRelativeYear = (
        $DateFuturePastModifier год* |
        [$Number] (год*|лет) назад:-1
        ) ||  converter = $converters.relativeMonthConverter
        # relativeMonthConverter

    $DateRelative = (
        [$DateRelativeYear] [$DateRelativeMonth] $DateRelativeDay |
        [$DateRelativeYear] $DateRelativeMonth |
        $DateRelativeYear |
        $DateRelativeDay $DateRelativeMonth |
        $DateRelativeDay
        ) ||  converter = $converters.relativeDateConverter

    $DateYearTwoNumber = $regexp<\d\d> ||  converter = $converters.numberConverterDigit
    $DateYearNumber = $regexp<(1|2)\d\d\d(г)?> ||  converter = $converters.numberConverterDigit
    $DateYearNumeric = $NumberThousands $NumberThreeDigit ||  converter = $converters.numberConverterSum

    $DateHoliday = (
        ({~рождество [христов*/православн*]}): 0 |
        ({католич* ~рождество}): 1 |
        (~новый ~год): 2 |
        (~хеллоуин|~хелоуин|~хэллоуин|~хэлоуин): 3 |
        (~день независимост* [сша/соединен* штат* [америк*]]): 4 |
        (~день (свят* валентин*/[~все] влюблен*)): 5 |
        (~день свят* патрик*): 6 |
        (~день защитник* отечеств*): 7 |
        ([международн*] женск* ~день): 8 |
        (~день ({весн* * [труд*]}/{труд* * [весн*]})): 9
        (~день ~победа): 10 |
        (~день (~россия/рф/российск* федераци*)): 11 |
        (~день народн* единств*): 12 |
        ({~день татьян*}/[международн*] ~день студент*): 13 |
        (~день (смех*/дурак*)): 14 |
        (~день космонавт*): 15 |
        (~день славянск* ({письм* * [культур*]}/{культур* * [письм*]})): 16 |
        ([международн*] ~день защит* дет*): 17 |
        (~день ~семья [{~любовь * [~верность]}/{~верность * [~любовь]}]): 18 |
        (~день [государствен*] флаг* [~россия/рф/российск* федераци*]): 19 |
        (~день ~знание): 20 |
        (~день учител*): 21 |
        (~день конституци*): 22 |
        (~старый ~новый ~год): 23 |
        (~день благодарени*): 24 |
        (~день матер*): 25 |
        (~день (медик*/медицинск* работник*)): 26 |
        (~день (пудинг*/пуддинг*)): 27)

    $DateAbsolute =  (
        [$DateWeekday] ($DateDayNumber::DateDayNumeric|$DateDayOrderNumber::DateDayNumeric) [числа] {($DateMonth) [месяц*]} [ [в] $DateYearNumber::DateYearNumeric [год*]] |
        {($DateDayNumber::DateDayNumeric [числа] $DateMonth [месяц*] [ $DateYearTwoNumber::DateYearNumeric год* |($DateDayDigit)($DateMonth)| $DateYearNumber::DateYearNumeric [года] | $DateYearNumeric [год*] ]) [[в|во] $DateWeekday]} |
        [в/во] $DateWeekday $DateDayNumber::DateDayNumeric [числа|число] |
        [[в/во] $DateWeekday] $DateDayNumber::DateDayNumeric (числа|число) |
        [[в/во] $DateWeekday] $DateDayOrderNumber::DateDayNumeric [числа|число] |        
        [это* [день]] $DateDayNumber::DateDayNumeric [число] $DateMonth [месяц*] [ $DateYearNumber::DateYearNumeric [года] | $DateYearNumeric [год*] | $DateYearTwoNumber::DateYearNumeric год* ] | 
        $DateWeekday $DateDayNumber::DateDayNumeric |
        $DateRelativeYear $DateDayNumber::DateDayNumeric [числа] $DateMonth [месяц*] |
        $DateDayNumber::DateDayNumeric [числа] $DateMonth [месяц*] [в] $DateRelativeYear |
        $DateDayNumber::DateDayNumeric [числа] $DateRelativeMonth [месяц*] |
        [на] $DateRelative |
        ($DateYearNumber::DateYearNumeric | $Number::DateYearNumeric) год* |
        $DateHoliday|$DateDayAndMonthDigit
        ) ||  converter = $converters.absoluteDateConverter

    $DateTimeSpecial = (
        $DateDayDigit::Day (в/на) $TimeAbsolute
        ) ||  converter = $converters.specialDateTimeConverter

    $DateWeekDaySpecial = [в|во] $DateWeekday ||  converter = $converters.weekDayConverter

    $DateTimeRelativeInterval = (
        (через|спустя) $DateTimeSimpleInterval :1 |
        $DateTimeSimpleInterval (назад):-1 
        ) ||  converter = $converters.relativeDateTimeMultiplierConverter
  
    $DateTimeAbsolute = (
        $DateAbsolute |
        $TimeAbsolute |
        $TimeAbsolute [на|в|во] $DateAbsolute |
        $DateAbsolute [на|в|после|около] $TimeAbsolute [$DateWeekDaySpecial] |
        $DateTimeSpecial |
        {([на] $DateRelative|$DateTimeRelativeInterval) ([на|в|после|около] $TimeAbsolute) [$TimeHoursModifier]}) ||  converter = $converters.composeConverter

    $DateTimeRelativeYears = ( (один|через) (год|годик):1 | $Number (года|годика|лет) ) ||  converter = $converters.numberConverterSum
    $DateTimeRelativeMothes = ( (один|через) месяц:1 | $Number (месяца|месяцев) | [через] полгода:6 ) ||  converter = $converters.numberConverterSum
    $DateTimeRelativeWeeks = ($Number ~неделя :7 | ([одна|через] недел*):7 ) ||  converter = $converters.numberConverterMultiply
    $DateTimeRelativeSimpleDays = ( (один/одни) (день/сутки):1 |через [один/одни] (день/сутки):1 | [один/одни] (день/сутки) назад:-1 | $Number суток | $Number (дня|дней))
    $DateTimeRelativeDays = ({$DateTimeRelativeSimpleDays [и] [$DateTimeRelativeWeeks]}|$DateTimeRelativeWeeks) ||  converter = $converters.numberConverterSum


    $DateTimeSimpleInterval = (
        $DateTimeRelativeYears [и] [$DateTimeRelativeMothes] [и] [$DateTimeRelativeDays] [и] [$TimeRelativeHours] [и] [$TimeRelativeMinutes] [и] [$TimeRelativeSeconds] | 
        $DateTimeRelativeMothes [и] [$DateTimeRelativeDays] [и] [$TimeRelativeHours] [и] [$TimeRelativeMinutes] [и] [$TimeRelativeSeconds] |
        $DateTimeRelativeDays [и] [$TimeRelativeHours] [и] [$TimeRelativeMinutes] [и] [$TimeRelativeSeconds] |
        $TimeRelativeHours [и] [$TimeRelativeMinutes] [и] [$TimeRelativeSeconds] |      
        $TimeRelativeMinutes [и] [$TimeRelativeSeconds] |
        $TimeRelativeSeconds |
        $TimeRelativeHoursHalf | 
        $TimeRelativeMinutesHalf
        ) ||  converter = $converters.relativeDateTimeConverter

    $DateTimeRelativeSimple = (
        [через|спустя] $DateTimeSimpleInterval :1 |
        $DateTimeSimpleInterval (назад):-1 
        ) ||  converter = $converters.relativeDateTimeMultiplierConverter

    $DateTimeNow = (сейчас|сегодня|этот день) ||  converter = $converters.nowDateTimeConverterNew

    $DateTimeRelative = ($DateTimeRelativeSimple [[на|в|после|около] $TimeAbsolute]|$DateTimeNow) ||  converter = $converters.propagateConverter

    $DateRelativeTimeAbsolute = $DateTimeRelative (на|в|после|около) $TimeAbsolute ||  converter = $converters.composeConverter

    $DateTime = ($DateTimeAbsolute|$DateTimeRelative|$DateRelativeTimeAbsolute) ||  converter = $converters.normalizeDateTimeConverter
    #$DateTime = ($DateTimeAbsolute|$DateTimeRelative) ||  converter = $converters.absoluteDateTimeConverter

