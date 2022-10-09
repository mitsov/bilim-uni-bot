$global.$converters = $global.$converters || {};
var cnv = $global.$converters;

function cloneObj(obj){
    var copy = {};
    for (var key in obj) {
        copy[key] = obj[key];
    }
    return copy;
}

cnv.lengthUnitConverter = function(pt) {
    var id = pt.LengthCsv[0].value;
    var ret = cloneObj($LengthCsv[id].value);
    ret.id = id;
    return ret
}
cnv.weightUnitConverter = function(pt) {
    var id = pt.WeightCsv[0].value;
    var ret = cloneObj($WeightCsv[id].value);
    ret.id = id;
    return ret
}
cnv.timeUnitConverter = function(pt) {
    var id = pt.TimeCsv[0].value;
    var ret = cloneObj($TimeCsv[id].value);
    ret.id = id;
    return ret
}
cnv.informationUnitConverter = function(pt) {
    var id = pt.InformationCsv[0].value;
    var ret = cloneObj($InformationCsv[id].value);
    ret.id = id;
    return ret
}

cnv.QuantityLengthConverter = function(pt){
    var unitSourceQuantity = pt.CalcNumber[0].value;
    var unitTargetQuantity = UnitConv.getConvertedQuantity( pt.lengthUnit[0].value, 
                            UnitConv.getRatioValue($LengthCsv[0].value),
                            unitSourceQuantity);
    var ret = {
        "unitTargetQuantity": unitTargetQuantity,
        "unitData": { "unitSourceQuantity": unitSourceQuantity,
                      "unitSourceValue": pt.lengthUnit[0].value }
    }
    return ret
}

cnv.QuantityWeightConverter = function(pt){
    var unitSourceQuantity = pt.CalcNumber[0].value;
    var unitTargetQuantity = UnitConv.getConvertedQuantity( pt.weightUnit[0].value, 
                            UnitConv.getRatioValue($WeightCsv[0].value),
                            unitSourceQuantity);
    var ret = {
        "unitTargetQuantity": unitTargetQuantity,
        "unitData": { "unitSourceQuantity": unitSourceQuantity,
                      "unitSourceValue": pt.weightUnit[0].value }
    } 
    return ret    
}
cnv.QuantityTimeConverter = function(pt){
    var unitSourceQuantity = pt.CalcNumber[0].value;
    var unitTargetQuantity = UnitConv.getConvertedQuantity( pt.timeUnit[0].value, 
                            UnitConv.getRatioValue($TimeCsv[0].value),
                            unitSourceQuantity);
    var ret = {
        "unitTargetQuantity": unitTargetQuantity,
        "unitData": { "unitSourceQuantity": unitSourceQuantity,
                      "unitSourceValue": pt.timeUnit[0].value }
    } 
    return ret    
}
cnv.QuantityInformationConverter = function(pt){
    var unitSourceQuantity = pt.CalcNumber[0].value;
    var unitTargetQuantity = UnitConv.getConvertedQuantity( pt.informationUnit[0].value, 
                            UnitConv.getRatioValue($InformationCsv[0].value),
                            unitSourceQuantity);
    var ret = {
        "unitTargetQuantity": unitTargetQuantity,
        "unitData": { "unitSourceQuantity": unitSourceQuantity,
                      "unitSourceValue": pt.informationUnit[0].value }
    } 
    return ret    
}
cnv.LengthConverter = function(pt){
    var $session = $jsapi.context().session;
    var sum;

    if (pt.CalcNumber){
        sum = UnitConv.getConvertedQuantity( pt.lengthUnit[0].value, 
                    UnitConv.getRatioValue($LengthCsv[0].value),
                    pt.CalcNumber[0].value);      
        var mem = [{"unitSourceQuantity":pt.CalcNumber[0].value, "unitSourceValue":pt.lengthUnit[0].value}];
    } else {
        if ($session.unitSourceQuantity){
            sum = UnitConv.getConvertedQuantity( pt.lengthUnit[0].value, 
                        UnitConv.getRatioValue($LengthCsv[0].value),
                        $session.unitSourceQuantity);  
            var mem = [{"unitSourceQuantity":$session.unitSourceQuantity, "unitSourceValue":pt.lengthUnit[0].value}];         
            $session.unitSourceQuantity = false;
        } else {
            sum = UnitConv.getConvertedQuantity( pt.lengthUnit[0].value, 
                        UnitConv.getRatioValue($LengthCsv[0].value),
                        1);  
            var mem = [{"unitSourceQuantity":1, "unitSourceValue":pt.lengthUnit[0].value}];            
        }
    }
    
    if (pt.QuantityLength){
        for (var key in pt.QuantityLength){
            sum += pt.QuantityLength[key].value.unitTargetQuantity;
            mem.push(pt.QuantityLength[key].value.unitData);
        }           
    }
    return {"sum": sum, "unitDataMem": mem}
}

