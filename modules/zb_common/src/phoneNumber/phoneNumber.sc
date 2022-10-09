
# Паттерн для российских номеров мобильных телефонов, состоящих из трех частей:
# 1) 8 или +7
# 2) цифра 9
# 3) 9 цифр
# 
# В паттерн попадают:
#     1) номера без первой части (8 или +7), начинающиеся на 9 и состоящие из 10 цифр. Например: 987 603 29 22
#     2) номера, начинающиеся на 89 и состоящие из 11 цифр. Например: 8 987 603 29 22
#     3) номера, начинающиеся на +79 и состоящие из 12 символов. Например: +7 987 603 29 22
# В value хранится строка типа +79876032922.
# При вводе словами используется следующая конструкция:
#     [8/+7] (трёхзначное число, где первое == 9) (три цифры / ноль и двухзначное число / трехзначное число) (четыре цифры / ноль, цифра и двухзначное число / ноль и трехзначное число / два двухзначных числа / двухзначное число, ноль и цифра)
# При вводе числами между цифрами допускается использование круглых скобок, пробелов, тире. 


require: ../number/number.sc
require: ../patterns.sc
require: ../common.js
require: phoneNumber.js


patterns:
    $zeroPN = (нол*|нул*|zero):0 || converter = $converters.numberConverterValue
    $oneToNineNormForm =    (один :1 |
                            два :2 |
                            три :3 |
                            четыре :4 |
                            пять :5 |
                            шесть :6 |
                            семь :7 |
                            восемь :8 |
                            девять :9  ) || converter = $converters.numberConverterValue

    $oneToNineGent =    ((нуля/ноля) :0 |
                        (однерки/единицы) :1 |
                        двойки :2 |
                        тройки :3 |
                        четверки :4 |
                        пятерки :5 |
                        шестерки :6 |
                        семерки :7 |
                        восьмерки :8 |
                        девятки :9) || converter = $converters.numberConverterValue

    $digitsNormalForm = (ноль :0 | $oneToNineNormForm) || converter = function(pt) { return pt.oneToNineNormForm ? pt.oneToNineNormForm[0].value : 0}

    $twoSameDigits = ((две/два) $oneToNineGent) : 2   || converter = $converters.PhoneNumberSameDigits
    $threeSameDigits = (три $oneToNineGent) : 3  || converter = $converters.PhoneNumberSameDigits
    $fourSameDigits = (четыре $oneToNineGent) : 4  || converter = $converters.PhoneNumberSameDigits

    $dozenAndDigitPN = $NumberDozen $digitsNormalForm || converter = function(parseTree) { return parseTree.NumberDozen[0].value + parseTree.digitsNormalForm[0].value }
    $twoDigitNumberPN = ($NumberTwoDigit / $NumberDozen / $dozenAndDigitPN)

    $hundredAndDozenPN = $NumberHundred $twoDigitNumberPN || converter = function(parseTree) { return parseTree.NumberHundred[0].value + parseTree.twoDigitNumberPN[0].value }
    $hundredAndDigitPN = $NumberHundred $oneToNineNormForm || converter = function(parseTree) { return parseTree.NumberHundred[0].value + parseTree.oneToNineNormForm[0].value }
    $threeDigitNumberPN = ($NumberHundred / $hundredAndDozenPN / $hundredAndDigitPN)

    $NumberThousandsWithDigitPN = $NumberOneDigit (тысяч*|тыщ*|thousand*) || converter = function(parseTree) { return (1000 * parseTree.NumberOneDigit[0].value) }
    $NumberThousandsWithOutDigitPN = [одна] (тысяча|тыща|thousand*):1000
    $NumberThousandsPN = ($NumberThousandsWithDigitPN / $NumberThousandsWithOutDigitPN)

    $ThousandsAndDigitPN = $NumberThousandsPN $digitsNormalForm || converter = function(parseTree) { return parseInt(parseTree.NumberThousandsPN[0].value) + parseTree.digitsNormalForm[0].value }
    $ThousandsAndHundredPN = $NumberThousandsPN $threeDigitNumberPN || converter = function(parseTree) { return parseInt(parseTree.NumberThousandsPN[0].value) + parseInt(parseTree.threeDigitNumberPN[0].value) }
    $fourDigitNumberPN = ($NumberThousandsPN / $ThousandsAndHundredPN / $ThousandsAndDigitPN )

    $ThreeDigitsPN = (
                $threeSameDigits /
                $zeroPN $twoDigitNumberPN /
                $twoSameDigits $digitsNormalForm /
                $digitsNormalForm $digitsNormalForm $digitsNormalForm /
                $threeDigitNumberPN) || converter = $converters.collectParseTreeValues

    $FourDigitsPN = (
                $digitsNormalForm $digitsNormalForm $digitsNormalForm $digitsNormalForm / 
                $zeroPN $digitsNormalForm $twoDigitNumberPN / 
                $zeroPN $threeDigitNumberPN / 
                $twoDigitNumberPN $twoDigitNumberPN / 
                $twoDigitNumberPN $zeroPN $digitsNormalForm / 
                $twoSameDigits $zeroPN $digitsNormalForm / 
                $twoSameDigits $twoDigitNumberPN / 
                $twoDigitNumberPN $twoSameDigits / 
                $threeSameDigits $digitsNormalForm /
                $digitsNormalForm $threeSameDigits /
                $fourSameDigits /
                $fourDigitNumberPN) || converter = $converters.collectParseTreeValues



    $patternNine = (~девять|~девятка|девят*|девятый|~девяточка|nine|ninth):9
    $patternNinety = (девяносто|девяност*|ninety|ninetieth):90 
    $patternNinetyAndDigit = $patternNinety $digitsNormalForm || converter = function(parseTree) { return parseTree.patternNinety[0].value + parseTree.digitsNormalForm[0].value }

    $patternNineHundred = (девятьсот|девятисот*|девятиста/~девять ~сотня):900 
    $NineHundredAndDozenPN = $patternNineHundred $twoDigitNumberPN || converter = function(parseTree) { return parseInt(parseTree.patternNineHundred[0].value) + parseInt(parseTree.twoDigitNumberPN[0].value) }
    $NineHundredAndDigitPN = $patternNineHundred $oneToNineNormForm || converter = function(parseTree) { return parseInt(parseTree.patternNineHundred[0].value) + parseInt(parseTree.oneToNineNormForm[0].value) }

    $threeDigitNumberNinePN = ($patternNineHundred / $NineHundredAndDozenPN / $NineHundredAndDigitPN)

    $ThreeDigitsStartsWith9 = ($patternNine $twoSameDigits / $patternNine $digitsNormalForm $digitsNormalForm / $patternNine $twoDigitNumberPN / ($patternNinety/$patternNinetyAndDigit) $digitsNormalForm / $threeDigitNumberNinePN) || converter = $converters.collectParseTreeValues


    $eightOrSevenPN = ([плюс/+] (~семь|седьм*|семи|семер*|seven|seventh):7 | (~восемь|восьм*|восем|eight*):8 )


    $mobilePhoneNumberInWords = [$eightOrSevenPN] $ThreeDigitsStartsWith9 $ThreeDigitsPN $FourDigitsPN || converter = $converters.mobilePhoneNumberInWordsConverter

    $mobilePhoneNumberInDigits = ($regexp<(\+?(8|7)[\-\s]?)?\(?9\d{2}\)?[\-\s]?\d{3}[\-\s]?\d{2}[\-\s]?\d{2}>) || converter = $converters.mobilePhoneNumberInDigitsConverter
    

    $mobilePhoneNumber = ($mobilePhoneNumberInDigits / $mobilePhoneNumberInWords)