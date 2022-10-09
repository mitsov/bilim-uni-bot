require: ../../common/common.sc

require: ../../common/wiki/Wiki.js
require: Wiki.js

require: where/whereEn.sc
  module = zb_common

require: patternsEn.sc
  module = zb_common

require: namesEn/namesEn.sc
   module = zb_common  

require: ../main.js

patterns:
    $honorific = (Master/Mr/Miss/Mrs/Ms/Mx/Sir/Gentleman/Sire/Mistress/Madam/Ma'am/Dame/Lord/Lady/Esq/Excellency/Excellence/(Her/His Honour))
theme: /Wiki

    state: Get
        q!: * (who|what) do [think/know] * [about/of] [a/an/the] ($Name $AnyWord /$AnyWord $AnyWord/$Text/(the/an/a) ($AnyWord::Text))
        q!: * (who|what) ('s/is/are/'re/does) (a/an/the) [it mean*] ($Name $AnyWord /$AnyWord $AnyWord/$Text/(the/an/a) ($AnyWord::Text)) [mean/means]
        q!: * (who|what) ('s/is/are/'re/does) [a/an/the] [it mean*] ($Name $AnyWord /$AnyWord $AnyWord/$Text/(the/an/a) ($AnyWord::Text)) [mean/means]
        q!: * (who|what) ('s/is/are/'re/does) [a/an/the] [it mean*] $Text (mean/means) $weight<+0.1>
        q!: * (who|what) [a/an/the] ($Name $AnyWord /$AnyWord $AnyWord/$Text) (is/are)
        q!: * [know/tell/inform] [me] * (who|what) (a/an/the) ($Name $AnyWord /$AnyWord $AnyWord/$Text) (is/are) 
        q!: * [$you] [know/tell/inform] (who|what) * ('s/is/are/'re) [music group/band] [$honorific] ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord | (the/an/a) ($AnyWord::Text)) *
        q!: * (who|what) [exactly] [a/an/the] $Text (is/means/mean) [really] 
        q!: * define [a/an/the] $Text [for me] 
        q!: * [a/the] (definition/meaning) of [a/an/the] $Text 
        q!: * where [a/an/the] ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord) is (situated/located) *
        q!: * where is [a/an/the] ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord) (situated/located) *
        q!: * where is [situated/located] [a/an/the] ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord) *
        q!: * (location/where) ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord)
        q!: * [$you] know [band|city|town|film|book|singer|poet|writer|receipt|music group] ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord)
        q!: * [[what] do] $you know [anything/something] * [about] ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord)
        q!: * (tell/inform) me * (about/what is) ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord)
        q!: ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord) (is what/ what (was/is) [it])
        q!: (do you know/who) * [was/were] ([$Name [$AnyWord]] $Text|($Name $AnyWord)|$AnyWord $AnyWord)
        q: * city $Text || fromState = ../Get, onlyThisState = true
        q: ([and] what about $Text) || fromState = ../Get
        script:
            if ($parseTree.Name && $parseTree.Name[0]) {
                if ($parseTree.Text && $parseTree.Text[0]) {
                    $temp.search = $parseTree.Text[0].text + " " + $parseTree.Name[0].value.name;
                    if ($parseTree.AnyWord && $parseTree.AnyWord[0]) {
                        $temp.search += " " + capitalize($parseTree.AnyWord[0].text);
                    }
                    $temp.search2 = capitalize($parseTree.Name[0].value.name) + "-" + capitalize($parseTree.Text[0].text); // Santa-Klaus
                } else if ($parseTree.AnyWord && $parseTree.AnyWord[0]) {
                    $temp.search = $parseTree.Name[0].value.name + " " + capitalize($parseTree.AnyWord[0].text);
                    $temp.search2 = capitalize($parseTree.Name[0].value.name) + "-" + capitalize($parseTree.AnyWord[0].text); // Santa-Klaus
                } else {
                    $temp.search = $parseTree.Name[0].value.name;
                }
            } else if ($parseTree.AnyWord && $parseTree.AnyWord[0] && $parseTree.AnyWord[1]) {
                $temp.search = $parseTree.AnyWord[0].text + " " + $parseTree.AnyWord[1].text;
                $temp.search2 = $parseTree.AnyWord[1].text + " " + $parseTree.AnyWord[0].text;
            } else if ($parseTree.Text) {
                $temp.search = $parseTree._Text;
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
                var res = wikiSearch("en", $temp.searches[i]);

                if (!res.isOk || !(res && res.data.query && res.data.query.pages.length > 0)) {
                    $temp.answer = cantGetWiki();
                    if ($session.addToSearch) {
                        delete $session.addToSearch;
                    }
                    continue;
                }

                $temp.answer = getSearchAns(res, $temp.searches[i], "en");
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
            q: * ((what/anything/something) else [[does] * [(a/an/the) * ] [it] mean*]) *
            q: * [any] (other/more) mean* *
            q: * what ('s/is) [the] ((another/other) meaning/opposite [meaning])  [of] *
            q: * [I] [am/was] (mean*/talk*/think*) (differ*/not [[about] (this/that/it)]) * [but $Text] 
            q: * [I] [am/was] (mean*/talk*/think*) *  ((about/that/which/who) $Text)
            q: * [I] [am/was] (mean*) [like]  [a/an/the] $Text
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
                    $reactions.answer(getSearchAns($session.gotRes, $session.search, "en", true, $session.needMore));
                // Если как-то сюда попали или не запускали первый поиск
                } else {
                    $reactions.answer(cantGetWiki());
                }

    state: Capitals
        q!: * what [is] [the] (capital/(main/republic) (city/town)) [of] $Country2City *
        q!: {((capital/republic) [city] [of]) $Country2City}
        q!: [(give/tell) me/name] * {((capital/republic) [city] [of]) $Country2City}
        q: * $Country2City * || fromState = ../Capitals, onlyThisState = true
        if: ($parseTree._Country2City.name)
            a: {{$parseTree._Country2City.name}}.
        else:
            random:
                a: I don't know the capital of {{ $parseTree._Country2City.country }}.
                a: I don't know about that.

    state: Presidents
        q!: * [(tell/remind) me/do you know/say] [please] * who is [the] [present] (head/ruler/president/leader) of $Country [now] *
        q!: * [what is the] name [of] [the] [present] (head/ruler/president/leader) of $Country [now] *
        q!: * [what is the] name [of] [present] $Country (head/ruler/president/leader) [now] *
        q!: * who [is] * $Country (head/ruler/president/leader) [now] *
        q!: * $Country (head/ruler/president/leader) *
        q!: * (head/ruler/president/leader) [of] $Country *
        q: * $Country * || fromState = ../Presidents, onlyThisState = true
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
                        }
                    }
                    if (!found) {
                        $reactions.answer("I don't know the president of " + $parseTree._Country.name);
                    }
                } else {
                    $reactions.answer(cantGetWiki());
                }
            }).catch(function(err) {
                $reactions.answer(cantGetWiki());
            });
