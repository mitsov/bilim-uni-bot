require: ../../common/common.sc

require: ../../common/wiki/Wiki.js
require: Wiki.js

require: where/where.sc
  module = zb_common

require: patterns.sc
  module = zb_common

require: namesRu/namesRu.sc
   module = zb_common  

require: ../main.js

patterns:
    $specialWikiQestion = ([александр] пушкин/[Владимир] Путин/(первый/второй/третий) закон ньютона)
    $name = $entity<NamesRu> || converter = function(pt) {
        var id = pt.NamesRu[0].value;
        return NamesRu[id].value;
        };

    $patronymic = $regexp<.*(вич|вна)>	

theme: /Wiki

    state: Get
        q!: * [расскажи] * [$you] [знаешь/можешь (ответить/*сказать)] (кто|что) [это] (такое|такая|такой|такие|*знач*) [музыкальн* групп*] ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord) [давай] [например]
        q!: * [$you] знаешь [так*] [группу|город|фильм|книгу|певицу|певца|поэта|писателя|рецепт|музыкальн* групп*] ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord)
        q!: * ([$you] расскажи/что ты знаешь) * (о (группе|городе|фильме|книге|певице|певце|поэте|писателе|музыкальн* групп*)/про (группу|город|фильм|книгу|певицу|певца|поэта|писателя|музыкальн* групп*)) ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord)
        q!: * [что] {$you знаешь [ли] [$smth]} * [о|про] ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord)
        q!: * расскажи * (про|о|об) ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord)
        q!: * где (находится|находятся) ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord)
        q!: ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord) {(что/кто) [это]} (такое/такая/такой/такие)
        q!: а ты ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord) знаешь
        q!: [такой] $specialWikiQestion
        q!: ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord) {[что/кто] это}
        q!: ([что] такое|что *знач*) ([$name [$patronymic]] $Text|($name $patronymic)|$AnyWord $AnyWord) $weight<+0.3>
        q: [а] город $Text || fromState = ../Get, onlyThisState = true
        q: (а $Text) || fromState = ../Get
        q: [а] что за $Text || fromState = ../Get
        script:
            if ($parseTree.name && $parseTree.name[0]) {
                if ($parseTree.Text && $parseTree.Text[0]) {
                    $temp.search = $parseTree.Text[0].value + " " + $parseTree.name[0].value.full;
                    if ($parseTree.patronymic && $parseTree.patronymic[0]) {
                        $temp.search += " " + $parseTree.patronymic[0].text;
                    }
                    $temp.search2 = capitalize($parseTree.name[0].value.full) + "-" + capitalize($parseTree.Text[0].value); // Санта-Клаус
                } else {
                    $temp.search = $parseTree.name[0].value.name + " " + $parseTree.patronymic[0].text;
                }
            } else if ($parseTree.AnyWord && $parseTree.AnyWord[0] && $parseTree.AnyWord[1]) {
                $temp.search = $parseTree.AnyWord[0].text + " " + $parseTree.AnyWord[1].text;
                $temp.search2 = $parseTree.AnyWord[1].text + " " + $parseTree.AnyWord[0].text;
            } else if ($parseTree.Text) {
                $temp.search = $parseTree.Text[0].value;
            } else {
                $temp.search = "";
            }
            /* Если перешли из state: AnotherMeaning
            $temp.search меняем на тот, который получили из state: AnotherMeaning
            $temp.search2 будет равен перевернутому $temp.search, если слов было 2
            Если слово было 1/больше 2 $temp.search2 будет равен $temp.search */
            if ($session.addToSearch) {
                $temp.search = $session.addToSearch;
            }
            $temp.search3 = $temp.search;
            // Мы выше уже могли получить $temp.search2, например, с вариантом Санта-Клаус.
            // Чтобы не перезаписывать это значение добавил это условие
            if (!$temp.search2) {
                // Это нужно для вариантов, когда мы попадаем в этот стейт через паттерн [а] что за $Text
                // Если слов было 2 переворачиваем их и записываем в $temp.search2
                var words = $temp.search.split(" ");
                if (words.length === 2) {
                    $temp.search2 = [words[1], words[0]].concat(words.splice(2, words.length)).join(" ");
                } else {
                    $temp.search2 = $temp.search;
                }
            }
            $temp.search4 = $temp.search2;
            /* Записываем в $temp.search и $temp.search2 нормализованные формы слов.
            Если не получилось, оставляем их, какими они были */
            $temp.search = getNormalFormForSearch($temp.search);
            $temp.search2 = getNormalFormForSearch($temp.search2);

            $temp.search = capitalize(prepareWikiQuestion($temp.search)).replace(/ /g, "_");
            $temp.search2 = capitalize(prepareWikiQuestion($temp.search2)).replace(/ /g, "_");
            $temp.search3 = capitalize(prepareWikiQuestion($temp.search3)).replace(/ /g, "_");
            $temp.search4 = capitalize(prepareWikiQuestion($temp.search4)).replace(/ /g, "_");
            // Cперва отправляем запрос без нормализованной формы слова, потом с ней и запросы с переставленными местами словами //
            $temp.searches = [$temp.search3, $temp.search, $temp.search4, $temp.search2];

            for (var i = 0; i < $temp.searches.length; i++) {
                var res = wikiSearch("ru", $temp.searches[i]);

                if (!res.isOk || !(res && res.data.query && res.data.query.pages.length > 0)) {
                    $temp.answer = cantGetWiki();
                    if ($session.addToSearch) {
                        delete $session.addToSearch;
                    }
                    continue;
                }

                $temp.answer = getSearchAns(res, $temp.searches[i], "ru");
                $session.needMore = 1;

                if ($session.addToSearch) {
                    delete $session.addToSearch;
                } else {
                    $session.search = $temp.searches[i];
                    $session.gotRes = res;
                }
                break;
            }
        a: {{$temp.answer}}

        state: AnotherMeaning
            q: * {([а] [что/какое] (еще/другое/иное) [это] * [значит*/означает/[имеет] значение/вариант*])} *
            q: * {([а] [какие/какое] * (еще/другое/иное)  * значен*)} *
            q: * {[я] (имел* в виду/подразумевал*/говорил*/думал*) (другое/не) } * [а $Text] 
            q: * {[я] * (про/имел* в виду/подразумевал*/говорил*/думал*) *  (что $Text/~который $Text)} 
            q: * [я] * (про/имел* в виду/подразумевал*) $Text 
            q: ((а/[что] (такое/за)|что *знач*) * ~который $Text)
            script:
                if ($parseTree.Text && $parseTree.Text[0]) {
                    $temp.searchNormalWords = [];
                    // Пытаемся отловить в $Text нормальную форму слова, по которой мы искали в 1 стейте
                    for (var j = 0; j < $parseTree.Text[0].words.length; j++) {
                        $temp.searchNormalWords[j] = $nlp.parseMorph($parseTree.Text[0].words[j]).normalForm;
                        if (capitalize($parseTree.Text[0].words[j]).match($session.search)
                            || capitalize($parseTree.Text[0].value).match($session.search)) {
                            $session.addToSearch = $parseTree.Text[0].value;
                            $reactions.transition("/Wiki/Get");
                            break;
                        } else {
                            $session.addToSearch = $session.search + " " + $parseTree.Text[0].value;
                        }
                    }
                    $reactions.transition("/Wiki/Get");
                // Если не было $Text, но был поиск в 1 стейте, запускаем повторно поиск и выводим следующий индекс, начиная со 2
                } else if ($session.gotRes && $session.search) {
                    $session.needMore += 1;
                    $reactions.answer(getSearchAns($session.gotRes, $session.search, "ru", true, $session.needMore));
                // Если как-то сюда попали или не запускали первый поиск
                } else {
                    $reactions.answer(cantGetWiki());
                }

    state: Capitals
        q!: * {(какая/назови*/знаешь) * столиц* * $Country2City} *
        q!: столица [страны] $Country2City
        q: * [а] $Country2City * || fromState = ../Capitals, onlyThisState = true
        if: ($parseTree._Country2City.name)
            a: {{$parseTree._Country2City.name}}.
        else:
            random:
                a: Не знаю.
                a: Чего не знаю, того не знаю.

        state: Hint

            state: Result
                q: * Париж* * || fromState = .., onlyThisState = true
                random:
                    a: Правильно!
                    a: Конечно!
                    a: Абсолютно верно!
                    a: Молодчина!

    state: Presidents
        q!: * {(кто/какой/назови*/знаешь/как зовут) * (президент*/~глава/презент) * $Country} *
        q!: [то] (президент*/~глава) [страны] $Country
        q: * [а] $Country * || fromState = ../Presidents, onlyThisState = true
        script:
            wikiPresidents().then(function(res) {
                if (res && res.results && res.results.bindings && res.results.bindings.length > 0) {
                    var found = false;
                    var country;
                    for (var i = 0; i < res.results.bindings.length; i++) {
                        country = res.results.bindings[i].countryLabel.value;
                        try {
                            country = decodeURIComponent(escape(country));
                        } catch (ex) { /* empty */ }
                        if ($parseTree._Country.name === country) {
                            if (res.results.bindings[i].headOfStateLabel && res.results.bindings[i].headOfStateLabel.value !== "") {
                                found = true;
                                var president = res.results.bindings[i].headOfStateLabel.value;
                                try {
                                    president = decodeURIComponent(escape(president));
                                } catch (ex) { /* empty */ }
                                $reactions.answer(president);
                                break;
                            }
                        } else if ($parseTree._Country.name === "Соединённые Штаты Америки") {
                            found = true;
                            $reactions.answer("Дональд Джон Трамп - 45-й президент Соединённых Штатов Америки с 20 января 2017 года.");
                            break;
                        }
                    }
                    if (!found) {
                        $reactions.answer("Я {к сожалению,/} не знаю, кто там {сейчас/} президент.");
                    }
                } else {
                    $reactions.answer(cantGetWiki());
                }
            }).catch(function(err) {
                $reactions.answer(cantGetWiki());
            });
