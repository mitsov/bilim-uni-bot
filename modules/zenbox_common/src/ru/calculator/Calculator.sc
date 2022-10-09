require: ../../common/common.sc
require: ../../common/calculator/Calculator.js

require: floatNumber/floatNumber.sc
  module = zb_common

require: ../main.js

require: answers.yaml
  var = CalculatorCommonAnswers

init:
    $global.$CalculatorAnswers = (typeof CalculatorCustomAnswers != 'undefined') ? applyCustomAnswers(CalculatorCommonAnswers, CalculatorCustomAnswers) : CalculatorCommonAnswers;


theme: /Calculator

    state: Hint

        state: Result
            q: [будет/получится/результат] (семь/7) || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($CalculatorAnswers["Сompliment"]) }}

    state: ExpressionCatchAll
        q!: * (ско*/сколько) будет *
        q!: * чему [будет] равн* *
        q!: * (синус/косинус/сумма/разность/тангенс/корень/степень/логарифм) *
        script:
            $reactions.answer(selectRandomArg($CalculatorAnswers["Cannot count"]));

    state: Expression
        q!: * [((ско*/сколько) будет|чему [будет] равн*)|$CalcQuestion [если]] * [$CalcNumber] $repeat<$MathExpression> [$CalcQuestion|(ско* будет|чему [будет] равн*)] *
        script: 
            var res = calculate($parseTree); 
            switch(res) {
                case "NaN":
                case "Invalid input":
                case "40828098382988424":
                    $reactions.answer(selectRandomArg($CalculatorAnswers["Invalid operation"]));
                    $session.calcResult = null; 
                    break;              
                case "error":
                    $reactions.answer(selectRandomArg($CalculatorAnswers["Cannot count"]));
                    $session.calcResult = null;     
                    break;                    
                default:
                    if ($parseTree.CalcQuestion) {
                        if ($parseTree.CalcQuestion[0].CalcNumber[0].value.toString().replace(/[.]/, ",") == res) {
                            $reactions.answer(selectRandomArg($CalculatorAnswers["Yes answer"]) + " " + res);
                        } else {
                            $reactions.answer(selectRandomArg($CalculatorAnswers["No answer"]) + " " + res);
                        }
                    } else {
                        $reactions.answer(prepareEAnswer(res));
                    }
                    $session.calcResult = res;
            }


        state: SimpleExpression
            q: {[а] [теперь] [результат]} [$CalcQuestion [если]] ($repeat<$SimpleExpression>|{[извлеки*|извлечь|возведи*] $CalcFunc}) [$CalcQuestion] || fromState = .., onlyThisState = true   
            script:
                try {
                    if ($session.calcResult != null && $session.calcResult != "" ) {
                        $session.calcResult = $session.calcResult.toString().replace(",", "."); 
                    }
                    var res;
                    res = simpleCalculate($parseTree);
                    switch(res) {
                        case "NaN":
                        case "Invalid input":
                        case "40828098382988424":
                            $reactions.answer(selectRandomArg($CalculatorAnswers["Invalid operation"]));
                            $session.calcResult = null; 
                            break;              
                        case "error":
                            $reactions.answer(selectRandomArg($CalculatorAnswers["Cannot count"]));
                            $session.calcResult = null;     
                            break;                                   
                        default:
                            if ($parseTree.CalcQuestion) {
                                if ($parseTree.CalcQuestion[0].CalcNumber[0].value.toString().replace(/[.]/, ",") == res) {
                                    $reactions.answer(selectRandomArg($CalculatorAnswers["Yes answer"]) + " " + res);
                                } else {
                                    $reactions.answer(selectRandomArg($CalculatorAnswers["No answer"]) + " " + res);
                                }
                            } else {
                                $reactions.answer(prepareEAnswer(res));
                            }
                            $session.calcResult = res;
                    }
                } catch (error) {
                    $reactions.answer(selectRandomArg($CalculatorAnswers["Start again"]));
                }

            state: SimpleExpression2
                q: {[а] [теперь] [результат]} ($repeat<$SimpleExpression>|{[извлеки*|извлечь] $CalcFunc}) [$CalcQuestion] || fromState = .., onlyThisState = true
                go!: ../../SimpleExpression       

    state: Pi
        q!: * [назови] $piNumber
        a: {{$parseTree.piNumber[0].value.toFixed(5)}}

    state: Fraction
        q!: * [назови] $Fraction
        script:
            var fraction_arr = $parseTree.Fraction[0].value.toString().split("/");
            var res = Number(fraction_arr[0]) / Number(fraction_arr[1]);
            res = Math.round(res * 100) / 100;
            res = res.toString().replace(".", ",");
            $reactions.answer(prepareEAnswer(res));


    state: Incorrect
        q: (врешь/неправильно/не правильно/нет)
        script:
            $reactions.answer(selectRandomArg($CalculatorAnswers["Incorrect"]));

    state: Correct
        q: (правильно/молодец/да)
        script:
            $reactions.answer(selectRandomArg($CalculatorAnswers["Correct"]));

    state: Fragment || noContext = true
        q!:   сколько будет
        script:
            $reactions.answer(selectRandomArg($CalculatorAnswers["Fragment"]));