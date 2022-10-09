$global.$converters = $global.$converters || {};
var cnv = $global.$converters;


// **********************************************************
//                  NUMBER CONVERTERS
// **********************************************************
cnv.numberConverterDigit = function(parseTree) {
    return parseInt(parseTree.text);
};

cnv.numberConverterCommaSeparatedDigit = function(parseTree) {
    return parseInt(parseTree.text.replace(/\./g,''));
};

cnv.numberConverterValue = function(parseTree) {
    return parseInt(parseTree.value);
};

cnv.numberConverterOrdinalDigit = function(parseTree) {
    var num = parseTree.text.substr(0, parseTree.text.length - 2);
    return parseInt(parseTree.text);
};


cnv.numberConverterDozenDash = function(parseTree) {
    var parts = parseTree.text.split("-");
    var dozens = parts[0];
    var units = parts[1];

    return cnv.convertDozens(dozens) + cnv.convertUnits(units);
};

cnv.convertDozens = function(text) {
    switch(text) {
        case "twenty": return 20;
        case "thirty": return 30;
        case "forty": return 40;
        case "fifty": return 50;
        case "sixty": return 60;
        case "seventy": return 70;
        case "eighty": return 80;
        case "ninety": return 90;
    }
};

cnv.convertUnits = function(text) {
    switch(text) {
        case "one": return 1;
        case "two": return 2;
        case "three": return 3;
        case "four": return 4;
        case "five": return 5;
        case "six": return 6;
        case "seven": return 7;
        case "eight": return 8;
        case "nine": return 9;
    }
    switch(text) {
        case "first": return 1;
        case "second": return 2;
        case "third": return 3;
        case "fourth": return 4;
        case "fifth": return 5;
        case "sixth": return 6;
        case "seventh": return 7;
        case "eighth": return 8;
        case "nineth": return 9;
    }
};

cnv.numberSimpleConverter = function(parseTree) {
    var values = cnv.collectParseTreeValues(parseTree);
    return parseInt(value[0]);
};

cnv.numberConverterSum = function(parseTree) {
    var values = cnv.collectParseTreeValues(parseTree);
    var ret = 0;
    for (var i = 0; i < values.length; i++) {
        ret = ret + parseInt(values[i]);
    }
    return ret;
};

cnv.numberConverterMultiplyInner = function(parseTree) {
    var values = cnv.collectParseTreeValues(parseTree);
    var ret = 0;
    for (var i = 0; i < values.length; i++) {
        if (ret == 0) {
            ret = parseInt(values[i]);
        } else {
            ret = ret * parseInt(values[i]);
        }
    }
    return ret;
};

cnv.numberConverterMultiply = function(parseTree) {
    var val = 0;
    if (typeof parseTree.value != "undefined") {
        val = parseInt(parseTree.value);
        delete parseTree['value'];
    }
    var innerVal = cnv.numberConverterMultiplyInner(parseTree);
    if (val == 0) {
        return innerVal;
    }
    if (innerVal == 0) {
        return val;
    }
    return val * innerVal;
};

// **********************************************************
//                  FLOAT NUMBERS CONVERTERS
// *********************************************************

cnv.floatNumberWithDelimeterConverter = function(parseTree) {
    return parseFloat(parseTree.text.replace(",", "."));
};

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
};

cnv.specialFloatNumberConverter = function(parseTree) {
    switch (parseTree.value) {
        case "1":
            return 1.5;
        case "2":
            return 90;
        case "3":
            var num = 1;
            if (parseTree.Number) {
                num = parseInt(parseTree.Number[0].value)
            }
            return (num%2) * Math.PI;    
        case "4":
            return Math.PI/2;
        case "5":    
            return parseTree.text.split('/')[0] / parseTree.text.split('/')[1]; 
        case "6":
            return 3 * Math.PI/2;
        case "7":
            return 45; 
        case "8":
            return 0.5; 
        case "9":
            return 0.25;
        case "10":
            return (1/3); 
        case "11":
            return Math.PI/4; 
        case "12":
            return Math.PI/6;  
        case "13":
            return parseTree.Number[0].value * Math.PI/parseTree.Number[1].value;
    } 
};

cnv.fractionConverter = function(parseTree) {
    var res = parseTree.Number[0].value / parseTree.Denominator[0].value;
    res = +(Math.round(res + "e+2")  + "e-2");
    return res;
};

