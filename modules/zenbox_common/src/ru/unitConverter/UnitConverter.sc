require: converterUnitConverter.js
require: scriptUnitConverter.js
require: scenario.js

require: Length.csv
    name = LengthCsv
    var = $LengthCsv
require: Weight.csv
    name = WeightCsv
    var = $WeightCsv
require: Time.csv
    name = TimeCsv
    var = $TimeCsv
require: Information.csv
    name = InformationCsv
    var = $InformationCsv

require: floatNumber/floatNumber.sc
  module = zb_common
require: ../../common/common.sc

patterns:

    $lengthUnit = $entity<LengthCsv> || converter = $converters.lengthUnitConverter
    $weightUnit = $entity<WeightCsv> || converter = $converters.weightUnitConverter
    $timeUnit = $entity<TimeCsv> || converter = $converters.timeUnitConverter
    $informationUnit = $entity<InformationCsv> || converter = $converters.informationUnitConverter

    $QuantityLength = [и] $CalcNumber $lengthUnit || converter = $converters.QuantityLengthConverter
    $QuantityWeight = [и] $CalcNumber $weightUnit || converter = $converters.QuantityWeightConverter
    $QuantityTime = [и] $CalcNumber $timeUnit || converter = $converters.QuantityTimeConverter
    $QuantityInformation = [и] $CalcNumber $informationUnit || converter = $converters.QuantityInformationConverter

    $Length = [$CalcNumber] $lengthUnit [$repeat<$QuantityLength>] || converter = $converters.LengthConverter
    $Weight = [$CalcNumber] $weightUnit [$repeat<$QuantityWeight>] || converter = $converters.WeightConverter
    $Time = [$CalcNumber] $timeUnit [$repeat<$QuantityTime>] || converter = $converters.TimeConverter
    $Information = [$CalcNumber] $informationUnit [$repeat<$QuantityInformation>] || converter = $converters.InformationConverter

    $measureUnit = ($lengthUnit :: length | $weightUnit :: weight | $timeUnit :: time | $informationUnit :: information)
    $measure = ($Length | $Weight | $Time | $Information)

    $unitName = ((~длина|~расстояние):length | (~время|~длительность):time | ~информация:information | ~вес:weight)
    $WhatKind = (какую | какие)
    $toConvert = (~конвертировать | ~сконвертировать | перевести | переведи | ~переводить | ~сравни | сконверт*)
    $ConverterEmelya = ( конвертер | конвертер емеля)
    $able = (можешь | можете | умеешь | умеете | знаешь | знаете)
    
