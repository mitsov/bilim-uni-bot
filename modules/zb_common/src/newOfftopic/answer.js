var realAnswer = $reactions.answer;

function resolveVariables(text) {
    text = resolveInlineDictionary(text);
    text = $reactions.template(text, $jsapi.context());
    text = resolveInlineDictionary(text);
    return text;
}

$reactions.answer = function(text) {
    var ctx = $jsapi.context();
    if (typeof text === "object") {
        text = text.value;
    }

    text = resolveVariables(text);

    if (ctx.currentState.indexOf('CatchAll') === -1 ) {
        ctx.session.lastAnswerState = ctx.currentState;
    }
    

    realAnswer(text);
};
