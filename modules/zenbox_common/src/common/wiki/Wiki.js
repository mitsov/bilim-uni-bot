var WIKI_SEARCH_LIMIT = 15;

function prepareWikiAnswer(text) {
    text = text.replace(/\u0301/g, "");
    text = text.replace(/(\]|\[)/g, "");
    return text.replace(/\([^)]+\)/g, "");
}

function wikiSearch(lang, search) {
    var ht = "https://" + lang + ".wikipedia.org/w/api.php?action=query&format=json&prop=extracts&generator=prefixsearch&redirects=1&converttitles=1&formatversion=2&exlimit=" + WIKI_SEARCH_LIMIT + "&exintro=1&explaintext=1&gpssearch=" + search + "&gpslimit=" + WIKI_SEARCH_LIMIT; // Можно еще добавить в конце для более строго поиска &gpsprofile=classic
    return $http.get(ht);
}
// удаление текста из скобок
function replaceParenthesis(text) {
    var counter = 0;
    var startIndex = 0;
    var endIndex = 0;
    var parts = [];
    var output = text;

    for (var i = 0; i < text.length; i++) {
        if (text[i] === "(" || text[i] === "{") {
            if (counter === 0) {
                startIndex = i;
            }
            counter += 1;
        } else if (text[i] === ")" || text[i] === "}") {
            counter -= 1;
            if (counter === 0) {
                endIndex = i + 1;
                parts.push(text.substring(startIndex, endIndex));
            }
        }
    }
    parts.forEach(function(part) {
        output = output.replace(part, "");
    });
    return output;
}

function makeShortAns(str, index) {
    if (index !== -1 && index !== 0) {
        return str.substr(0, index - 2);
    }
    return str;
}
function chooseAns(ans, request, lang) {
    var retAns = "";
    if (typeof request !== "undefined" || ans.length < 2) {
        /*Т.к. в api en wiki в ряде случаев, мы не получаем символ переноса строки (\n),
           и при этом слова "слипаются", например,
          "extract": "In classical physics and general chemistry, matter is [.......]–gluon !!plasma.Usually!! atoms can be [.......],
           если же абзац не заканчивается на сноску, мы получаем нормальный ответ, как ниже
          "extract": "The Matterhorn; French: Cervin is a [.......] since the Roman Era.\nThe Matterhorn was studied by Horace-Bénédict [.......] 
           Поэтому нужно найти "слипшиеся" слова и отрезать, все, что идет после первого "слипшегося" слова, так мы получим первый абзац
         */
        var shortAnswer = ans.split("\n")[0];
        if (lang !== "ru") {
            var re = (/(\w+\.\w{2,}?)|(\w{4,}?\.\w{1,}?)/g);
            var result = re.exec(ans);
            var lastIndex = re.lastIndex;
            shortAnswer = makeShortAns(ans, lastIndex);
        }
        if (ans.length <= request.length + 2 || shortAnswer.match(request + ":")
            || shortAnswer.match(request + " :") || ans.match(" — это:")
            || ans.match("неоднозначный термин") || ans.match("несколько значений:")
            || ans.match("может означать") || ans.match("многозначный термин")
            || ans.match("может обозначать") || ans.match("Эта статья")
            || ans.match("многозначное слово") || ans.match("переносном смысле")
            || ans.match("может относиться к:") || ans.match("термин, используемый для обозначения:")
            || ans.match("в широком смысле слова ― это") || ans.match("refer to:")
            || ans.match("refers to:") || ans.match("may be:")) {
            retAns = (lang === "ru" ? "Это многозначное слово, попробуй спросить по-другому." : "This word is ambiguous, say it differently.");
        } else {
            retAns = (shortAnswer.length > request.length + 10 ? shortAnswer : ans);
            var checkShortAns = shortAnswer.trim(); // Удаляем неотображаемые символы в ответе, чтобы сравнить с запросом //
            var requestToUpperCase = capitalize(request);
            if (checkShortAns === requestToUpperCase || checkShortAns === request) {
                retAns = (lang === "ru" ? "Это многозначное слово, попробуй спросить по-другому." : "This word is ambiguous, say it differently.");
            }
        }
    } else {
        retAns = cantGetWiki();
    }
    return retAns;
}

