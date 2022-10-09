// запоминаем настоящую функцию обработки ответа, т.к. мы будем вызывать её после обработки шаблона
var realAnswerFunction = $reactions.answer;

$reactions.answer = function (params) {
    // Приоритет выбора языка
    // 1. Параметр ответ a: Answer || lang = en
    // 2. $request.language
    // 3. языка проекта chatbot.yml language: ru
    if (params) {  
        var lang = params.lang;
    }
    var $response = $jsapi.context().response;
    // Есть разница в том, как функция вызывается из JS-кода и из сценария, в js-файлах мы передаём строковый аргумент, а движок сценариев передаём объект с полем value. Нужно обработать этот случай
    if (typeof (params) === "object") {
        if (params.tts) {
            if ($response.replies) {
                $response.replies[$response.replies.length - 1].tts = params.tts;
            }
        }
        if (typeof $response.question !== "boolean") {
            if (params.question) {
                $response.question = (params.question === 'true');
            } else {
                $response.question = true;
            }
        }

        if (params.go) {
            $reactions.transition(params.go);
        }
        params = Array.isArray(params) ? selectRandomArg(params) : params.value;
    }
    // Выполняем обработку подстановок
    var tt = resolveInlineDictionary(resolveVariables(resolveInlineDictionary(params)));
    // Вызываем системную функцию обработки ответа
    realAnswerFunction({value: tt, lang: lang});
};


function checkConverterErrors() {
    var $parseTree = $jsapi.context().parseTree;
    var $temp = $jsapi.context().temp;    
    if ($parseTree && $parseTree.Where && $parseTree.Where[0].value.error && $parseTree.Where[0].value.error == "unknown location") {
        $temp.targetState =  "/Errors/Unknown location";
    }
}

bind("preProcess", checkConverterErrors);

function getRandomQuestionType() {
    var $session = $jsapi.context().session;  
    var types = [];
    if ($session.lastQuiz) {
        for (var j = 0; j < arguments.length; j++) {
            if ($session.lastQuiz.type != arguments[j]) {
                types.push(arguments[j]);
            }
        }
    } else {
        types = arguments;
    } 
    var index = randomIndex(types);
    return types[index];
}

function saveLastAnswer() {
    var session = $jsapi.context().session; 
    var response = $jsapi.context().response; 
    session.lastAnswer = response.answer;
}

bind("postProcess", saveLastAnswer);

function savePrevState() {
    var session = $jsapi.context().session; 
    session.prevState = $jsapi.context().contextPath;
}

bind("postProcess", savePrevState);

function checkTag(tag){
    var $parseTree = $jsapi.context().parseTree;
    var parseTreeString = JSON.stringify($parseTree);
    var regexp = new RegExp("\"tag\":\"" + tag + "\"", "i");
    return (parseTreeString.search(regexp) != -1);
}

function shuffleArr(arr) {
    if ($jsapi.context().testContext) {
        return arr
    }

    var y, x;
    for (var i = arr.length - 1; i > 0; i--) {
        y = Math.floor(Math.random() * (i + 1));
        x = arr[i];
        arr[i] = arr[y];
        arr[y] = x;
    }
    return arr;
}

