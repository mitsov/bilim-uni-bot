require: ../number/number.sc
require: floatNumberConverter.js

patterns:
    $FloatNumberFraction = ((десяты*|десятки|tenth*):10 |
        (соты*|сотки|hundredth*):100 |
        (тысяч*|thousandth*):1000 |
        (десят* тысяч*|десятитысяч*|ten-thousandth*):10000)
    
    $nought = nought

    $fractionWithDelimeter = $regexp<\d+\/10+>
    $floatNumberWithDelimeter = $regexp<\d+[.,]\d+> || converter = $converters.floatNumberWithDelimeterConverter
    $floatNumberWordsDelimeted = ($Number::IntegerNumber (цел*|запятая|и) [и] ($Number::FractionalNumber [$FloatNumberFraction]|$fractionWithDelimeter) | 
                                  $Number::IntegerNumber point [$repeat<$nought>] $Number::FractionalNumber [$FloatNumberFraction]) || converter = $converters.floatNumberWordsDelimetedConverter
    
    $numberAndHalf = $Number (с половиной/and half) || converter = $converters.numberAndHalfConverter
    $specialNumber = ((полтора/one and [a] half):1|(~прямой ~угол):2|(~половина ~прямой ~угол):7|половина:8|четверть:9|треть:10) || converter = $converters.specialFloatNumberConverter
    $numberDivision = $regexp<\d+(\/)\d+>:5 || converter = $converters.specialFloatNumberConverter
    $piNumber = (([числ*] [$Number|$NumberDigit::Number] пи | [$Number|$NumberDigit::Number] [number] (pi|π)):3|
                [число*] пи [на] (пополам|два|2):4|
                [число*] (три|3) пи [на] (пополам|два|2):6|
                [число*] пи [на] (четыре|4):11|
                [число*] пи [на] (шесть|6):12|
                [число*] $Number пи [на] $Number:13) || converter = $converters.specialFloatNumberConverter
    $piNumberWithoutSpaces = $regexp<\-?\d+(пи|pi|π)(/\d+)?> || converter = $converters.piNumberWithoutSpacesConverter
    $specialFloatNumber = ($specialNumber|$numberAndHalf|$numberDivision)

    $Denominator = ((первых|первая|первой|первую|первые|first*):1|
        (вторых|вторая|второй|вторую|вторые|half):2|
        (третьих|трети|треть|третья|третьей|третью|~третий|третьи|third*):3|
        (четвертых|четверти|четвертая|четвертую|четвертой|четвертые|fourth*):4|
        (пятых|пятая|пятую|пятой|пятые|fifth*):5|
        (шестых|шестая|шестой|шестую|шестые|sixth*):6|
        (седьмых|седьмая|седьмой|седьмую|седьмые|seventh*):7|
        (восьмых|восьмая|восьмой|восьмую|восьмые|eighth*):8|
        (девятых|девятая|девятой|девятую|девятые|ninth*):9|
        (десятых|десятая|десятой|десятую|десятые|tenth*):10|
        (одиннадцатых|одиннадцатая|одиннадцатую|одиннадцатой|одиннадцатые|eleventh*):11|
        (двенадцатых|двенадцатая|двенадцатой|двенадцатую|двенадцатые|twelfth*):12|
        (тринадцатых|тринадцатая|тринадцатой|тринадцатую|тринадцатые|thirteenth*):13|
        (четырнадцатых|четырнадцатая|четырнадцатой|четырнадцатую|четырнадцатые|fourteenth*):14|
        (пятнадцатых|пятнадцатая|пятнадцатой|пятнадцатую|пятнадцатые|fifteenth*):15|
        (шестнадцатых|шестнадцатая|шестнадцатой|шестнадцатую|шестнадцатые|sixteenth*):16|
        (семнадцатых|семнадцатая|семнадцатой|семнадцатую|семнадцатые|seventeenth*):17|
        (восемнадцатых|восемнадцатая|восемнадцатой|восемнадцатую|восемнадцатые|eighteenth*):18|
        (девятнадцатых|девятнадцатая|девятнадцатой|девятнадцатую|девятнадцатые|nineteenth*):19|
        (двадцатых|двадцатая|двадцатой|двадцатую|двадцатые|twentieth*):20|
        (тридцатых|тридцатая|тридцатой|тридцатую|тридцатые|thirtieth*):30|
        (сороковых|сороковая|сороковой|сороковую|сороковые|fortieth*):40|
        (пятидесятых|пятидесятая|пятидесятой|пятидесятую|пятидесятые|fiftieth*):50|
        (шестидесятых|шестидесятая|шестидесятой|шестидесятую|шестидесятые|sixtieth*):60|
        (семидесятых|семидесятая|семидесятой|семидесятую|семидесятые|seventieth*):70|
        (восьмидесятых|восьмидесятая|восьмидесятой|восьмидесятую|восьмидесятые|eightieth*):80|
        (девяностых|девяностая|девяностой|девяностую|девяностые|ninetieth*):90|
        (сотых|сотая|сотой|сотую|сотые|one hundredth*):100|
        (двухсотых|двухсотая|двухсотой|двухсотую|двухсотые|two hundredth*):200|
        (трехсотых|трехсотая|трехсотой|трехсотую|трехсотые|three hundredth*):300|
        (четырехсотых|четырехсотая|четырехсотой|четырехсотую|четырехсотые|four hundredth*):400|
        (пятисотых|пятисотая|пятисотой|пятисотую|пятисотые|five hundredth*):500|
        (шестисотых|шестисотая|шестисотой|шестисотую|шестисотые|six hundredth*):600|
        (семисотых|семисотая|семисотой|семисотую|семисотые|seven hundredth*):700|
        (восьмисотых|восьмисотая|восьмисотой|восьмисотую|восьмисотые|eight hundredth*):800|
        (девятисотых|девятисотая|девятисотой|девятисотую|девятисотые|nine hundredth*):900|
        (тысячных|тысячная|тысячную|тысячной|тысячные|thousandth*):1000|
        (десяти тысячных|десяти тысячная|десяти тысячной|десяти тысячную|десяти тысячные|десятитысячных|десятитысячная|десятитысячной|десятитысячную|десятитысячные|ten-thousandth*):10000|
        (сто тысячных|сто тысячная|сто тысячной|сто тысячную|сто тысячные|стотысячных|стотысячная|стотысячной|стотысячную|стотысячные|hundredth-thousandth*):100000|
        (миллионных|миллионная|миллионной|миллионную|миллионные|millionth*) :1000000|
        (миллиардных|миллиардная|миллиардной|миллиардную|миллиардные|billionth*) :1000000000)
    $Fraction = $Number [$Number] $Denominator|| converter = $converters.fractions

    $FloatNumber = ($floatNumberWordsDelimeted|$floatNumberWithDelimeter|$specialFloatNumber|$Fraction) || converter = $converters.propagateConverter

    $FloatMillions = $FloatNumber (миллион*|милион*|млн|лям*|million*|millon*) || converter = $converters.floatMillionsConverter

    


    $CalcNumber = ($Number|$FloatNumber) || converter = $converters.propagateConverter
    $CalcQuestion = [это] (будет [ли] [равн*]|равн* [ли]|это|=| equals) $CalcNumber

    $combinedExpressionWithoutSpaces = $regexp<(ноль|один|два|три|четыре|пять|шесть|семь|восемь|девять|десять|одиннадцать|двенадцать|тринадцать|четырнадцать|пятнадцать|шестнадцать|семнадцать|восемнадцать|девятнадцать|двадцать)(\+|-|\*|×|÷|\/)\d+> || converter = $converters.combinedExpressionWithoutSpacesConverter
    $digitWithOperator = $regexp<((\+|-)\d+)+>
    $SimpleExpressionWithoutSpaces = $regexp<(-?\d+[\.|,]?\d*)(\+|-|\*|×|÷|\/)(-?\d+[\.|,]?\d*)>
    $NegNumberWithoutSpaces = $regexp<\-\d+[\.|,]?\d*>
    $divider = $regexp<(÷|\/)>
    $star = $regexp<(\*|×)>  
    $CalcNegOp = (минус|-)
    $CalcMul = (дважды:2 | трижды:3 | четырежды:4 | пятью:5 | шестью:6 | семью:7 | восемью:8 | девятью:9)
    $CalcOp = (([плюс] плюс*|минус минус|прибав*|добав*|сложи с|plus|add |+):+ |
                ((minus|subtract|[плюс] минус|отнять|отними*|вычесть|вычита*|вычти|отнимай|-) [от|из|from]):- |
                (*множ* [на]|$star|multipl* [by/about]|times|x):mul |
                (divid* [by]|(*дели*|дели) [на]|$divider):del)
    $leftBracket = $regexp<\(>
    $rightBracket = $regexp<\)>

    $NegNumber = ($CalcNegOp $CalcNumber|$NegNumberWithoutSpaces)
    $CalcOperand = ($CalcNumber|$NegNumber|[$CalcOp] $MathExpressionInBrackets)
    $MathExpressionInBrackets = $leftBracket ([$CalcNumber] $repeat<$MathExpression>) $rightBracket

    $inHalf = ($CalcOperand пополам|$CalcOperand in half)
    $takePart = ($Number::part часть [от] $CalcOperand::num|[от] $CalcOperand::num $Number::part часть|$CalcOperand::part part [of] $Number::num)
    $Factorial = ({$Number (факториал|factorial [of])}|$FactorialExpression)
    $FactorialExpression = $regexp<\d+\!>
    $CalcOpPrefix = ((~сумма|amount|sum|total):+|(~разность/~разница/differen*):-|(~произведение/multipl* [by]):mul/(частное/divided [by]):del)
    
    $CalcPower = (([возведи*] [в] (квадрате/квадрат) [числ*]| squared | to [the] second power):2|
                 ([возведи*] [в] (куб|кубе) [числ*]| cubed | to [the] third power):3|
                 [возв*] [в|во] {степен* $CalcOperand} [числ*]|
                 [возв*] [в|во] $CalcOperand степень [числ*]|
                 [возв*] (в|во) $CalcOperand [числ*]|
                 to [the] $CalcOperand power)
    $CalcFuncSqrt = ([квадрат*] ~корень [квадрат*] [из/от] |
                    ([the] [squar*] root [out of/of]) |
                    ([the] squar* (out of/of)))
    $CalcFuncCbrt = ({кубич* кор*} [из/от] | [the] cube root [out of/of])
    $CalcFuncRt = ~корень {$CalcOperand степени} [из|от]

    $CalcFuncLn = ([натуральн*] логарифм* [от]  | [the] [natural*] (logarithm|log) * [of])
    $CalcFuncLg = (десятичн* логарифм* [от] |логарифм по основанию (десять|10) [от] | [the] (decimal|common) (logarithm|log) * [of])
    $CalcFuncLog = (логарифм по основанию $CalcNumber::base [от] $CalcNumber::num|логарифм [от] $CalcNumber::num по основанию $CalcNumber::base| [the] base $CalcNumber::base (logarithm|log) * [of] $CalcNumber::num)
    $CalcFunc = ($CalcFuncSqrt|$CalcFuncLn|$CalcFuncCbrt|$CalcFuncRt)
    
    $CalcPercent = $CalcNumber ((процент*/%) [от/из] | (percent*/per cent/%) [of] [the])

    $CalcSin = (синус* [для|от] [угл* [в]] | (sine|sin) [of])
    $CalcCos = (косинус* [для|от] [угл* [в]] | (cosine|cos) [of])
    $CalcTan = (тангенс* [для|от] [угл* [в]] | (tangent/tg/tan) [of])
    $CalcCtg = (котангенс* [для|от] [угл* [в]] | (сotangent/ctg) [of])
    $CalcTrigonometryDegree = ($CalcSin|$CalcCos|$CalcTan|$CalcCtg)
    
    $CalcSinWithoutSpaces = $regexp<sin-?([0-9]+)>
    $CalcCosWithoutSpaces = $regexp<cos-?([0-9]+)>
    $CalcTgWithoutSpaces = $regexp<tg-?([0-9]+)>
    $CalcCtgWithoutSpaces = $regexp<ctg-?([0-9]+)>
    $CalcTrigonometryDegreeWithoutSpaces = ($CalcSinWithoutSpaces|$CalcCosWithoutSpaces|$CalcTgWithoutSpaces|$CalcCtgWithoutSpaces)
    
    $CalcRSin = (синус* [для|от] [угл* [в]] | (sine|sin) [of]) [$CalcNegOp]
    $CalcRCos = (косинус* [для|от] [угл* [в]] | (cosine|cos) [of]) [$CalcNegOp]
    $CalcRTan = (тангенс* [для|от] [угл* [в]] | (tangent/tg/tan) [of]) [$CalcNegOp]
    $CalcRCtg = (котангенс* [для|от] [угл* [в]]| (сotangent/ctg) [of]) [$CalcNegOp]
    $CalcTrigonometryRadian = ($CalcRSin|$CalcRCos|$CalcRTan|$CalcRCtg)


    $FreeSimpleExpression = ($CalcOp [$CalcNumber]|[$CalcNumber] $CalcPower|$CalcPower [$CalcNumber]|$digitWithOperator) || converter = $converters.mathSimpleExpressionConverter
    $SimpleExpression = ($CalcOp $CalcNumber|[$CalcNumber] $CalcPower|$CalcPower [$CalcNumber]|$CalcFuncLg [$CalcNumber]|$CalcFuncLog|$takePart|$inHalf|$digitWithOperator|($CalcOpPrefix [между/between] $CalcNumber::Number1 (и/and) $CalcNumber::Number2)) || converter = $converters.mathSimpleExpressionConverter

    $MathExpression = [$CalcOp] ($MathExpressionInBrackets|$specialMathExpression|$SimpleExpression|$CalcFunc [$CalcNumber]|$CalcPercent [$CalcNumber]|$Factorial|$CalcMul [$CalcNumber]|$CalcTrigonometryDegree $CalcOperand [градус*]|$CalcTrigonometryDegreeWithoutSpaces|$CalcTrigonometryRadian ($CalcNumber (радиан*/radian*)|($piNumber|$piNumberWithoutSpaces) [радиан*/radian*])) || converter = $converters.mathExpressionConverter

    $mathMinus1 = (вычти|вычесть|отними | subtract) 
    $mathMinus2 = (вычти|вычесть|отними | subtract) 
    $mathMinus3 = (минус/subtract) 
    $mathPlus = (сложи|сложить|add)
    $mathAdd = (прибавь|прибавить)
    $mathAdd2 = (и/plus)
    $mathDiv = (*делить|дели|раздели|подели|divide)
    $mathMul1 = (помножь|перемножь|умножь|умножить|перемножить|помножить|multiply)
    $mathMul2 = (на|раз* по|by|about)
    $mathMul3 = (от|from)
    $specialMathExpression = ($mathMinus1 $CalcOperand (из|от|from) $CalcOperand|
        $mathMinus2 (из|от|from) $CalcOperand $CalcOperand|
        $mathPlus $CalcOperand (и|с|and) $CalcOperand|
        $mathAdd {(к/to) $CalcOperand} $CalcOperand|
        $CalcOperand $mathAdd2::mathAdd $CalcOperand|
        $mathMul1 $CalcOperand (и|на|and) $CalcOperand|
        $mathDiv $CalcOperand (на|by) $CalcOperand|
        $CalcOperand $mathMul2 $CalcOperand|
        $SimpleExpressionWithoutSpaces|
        $combinedExpressionWithoutSpaces::SimpleExpressionWithoutSpaces|
        $CalcOperand $mathMul3 $CalcOperand) || converter = $converters.specialMathExpressionConverter