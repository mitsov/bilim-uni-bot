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


function offtopicReaction(state) {

    var stateInfo;

    if (_.isArray(state)) {
        stateInfo = state.reduce(function(acc, cur){
            return acc[cur];
        }, newOfftopicAnswers);

        $jsapi.context().temp.offtopicState = state[0];
    } else {
        stateInfo = newOfftopicAnswers[state];
        
        $jsapi.context().temp.offtopicState = state;
    }

    if (!stateInfo) {
        throw "Could not find answers in offtopic by key: " + state;
    }

    if (stateInfo.go) {
        $reactions.transition(stateInfo.go);
        return;
    }

    var type = offtopType();
    var answerLocation = stateInfo[type] || stateInfo;

    if (answerLocation.male || answerLocation.female) {
        var gender = offtopGender();
        answerLocation = answerLocation[gender] || answerLocation.male || answerLocation.female;
    }

    var answer;

    if (typeof answerLocation != "string") {
        answer = testMode() ? answerLocation[0] : _.sample(answerLocation);
    } 
    
    answer = answer || answerLocation;

    if (_.isObject(answer)) {
        answer = answer[type] || answer;
        answer = answer[offtopGender()] || answer;
    }

    $reactions.answer(answer);
}

function offtopType() {
    var $session = $jsapi.context().session;
    var $injector = $jsapi.context().injector;

    if ($session.offtopicDefinedType) {
        return $session.offtopicDefinedType;
    }
    return ($injector.offtopic && $injector.offtopic.type) || 'formal';
}

function offtopGender() {
    var $session = $jsapi.context().session;
    var $injector = $jsapi.context().injector;

    if ($session.offtopicDefinedGender) {
        return $session.offtopicDefinedGender;
    }
    return ($injector.offtopic && $injector.offtopic.gender) || 'male';
}

function offtopFallbackLogic(ctx) {
    // Функция, которая вызывается в пост процессе, если тема == офтопик
    // В зависимости от параметров в .yaml может дополнить ответ "Нужно вернуться к работе"
    // И/или "Чем я могу вам помочь?"
    // И/или возможен переход в указанный стейт 
    // to do:
    //      - добавить возможность просто сменить контекст (тк по transition будет
    //  еще раз обрабатываться пост процесс)
    //      - bug: При переходе из пост процесса deferred не работает

    var state = ctx.temp.offtopicState || ctx.contextPath.split('/')[2];

    if (!state || !newOfftopicAnswers || !offtopicFallbackLogic) {
        return;
    }

    var fallbackLogic = offtopicFallbackLogic[state] || offtopicFallbackLogic.default;

    if (fallbackLogic.doNothing) {
        return;
    }

    if (fallbackLogic.addOfftopicFallback) {
        offtopicReaction("Fallback");
    }

    if (fallbackLogic.addHowCanIHelpYou) {
        offtopicReaction("NeedHelp");
    }

    if (fallbackLogic.changeContextPathTo && fallbackLogic.changeContextPathTo.startsWith("/")) {
        
        ctx.contextPath = fallbackLogic.changeContextPathTo;

    } else if (fallbackLogic.transition) {

        var deferred = !!fallbackLogic.transition.deferred;





        var targetState;

        if (fallbackLogic.transition.lastState) {
            targetState = ctx.session.lastState || ctx.session.prevState;
        }

        targetState = fallbackLogic.transition.state || targetState;


        if (targetState) {
            $reactions.transition({value: targetState, deferred: deferred});
        }
    }

}