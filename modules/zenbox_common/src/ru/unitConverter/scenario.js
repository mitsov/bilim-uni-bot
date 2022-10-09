function ConvertSourceToTargetFunc(){
    var $session = $jsapi.context().session;
    var $temp = $jsapi.context().temp;
    var $parseTree = $jsapi.context().parseTree;

    $session.afterNewSourceNumberFunc = false;
    if ($parseTree.measureUnit && $parseTree.measure) {
        if (($parseTree.measureUnit[0].text).toLowerCase() === UnitConv.InflectToNumCase($parseTree.measureUnit[0].value, 'singNomn')) {
            $session.SourceValue = $parseTree.measureUnit[0].value;
            $session.dataForAnswer = [{'unitSourceQuantity':1, 'unitSourceValue':$session.SourceValue}];
            $session.quantitySource = UnitConv.getConvertedQuantity($session.SourceValue, UnitConv.getRatioValue($session.SourceValue), 1);
            $session.TargetValue = $parseTree.measure[0].value.unitDataMem[0].unitSourceValue;
            $session.avoidParseTree = true;
        }            
    }
    if ($temp.entryPoint === 'NewTargetValue'){
        if (($parseTree.measureUnit[0].text).toLowerCase() === UnitConv.InflectToNumCase($parseTree.measureUnit[0].value, 'singLoct')) {
            $session.avoidParseTree = true;
            $session.SourceValue = $parseTree.measureUnit[0].value;
            $session.dataForAnswer = [{'unitSourceQuantity':1, 'unitSourceValue':$session.SourceValue}];
            $session.quantitySource = UnitConv.getConvertedQuantity($session.SourceValue, UnitConv.getRatioValue($session.SourceValue), 1);
        }
    }

    if ($temp.entryPoint === 'NewSourceValue'){
        var newValue = $parseTree.measure[0].value.unitDataMem[0].unitSourceValue.toString();
        var caseValue;
        if ((newValue.endsWith("2") && !neWvalue.endsWith("12")) || (newValue.endsWith("3") && !neWvalue.endsWith("13")) || (newValue.endsWith("4") && !neWvalue.endsWith("14")) || (newValue.indexOf('.') > -1)) {
            caseValue = "singGent";
        } else {
            caseValue = "plurGent";
        }
        if ($parseTree.measure[0].text === UnitConv.InflectToNumCase($parseTree.measure[0].value.unitDataMem[0].unitSourceValue, caseValue)) {
            $session.avoidParseTree = true;
            $session.TargetValue = $parseTree.measure[0].value.unitDataMem[0].unitSourceValue;
        }
    }

    if ($session.avoidParseTree){
        $session.avoidParseTree = false;
    } else {
        $session.SourceValue = $parseTree.measure ? $parseTree.measure[0].value.unitDataMem[0].unitSourceValue : $session.SourceValue;
        $session.dataForAnswer = $parseTree.measure ? $parseTree.measure[0].value.unitDataMem : $session.dataForAnswer;
        $session.quantitySource = $parseTree.measure ? $parseTree.measure[0].value.sum : $session.quantitySource;
        $session.TargetValue = $parseTree.measureUnit ? $parseTree.measureUnit[0].value : $session.TargetValue;
    }

    if (UnitConv.AreSameClass($session.SourceValue, $session.TargetValue)){
        $session.SourceValue = UnitConv.getRatioValue($session.SourceValue)
        var answer = UnitConv.ClientsQuestionFunc($session.dataForAnswer) + ' – это ' + UnitConv.getConvertedAnswer($session.SourceValue, $session.TargetValue, $session.quantitySource, $session.dataForAnswer);
        answer = answer + '.';
        
        $reactions.answer(answer);
    } else {
        if ($temp.entryPoint === 'NewSourceValue') {
            $reactions.transition('/UnitConverter/ConvertSourceToSomething/');
        } else {
            $reactions.answer(ClassesDoesntMatch());
        }
    }
    
}


function NewSourceNumberFunc(){
    var $session = $jsapi.context().session;
    var $parseTree = $jsapi.context().parseTree;

    if ($session.dataForAnswer.length === 1){
        $session.dataForAnswer = [{'unitSourceQuantity':$parseTree.CalcNumber[0].value,
                                    'unitSourceValue':$session.dataForAnswer[0].unitSourceValue}];
        $session.quantitySource = UnitConv.getConvertedQuantity($session.dataForAnswer[0].unitSourceValue, 
                                                                UnitConv.getRatioValue($session.SourceValue), 
                                                                $session.dataForAnswer[0].unitSourceQuantity);
        $reactions.transition('/UnitConverter/ConvertSourceToTarget/');     
    } else {
        $session.unitSourceQuantity = $parseTree.CalcNumber[0].value; // will be used as unitSourceQuantity in converterUnitConverter.js
        $session.afterNewSourceNumberFunc = true;
        $reactions.answer($parseTree.CalcNumber[0].value + ' чего?');

    }    
}

function ConvertSourceToSomethingFunc(){ 
    var $session = $jsapi.context().session;
    var $temp = $jsapi.context().temp;
    var $parseTree = $jsapi.context().parseTree;
    
    $session.SourceValue = $parseTree.measure[0].value.unitDataMem[0].unitSourceValue;
    $session.quantitySource = $parseTree.measure[0].value.sum;
    $session.dataForAnswer = $parseTree.measure[0].value.unitDataMem;   


    if ($session.SourceValue.id !== '0'){ // 0 is ratio ID in csv files
        $session.TargetValue = UnitConv.getRatioValue($session.SourceValue);
        $session.avoidParseTree = true;
        $reactions.transition ('/UnitConverter/ConvertSourceToTarget/');           
    } else {
        $session.mem = $parseTree.measure[0].value;
        $session.transitionMark = true; // for state '/UnitConverter/ShowMeMeasures'
        $reactions.answer('В какую величину перевести?');
    }
}

function ClassesDoesntMatch(){
    var $temp = $jsapi.context().temp;
    var message = ' Единицы ' + UnitConv.GetMeasureByClass($temp.sourceCl,'gent') + ' я могу перевести только в ' + $temp.units + '.';
    return message
}