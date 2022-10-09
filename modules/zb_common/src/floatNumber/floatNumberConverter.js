$global.$converters = $global.$converters || {};
var cnv = $global.$converters;

cnv.floatNumberWithDelimeterConverter = function(parseTree) {
    return parseFloat(parseTree.text.replace(",", "."));
}

cnv.floatNumberWordsDelimetedConverter = function(parseTree) {
    var res, fraction;
    if (parseTree.fractionWithDelimeter) {
       fraction = eval(parseTree.fractionWithDelimeter[0].text);  
    } else {
        if (parseTree.FloatNumberFraction) {
            fraction = parseTree.FractionalNumber[0].value / parseTree.FloatNumberFraction[0].value;
        } else {
            fraction = parseTree.FractionalNumber[0].value / 10;
            if (parseTree.nought) {
                for (var i = 0; i < parseTree.nought.length; i++) {
                    fraction = parseTree.FractionalNumber[0].value / 10;
                }       
            }
            while (fraction >= 1) {
                fraction = fraction / 10;
            }
        }
    }
    res = parseFloat(parseTree.IntegerNumber[0].value) + parseFloat(fraction); 
    return res;        
}
cnv.numberAndHalfConverter = function(parseTree) {
    var num, ret;
    num = parseInt(parseTree.Number[0].value);
    ret = num + 0.5;
    return ret
}

cnv.piNumberWithoutSpacesConverter = function(parseTree) {
    var str = parseTree.text;
    str = str.replace("пи", "").replace("pi", "").replace("π", "");
    return "(" + str + ") * Math.PI";
}

cnv.fractions = function(parseTree) {
    var num = Number(parseTree.Number[0].value);
    var denominator = Number(parseTree.Denominator[0].value);
    if (parseTree.Number.length > 1) {
        denominator += Number(parseTree.Number[1].value);
    }
    return num + "/" + denominator;
}



