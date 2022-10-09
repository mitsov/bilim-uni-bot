require: Poets.js

require: poets-ru.csv
    name = Poets
    var = $Poets

require: poems-ru.csv
    name = Poems
    var = $Poems

require: number/number.sc
    module = zb_common

require: ../../common/common.sc
  
require: ../main.js


init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .PoetTagConverter = function(parseTree) {
            var id = parseTree.Poets[0].value;
            return $Poets[id].value;
        };

    $global.$converters
        .PoemTagConverter = function(parseTree) {
            var id = parseTree.Poems[0].value;
            return $Poems[id].value;
        };

patterns:
    $poet = $entity<Poets> || converter = $converters.PoetTagConverter
    $poem = $entity<Poems> || converter = $converters.PoemTagConverter

theme: /Poets

    state: Tell a poem
        q!: * {(*скажи|[ты] (знаешь/*учил/запомнил)|рассказать|давай*|почитай|прочитай|прочти|[хочу] (послушать/услышать)) * [стих*/стиш*] $poet [$poem]} *
        q!: * {(*скажи|[ты] (знаешь/*учил/запомнил)|рассказать|давай*|почитай|прочитай|прочти|[хочу] (послушать/услышать)) * [стих*/стиш*] $poem [$poet]} *        
        q!: * {читать стихи $poet [$poem]}
        q!: * {читать стихи $poem [$poet]}        
        q!: * {(стихотворение/стих творени*/стишок) $poet [$poem]}
        q!: * {(стихотворение/стих творени*/стишок) $poem [$poet]}
        q:  * {$poet * [$poem]} *
        q:  * {$poem * [$poet]} *
        script:
            // Для вариантов, когда есть несколько авторов для 1 произведения
            $session.poem = $parseTree.poem ? $parseTree._poem : undefined;
            $session.poems = [];
        if: ($session.poem && !$parseTree.poet)
            script:
                // Ищем повторы в словаре стихов и добавляем в массив
                for (var j = 1; j <= Object.keys($Poems).length; j++) {
                    var poem = $Poems[j].value;
                    if (poem.title == $session.poem.title) {
                        $session.poems.push(poem);
                    }
                }
        if: $session.poems.length > 1 
            script:
                $temp.needToChooseOne = true;
                $temp.answer = "";
                for (var k = 0; k < $session.poems.length; k++) {
                    $temp.valPoet = $session.poems[k];
                    $temp.answer += $temp.valPoet.author + " или ";
                }
                $temp.answer = $temp.answer.slice(0, -5);
            a: У меня в памяти несколько стихотворений разных авторов с названием '{{$session.poem.title}}'. Выбирай: {{$temp.answer}}?
            go!: /Poets/Tell a poem/Choose One poet

        if: (!$temp.needToChooseOne)
            script:
                getPoem($parseTree.poet ? $parseTree._poet : undefined, $parseTree.poem ? $parseTree._poem : undefined);
            a: Это стихотворение '{{$session.currentSong.title}}'. {Автор/Его автор} {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}}.

        state: Choose One poet || modal = true

            state: Get One poet
                q: * $poet *
                q: * $Number *
                if: $parseTree.Number
                    script:
                        if ($parseTree._Number > $session.poems.length || $parseTree._Number < 1) {
                            $reactions.transition("../Local Catch All");
                        } else {
                            $temp.valPoet = getAuthorByPoem($session.poems[$parseTree._Number - 1]);
                        }
                else:
                    script:
                        $temp.valPoet = $parseTree._poet;
                script:
                    getPoem($temp.valPoet, $session.poem);
                    delete $session.poems;
                    delete $session.poem;
                a: Это стихотворение '{{$session.currentSong.title}}'. {Автор/Его автор} {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}}.
                go: /Poets/Tell a poem

            state: Local Catch All
                event: noMatch
                a: Прости, назови, пожалуйста, фамилию писателя.

            state: Not Want
                q: * не (хочу/надо) *
                q: * (передумал*/друго*) *
                a: Хорошо, назови другое произведение или автора.
                go: /Poets/Tell a poem

        state: What poems you know
            q!: * {(какие/каких поэтов) стих* ты знаешь} * 
            script:
                $temp.answer = "";
                $temp.ids = _.pluck($Poets, "id");
                $temp.randNumbers = _.sample($temp.ids, 3);
                for (var i = 1; i <= 3; i++) {
                    var index = $temp.randNumbers[i - 1];
                    $temp.answer += $Poets[index].value.firstName
                    + " " + $Poets[index].value.thirdName + " " + $Poets[index].value.secondName + ", ";
                }
                $temp.answer = $temp.answer.slice(0, -2);
            a: Вот, например, поэты, стихи которых я знаю: {{$temp.answer}}.
            go: ../../Tell a poem

        state: One More
            q: ** [давай] (еще/другое) * || fromState = .., onlyThisState = true
            script:
                getPoem($session.currentPoet, undefined);
            a: Это стихотворение '{{$session.currentSong.title}}'. {Автор/Его автор} {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}}.
            go: ../../Tell a poem

        state: Repeat
            q: * (еще раз*/повтори*) * || fromState = .., onlyThisState = true
            q: * (прочитай/расскажи) * (это сти*/его) * || fromState = ../Author, onlyThisState = true
            q: * (прочитай/расскажи/рассказывай) || fromState = ../Author, onlyThisState = true
            script:
                $response.intent = "poems_on";
                $response.stream = $session.currentSong.stream;
            a: Это стихотворение '{{$session.currentSong.title}}'. {Автор/Его автор} {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}}.
            go: ../../Tell a poem

        state: Author
            q!: * кто (написал/автор/поэт*) * [сти*] * $poem *
            q!: * как зовут (автор*/поэт*) * [сти*] * $poem *
            q: * (кто/как* поэт*) (автор этого [сти*]/{это* [сти*] написал}) *
            q: * (кто/скажи/назови) (автор*/поэт*/написал)
            q: * $poem * || fromState = ../Author, onlyThisState = true
            script:
                $session.poem = $parseTree.poem ? $parseTree._poem : undefined;
                $session.poems = [];
            if: $session.poem
                script:
                    // Ищем повторы в словаре стихов и добавляем в массив
                    for (var j = 1; j <= Object.keys($Poems).length; j++) {
                        var poem = $Poems[j].value;
                        if (poem.title == $session.poem.title) {
                            $session.poems.push(poem);
                        }
                    }
                if: $session.poems.length > 1 
                    script:
                        $temp.needToChooseOne = true;
                        $temp.answer = "";
                        for (var k = 0; k < $session.poems.length; k++) {
                            $temp.valPoet = getAuthorByPoem($session.poems[k]);
                            $temp.answer += $temp.valPoet.firstName + " " + $temp.valPoet.thirdName + " " + $temp.valPoet.secondName + " и ";
                        }
                        $temp.answer = $temp.answer.slice(0, -3);
                        $session.currentSong = $parseTree._poem;
                        $session.fromTellAuthorOfPoem = true;
                    a: У меня в памяти несколько стихотворений разных авторов с названием '{{$session.poem.title}}'. Это: {{$temp.answer}}.
                else:
                    script:
                        $session.currentSong = $parseTree._poem;
                        $session.currentPoet = getAuthorByPoem($parseTree._poem);
                    a: Автор стихотворения '{{$session.currentSong.title}}' - {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}}.
            else: 
                a: Автор стихотворения '{{$session.currentSong.title}}' - {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}}.

    state: Birth Date
        q!: * {(когда/как* год*) * (родился/родилась) * ($poet/автор* [сти*] $poem)} *
        q!: * {((дат*/день) рожден*) * ($poet/автор* [сти*] $poem)} *
        q: * {(когда/как* год*) * (родился/родилась)} *
        q: * ((дат*/день) рожден*) *
        q: * (когда/как* год*) * || fromState = "../Birth Place", onlyThisState = true 
        q: * ($poet/автор* [сти*] $poem) * || fromState = "../Birth Date", onlyThisState = true
        q: * (родился/родилась) * || fromState = "../Death Date", onlyThisState = true
        q!: * ((расскажи*/скажи*) [мне]/знаешь) (о/про/об) ($poet/автор* [сти*] $poem) * 
        script:
            if ($parseTree.poet) {
                $session.currentPoet = $parseTree._poet;
            } else if ($parseTree.poem) {
                $session.currentSong = $parseTree._poem;
                $session.currentPoet = getAuthorByPoem($parseTree._poem);            
            }
            $temp.verb = $session.currentPoet.gender == "male" ? "родился" : "родилась";
        a: {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}} {{$temp.verb}} {{moment($session.currentPoet.birthDate).locale('ru').format('Do MMMM YYYY года')}}.

    state: Death Date
        q!: * {(когда/как* год*) * (умер/умерла/скончал*) * ($poet/автор* [сти*] $poem)} *
        q!: * {((дат*/день) смерт*) * ($poet/автор* [сти*] $poem)} *
        q: * {(когда/как* год*) * (умер/умерла/скончал*)} *
        q: * ((дат*/день) смерт*) *
        q: * (когда/как* год*) * || fromState = "../Death Place", onlyThisState = true  
        q: * ($poet/автор* [сти*] $poem) * || fromState = "../Death Date", onlyThisState = true
        q: * (умер/умерла/скончал*) * || fromState = "../Birth Date", onlyThisState = true   
        script:
            if ($parseTree.poet) {
                $session.currentPoet = $parseTree._poet;
            } else if ($parseTree.poem) {
                $session.currentSong = $parseTree._poem;
                $session.currentPoet = getAuthorByPoem($parseTree._poem);            
            }
            $temp.verb = $session.currentPoet.gender == "male" ? "умер" : "умерла";
        a: {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}} {{$temp.verb}} {{moment($session.currentPoet.deathDate).locale('ru').format('Do MMMM YYYY года')}}.

    state: Birth Place
        q!: * {(где/как* (город*/стран*)) * (родился/родилась) * ($poet/автор* [сти*] $poem)} *
        q!: * {((мест*/город*/стран*) рожден*) * ($poet/автор* [сти*] $poem)} *
        q: * {(где/как* (город*/стран*)) * (родился/родилась)} *
        q: * ((мест*/город*/стран*) рожден*) *
        q: * (где/[в] как* (город*/стран*)) * || fromState = "../Birth Date", onlyThisState = true
        q: * ($poet/автор* [сти*] $poem) * || fromState = "../Birth Place", onlyThisState = true
        q: а (родился/родилась/вырос*/жил*) || fromState = "../Death Place", onlyThisState = true 
        script:
            if ($parseTree.poet) {
                $session.currentPoet = $parseTree._poet;
            } else if ($parseTree.poem) {
                $session.currentSong = $parseTree._poem;
                $session.currentPoet = getAuthorByPoem($parseTree._poem);            
            }
            $temp.verb = $session.currentPoet.gender == "male" ? "родился" : "родилась";
        a: {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}} {{$temp.verb}} {{$session.currentPoet.birthPlace}}.

    state: Death Place
        q!: * {(где/как* (город*/стран*)) * (умер/умерла/скончал*) * ($poet/автор* [сти*] $poem)} *
        q!: * {((мест*/город*/стран*) смерт*) * ($poet/автор* [сти*] $poem)} *
        q: * {(где/как* (город*/стран*)) * (умер/умерла/скончал*)} *
        q: * ((мест*/город*/стран*) смерт*) *
        q: * (где/как* город*/[в] как* стран*) * || fromState = "../Death Date", onlyThisState = true 
        q: * ($poet/автор* [сти*] $poem) * || fromState = "../Death Place", onlyThisState = true
        q: а (умер*/погиб*/скончал*) || fromState = "../Birth Place", onlyThisState = true
        script:
            if ($parseTree.poet) {
                $session.currentPoet = $parseTree._poet;
            } else if ($parseTree.poem) {
                $session.currentSong = $parseTree._poem;
                $session.currentPoet = getAuthorByPoem($parseTree._poem);            
            }
            $temp.verb = $session.currentPoet.gender == "male" ? "умер" : "умерла";
        a: {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}} {{$temp.verb}} {{$session.currentPoet.deathPlace}}.

    state: Full Name
        q!: * {как* (зовут/звали/имя/отчеств*) * [поэт*] * ($poet/автор* [сти*] $poem)} *
        q: * {как* (зовут/звали/имя/отчеств*) * [поэт*]} *
        q: * ($poet/автор* [сти*] $poem) * || fromState = "../Full Name", onlyThisState = true
        script:
            if ($parseTree.poet) {
                $session.currentPoet = $parseTree._poet;
            } else if ($parseTree.poem) {
                $session.currentSong = $parseTree._poem;
                $session.currentPoet = getAuthorByPoem($parseTree._poem);            
            }
        a: {{$session.currentPoet.gender == "male" ? "Этого поэта" : "Эту поэтессу"}} звали {{$session.currentPoet.firstName}} {{$session.currentPoet.thirdName}} {{$session.currentPoet.secondName}}.