theme: /UnitConverter

    state: StartUnitConverter
        q!: * [(~запустить|~включить|~активировать)] $ConverterEmelya *
        script:
            $session.transitionMark = false;
        a: Какие величины перевести?

        state: NewSourceValue
            q: * [$toConvert] * $measure *
            a: В какую величину перевести?
            script:
                $session.SourceValue = $parseTree.measure[0].value.unitDataMem[0].unitSourceValue;
                $session.dataForAnswer = $parseTree.measure[0].value.unitDataMem;
                $session.quantitySource = $parseTree.measure[0].value.sum;
                $session.mem = $parseTree.measure[0].value; // for possible converterMenu 
                $session.transitionMark = true; // for possible converterMenu 
            state: NewTargetValue
                q: * $measureUnit *
                go!: /UnitConverter/ConvertSourceToTarget

    state: ConvertSourceToTarget
        q!: * [a] [$toConvert] $measure [[это]сколько [будет]] (в|и) $measureUnit [[это] сколько [будет]] *
        q!: * [а] [сколько] [в] $measure [[это] сколько] $measureUnit *
        q!: * [а] сколько [будет] $measureUnit [будет][в] $measure * 
        q!: * [$toConvert]  в $measureUnit $measure *
        q: * [a] [$toConvert] $measure [[это]сколько [будет]] (в|и) $measureUnit [[это] сколько [будет]]  *
        q: * [а] [сколько] [в] $measure [[это] сколько] $measureUnit *  
        q: * [а] [сколько] [в] $measure [[это] сколько] $measureUnit * || fromState="/UnitConverter/ConvertSourceToTarget"
        q: * [а] сколько [будет] $measureUnit [будет][в] $measure *  
        q: [$toConvert] в $measureUnit $measure *
        script:
            ConvertSourceToTargetFunc();
        state: NewTargetValue
            q:  * [тогда][давай] $toConvert [это] (в|к) $measureUnit [[это] сколько] *  
            q:  * [а] сколько [это] (в|к) $measureUnit [[это] сколько] *  
            q:  * [а] [это] (в|к) $measureUnit [это] сколько * 
            q:  * а [в|к] $measureUnit *
            q:  * [а] (в|к) $measureUnit *
            script:
                $temp.entryPoint = 'NewTargetValue';
            go!: /UnitConverter/ConvertSourceToTarget   

        state: NewSourceValue
            q: *  [а] $measure [ [это] сколько] * 
            if: $session.afterNewSourceNumberFunc
                go!: /UnitConverter/ConvertSourceToTarget
            else:
                script:
                    $temp.entryPoint = 'NewSourceValue';
            go!: /UnitConverter/ConvertSourceToTarget

        state: NewSourceNumber
            q:  * [а] [в] $CalcNumber [[это] сколько] *  
            script:
                NewSourceNumberFunc();
 
    state: ConvertSourceToSomething
        q!: * [а] $measure это сколько *  
        q!: * $toConvert  $measure *  
        q!: * Сколько будет  $measure *  
        script:
            ConvertSourceToSomethingFunc();
        state: AddTargetValue
            q: * [в] $measureUnit *  
            go!: /UnitConverter/ConvertSourceToTarget 

    state: ConverterMenu
        q!: *  Во что [еще] $you $able $toConvert $measure *
        q!: *  [а] в $WhatKind [еще] [~величина] $you $able [$toConvert] $measure *
        q:  * [а] [во/в] (что/$WhatKind) [еще] [~величина] $you $able [$toConvert] || fromState = '/../StartUnitConverter/NewSourceValue'
        q!: * в чем (измеряется|измеряются) $measure *  
        script:
            $session.transitionMark = false;
            
            $temp.Value =  $parseTree.measure ? $parseTree.measure[0].value.unitDataMem[0].unitSourceValue : $session.mem.unitDataMem[0].unitSourceValue;

            //remember SourceValue for possible further state 'ChooseTargetValue'
            $session.SourceValue = $parseTree.measure ? $parseTree.measure[0].value : $session.mem;
        a: Могу перевести {{UnitConv.InflectToNumCase($temp.Value, 'plurNomn') }} в {{ UnitConv.suggestToConvert($temp.Value) }}.

        state: MenuOfNewSourceValue
            q: * [а] $measure [во что] [(~мочь|~уметь)] [$toConvert] *  
            script:
                $session.mem = $parseTree.measure[0].value;
            go!: /UnitConverter/ConverterMenu

        state: ChooseTargetValue
            q:  * [тогда][($toConvert|давай)] в $measureUnit * 
            script:
                $session.quantitySource = $session.SourceValue.sum; 
                $session.dataForAnswer = $session.SourceValue.unitDataMem; 
                $session.SourceValue = $parseTree.measureUnit[0].value;
            go!: /UnitConverter/ConvertSourceToTarget             
    
    state: ShowMeMeasures || noContext = true
        q!: * [во] что [еще] $you [еще] $able $toConvert *
        q!: * [в] $WhatKind [еще] единицы [измерения] [$you] $able [$toConvert] *
        q!: * [в] $WhatKind [еще] ~величина [$you] $able [$toConvert] *
        q!: * [в] $WhatKind [еще] [(типы/виды/разновидности)] [$you] ~величина [измерения] [$you] знаешь *  
        q!: * еще [~знать] какие-нибудь [(типы/виды/разновидности)] величины [измерения] *
        q: * [а] $WhatKind [$you] $able [$toConvert] * 
        if: $session.transitionMark
            go!: /UnitConverter/ConverterMenu
        else:
            a: Я умею конвертировать величины: времени, информации, длины и веса.
            go!: /UnitConverter

    state: MenuOfMeasure || noContext = true
        q!: * во что [еще] $you [еще] $able ($toConvert) [~единица] $unitName *  
        q!: * [подскажи] в чем измеряется $unitName *  
        q!: * $unitName в чем (измеряется|измеряются) *  
        q!: * $unitName во что $able *  
        q!: * [$WhatKind] (величины/единицы) [измерения] $unitName [$you] [$toConvert] $able [переводить/перевести/конвертировать] *  
        q!: * [тогда] давай [тогда] [(во|в)] $unitName 
        a: Из величин измерения {{$nlp.inflect($parseTree.unitName[0].text,'gent')}} я знаю: {{ UnitConv.suggestToConvert($parseTree.unitName[0].value) }}.