cnv.mathExpressionConverter = function(parseTree) {
    var operation = '';
    var operand = '';
    var rt = '';
    if (parseTree.CalcOp) {
        if (parseTree.CalcOp[0].value === 'mul') {
            operation = '*';
        } else if (parseTree.CalcOp[0].value === 'del') {
            operation = '/';
        } else {
            operation = parseTree.CalcOp[0].value;
        }
    }
    if (parseTree.MathExpressionInBrackets) {
        return convertMathExpressionInBrackets(parseTree);
    }
    if (parseTree.SimpleExpression) {
        return operation + cnv.mathSimpleExpressionConverter(parseTree.SimpleExpression[0]);
    }
    if (parseTree.specialMathExpression) {
        return operation + cnv.specialMathExpressionConverter(parseTree.specialMathExpression[0]);
    }
    if (parseTree.CalcNumber) {
        operand = parseTree.CalcNumber[0].value;
    }
    if (parseTree.NegNumber) {
        if (parseTree.NegNumber[0].CalcNumber) {
            operand = "-" + parseTree.NegNumber[0].CalcNumber[0].value;
        } else {
            operand = parseTree.NegNumber[0].NegNumberWithoutSpaces[0].text;
        }
    }
    if (parseTree.Factorial) {
        var num;
        if (parseTree.Factorial[0].Number) {
            num = Number(parseTree.Factorial[0].Number[0].value);
        } else {
            num = Number(parseTree.Factorial[0].FactorialExpression[0].text);
        }
        var res = 1;
        for (var i = 2; i <= num; i++) {
            res += "*" + i
        }
        return res;
    }
    if (parseTree.CalcFunc) {
        if (parseTree.CalcFunc[0].CalcFuncSqrt) {
            operation += "Math.sqrt";
        } else if (parseTree.CalcFunc[0].CalcFuncLog) {
            operation += "Math.log10";
        } else if (parseTree.CalcFunc[0].CalcFuncLn) {
            operation += "Math.log";
        } else if (parseTree.CalcFunc[0].CalcFuncCbrt) {
            operation += "Math.pow";
            rt = "1/3";
        } else if (parseTree.CalcFunc[0].CalcFuncRt) {
            operation += "Math.pow";
            rt =  "1/" + getOperandNumber(parseTree.CalcFunc[0].CalcFuncRt[0].CalcOperand[0]);
        }
        return operation + "(" + (operand === '' ? "next" : operand) + (operation == "Math.pow" ? ", " + rt : "") + ")";
    }
    if (parseTree.CalcPercent) {
        var percent = parseTree.CalcPercent[0].CalcNumber[0].value;     
        return operation + "CalcPercent(" + percent + "," + (operand === '' ? "next" : operand) + ")";
    }
    if (parseTree.CalcMul) {
        return operation + parseTree.CalcMul[0].value + "*" + (operand === '' ? "next" : operand);
    }
    if (parseTree.CalcTrigonometryDegree) {
        if (operand === '') {
            operand = getOperandNumber(parseTree.CalcOperand[0]);
        }
        if (parseTree.CalcTrigonometryDegree[0].CalcSin) {
            operation += "Math.sin";
        } else if (parseTree.CalcTrigonometryDegree[0].CalcCos) {
            operation += "Math.cos";
        } else if (parseTree.CalcTrigonometryDegree[0].CalcTan) {
            if (operand%90 == 0 && operand%180 != 0) {
                return "Invalid input";
            } else {
                operation += "Math.tan";
            }
        } else if (parseTree.CalcTrigonometryDegree[0].CalcCtg) {
            if (operand%180 == 0) {
                return "Invalid input";
            } else {                   
                operation += "1.0/Math.tan";
            }  
        }
        return operation + "((" + operand + ") * (Math.PI/180))";
    }
    if (parseTree.CalcTrigonometryDegreeWithoutSpaces) {
        var operand;
        if (parseTree.CalcTrigonometryDegreeWithoutSpaces[0].CalcSinWithoutSpaces) {
            operand = Number(parseTree.CalcTrigonometryDegreeWithoutSpaces[0].CalcSinWithoutSpaces[0].text.match("-?[0-9]+")[0])
            operation += "Math.sin";
        } else if (parseTree.CalcTrigonometryDegreeWithoutSpaces[0].CalcCosWithoutSpaces) {
            operand = Number(parseTree.CalcTrigonometryDegreeWithoutSpaces[0].CalcCosWithoutSpaces[0].text.match("-?[0-9]+")[0])
            operation += "Math.cos";
        } else if (parseTree.CalcTrigonometryDegreeWithoutSpaces[0].CalcTgWithoutSpaces) {
            operand = Number(parseTree.CalcTrigonometryDegreeWithoutSpaces[0].CalcTgWithoutSpaces[0].text.match("-?[0-9]+")[0])
            if (operand%90 == 0 && operand%180 != 0) {
                return "Invalid input";
            } else {
                operation += "Math.tan";
            }
        } else if (parseTree.CalcTrigonometryDegreeWithoutSpaces[0].CalcCtgWithoutSpaces) {
            operand = Number(parseTree.CalcTrigonometryDegreeWithoutSpaces[0].CalcCtgWithoutSpaces[0].text.match("-?[0-9]+")[0])
            if (operand%180 == 0) {
                return "Invalid input";
            } else {                   
                operation += "1.0/Math.tan";
            }  
        }
        return operation + "(" + operand + " * (Math.PI/180))";
    }
    if (parseTree.CalcTrigonometryRadian) {
        if (parseTree.CalcNumber) {
            operand += parseTree.CalcNumber[0].value;                                   
        } else if (parseTree.piNumber) {
            operand += parseTree.piNumber[0].value;
        } else if (parseTree.piNumberWithoutSpaces) {
            operand += parseTree.piNumberWithoutSpaces[0].value;
        }
        var negOp = "";
        if (parseTree.CalcTrigonometryRadian[0].CalcRSin) {
            operation += "Math.sin";
            if (parseTree.CalcTrigonometryRadian[0].CalcRSin[0].CalcNegOp) {
                negOp = "-";
            }
        } else if (parseTree.CalcTrigonometryRadian[0].CalcRCos) {
            operation += "Math.cos";
            if (parseTree.CalcTrigonometryRadian[0].CalcRCos[0].CalcNegOp) {
                negOp = "-";
            }
        } else if (parseTree.CalcTrigonometryRadian[0].CalcRTan) {
            if (operand%(Math.PI / 2) == 0 && operand%Math.PI != 0) {
                return "Invalid input";
            } else {
                operation += "Math.tan";
                if (parseTree.CalcTrigonometryRadian[0].CalcRTan[0].CalcNegOp) {
                    negOp = "-";
                }
            }
        } else if (parseTree.CalcTrigonometryRadian[0].CalcRCtg) {
            if (operand%Math.PI == 0) {
                return "Invalid input";
            } else {                   
                operation += "1.0/Math.tan";
                if (parseTree.CalcTrigonometryRadian[0].CalcRCtg[0].CalcNegOp) {
                    negOp = "-";
                }
            }  
        }
        return operation + "(" + negOp + operand + ")";  
    }
}

