//получаем случайные id городов 
function getInfo(level, extra_value){
    var $session = $jsapi.context().session;
    var $client = $jsapi.context().client;
    var levelsDict = $client.levels[level][$session.questionType];

    var id = selectRandomArg.apply(this, levelsDict);
    levelsDict.splice(levelsDict.indexOf(id), 1);
    var value = $Geography[id].value;

    if (extra_value) {
        var id2 = selectRandomArg.apply(this, levelsDict);
        var value2 = $Geography[id2].value;
        return [value, value2];
    } else {
        return value;
    }    
}

//проверяем не закончился ли словарь неиспользованных городов
function check_dict(level){
    var $session = $jsapi.context().session;
    var $client = $jsapi.context().client;
    var level = $client.geographyLevel;
    var type = $session.questionType;

    if ($client.levels[level][type].length < 2) {
        $client.levels[level][type] = $client.levelsConst[level][type];
    }
}