// This file dedicated for highlighting in IDE and shouldn't be included in any scenario

var $jsapi = {
    context: function () {
        return {
            parseTree: {
                words: [],
                text: "",
                tag: "",
                pattern: "",
                value: undefined
            },
            client: {},
            session: {},
            request: {
                query: "",
                event: "",
                targetState: "",
                data: {},
                rawRequest: {},
                botId: "",
                channelType: "",
                channelBotId: "",
                channelUserId: "",
                questionId: ""
            },
            response: {},
            temp: {},
            injector: {},
            currentState: "",
            contextPath: "",
            contextHistory: [],
            testContext: {}
        }
    }
};

var $nlp = {
    inflect: function(text, declension) {
        return "";
    },
    conform: function(word, number) {
        return "";
    },
    parseMorph: function(word) {
        return {};
    },
    match: function (text, state, onlyThisState) {
        return {
            targetState: "",
            patternId: "",
            pattern: "",
            effectivePattern: "",
            score: 0,
            parseTree: {}
        }
    },
    tokenize: function (text) {
        return [];
    },
    checkVocabulary: function (word, lang, dict) {
        return true;
    },
    fixKeyboardLayout: function (text) {
        return "";
    }
};

function log(text) {
}