// 3 -> "три" или "третий"
// Категории: 'cardinal' (по умолчанию), 'ordinal' 
// Максимальное количественное числительное 999 999 999 999
// Максимальное порядковое числительное 1 000 000
var numberToString = (function() {

    var arr_numbers = new Array();
    arr_numbers[1] = new Array('', 'один', 'два', 'три', 'четыре', 'пять', 'шесть', 'семь', 'восемь', 'девять', 'десять', 'одиннадцать', 'двенадцать', 'тринадцать', 'четырнадцать', 'пятнадцать', 'шестнадцать', 'семнадцать', 'восемнадцать', 'девятнадцать');
    arr_numbers[2] = new Array('', '', 'двадцать', 'тридцать', 'сорок', 'пятьдесят', 'шестьдесят', 'семьдесят', 'восемьдесят', 'девяносто');
    arr_numbers[3] = new Array('', 'сто', 'двести', 'триста', 'четыреста', 'пятьсот', 'шестьсот', 'семьсот', 'восемьсот', 'девятьсот');

    function parseNumber(num, desc) {
        var string = '';
        var num_hundred = '';
        var source_num = num;
        if (num.length == 3) {
            num_hundred = num.substr(0, 1);
            num = num.substr(1, 3);
            string = arr_numbers[3][num_hundred] + ' ';
        }
        if (num < 20 && num[0] != '0') {
            string += arr_numbers[1][num] + ' ';
        }
        else {
            var first_num = num.substr(0, 1);
            var second_num = num.substr(1, 2);
            string += arr_numbers[2][first_num] + ' ' + arr_numbers[1][second_num] + ' ';
        }             
        switch (desc){
            case 1:
                if (source_num == '000') {
                    break;
                }
                var last_num = num.substr(-1);
                if (last_num == 1) {
                    string += 'тысяча ';
                }
                else if (last_num > 1 && last_num < 5) {
                    string += 'тысячи ';
                }
                else {
                    string += 'тысяч ';
                }
                string = string.replace('один ', 'одна ');
                string = string.replace('два ', 'две ');
                break;
            case 2:
                if (source_num == '000') {
                    break;
                }
                var last_num = num.substr(-1);
                if (last_num == 1) {
                    string += 'миллион ';
                }
                else if (last_num > 1 && last_num < 5) {
                    string += 'миллиона ';
                }
                else {
                    string += 'миллионов ';
                }
                break;
            case 3:
                var last_num = num.substr(-1);
                if (last_num == 1) {
                    string += 'миллиард ';
                }
                else if (last_num > 1 && last_num < 5) {
                    string += 'миллиарда ';
                }
                else {
                    string += 'миллиардов ';
                }
                break;
        }
        string = string.replace('  ', ' ');
        return string;
    }

    return function(number, category) {
        if (category && !_.contains(["cardinal", "ordinal"], category)) {
            throw new Error("Unknown number category: " + category);
        }

        if (number === 0) {
            return (category === 'ordinal') ? "нулевой" : "ноль";
        }

        var numStr = number.toString();

        if (!number || typeof(number) !== 'number' || numStr.indexOf('.') > -1) {
            throw new Error("Invalid input, integer required: " + number);
        }

        if (numStr.length > 12) {
            throw new Error("Number too large: " + numStr);
        }

        if (category === 'ordinal' && numStr.length >= 7 && numStr !== '1000000') {
            throw new Error("Number too large to get ordinal category: " + numStr);
        }

        var number_length = numStr.length;
        var string = '';
        var num_parser = '';
        var count = 0;
        for (var p = (number_length - 1); p >= 0; p--) {
            var num_digit = numStr.substr(p, 1);
            num_parser = num_digit + num_parser;
            if ((num_parser.length == 3 || p == 0) && !isNaN(num_parser)) {
                string = parseNumber(num_parser, count) + string;
                num_parser = '';
                count++;
            }
        }

        string = string.trim();

        if (category === 'ordinal') {
            var num_parts = string.split(' ');
            var ordinal_part = num_parts.pop();

            if (ordinal_part == 'сто') {
                string = 'сотый'
            }
            else if (ordinal_part == 'тысяча') {
                string = 'тысячный';
            }
            else if (ordinal_part == 'миллион') {
                string = 'миллионный';
            }
            else if (string.indexOf('миллион') == -1 &&
                     ordinal_part.indexOf('тысяч') > -1) {
                string = '';
                for (var i = 0; i < num_parts.length; i++) {
                    string += $nlp.inflect(num_parts[i], "gent");
                }
                string += "тысячный";
            }
            else {
                ordinal_part = $nlp.inflect(ordinal_part, "Anum");
                string = num_parts.join(' ') + ' '  + ordinal_part; 
            }
        }

        return string;
    }
} ());