cnv.WeightConverter = function(pt){
    var $session = $jsapi.context().session;
    var sum;
    if (pt.CalcNumber){
        sum = UnitConv.getConvertedQuantity( pt.weightUnit[0].value, 
                    UnitConv.getRatioValue($WeightCsv[0].value),
                    pt.CalcNumber[0].value);   
        var mem = [{"unitSourceQuantity":pt.CalcNumber[0].value, "unitSourceValue":pt.weightUnit[0].value}];
    } else {
        if ($session.unitSourceQuantity){
            sum = UnitConv.getConvertedQuantity( pt.weightUnit[0].value, 
                        UnitConv.getRatioValue($WeightCsv[0].value),
                        $session.unitSourceQuantity);  
            var mem = [{"unitSourceQuantity":$session.unitSourceQuantity, "unitSourceValue":pt.weightUnit[0].value}];         
            $session.unitSourceQuantity = false;
        } else {
            sum = UnitConv.getConvertedQuantity( pt.weightUnit[0].value, 
                        UnitConv.getRatioValue($WeightCsv[0].value),
                        1);  
            var mem = [{"unitSourceQuantity":1, "unitSourceValue":pt.weightUnit[0].value}];            
        }
    }
    if (pt.QuantityWeight){
        for (var key in pt.QuantityWeight){
            sum += pt.QuantityWeight[key].value.unitTargetQuantity;
            mem.push(pt.QuantityWeight[key].value.unitData);
        }           
    }
    return {"sum": sum, "unitDataMem": mem}
}

cnv.TimeConverter = function(pt){
    var $session = $jsapi.context().session;
    var sum;
    if (pt.CalcNumber){
        sum = UnitConv.getConvertedQuantity( pt.timeUnit[0].value, 
                    UnitConv.getRatioValue($TimeCsv[0].value),
                    pt.CalcNumber[0].value);   
        var mem = [{"unitSourceQuantity":pt.CalcNumber[0].value, "unitSourceValue":pt.timeUnit[0].value}];
    } else {
        if ($session.unitSourceQuantity){
            sum = UnitConv.getConvertedQuantity( pt.timeUnit[0].value, 
                        UnitConv.getRatioValue($TimeCsv[0].value),
                        $session.unitSourceQuantity);  
            var mem = [{"unitSourceQuantity":$session.unitSourceQuantity, "unitSourceValue":pt.timeUnit[0].value}];         
            $session.unitSourceQuantity = false;
        } else {
            sum = UnitConv.getConvertedQuantity( pt.timeUnit[0].value, 
                        UnitConv.getRatioValue($TimeCsv[0].value),
                        1);  
            var mem = [{"unitSourceQuantity":1, "unitSourceValue":pt.timeUnit[0].value}];            
        }
    }
    if (pt.QuantityTime){
        for (var key in pt.QuantityTime){
            sum += pt.QuantityTime[key].value.unitTargetQuantity;
            mem.push(pt.QuantityTime[key].value.unitData);
        }           
    }
    return {"sum": sum, "unitDataMem": mem}
}

cnv.InformationConverter = function(pt){
    var $session = $jsapi.context().session;
    var sum;
    if (pt.CalcNumber){
        sum = UnitConv.getConvertedQuantity( pt.informationUnit[0].value, 
                    UnitConv.getRatioValue($InformationCsv[0].value),
                    pt.CalcNumber[0].value);   
        var mem = [{"unitSourceQuantity":pt.CalcNumber[0].value, "unitSourceValue":pt.informationUnit[0].value}];
    } else {
        if ($session.unitSourceQuantity){
            sum = UnitConv.getConvertedQuantity( pt.informationUnit[0].value, 
                        UnitConv.getRatioValue($InformationCsv[0].value),
                        $session.unitSourceQuantity);  
            var mem = [{"unitSourceQuantity":$session.unitSourceQuantity, "unitSourceValue":pt.informationUnit[0].value}];         
            $session.unitSourceQuantity = false;
        } else {
            sum = UnitConv.getConvertedQuantity( pt.informationUnit[0].value, 
                        UnitConv.getRatioValue($InformationCsv[0].value),
                        1);  
            var mem = [{"unitSourceQuantity":1, "unitSourceValue":pt.informationUnit[0].value}];            
        }
    }
    if (pt.QuantityInformation){
        for (var key in pt.QuantityInformation){
            sum += pt.QuantityInformation[key].value.unitTargetQuantity;
            mem.push(pt.QuantityInformation[key].value.unitData);
        }           
    }
    return {"sum": sum, "unitDataMem": mem}
}