function capitalizeAll(str) {
    return str.replace(/(^|\s)\S/g, function(a) { return a.toUpperCase(); });
}

function getNormalFormForSearch(text) {
    return text
        .split(" ")
        .map(function(word) {
            return $nlp.parseMorph(word).normalForm;
        })
        .join(" ");
}

function getSearchAns(res, request, lang, needMoreRes, index) {
    var $session = $jsapi.context().session;
    var retAns = cantGetWiki();

    if (typeof request !== "undefined") {
        var req = request.replace(/_/g, ""); // убираем _ для сравнения с тайтлами
        var name = capitalizeAll(req); // для сравнения с большими буквами в именах/названиях, распознанных как текст

    // ищем полное соответствие в редиректе и подменяем, если есть
    // есть ощущение, что редиректы появляются редко и не когда нужно
    if (res.data.query.redirects) {
        for (var i = 0; i < res.data.query.redirects.length; i++) {
            if (res.data.query.redirects[i].from === req || res.data.query.redirects[i].from === name) {
                req = res.data.query.redirects[i].to;
            }
        }
    }
    // Если просят еще значений слова, выводим следующее по индексу значение
    // Если слово многозначно, выводим следующее значение
    if (needMoreRes) {
        for (var n = index; n < WIKI_SEARCH_LIMIT + 1; n++) {
            for (var k = 0; k < $session.usedIndexes.length; k++) {
                if ($session.usedIndexes[k] === n) {
                    n++;
                }
            }
            for (var x = 0; x < res.data.query.pages.length; x++) {
                if (res.data.query.pages[x].index === n) {
                    ans = replaceParenthesis(res.data.query.pages[x].extract.replace(/́/g, ""));
                    retAns = chooseAns(ans, req, lang);
                    $session.usedIndexes.push(res.data.query.pages[x].index);
                    break;
                }
            }
            if (retAns.length > req.length + 2 && retAns !== "Это многозначное слово, попробуй спросить по-другому."
                && retAns !== "This word is ambiguous, say it differently.") {
                break;
            }
        }
    } else {
    var tmpCount = 0;
    var ans = "";
    // ищем полное соответствие запросу
    for (var j = 0; j < res.data.query.pages.length; j++) {
        if (res.data.query.pages[j].title === req || res.data.query.pages[j].title === name) {
            ans = replaceParenthesis(res.data.query.pages[j].extract.replace(/́/g, ""));
            retAns = chooseAns(ans, req, lang);
            $session.usedIndexes = [res.data.query.pages[j].index];
        } else {
            tmpCount += 1;
        }
    }
    // если нет полного совпадения - выдаём результат с первым индексом
    if (tmpCount === res.data.query.pages.length) {
        for (var n = 1; n < WIKI_SEARCH_LIMIT + 1; n++) {
            for (var x = 0; x < res.data.query.pages.length; x++) {
                if (res.data.query.pages[x].index === n) {
                    ans = replaceParenthesis(res.data.query.pages[x].extract.replace(/́/g, ""));
                    retAns = chooseAns(ans, req, lang);
                    $session.usedIndexes = [res.data.query.pages[x].index];
                    break;
                }
            }
            if (ans.length > 1) {
                break;
            }
        }
    }
    }
    }
    return retAns;
}

function wikiPresidents() {
    var sparql = 'SELECT DISTINCT ?countryLabel ?headOfStateLabel WHERE {  ?country wdt:P31 wd:Q3624078 . FILTER NOT EXISTS {?country wdt:P31 wd:Q3024240} OPTIONAL { ?country wdt:P35 ?headOfState } . SERVICE wikibase:label { bd:serviceParam wikibase:language "' + global_locale + '" }}';
    return $http.get("https://query.wikidata.org/sparql?format=${format}&query=${sparql}", {
        timeout: 10000,
        query: {
            format: "json",
            sparql: sparql
        }
    }).then(parseHttpResponse).catch(httpError);
}
