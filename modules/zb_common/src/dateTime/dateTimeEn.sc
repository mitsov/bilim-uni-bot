require: ../number/number.sc
require: moment.min.js
require: moment-business.js
require: dateTimeConverter.js


patterns:

    $Weekend = (weekend|week end|week-end|wkend|wkends|weekends|week ends|week-ends)

    $Season = ((winter/winter-tide/winter-time/wintertime): 0| (spring/springtime/springtide/prime): 1| (summer/summery/aestival/summerly/estival/summertime): 2| (autumn/autumnal/fall): 3)

    $TimeHhMmSs = $regexp<((0)?\d|1\d|2(0|1|2|3|4))(:|\.|-)((0|1|2|3|4|5)\d)((:|\.|-)((0|1|2|3|4|5)\d))?> ||  converter = $converters.timeConverterHhMmSs

    $TimeHhMmWithoutSeparator = $regexp<((0)?\d|1\d|2(0|1|2|3|4))(0|1|2|3|4|5)\d> ||  converter = $converters.timeConverterHhMmWithoutSeparator

    $TimeHourDigit = $regexp<((0)?\d|1\d|2(0|1|2|3|4))> ||  converter = $converters.numberConverterDigit

    $TimeAbsoluteWithPeriod = $regexp<((0|1|2)?\d|\d{3,4}|\d?\d[\s\.]\d?\d)(am|pm)> ||  converter = $converters.timeConverterWithPeriod

    $TimeHourNumber = ( $NumberDozen [$NumberOrdinalDigit | $NumberOneDigit] | ($NumberOrdinalDigit | [zero|oh] $NumberOneDigit | $NumberTwoDigit ) | $TimeHourDigit) ||  converter = $converters.numberConverterSum

    $TimeMinuteDigit = $regexp<(0|1|2|3|4|5)?\d> ||  converter = $converters.numberConverterDigit

    $TimeMinuteNumber = ( $NumberDozen [$TimeMinuteDigit|$NumberOneDigit] | $TimeMinuteDigit | $NumberTwoDigit | (zero|oh) [$NumberOneDigit] | $NumberDozenWithDash ) ||  converter = $converters.numberConverterSum

    $DateMonthDigit = $regexp<((0|1)?\d)> ||  converter = $converters.numberConverterDigit

    $DateDayDigit = $regexp<((1|2)\d|0?[1-9]|3[0-1])> ||  converter = $converters.numberConverterDigit

    $DateDayNumber = [the] ( $NumberDozen [$NumberOrdinalDigit | $NumberOneDigit] | ($NumberOrdinalDigit | $NumberOneDigit | $NumberTwoDigit) | $DateDayDigit) ||  converter = $converters.numberConverterSum

    $TimeHhMm = $TimeHourNumber $TimeMinuteNumber ||  converter = $converters.timeConverterHhMmSs

    $TimeHoursModifier = (
        ([in the] morning|am|a m|a.m.|a.m|a. m.|a .m .|a m): am |
        ([in the] evening|[in the] (afternoon|noon)|[at] night|pm|p m|p.m.|p.m|p. m.|p .m .|p m): pm )

    $TimeHoursSpecial = (
# maybe it's better to introduce special fields for morning and evening hours
        morning: 8 |
        evening: 18 |
        night: 0 |
        midnight: 0 |
        ([high] noon|midday): 12 |
        ([in the] afternoon): 14
        ) ||  converter = $converters.numberConverterValue

    $TimeHourSpecial = (
        an hour: 1
        ) ||  converter = $converters.numberConverterValue

    $TimeHoursNumeric = (
        $TimeHoursModifier at ($NumberNumeric::Number|$TimeHourDigit::Number) |
        ($NumberNumeric::Number|$TimeHourDigit::Number) ($TimeHoursModifier| (hour*|o'clock|o'clok|oclock|o clock) [$TimeHoursModifier]) |
        $TimeHoursSpecial
        ) ||  converter = $converters.timeHoursConverter

    $TimeMinutesModifier = (
        $Number [min|minute*] (to|before) |
        [a] quarter (to|of|till|before):15                      
        ) ||  converter = $converters.numberConverterSum

    $TimeAddMinutesModifier = (
        $Number [min|minute*] (past|after) |
        [a] half [past|after]:30 |
        [a] quarter (past|after):15 |
        sharp:0             
        ) ||  converter = $converters.numberConverterSum

    $TimeRelativeHours = ( [one|an] (hour):1 | $Number (hour|hours) ) ||  converter = $converters.numberConverterSum
    $TimeRelativeMinutes = ( 
        [one|a] (minute):1 | 
        ([one|a] (half [of] [an] hour | hour-half)):30 | 
        ((one|an) hour and [a] half):90 |
        [one|a] quarter [of] [an] hour:15 |
        $Number (minute*) 
        ) ||  converter = $converters.numberConverterSum

    $TimeRelativeSeconds = ( 
        [a|one] (second) :1 | 
        $Number (second*)
        ) ||  converter = $converters.numberConverterSum

    $TimeRelative = (
        (in|after|for) $TimeRelativeHours [and] [$TimeRelativeMinutes] |
        $TimeRelativeHours [and] [$TimeRelativeMinutes] (later|from now) |
        (in|after|for) $TimeRelativeMinutes |
        $TimeRelativeMinutes (later|from now) |
        (in|after|for) ($TimeHourNumber::TimeRelativeHours|$TimeRelativeHours) $TimeMinuteNumber::TimeRelativeMinutes 
        ) ||  converter = $converters.relativeDateTimeConverter

    $TimeMinutesNumeric = ($TimeMinuteNumber | and [zero] $NumberOneDigit) [min|minute|minutes] ||  converter = $converters.propagateConverter
    $TimeSecondsNumeric = $Number (sec|second*) ||  converter = $converters.propagateConverter
    $TimeAbsolute = [$TimeHoursModifier] [at|by|for|of] (
        $TimeHhMmSs::Time [$TimeHoursModifier] |
        (at|for|an|in) $TimeHourNumber::TimeHoursNumeric [$TimeHoursModifier|$TimeAddMinutesModifier] |
        to $TimeHourDigit::TimeHoursNumeric [$TimeHoursModifier] |
        [at|for|an|in|to] $TimeHourDigit::TimeHoursNumeric $TimeHoursModifier |
        $TimeHourNumber::TimeHoursNumeric [00] $TimeMinuteNumber::TimeMinutesNumeric [minute*] [$TimeHoursModifier] |
        at ($TimeHourNumber::TimeHoursNumeric $TimeMinuteNumber::TimeMinutesNumeric) $TimeHoursModifier |
        $TimeHoursModifier at ($TimeHourNumber::TimeHoursNumeric $TimeMinuteNumber::TimeMinutesNumeric) |  
        $TimeMinutesModifier ($Number::TimeHoursNumeric | $TimeHoursNumeric) |
        $TimeAddMinutesModifier ($Number::TimeHoursNumeric | $TimeHoursNumeric) |
        (to|for) $TimeHourSpecial::TimeHoursNumeric |  
        $TimeHoursSpecial::TimeHoursNumeric |
        [at|to [the]] $TimeHourNumber::TimeHoursNumeric [hour|hours] [and] ($TimeMinutesNumeric| a $TimeAddMinutesModifier) [$TimeHoursModifier] |
        [on [the]] $TimeHoursNumeric |
        $TimeRelative |
        $TimeAbsoluteWithPeriod::Time |
        $TimeEn::Time [$TimeHoursModifier] |
        (at [the]|for|in|at) $TimeHhMmWithoutSeparator::Time [hours] [$TimeHoursModifier] |
        $TimeHhMmWithoutSeparator::Time hours [$TimeHoursModifier] | 
        $TimeHhMmWithoutSeparator::Time [hours] $TimeHoursModifier |
        $TimeHhMmWithoutSeparator::Time [$TimeHoursModifier] on) ||  converter = $converters.absoluteTimeConverter

    # Converts any date representation into absolute Date value
    $DateMonth = (
        (january|jan):1 | 
        (february|feb):2 | 
        (march|mar):3 | 
        (april|apr):4 | 
        (may):5 | 
        (june|jun):6 | 
        (july|jul):7 | 
        (august|aug):8 | 
        (september|sep|sept):9 | 
        (october|oct):10 | 
        (november|nov):11 | 
        (december|dec):12 ) ||  converter = $converters.numberConverterValue

    $DateWeekday = (
        (monday|mon|mon.):1 | (tuesday|tues|tue|tu|tue.):2 | (wednesday|wed|wed.):3 | (thursday|thurs|thur|thu|th.|thu.):4 | (friday|fri|fri.):5 | (saturday|sat|sat.):6 | (sunday|sun|sun.):7 
        ) ||  converter = $converters.numberConverterValue

    $DateWeekdays = (
        (mondays):1 | (tuesdays):2 | (wednesdays):3 | (thursdays):4 | (fridays):5 | (saturdays):6 | (sundays):7 
        ) ||  converter = $converters.numberConverterValue        

    $DateFuturePastModifier = (
        (this):0 | 
        (next*) :1 |
        last* :-1 |
        (before last) :-2
        ) ||  converter = $converters.numberConverterMultiply

    $DateRelativeDay = (
        [$DateFuturePastModifier] $DateWeekday |
        (next day):1 |
        ((today|todays|today's)|(this|present) (day|night)|*onight)|([the] [day] before tomorrow):0 |
        ((yesterday):-1|([the] [day] before yesterday):-2|(tomorrow [s]|tomorow [s]|tomorrow's|tomorrow 's):1|([the] day after tomorrow):2) |
        in $Number (day|days) |
        (in a day) : 1 |
        (a day (ago|before)) : -1 | 
        $Number (day|days) (ago|before):-1 |
        $Number (day|days) (later):1 |
        (a week ago) : -7 |
        $Number (week*) (ago):-7 |
        in $Number (week*):7 |
        $Number (week*) (later): 7 |
        in a week :7
        ) ||  converter = $converters.relativeDayConverter

    $DateRelativeMonth = (
        $DateFuturePastModifier month* |
        in $Number month* : 1 |
        in a month : 1 |
        a month later : 1 |
        $Number (month*) (later) : 1 |
        a month (ago|before) : -1 |
        $Number (month*) (ago|before):-1
        ) ||  converter = $converters.relativeMonthConverter

    $DateRelativeYear = (
        $DateFuturePastModifier (year*) |
        in $Number year* : 1 |
        in a year : 1 |
        a year later : 1 |
        $Number (year*) (later) :1|
        a year (ago|before) : -1 |
        $Number (year*) (ago|before):-1
        ) ||  converter = $converters.relativeMonthConverter

    $DateRelative = (
        [$DateRelativeYear] [and] [$DateRelativeMonth] [and] $DateRelativeDay |
        [$DateRelativeYear] [and] $DateRelativeMonth |
        $DateRelativeYear
        ) ||  converter = $converters.relativeDateConverter

    $DateYearNumber = $regexp<(1|2)\d\d\d> ||  converter = $converters.numberConverterDigit
    $DateYearShortNumber = $regexp<\d\d> ||  converter = $converters.numberConverterDigit
    $DateYearNumberWithSpace = $regexp<(1|2)\d \d\d> ||  converter = $converters.yearWithSpaceConverter

    $DateAbsoluteEn = $regexp<(1|2)\d\d\d-\d\d-\d\d> ||  converter = $converters.dateAbsoleteEnConverter
    $DateWithSlashes = $regexp<\d?\d/\d?\d(/\d\d)?> ||  converter = $converters.dateWithSlashesConverter
    $DateWithSlashesAndWeekDay = $DateWithSlashes [$DateWeekday] ||  converter = $converters.dateWithSlashesConverter
    $DateWithDots = $regexp<\d\d\.\d\d\.\d\d> ||  converter = $converters.dateWithDotsConverter

    $DateHoliday = (
        (christmas [day]): 1 |
        (new year): 2 |
        (halloween): 3 |
        (Independence Day): 4 |
        ([Saint|St.] Valentine 's Day | Saint Valentine | [the] Feast of Saint Valentine) : 5 |
        ((Saint|St.) Patrick 's Day | [the] Feast of Saint Patrick) : 6 |
        (Thanksgiving [day]) : 7 |
        (Mother 's day) : 8 |
        (Father 's day) : 9 |
        (Super Bowl [Sunday]) : 10
        )

    $DateAbsolute = (
        [$DateWeekday] ($DateMonth [month*] $DateDayNumber::DateDayNumeric [day] [ ($DateYearNumber::DateYearNumeric|$DateYearShortNumber::DateYearNumeric) [year*]]) |
        ({([of] $DateMonth [month*]) $DateDayNumber::DateDayNumeric [day]} [ [in] ($DateYearNumber::DateYearNumeric|$DateYearShortNumber::DateYearNumeric|$DateRelativeYear:DateYearNumeric) [year*]]) [$DateWeekday] |
        [$DateWeekday] ($DateDayNumber::DateDayNumeric [day] [of] ($DateMonth [month*]|[the] month) [ $DateYearNumber::DateYearNumeric [year*]]) |
        ($DateDayNumber::DateDayNumeric [day] [of] ($DateMonth [month*]|[the] month) [ $DateYearNumber::DateYearNumeric [year*]]) [$DateWeekday] |
        [$DateWeekday] ($DateDayNumber::DateDayNumeric [day] [of] $DateMonth [month*] [ $DateYearNumber::DateYearNumeric [year*] | $Number::DateYearNumeric year* ] [[on] $DateWeekday]) |
        ($DateDayNumber::DateDayNumeric [day] [of] $DateMonth [month*] [ $DateYearNumber::DateYearNumeric [year*] | $Number::DateYearNumeric year* ] [[on] $DateWeekday]) [$DateWeekday] |
        ($DateYearNumber::DateYearNumeric|$DateYearShortNumber::DateYearNumeric) [year*] $DateMonth [month*] $DateDayNumber::DateDayNumeric [day] |
        $DateWeekday ($DateDayNumber::DateDayNumeric [ay]) |
        ([the] $NumberOrdinalDigit::DateDayNumeric [ay]) [$DateWeekday] |
        $DateYearNumber::DateYearNumeric (year*) |
        [at] $DateMonth [the] ($DateDayNumber::DateDayNumeric|$NumberDozenWithDash::DateDayNumeric) |
        $DateDayNumber::DateDayNumeric ($DateMonthDigit::DateMonth|$DateMonth) ($DateYearNumberWithSpace::DateYearNumeric|$DateYearShortNumber::DateYearNumeric) |
        $DateMonth $DateYearNumber::DateYearNumeric [year*] |
        $DateAbsoluteEn::Date |
        $DateWithSlashesAndWeekDay::Date |
        $DateWithDots::Date |
        $DateHoliday [[in] ([$DateYearNumber [year*]|$DateRelativeYear])] |
        $DateWeekdayOrder
        ) ||  converter = $converters.absoluteDateConverter


    $DateWeekdayOrder = $Number $DateWeekday [of] ($DateMonth|$DateRelativeMonth) [[in] $DateYearNumber [year]|$DateRelativeYear]  ||  converter = $converters.dateWeekdayOrderConverter

    $DateTimeSpecial = (
        $DateDayDigit::Day (at/by/on) $TimeAbsolute
        ) ||  converter = $converters.specialDateTimeConverter

    $TimeDateHoursModifier = $TimeAbsolute [on|of] $DateAbsolute ||  converter = $converters.composeTimeDateHoursModifierConverter

    $TimeDateRelativeHoursModifier = $TimeAbsolute [in [a]|after|on] $DateRelative [$TimeHoursModifier] ||  converter = $converters.composeTimeDateHoursModifierConverter

    $DateTimeAbsolute = (
        $DateAbsolute |
        [at|for] $TimeAbsolute |
        $TimeDateHoursModifier |
        $DateAbsolute [at|by|on] $TimeAbsolute |
        $DateTimeSpecial |
        [in [a]|after] $DateRelative [at|on|before|after] $TimeAbsolute |
        $TimeDateRelativeHoursModifier |
        $DateTimeEnSpecial
        ) ||  converter = $converters.composeConverter

    $DateTimeRelativeYears = ( (in [a]|after) [one] (year):1 | $Number (years) ) ||  converter = $converters.numberConverterSum
    $DateTimeRelativeMothes = ( (in [a]|after|a) [one] (month):1 | (half of [the] year):6 | (year and a half):18 | $Number (monthes|month*) ) ||  converter = $converters.numberConverterSum
    $DateTimeRelativeWeeks = ( $Number (week*) :7 | (fortnight) :14 | (week and a half) : 10 | (in [a]|after) [one] (week|sennight) :7 ) ||  converter = $converters.numberConverterMultiply
    $DateTimeRelativeSimpleDays = ( (one|in [a]|after) (day):1 | $Number (day|days) )
    $DateTimeRelativeDays = ({($DateTimeRelativeSimpleDays) [and|,] [$DateTimeRelativeWeeks]}|$DateTimeRelativeWeeks) ||  converter = $converters.numberConverterSum
    $DateTimeRelativeWeekDays = $Number business (day|days) ||  converter = $converters.numberConverterSum


    $DateTimeSimpleInterval = (
        $DateTimeRelativeYears [and] [$DateTimeRelativeMothes] [and] [$DateTimeRelativeDays|$DateTimeRelativeWeekDays] [and] [$TimeRelativeHours] [and] [$TimeRelativeMinutes] [and] [$TimeRelativeSeconds] | 
        $DateTimeRelativeMothes [and] [$DateTimeRelativeDays|$DateTimeRelativeWeekDays] [and] [$TimeRelativeHours] [and] [$TimeRelativeMinutes] [and] [$TimeRelativeSeconds] |
        ($DateTimeRelativeDays|$DateTimeRelativeWeekDays) [and] [$TimeRelativeHours] [and] [$TimeRelativeMinutes] [and] [$TimeRelativeSeconds] |
        $TimeRelativeHours [and] [$TimeRelativeMinutes] [and] [$TimeRelativeSeconds] |
        $TimeRelativeMinutes [and] [$TimeRelativeSeconds] |
        $TimeRelativeSeconds
        ) ||  converter = $converters.relativeDateTimeConverter

    $DateTimeRelativeSimple = (
        [in [a]|after|for] $DateTimeSimpleInterval :1 |
        $DateTimeSimpleInterval (from now|from today|later):1 |
        $DateTimeSimpleInterval (before|ago):-1
        ) ||  converter = $converters.relativeDateTimeMultiplierConverter

    $DateTimeNow = (now|today) ||  converter = $converters.nowDateTimeConverter

    $DateTimeRelative = ($DateTimeRelativeSimple|$DateTimeNow|$DateRelative) ||  converter = $converters.propagateConverter

    $DateTimeEnSpecial = (
        tonight: 1
        ) ||  converter = $converters.specialDateTimeEnConverter

    $TimeEn = $regexp<(one|two|three|four|five|six|seven|eight|nine)-(twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)> ||  converter = $converters.timeAbsoluteEnConverter    

    $DateTime = ($DateTimeAbsolute|$DateTimeRelative) ||  converter = $converters.normalizeDateTimeConverter
    #DateTime = ($DateTimeAbsolute|$DateTimeRelative) ||  converter = $converters.absoluteDateTimeConverter