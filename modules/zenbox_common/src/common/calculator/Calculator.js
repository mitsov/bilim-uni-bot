function CalcMul(x, y) {
    return x*y;
}

function CalcPercent(p, x) {
    return (x/100)*p;
}

function CalcSin(degrees) {
    return Math.sin(degrees * (Math.PI/180));
}

function CalcCos(degrees) {
    return Math.cos(degrees * (Math.PI/180));
}

function CalcTan(degrees) {
    return Math.tan(degrees * (Math.PI/180));
}

function calculate(parseTree) {
    var res;
    var array = [];
    var expression;
    var item;
    var next = false;
    if (parseTree.MathExpression[0].value == "Invalid input") {
        return "Invalid input";
    }
    if (parseTree.CalcNumber) {
        array.push(parseTree.CalcNumber[0].value);
    }
    
    for (var i = 0; i < parseTree.MathExpression.length; i++) {
        expression = parseTree.MathExpression[i];
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
    var parts = [];
    parts = array.join("");
    if (/.*next.*/.test(parts) || /.*prev.*/.test(parts) || /.*undefined.*/.test(parts)) {
        res = "error";
    } else {
        try {
            res = eval(parts);
            res = +(Math.round(res + (/.*e.*/.test(res) ? "" : "e+2")) + (/.*e.*/.test(res) ? "" : "e-2"));
            res = res.toString().replace(".", ",");
        } catch (e) {
            res = "error";
        }
    }
    return res;
}

function contextFuncCalculate(parseTree) {
    var $session = $jsapi.context().session;

    var res, rtPower, operation;
    var keys =  Object.keys(parseTree.CalcFunc[0]);
    operation = "";
    if (keys.indexOf("CalcFuncSqrt") != -1) {
        operation = "Math.sqrt";
    }
    if (keys.indexOf("CalcFuncLn") != -1) {
        operation = "Math.log";
    }
    if (keys.indexOf("CalcFuncLog") != -1) {
        operation = "Math.log10";
    }
    if (keys.indexOf("CalcFuncCbrt") != -1) {
        operation = "Math.pow";
        rtPower = 3;
    }
    if (keys.indexOf("CalcFuncRt") != -1) {
        operation = "Math.pow";
        rtPower = parseTree.CalcFunc[0].CalcFuncRt[0].CalcNumber[0].value;
    }
    var parts = operation + "(" + $session.calcResult + (operation === "Math.pow" ? ", 1/" + rtPower : "") + ")";
    try {
        res = eval(parts);
        res = +(Math.round(res + (/.*e.*/.test(res) ? "" : "e+2")) + (/.*e.*/.test(res) ? "" : "e-2"));
        res = res.toString().replace(".", ",");
    } catch (e) {
        res = "error";
    }
    return res;
}

function simpleCalculate(parseTree) {
    var $session = $jsapi.context().session;

    var res;
    var array = [];
    var expression;
    var item;
    var next = false;

    if (!(parseTree.SimpleExpression[0].CalcPower && parseTree.SimpleExpression[0].CalcNumber) &&
        !parseTree.SimpleExpression[0].CalcOpPrefix &&
        !(parseTree.SimpleExpression[0] && parseTree.SimpleExpression[1]) &&
        !parseTree.SimpleExpression[0].inHalf &&
        !parseTree.SimpleExpression[0].takePart &&
        !parseTree.SimpleExpression[0].CalcFuncLog &&
        !parseTree.SimpleExpression[0].CalcFuncLg) {
        array.push($session.calcResult);
    }
    for (var i = 0; i < parseTree.SimpleExpression.length; i++) {
        expression = parseTree.SimpleExpression[i];
        if (next) {
            item = array.pop();
            array.push(item.replace("next", expression.value));
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
    var parts = [];
    parts = array.join("");
    if (/.*next.*/.test(parts) || /.*prev.*/.test(parts) || /.*undefined.*/.test(parts)) {
        res = "error";
    } else {
        try {
            res = eval(parts);
            res = +(Math.round(res + (/.*e.*/.test(res) ? "" : "e+2")) + (/.*e.*/.test(res) ? "" : "e-2"));
            res = res.toString().replace(".", ",");
        } catch (e) {
            res = "error";
        }
    }
    return res;
}

function prepareEAnswer(res) {
    if (/.*e\+.*/.test(res)) {
        res = res.replace("e+", $CalculatorAnswers["e"]["plus"]);
    } else if (/.*e\-.*/.test(res)) {
        res = res.replace("e-", $CalculatorAnswers["e"]["minus"]);
    }
    return res;
}