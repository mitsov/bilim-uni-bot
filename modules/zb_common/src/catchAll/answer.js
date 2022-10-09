function getAnswer(answer){
    if(typeof(specialSwitch) != 'undefined'){
        return selectRandomArg(specialSwitch[answer]);
    }
    return selectRandomArg(zwitch[answer])
}