cnv.mathSimpleExpressionConverter = function(parseTree) {
    var operation = '';
    var operand = '';
    if (parseTree.CalcNumber) {
        operand = parseTree.CalcNumber[0].value;
    }
    if (parseTree.CalcOp) {
        if (parseTree.CalcOp[0].value == "mul") {
            operation = "*";
        } else if (parseTree.CalcOp[0].value == "del") {
            operation = "/";
        } else {
            operation = parseTree.CalcOp[0].value;
        }

        if (parseTree.CalcOp[1] && parseTree.CalcOp[1].value) {
            if (parseTree.CalcOp[1].value === 'mul') {
                operation += '*';
            } else if (parseTree.CalcOp[1].value === 'del') {
                operation += '/';
            } else {
                operation += parseTree.CalcOp[1].value;
            }
        }
        return operation + operand;
    }
    if (parseTree.CalcPower) {
        var power;
        if (parseTree.CalcPower[0].CalcOperand) {
            power = getOperandNumber(parseTree.CalcPower[0].CalcOperand[0]);
        } else {
            power = parseTree.CalcPower[0].value;
        }
        return "Math.pow(("+ (operand === '' ? "prev" : operand) + "),("+ power +"))";
    }
    if (parseTree.CalcFuncLg) {
        return "Math.log(" + operand + ") / Math.LN10";
    }
    if (parseTree.CalcFuncLog) {
        var base;
        base = parseTree.CalcFuncLog[0].base[0].value;
        var num;
        num = parseTree.CalcFuncLog[0].num[0].value;
        return "Math.log(" + num + ") / Math.log(" + base + ")";
    }
    if (parseTree.takePart) {
        var num = getOperandNumber(parseTree.takePart[0].num[0]);
        var part = parseTree.takePart[0].part[0].value;
        return "(" + num + ")/(" + part + ")";
    }
    if (parseTree.inHalf) {
        return "(" + getOperandNumber(parseTree.inHalf[0].CalcOperand[0])  + ")/2";
    }
    if (parseTree.digitWithOperator) {
        return parseTree.text.replace(/÷/g,'/');
    }
    if (parseTree.CalcOpPrefix){
        if (parseTree.CalcOpPrefix[0].value == "mul") {
            operation = "*";
        } else if (parseTree.CalcOpPrefix[0].value == "del") {
            operation = "/";
        } else {
            operation = parseTree.CalcOpPrefix[0].value;
        }
        return (parseTree.Number1[0].value +operation + parseTree.Number2[0].value);
    }
    if (parseTree.Fraction) {
        return parseTree.Fraction[0].value;
    }
}

