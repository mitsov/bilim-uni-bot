require: ../common.js
require: numberConverter.js

patterns:
    $NumberDigit = $regexp<\d+>
            || converter = $converters.numberConverterDigit
    
    $NubmerCommaSeparated = $regexp<\d+\.\d+(\.\d+)+>
            || converter = $converters.numberConverterCommaSeparatedDigit

    $NumberOrdinalDigit = $regexp<\d?(1st|2nd|3rd|\dth)>
            || converter = $converters.numberConverterOrdinalDigit

    $NumberDotZero = $regexp<[0-9]+(\.[0]{3,})+>
            || converter = $converters.numberConverterCommaSeparatedDigit

    $NumberOneDigit = (
        (нол*|нул*|zero):0 | 
        (~один|~одна|перв*|единиц*|единичк*|однерка|однёрка|one|first):1 | 
        (~два|дву*|две|втор*|пара|пару|двое|~двойка|~двоечка|two|[a] couple of|second):2 | 
        (~три|трет*|трех|трое|three|~тройка|~троечка|~трешечка|third):3 | 
        (~четыре|четверт*|четырех|четверо|~четверка|~четверочка|*four|fourth):4 | 
        (~пять|пята*|пято*|пятый|пятым*|пяти|пятер*|five|fifth):5 | 
        (~шесть|шест*|шестым*|шести|шестер*|six|sixth):6 | 
        (~семь|седьм*|семи|семер*|seven|seventh):7 | 
        (~восемь|восьм*|восем|eight*):8 | 
        (~девять|~девятка|девят*|девятый|~девяточка|nine|ninth):9 )
            || converter = $converters.numberConverterValue

    $NumberTwoDigit = (
        (десят*|ten|tenth):10 | 
        (одиннадцат*|eleven*):11 | 
        (двенадцат*|twelve|twelfth):12 | 
        (тринадцат*|thirteen*):13 | 
        (четырнадцат*|fourteen*):14 | 
        (пятнадцат*|fifteen*):15 | 
        (шестнадцат*|sixteen*):16 | 
        (семнадцат*|seventeen*):17 | 
        (восемнадцат*|eighteen*):18 | 
        (девятнадцат*|nineteen*):19 )
            || converter = $converters.numberConverterValue

    $NumberNumeric = ($NumberOneDigit | $NumberTwoDigit)
            || converter = $converters.propagateConverter

    $NumberSimple = ($NumberDigit | $NumberOrdinalDigit | $NumberNumeric)
            || converter = $converters.numberConverterSum

    $NumberDozenWithDash = $regexp<(twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)-((one|two|three|four|five|six|seven|eight|nine)|(first|second|third|fourth|fifth|sixth|seventh|eighth|nineth))>
            || converter = $converters.numberConverterDozenDash

    $NumberDozen = (
        (двадцать|двадцат*|twenty|twentieth):20 | 
        (тридцать|тридцат*|thirty|thirtieth):30 | 
        (сорок*|forty|fortieth):40 | 
        (пятьдесят|пятидесят*|fifty*|fiftieth):50 | 
        (шестьдесят|шестидесят*|шисят|sixty|sixtieth):60 | 
        (семьдесят|семидесят*|seventy|seventieth):70 | 
        (восемьдесят|восьмидесят*|eighty|eightieth):80 | 
        (девяносто|девяност*|ninety|ninetieth):90 )
            || converter = $converters.numberConverterValue

    $NumberHundred = (
        (сто|ста|[~один] ~сотня|~соточка):100 | 
        (двести|двухсот*|двухста|~два ~сотня):200 | 
        (триста|тристо|трехсот*|трёхста|трехста/~три ~сотня):300 | 
        (четырест*|четырехсот*|четырехста|четырёста/~четыре ~сотня):400 | 
        (пятьсот|пятисот*|пятиста/~пять ~сотня):500 | 
        (шестьсот|шестисот*|шестиста/~шесть ~сотня):600 | 
        (семьсот|семисот*|семиста/~семь ~сотня):700 | 
        (восемьсот|восьмисот*|восемьста/~восемь ~сотня):800 | 
        (девятьсот|девятисот*|девятиста/~девять ~сотня):900 )
            || converter = $converters.numberConverterValue

    $NumberHundredComplex = 
        [$NumberDozen] [$NumberSimple] hundred*: 100
            || converter = $converters.numberConverterMultiply
        
    $NumberThreeDigit = (
        $NumberHundredComplex [and] [[$NumberDozen] [$NumberSimple]|$NumberDozenWithDash] |
        $NumberHundred [and] [[$NumberTwoDigit]|[$NumberDozen] [$NumberSimple]|$NumberDozenWithDash] |
        $NumberDozen [$NumberSimple] |
        $NumberDozenWithDash |
        $NumberSimple 
        )
            || converter = $converters.numberConverterSum

    $NumberThousands = [$NumberThreeDigit] (тысяч*|тыщ*|тыс|к|т|thousand*):1000
            || converter = $converters.numberConverterMultiply

    $NumberMillions = [$NumberThreeDigit] (миллион*|милион*|млн|лям*|million*|millon*):1000000
            || converter = $converters.numberConverterMultiply

    $NumberBillions = [$NumberThreeDigit] (миллиард*|лярд*|billion*|billones*):1000000000
            || converter = $converters.numberConverterMultiply

    $Number = (
        $NumberBillions [and|и] [$NumberMillions] [and|и] [$NumberThousands] [and|и] [$NumberThreeDigit] |
        $NumberMillions [and|и] [$NumberThousands] [and|и] [$NumberThreeDigit] |
        $NumberThousands [and|и] [$NumberThreeDigit] |
        $NumberThreeDigit |
        $NubmerCommaSeparated |
        $NumberDotZero
        )
            || converter = $converters.numberConverterSum

theme: /
    state: nlp || modal = true
        q: $Number  || fromState = .