require: Why.js

require: why-ru.csv
    name = WhyQuestions
    var = $WhyQuestions

require: ../main.js
require: ../../common/common.sc
require: answers.yaml
    var = WhyCommonAnswers

init:
    $global.themes = $global.themes || [];
    $global.themes.push("WhyQuestion");
    $global.$WhyAnswers = (typeof WhyCustomAnswers != 'undefined') ? applyCustomAnswers(WhyCommonAnswers, WhyCustomAnswers) : WhyCommonAnswers;

patterns:
    $noMatter = [$me] (плевать|все равно|не принципиально|не важно|неважно|без разницы|безразлично|параллельно|пофи*|(по|один|1) (фиг*|фик|хер*|хрен*|ху*)|похер*|поху*|до пизд*|допизд*|не (имеет|играет) (значения|роли)|[(до этого)] нет дела) [$compliment]

theme: /WhyQuestion

    state: Ask
        q!: * спроси [[у] меня] [еще] почему *
        q!: * [а] что {[ты] [еще] знаешь} *
        q!: * $tellMe [полезный] факт [из энциклопедии] *
        q!: * $tellMe (что-нибудь/что нибудь) (полезн*/интерес*) *
        q!: * сыграем в почемучк* *
        q!: * {[спроси/задай] (вопрос почему) [[у] меня/мне]} *
        q: * {[спроси/задай/расскажи] ([еще] вопрос [почему]/еще/[еще] что нибудь) [[у] меня/мне]} * || fromState = "../Tell the answer why"
        script:
            askWhy();
            if (typeof alias !== "undefined") {
                $temp.alias = alias();
            } else {
                $temp.alias = selectRandomArg($WhyAnswers["alias"]);
            }

        random:
            a: {{$temp.alias}}
            a: 
            a: 
            a: 
            a: 
            a: 
        a: {{ selectRandomArg($WhyAnswers["Ask"]) }}
        a: {{$session.askedWhy.question}}

    state: Tell the answer why
        a: {{$session.askedWhy.answer}}

        state: not so
            q: * $disagree * || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($WhyAnswers["Tell the answer why"]["Not so"]) }}


        state: I will know
            q: * (буду знать/понятно/ясно/интересно/ничего себе/{[этого/такого] не (знал/знала)}/хорошо/молодец/круто/понял/поняла/пони) * || fromState = .., onlyThisState = true
            q: да || fromState = .., onlyThisState = true
            q: * $good * || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($WhyAnswers["Tell the answer why"]["I will know"]["a1"]) }}


            state: OK
                q: (да/очень интересно/пригодится) || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($WhyAnswers["Tell the answer why"]["I will know"]["Ok"]) }}
                if: themeExists("StartDialog")
                    go!: /StartDialog
                else:
                    go!: /

    state: do not know why
        q: * ($dontKnow|$question|интересн*|$disagree|расскажи|скажи) * || fromState = ../Ask, onlyThisState = true
        a: {{ selectRandomArg($WhyAnswers["Do not know why"]) }}

        go!: ../Tell the answer why

    state: Because
        q: * [это] (потому [что]/чтобы/(из-за/из за) того что) * || fromState = ../Ask, onlyThisState = true
        q: * [$agree] * $weight<+0.2>|| fromState = ../Ask, onlyThisState = true
        a: {{ selectRandomArg($WhyAnswers["Because"]) }}

        go!: ../Tell the answer why

    state: never want to know
        q: * $shutUp * || fromState = ../Ask, onlyThisState = true
        q: * $noMatter * || fromState = ../Ask, onlyThisState = true
        q: * {(не хочу) знать} * || fromState = ../Ask, onlyThisState = true
        a: {{ selectRandomArg($WhyAnswers["Never want to know"]) }}

        if: themeExists("StartDialog")
            go!: /StartDialog
        else:
            go!: /

    state: no number
        q: * (ни почему/нисколько/ ни сколько/ никакая/никак/нигде/ никто) $weight<+0.1> * || fromState = ../Ask, onlyThisState = true
        a: {{ selectRandomArg($WhyAnswers["No number"]) }}

        go!: ../Tell the answer why

    state: vegetable
        q: * по кочану * || fromState = ../Ask, onlyThisState = true
        a: {{ selectRandomArg($WhyAnswers["Vegetable"]["a1"]) }}

        state: vegetable next
            q: * ($question/[так] какой ответ/[ну] скажи) * || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($WhyAnswers["Vegetable"]["Vegetable next"]) }}

            go!: ../../Tell the answer why
