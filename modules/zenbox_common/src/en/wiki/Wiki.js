function cantGetWiki() {
    return selectRandomArg("I do not have the answer to that question.",
        "I could not find the answer.");
}

function prepareWikiQuestion(text) {
    var result = text;
    if (/^is .*/.test(text)) {
        result = text.substring(3, text.length);
    } else if (/^was .*/.test(text)) {
        result = text.substring(4, text.length);
    } else if (/^were .*/.test(text)) {
        result = text.substring(5, text.length);
    } else if (/^a .*/.test(text)) {
        result = text.substring(2, text.length);
    } else if (/^an .*/.test(text)) {
        result = text.substring(3, text.length);
    } else if (/^the .*/.test(text)) {
        result = text.substring(4, text.length);
    } else if (/^to .*/.test(text)) {
        result = text.substring(3, text.length);
    } else if (/^of .*/.test(text)) {
        result = text.substring(3, text.length);
    }
    return result;
}
