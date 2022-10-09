//Позволяет избежать пустых ответов, если в offtopic.yaml добавятся поля, а в specialOfftopic их не будет.
//Если в проекте используется specialOfftopic, то поиск ответа идёт в нём.
//Если ответ не найден, то поиск продолжается в словаре из common. 
function applyCustomAnswers(common, custom) {
    if (typeof custom !== 'undefined') {
        var merged = {};
        for (var key in common) {
            if (Object.prototype.toString.call(common[key]) === '[object Object]') {
                merged[key] = applyCustomAnswers(common[key], custom[key]);
            } else if (custom[key]) {
                merged[key] = custom[key];
            } else {
                merged[key] = common[key];
            }
        }
        if (merged){
            return merged;
        } else {
            return {"1":"2"};
        }
        
    }
    return common;
}