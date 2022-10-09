function prepareWikiQuestion(text) {
    var result = text;
    if (/^о .*/.test(text)) {
        result = text.substring(2,text.length);
    } else if (/^об .*/.test(text)) {
        result = text.substring(3,text.length);
    } else if (/^про .*/.test(text)) {
        result = text.substring(4,text.length);
    }
    return result;
}

function cantGetWiki() {
    return selectRandomArg("Хотелось бы научиться отвечать на все вопросы!",
        "Не могу найти подходящего ответа в энциклопедии.",
        "Не получилось найти ответ.",
        "На этот вопрос я пока не знаю ответа.",
        "Вот поучусь ещё - и буду отвечать на все вопросы!",
        "Чтобы отвечать на все вопросы, мне ещё надо учиться и учиться!",
        "Наверное, в энциклопедии есть ответ на этот вопрос, но мне не удалось его найти.");
}

