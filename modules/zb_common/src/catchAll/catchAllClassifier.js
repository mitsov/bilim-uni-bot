var catchAllClassifier = new function() {

    this.check = function(parseTree) {
        var words = parseTree.words;
        if (checkTranslitration(parseTree.text)) {
            return "Transliteration";
        }

        if (checkNegative(parseTree.text)) {
            return "NegativeEmotion";
        }

        if (checkNonsense(words)) {
            return "Nonsense";
        }

//Часто дает ложные срабатывания
/*        if (checkMaybeNonsense(words)) {
            return "ProbablyNonsense";
        }*/

        return "SeemsMeaningful";
    };

    function checkNonsense(words) {
        if (words.length > 3) {
            return false;
        }
        var count = 0;

        try{
            for (var i = 0; i < words.length; i++) {
                if ($nlp.checkVocabulary(words[i], 'ru', 'pymorphy')) {
                    count++;
                }
            }
        } catch(error){
            log("ERROR: " + error);
            return false
        }

        return count == 0;
    }

    function checkMaybeNonsense(words) {
        var count = 0;

        try{
            for (var i = 0; i < words.length; i++) {
                if ($nlp.checkVocabulary(words[i], 'ru', 'pymorphy')) {
                    count++;
                }
            }
        } catch(error){
            return false
        }

        return count < 2;
    }

    var NO_MATCH = "NO_MATCH";
    function cachedNlpMatch(text, state) {
        var key = text + "  " + state;
        key = escape(key).replace(/[\._\s]/g, "_");
        var $session = $jsapi.context().session;
        $session.matchCache = $session.matchCache || {};
        var res = $session.matchCache[key];
        if (!res) {
            res = $nlp.match(text, state, false);
            log( "Result from NLP Match: " + JSON.stringify(res) );
            if (!res) {
                res = NO_MATCH;
            }
            $session.matchCache[key] = res;
        }
        if (res == NO_MATCH) {
            return null;
        } else {
            return res;
        }
    }

    function checkTranslitration(text) {
        text = $nlp.fixKeyboardLayout(text);

        if (text == null) {
            return false;
        }

        var result = cachedNlpMatch(text, "/");

        if (result == null) {
            return false;
        }

        var state = result.targetState || null;

        var catchAll = $jsapi.context().session.catchAll;
        
        if (state != null && !state.startsWith("/CatchAll")) {
            catchAll.transliterationText = text;
            catchAll.transliterationState = state;
            return true;
        }

        return false;
    }

    function checkNegative(text) {
        var result = cachedNlpMatch(text, "/CatchAll/EmotionClassifier");
        if (result && result.targetState == "/CatchAll/EmotionClassifier/Negative") {
            //$jsapi.context().temp.isMeaningfull = checkMeaningful(text);
            return true;
        } else {
            return false;
        }
    }

    var stopWords = RegExp("^(а|е|и|ж|м|о|на|не|ни|об|но|он|мне|мои|мож|она|они|оно|мной|много|многочисленное|многочисленная|многочисленные|многочисленный|мною|мой|мог|могут|можно|может|можхо|мор|моя|моё|мочь|над|нее|оба|нам|нем|нами|ними|мимо|немного|одной|одного|менее|однажды|однако|меня|нему|меньше|ней|наверху|него|ниже|мало|надо|назад|наиболее|недавно|недалеко|между|низко|меля|нельзя|нибудь|непрерывно|наконец|никогда|никуда|нас|наш|нет|нею|неё|них|мира|наша|наше|наши|ничего|начала|нередко|несколько|обычно|опять|около|мы|ну|нх|от|отовсюду|особенно|нужно|очень|отсюда|в|во|вон|вниз|внизу|вокруг|вот|вверх|вам|вами|важное|важная|важные|важный|вдали|везде|ведь|вас|ваш|ваша|ваше|ваши|впрочем|весь|вдруг|вы|все|всем|всеми|времени|время|всему|всего|всегда|всех|всею|всю|вся|всё|всюду|г|год|говорил|говорит|года|году|где|да|ее|за|из|ли|же|им|до|по|ими|под|иногда|довольно|именно|долго|позже|более|должно|пожалуйста|значит|иметь|больше|пока|ему|имя|пор|пора|потом|потому|после|почему|почти|посреди|ей|два|две|его|дел|или|без|день|занят|занята|занято|заняты|действительно|давно|даже|алло|жизнь|далеко|близко|здесь|дальше|для|лет|зато|даром|первый|перед|затем|зачем|лишь|ею|её|их|бы|еще|при|был|про|процентов|против|просто|бывает|бывь|если|люди|была|были|было|будем|будет|будете|будешь|прекрасно|буду|будь|будто|будут|ещё|друго|другое|другой|другие|другая|других|есть|пять|быть|лучше|к|ком|конечно|кому|кого|когда|которой|которого|которая|которые|который|которых|кем|каждое|каждая|каждые|каждый|кажется|как|какой|какая|кто|кроме|куда|кругом|с|т|у|я|та|те|уж|со|то|том|снова|тому|совсем|того|тогда|тоже|собой|тобой|собою|тобою|сначала|только|уметь|тот|тою|хорошо|хотеть|хочешь|хоть|хотя|свое|свои|твой|своей|своего|своих|свою|твоя|твоё|раз|уже|сам|там|тем|чем|сама|сами|теми|само|рано|самом|самому|самой|самого|самим|самими|самих|саму|семь|чему|раньше|сейчас|чего|сегодня|себе|тебе|сеаой|человек|разве|теперь|себя|тебя|седьмой|спасибо|слишком|так|такое|такой|такие|также|такая|сих|тех|чаще|четвертый|через|часто|шестой|шесть|четыре|сколько|сказал|сказала|сказать|ту|ты|три|эта|эти|что|это|чтоб|этом|этому|этой|этого|чтобы|этот|стал|туда|этим|этими|рядом|этих|тут|эту|суть|чуть)$");

    function getWords(text) {
        return $nlp.tokenize(text);
    }

    function collectTextByPatterns(parseTree) {
        var texts = [];
        for (var key in parseTree) {
            if (parseTree.hasOwnProperty(key)) {
                if (key == 'text' || key == 'value' || key == 'tag' || key == 'pattern' || key == 'words') {
                    continue;
                }
                var tags = parseTree[key];
                for (var i = 0; i < tags.length; i++) {
                    texts.push(tags[i].text);
                }
            }
        }
        return texts;
    }

    function removeWordsByPatterns(text) {
        var result = cachedNlpMatch(text, "/CatchAll/EmotionClassifier");
        if (!result) {
            return text;
        }

        var pt = result.parseTree;
        var texts = collectTextByPatterns(pt);
        if (!texts || texts.length == 0) {
            return text;
        }
        for (var i = 0; i < texts.length; i++) {
            text = text.replace(texts[i], "");
        }

        return removeWordsByPatterns(text);
    }
    
    function checkMeaningful(text) {
        // 1. remove brutal and polite words by pattern
        text = removeWordsByPatterns(text);
        // 2. remove stop words
        text = text.split(stopWords).join("");
        // 3. check vocabulary for the rest
        return !checkNonsense(getWords(text));
    }
    
};

