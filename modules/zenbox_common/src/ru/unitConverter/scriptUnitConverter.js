 var UnitConv =  (function(){
     
    function getConvertedQuantity(SourceValue, TargetValue, quantitySource){
        var ratioSource, ratioTarget;
    
        if (SourceValue.group && TargetValue.group && SourceValue.group === TargetValue.group){
            ratioSource = SourceValue.localratio;
            ratioTarget = TargetValue.localratio;
        } else {
            ratioSource = SourceValue.ratio;
            ratioTarget = TargetValue.ratio;
        }
        var quantityTarget = (quantitySource * ratioSource) / ratioTarget;

        // Normalize super small and super big numbers with 'e' like '1e-10'
        // Doing '0.0000000001' out of '1e-10'
        if ((quantityTarget.toString()).match(/\d+\.\d+e(\+|-)\d+/) || (quantityTarget.toString()).match(/\d+e(\+|-)\d+/)){
            quantityTarget = NumberWithE(quantityTarget);  
        }
        return quantityTarget;
    }
    function getConvertedAnswer(SourceValue, TargetValue, quantitySource, dataForAnswer){
        
        

        var BotAnswer;
        var quantityTarget = getConvertedQuantity(SourceValue, TargetValue, quantitySource); 
        
        if (quantityTarget.toString().split(".").length > 1 && quantityTarget.toString().split(".")[0] !== "0" && (quantityTarget.toString().split(".")[1].startsWith("99") || quantityTarget.toString().split(".")[1].startsWith("00"))) {
            quantityTarget = Math.round(quantityTarget)
        }

        if ((quantityTarget.toString()).match(/\d+\.\d+e(\+|-)\d+/) || (quantityTarget.toString()).match(/\d+e(\+|-)\d+/)) {
            BotAnswer = ConformWithNumber(quantityTarget, TargetValue).replace(/\./g, ',');
            return BotAnswer
        }

        var ConvertedUnit;
        if ((quantityTarget.toString()).match(/\d+\.\d+/)){
            var normalFloat = (quantityTarget.toString()).match(/[1-9]\d{0,}\.0{0,2}[1-9]{0,3}/) || (quantityTarget.toString()).match(/0\.0+[1-9]{1,3}/);   
            if (normalFloat !== null){
                ConvertedUnit = ConformWithNumber(normalFloat, TargetValue).replace(/\./g, ',');
                if (!TargetValue.idLess){
                    BotAnswer = ConvertedUnit;
                    return BotAnswer
                } 
                var ApproximateUnit = normalOutput(SourceValue, TargetValue, quantitySource, dataForAnswer) ? normalOutput(SourceValue, TargetValue, quantitySource, dataForAnswer) : '';
                BotAnswer = ConvertedUnit + ApproximateUnit;
                return BotAnswer
            }
    
            quantityTarget = parseFloat(quantityTarget.toFixed(3));
            if (Math.ceil(quantityTarget) - 0.001 === quantityTarget){
                quantityTarget = parseInt(quantityTarget) + 1;
            }
    
            ConvertedUnit = ConformWithNumber(quantityTarget, TargetValue).replace(/\./g, ','); 
            var ApproximateUnit = normalOutput(SourceValue, TargetValue, quantitySource, dataForAnswer) ? normalOutput(SourceValue, TargetValue, quantitySource, dataForAnswer) : '';
        } else {
            ConvertedUnit = ConformWithNumber(quantityTarget, TargetValue);
            var ApproximateUnit = normalOutput(SourceValue, TargetValue, quantitySource, dataForAnswer) ? normalOutput(SourceValue, TargetValue, quantitySource, dataForAnswer) : '';
        }

        BotAnswer = ConvertedUnit + ApproximateUnit;
        return BotAnswer
    }
    function NumberWithE(quantityTarget){ // for example: 1,2e+32
        var int = parseInt(quantityTarget);
        var part = (quantityTarget.toString()).match(/\.\d+/);
        part = part.toString().substring(1);
        var e = (quantityTarget.toString()).match(/e(\+|-)\d+/);
        
        if (part.match(/0{5,}1/)){
            part = '';
        }
        if (e.toString().match(/\+/)){
            
            if (part){
                if (part.length > parseInt(e)){
                    int = int + part.substring(0, parseInt(e));
                    part = part.substring(parseInt(e.toString().match(/\d+/)));
                    quantityTarget = int + '.' + part;
                } else {
                    var zero = '';
                    var num = parseInt(e.toString().match(/\d+/)) - part.length;
                    for (var i = 0; i < num; ++i){
                        zero += '0';
                    }    
                    quantityTarget = int + part + zero;    
                }
            } else {
                var zero = '0';
                var num = parseInt(e.toString().match(/\d+/));
                
                for (var i = 0; i < num; ++i){
                    zero += '0';
                }
                int = int + zero;
                quantityTarget = parseInt(int);
            }
        } else {
            var zero = '';
            var num = parseInt(e.toString().match(/\d+/));
            for (var i = 1; i < num; ++i){
                zero += '0';
            }
            quantityTarget = '0.' + zero + int + part;
            quantityTarget = quantityTarget.match(/0\.0+\d{1,3}/);
        }
        return quantityTarget    
    }
    function InflectToNumCase(Value, NumCase){
        var ret;
        switch (NumCase){
            case 'singNomn':
                ret = Value.title;
                break
            case 'singGent':
                ret = Value.conform ? Value.conform.singGent : $nlp.inflect(Value.title, 'gent');
                break
            case 'singLoct':
                ret = Value.conform ? Value.conform.singLoct : $nlp.inflect(Value.title, 'loct');
                break
            case 'plurNomn':
                ret = Value.conform ? Value.conform.plurNomn : $nlp.inflect(Value.title, 'plur');
                break
            case 'plurGent':
                ret = Value.conform ? Value.conform.plurGent : $nlp.inflect($nlp.inflect(Value.title, 'plur'),'gent');
                break
            case 'plurLoct':
                ret = Value.conform ? Value.conform.plurLoct : $nlp.inflect($nlp.inflect(Value.title, 'plur'),'loct');
                break
        }
        return ret
    }
    function ConformWithNumber(NumberToConform, TargetValue){
        if (TargetValue.conform){
            if (NumberToConform !== Math.round(NumberToConform)){
                return NumberToConform + ' ' + TargetValue.conform.singGent;
            }
    
            var conformed = NumberToConform % 10 === 1 && NumberToConform % 100 !== 11 ? TargetValue.conform.singNomn 
            : (NumberToConform % 10 >= 2 && NumberToConform % 10 <= 4 && (NumberToConform % 100 < 10 || NumberToConform % 100 >= 20) 
            ? TargetValue.conform.singGent 
            : TargetValue.conform.plurGent);
            return NumberToConform + ' ' + conformed;
        } 
        if (NumberToConform !== Math.round(NumberToConform)){
            return NumberToConform + ' ' + $nlp.inflect(TargetValue.title, 'gent');
        }
        return NumberToConform + ' ' + $nlp.conform(TargetValue.title, NumberToConform);
    }
    
    // For output: '45,345 meters 123 centimeters' + ' is ' + ..
    function ClientsQuestionFunc(dataForAnswer) {
        
        var ClientsQuestion = '';
        var spaces = dataForAnswer.length;
        for (var key in dataForAnswer){
            ClientsQuestion += ConformWithNumber(dataForAnswer[key].unitSourceQuantity, dataForAnswer[key].unitSourceValue);
            if (spaces > 1){
                ClientsQuestion += ' ';
                --spaces;
            }
        }
        ClientsQuestion = (ClientsQuestion).replace(/\./g, ',');
        return ClientsQuestion;
    }
    
    function suggestToConvert(unitValue, NumberOfUnits){ // UnitValue can be object or string. 
    
        if (typeof(unitValue) === 'string'){
            unitValue = {'class':unitValue,'title':'None'};
        }
        var Csv;
        switch (unitValue.class){
            case 'weight': Csv = $WeightCsv; break
            case 'length': Csv = $LengthCsv; break
            case 'time': Csv = $TimeCsv; break
            case 'information': Csv = $InformationCsv; break
        }
        var units = '';
        for (var key in Object.keys(Csv)){
            if (Csv[key].value.title != unitValue.title){
                if (parseInt(key) + 1 === Object.keys(Csv).length){
                    units += InflectToNumCase(Csv[key].value, 'plurNomn');
                    break
                } 
                if (key === NumberOfUnits){
                    units += InflectToNumCase(Csv[key].value, 'plurNomn'); 
                    break
                } else {
                    units += InflectToNumCase(Csv[key].value, 'plurNomn') + ', ';
                }
            }
                  
        }
        if (units.slice(-2) !== ', '){
            return units
        }
        return units.substring(0, units.length - 2)
    }
    
    function normalOutput(SourceValue, TargetValue, quantitySource, dataForAnswer){
        var Csv;
        switch (SourceValue.class){
            case 'weight': Csv = $WeightCsv; break    
            case 'length': Csv = $LengthCsv; break
            case 'time': Csv = $TimeCsv; break
            case 'information': Csv = $InformationCsv; break
        }
    
        // Finding out the biggest value of the group or in general
        var IDs = [];
        var idLesses = [];
    
        for (var id in Object.keys(Csv)) { 
            var condition = TargetValue.group ? Csv[id].value.idLess && Csv[id].value.group === TargetValue.group 
                            : Csv[id].value.idLess && !Csv[id].value.group;
            if (condition){
                IDs.push(id); 
                idLesses.push(Csv[id].value.idLess);
            } 
        }
        var idMax;
        IDs.forEach(function(id) {
            if(idLesses.indexOf(id) === -1) {
                if(!Csv[id].value.AvoidUnit){
                    idMax = id;
                }
            } 
        });
        
        var ID = idMax;
    
        var idSource; // Remember the source id to avoid '45 meters is 45 meters'
        if (dataForAnswer.length === 1){
            idSource = dataForAnswer[0].unitSourceValue.id; 
        } else { // When there are more than one unit in sourceValue. For instance '3 miles 45 foots to..'
            var idsOfQuestion = []; 
            for (var id = 0; id < dataForAnswer.length; ++id){
                idsOfQuestion.push(dataForAnswer[id].unitSourceValue.id); 
            }
        }


        // Loop from the id max to the smallest value..
        while (Csv[ID].value.idLess){
            //  ..meanwhile checking if the sourceValue converted to the current value is more then 1..
            if (getConvertedQuantity(SourceValue, Csv[ID].value, quantitySource) > 1 && ID != idSource){
                
                if (idsOfQuestion){
                    if (ID === idsOfQuestion[0] && Csv[ID].value.idLess === idsOfQuestion[1]){
                        ID = Csv[ID].value.idLess; 
                    }   
                }
    
                quantitySource = getConvertedQuantity(SourceValue, Csv[ID].value, quantitySource);
                if (quantitySource.toString().split(".").length > 1 && quantitySource.toString().split(".")[0] !== "0" && (quantitySource.toString().split(".")[1].startsWith("99") || quantitySource.toString().split(".")[1].startsWith("00"))) {
                    quantitySource = Math.round(quantitySource)
                }

                if (ID === TargetValue.id){
                    if (quantitySource > 1 && Math.round(quantitySource) === quantitySource){
                        return false
                    }
                    if (Csv[ID].value.idLess){
                        var ConvertedUnit = ConformWithNumber(Math.floor(quantitySource), Csv[ID].value);
                        var idLess = Csv[ID].value.idLess;
                        TargetValue = Csv[idLess].value;
                        var quantitySource = quantitySource - Math.floor(quantitySource);
                        var quantityTarget = getConvertedQuantity(Csv[ID].value, TargetValue, quantitySource);
                        quantityTarget = Math.round(quantityTarget);
                        if (quantityTarget === 0){
                            return false
                        }
                        ConvertedUnit = ConvertedUnit + ' ' + ConformWithNumber(quantityTarget, TargetValue);
                        return ' или примерно ' + ConvertedUnit
                    }
                } 
                
                if (Math.abs( Math.round(quantitySource) - quantitySource ) >= 0.3){
                    if (TargetValue.id === ID) {
                        return false
                    }
                    return ' или ' + ConformWithNumber(quantitySource.toFixed(1), Csv[ID].value)
                }
    
                if (Math.round(quantitySource) === quantitySource){
                    return ' или ' + ConformWithNumber(Math.round(quantitySource), Csv[ID].value)
                }
                if (Math.round(quantitySource) === Math.ceil(quantitySource)){
                    return ' или примерно ' + ConformWithNumber(Math.round(quantitySource), Csv[ID].value)
                }
    
                if (Math.round(quantitySource) != quantitySource && Csv[ID].value.idLess){
                    var ConvertedUnit = ConformWithNumber(Math.round(quantitySource), Csv[ID].value);
                    var idLess = Csv[ID].value.idLess;
                    TargetValue = Csv[idLess].value;
                    var quantitySource = '0'+ (quantitySource.toString()).match(/\.\d+/);
                    var quantityTarget = getConvertedQuantity(Csv[ID].value, TargetValue, quantitySource);
                    quantityTarget = Math.round(quantityTarget);
                    if (quantityTarget === 0){
                        return ' или примерно ' + ConvertedUnit
                    }
                    ConvertedUnit = ConvertedUnit + ' ' + ConformWithNumber(quantityTarget, TargetValue)
                    return ' или примерно ' + ConvertedUnit
                }
                return false
            }
            ID = Csv[ID].value.idLess;
        }
        return false
    }
    
    function GetMeasureByClass(cl, gramCase){
        if (!gramCase){
            var gramCase = 'nomn';
        }
        switch(cl){
            case 'length': return $nlp.inflect('длина', gramCase)
            case 'weight': return $nlp.inflect('вес', gramCase)
            case 'time': return $nlp.inflect('время', gramCase)
            case 'information': return $nlp.inflect('информация', gramCase) 
        }                
    }
    function getRatioValue(val){
        var ret;
        switch (val.class){
            case 'length': 
                ret = cloneObj($LengthCsv[0].value);
                break
            case 'weight': 
                ret = cloneObj($WeightCsv[0].value);
                break
            case 'time': 
                ret = cloneObj($TimeCsv[0].value);
                break
            case 'information': 
                ret = cloneObj($InformationCsv[0].value);
                break
        }
        ret.id = '0';
        return ret
    }
    function AreSameClass(SourceValue, TargetValue){
        if (SourceValue.class === TargetValue.class){
            return true
        }
        var $temp = $jsapi.context().temp;
        $temp.targetCl = TargetValue.class;
        $temp.sourceCl = SourceValue.class;
        $temp.units = suggestToConvert(SourceValue);
        return false
    }
    
    return {
        getConvertedAnswer : getConvertedAnswer,
        getConvertedQuantity : getConvertedQuantity,
        InflectToNumCase : InflectToNumCase,
        suggestToConvert : suggestToConvert,
        GetMeasureByClass : GetMeasureByClass,
        getRatioValue : getRatioValue,
        AreSameClass : AreSameClass,
        ClientsQuestionFunc : ClientsQuestionFunc 
    }
})();