require: ../../common/common.sc
require: ../../common/calculator/Calculator.js

require: floatNumber/floatNumber.sc
  module = zb_common
require: patternsEn.sc
  module = zb_common

require: ../main.js

require: answers.yaml
  var = CalculatorCommonAnswers

init:
    $global.$CalculatorAnswers = (typeof CalculatorCustomAnswers != 'undefined') ? applyCustomAnswers(CalculatorCommonAnswers, CalculatorCustomAnswers) : CalculatorCommonAnswers;


theme: /Calculator
    state: Hint

        state: Result
            q: [it/result/equal*] ['s|is]  (seven/семь/7) || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($CalculatorAnswers["Сompliment"]) }}

    state: ExpressionCatchAll
        q!: * {$CalcOp * [much/result]} *
        q!: * (sine/cosine/summ/tangent/root/power/degree/exponent/involution/logarithm) *
        script:
            $reactions.answer(selectRandomArg($CalculatorAnswers["Cannot count"]));

    state: Expression
        q!: * [( (how much|what) [it] [is|'s|will be|shall be]|result)] * [$CalcNumber] $repeat<$MathExpression> [$CalcQuestion|(how much|result)] *
        q!: * $MathExpression *
        q!: * $CalcNumber $CalcOp $CalcNumber *
        script: 
            log(toPrettyString($parseTree));
            var res = calculate($parseTree); 
            switch(res) {
                case "NaN":
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
                        if ($parseTree.CalcQuestion[0].CalcNumber[0].value == parseFloat(res.replace(",","."))) {
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
            q: {[and] [now] [[the] result]} ($repeat<$SimpleExpression>|$CalcFunc) [$CalcQuestion] || fromState = .., onlyThisState = true
            if: ($session.calcResult != null)
                script:
                    $session.calcResult = $session.calcResult.toString().replace(",", "."); 
                    var res;
                    if ($parseTree.CalcFunc) {
                        res = contextFuncCalculate($parseTree);
                    } else {
                        res = simpleCalculate($parseTree);
                    }
                    switch(res) {
                        case "NaN":
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
                                if ($parseTree.CalcQuestion[0].CalcNumber[0].value == res) {
                                    $reactions.answer(selectRandomArg($CalculatorAnswers["Yes answer"]));
                                } else {
                                    $reactions.answer(selectRandomArg($CalculatorAnswers["No answer"]));
                                }
                            }
                            $reactions.answer(prepareEAnswer(res));
                            $session.calcResult = res;
                    }
            else:
                script:
                    $reactions.answer(selectRandomArg($CalculatorAnswers["Start again"]));

            state: SimpleExpression2
                q: * ($repeat<$SimpleExpression>| $CalcFunc) [$CalcQuestion] * || fromState = .., onlyThisState = true
                go!: ../../SimpleExpression       

    state: Pi
        q!: * $piNumber
        a: {{$parseTree.piNumber[0].value.toFixed(5)}}

    state: Incorrect
        q: [you ['r/are]] * (lier/wrong/incorrect/no/nope/not) [maths/calculat*/result*]
        script:
            $reactions.answer(selectRandomArg($CalculatorAnswers["Incorrect"]));

    state: Correct
        q: (rigth/good/yes/thank* [you])
        script:
            $reactions.answer(selectRandomArg($CalculatorAnswers["Correct"]));

    state: Fragment || noContext = true
        q!:   calculate
        script:
            $reactions.answer(selectRandomArg($CalculatorAnswers["Fragment"]));

    state: ParsingIssuesFixingAttempt
        q: $regexp<((\w+\s){0,3}\w+|\d+)(\/|-)((\w+\s){0,3}\w+|\d+)> || fromState = /, onlyThisState = true
        q: $regexp<((\w+\s){0,3}\w+|\d+)(\/|-)((\w+\s){0,3}\w+|\d+)> || fromState = /Calculator, onlyThisState = true
        script:
            var text = $parseTree.text;
            text = text.replace("/", " / ");
            text = text.replace("-", " - ");
            
            var result = $nlp.match(text, '/');
            $temp.nextState = result.targetState;
            $parseTree = result.parseTree;
        go!: {{$temp.nextState}}