cnv.specialMathExpressionConverter = function(parseTree) {
    var operand_arr = [];
    if (parseTree.CalcOperand) {
        for (var i=0; i < parseTree.CalcOperand.length; i++) {
            operand_arr.push(getOperandNumber(parseTree.CalcOperand[i]));
        }
    }
    if (parseTree.mathMinus1) {
        return operand_arr[1] + '-' + operand_arr[0];
    }
    if (parseTree.mathMinus2) {
        return operand_arr[0] + '-' + operand_arr[1];
    }
    if (parseTree.mathPlus || parseTree.mathAdd) {
        return operand_arr[0] + '+' + operand_arr[1];
    }
    if (parseTree.mathMul1 || parseTree.mathMul2 || parseTree.mathMul3) {
        return operand_arr[0] + '*' + operand_arr[1];
    }
    if (parseTree.mathDiv) {
        return operand_arr[0] + '/' + operand_arr[1];     
    }
    if (parseTree.SimpleExpressionWithoutSpaces) {
        if (parseTree.SimpleExpressionWithoutSpaces[0].value) {
            return parseTree.SimpleExpressionWithoutSpaces[0].value.replace(/÷/g,'/');
        } else {
            return parseTree.text.replace(/÷/g,'/');
        }
    } 
}

cnv.combinedExpressionWithoutSpacesConverter = function(parseTree) {
    var re = /(\+|-|\*|×|÷|\/)/;
    var parts = parseTree.text.split(re);
    return parseTree.text.replace(parts[0],word2value(parts[0]));
}

cnv.floatMillionsConverter = function(parseTree) {
    var val = parseTree.FloatNumber[0].value * 1000000;
    return val;
}

function word2value(word) {
    var result = $nlp.match(word, '/nlp');
    return cnv.propagateConverter(result.parseTree);
}

function convertMathExpressionInBrackets(path) {
    var res;
    var expression;
    var operation;
    var array = [];
    var next;
    var item;
    if (path.CalcOp) {
        if (path.CalcOp[0].value === 'mul') {
            operation = '*';
        } else if (path.CalcOp[0].value === 'del') {
            operation = '/';
        } else {
            operation = path.CalcOp[0].value;
        }
    }
    if (path.MathExpressionInBrackets[0].CalcNumber) {
        array.push(path.MathExpressionInBrackets[0].CalcNumber[0].value + "+");
    }
    for (var i = 0; i < path.MathExpressionInBrackets[0].MathExpression.length; i++) {
        expression = path.MathExpressionInBrackets[0].MathExpression[i];
        if (next) {
            item = array.pop();
            array.push(item.replace("next", "(" + expression.value + ")"));
            next = (expression.value.indexOf("next") >= 0);
            continue;
        }
        if (expression.value.indexOf("prev") >= 0) {
            item = array.pop();
            array.push(expression.value.replace("prev", item));
            continue;
        }
        if (expression.value.indexOf("next") >= 0) {
            next = true;
        }
        array.push(expression.value);
    }
    var res = [];
    res = array.join("");
    if (path.CalcOp) {
        res = operation + "(" + res + ")";
    }
    res = "(" + res + ")"
    res = res.replace(new RegExp("[\\(]([\\/|\\*|\\-|\\+])[\\(]"), "$1((");
    res = res.replace(new RegExp("\\+([\\+|\\-|\\/|\\*|\\)])"), "$1");
    return res;
}

function getOperandNumber(path) {
    if (path.CalcNumber) {
        return path.CalcNumber[0].value;
    } else if (path.NegNumber) {
        if (path.NegNumber[0].CalcNumber) {
            return "-" + path.NegNumber[0].CalcNumber[0].value;
        } else if (path.NegNumber[0].NegNumberWithoutSpaces) {
            return path.NegNumber[0].NegNumberWithoutSpaces[0].text.replace(",",".");
        }
    } else if (path.MathExpressionInBrackets) {
        return convertMathExpressionInBrackets(path);
    }
    return null;
}