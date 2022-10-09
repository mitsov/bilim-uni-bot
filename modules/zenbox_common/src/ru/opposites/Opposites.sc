require: Opposites.js

require: opposites-ru.csv
    name = Opposites
    var = $Opposites
    
require: ../../common/common.sc
require: ../Games.sc
require: ../main.js
require: answers.yaml
    var = OppositesCommonAnswers

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .OppositeTagConverter = function(parseTree) {
            var id = parseTree.Opposites[0].value;
            return $Opposites[id].value;
        };

    $global.themes = $global.themes || [];
    $global.themes.push("Opposites");
    $global.$OppositesAnswers = (typeof OppositesCustomAnswers != 'undefined') ? applyCustomAnswers(OppositesCommonAnswers, OppositesCustomAnswers) : OppositesCommonAnswers;

patterns:
    $Opposite = $entity<Opposites> || converter = $converters.OppositeTagConverter
    $adjective = (*ой/*ий/*ый)

theme: /Opposites

    state: Pudding
        q!: * [давай] * {[играть/сыграем/сыгрыть/играем/поиграем/поиграть/игра] * (противоположн*/перевертыш*)} *    
        q!: { (противоположно*/перевертыш*)}
        q!: * [давай] * [играть/сыграем/сыгрыть/играем/поиграем/поиграть/игра] * {[загадай/загадывай/загадывать/[ты] загадаешь/разгадаю/загадать/загадаем/загадаешь/загадывает/дай/задай/буду (отгадывать/угадывать/разгадывать)/{(можешь/умеешь/будешь) загад*}] * [мне] [еще] (противоп*/антоним*/перевертыш*)} *
        q: * перевер* * || fromState = /PlayGames/Games, onlyThisState = true
        a: {{ selectRandomArg($OppositesAnswers["Pudding"]["a1"]) }}

        state: Play || modal = true
            q: * ($agree|начин*|начн*|помогу|поехали|вопрос*|задавай|называй|назови|говори) [*игра*] * || fromState = .., onlyThisState = true
            q: * ($agree|начин*|начн*|поехали|вопрос*|задавай|называй|назови|говори) * || fromState = "/Games/How to play Opposites", onlyThisState = true
            q: * [давай] * {[ты/сам] (загадай/загадывай/загадаешь/[я] (отгадаю/угадаю/разгадаю)/{(могу/умею/буду) (отгад*/разгад*/угад*)}/{(можешь/умеешь/будешь) загад*}/не (хочу/буду) загадывать) * [против*/перевертыш*]} * || fromState = ../../User/Play, onlyThisState = true  
            q: * [давай] * {[ты/сам] (загадай/загадывай/[я] (отгадаю/угадаю/разгадаю)/{(могу/умею/буду) (отгад*/разгад*/угад*)}/{(можешь/умеешь/будешь) загад*}) * [против*/перевертыш*]} * || fromState = /Opposites
            q: * [давай] * { ты [сам] говори} * || fromState = ../../User/Play, onlyThisState = true
            q: * {[давай] [лучше] (наоборот/*меняемся)} * || fromState = ../../User/Play, onlyThisState = true
            script:
                getOpposite();
            a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["a1"]) }}

            state: Opposite
                q: * [$maybe|$sure] * $Opposite * || fromState = .., onlyThisState = true
                script:
                    if ($session.barrier) {
                        $session.barrier = undefined;
                    }
                if: $parseTree._Opposite.word == $session.opposites.last
                    a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Opposite"]["if"]) }}
                    go: ../../Play
                else:
                    if: checkOpposite($parseTree._Opposite)
                        script:
                            $session.opposites.correct += 1;
                        a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Opposite"]["else"]["if"]["a1"]) }}
                        random:
                            a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Opposite"]["else"]["if"]["a2"]) }}
                            a: 
                            a: 
                            a: 
                            a: 
                            a: 
                            a: 
                        if: $session.opposites.turn > 2
                            script:
                                $session.opposites.turn = 0;
                            a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Opposite"]["else"]["if"]["a3"]) }}
                            go!: ../../../User/Play
                        else:
                            go!: ../../Next
                    else:
                        a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Opposite"]["else"]["else"]["a1"]) }}
                        script:
                            $temp.answer = findOpposite($session.opposites.last);
                        a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Opposite"]["else"]["else"]["a2"]) }}

                        go!: ../../Next

            state: DontKnow
                q: * $dontKnow * || fromState = .., onlyThisState = true
                q: * ($agree/$disagree/не хочу/не буду/не надо/подскажи/помоги/уже спрашивал/повторяешься/(скажи/какой/назови) * (ответ/сам/ты)/(сам/ты) (ответь/отвечай)) * || fromState = .., onlyThisState = true
                q: * [$agree] (дальше|след*|ещё|еще|игра*|задавай|другой) [дальше|след*|ещё|еще|игра*|задавай|другой] [вопрос*] * || fromState = .., onlyThisState = true
                q: * $agree (дальше|след*|ещё|еще|игра*|задавай|другой) [дальше|след*|ещё|еще|игра*|задавай|другой] вопрос* * || fromState = .., onlyThisState = true
                script:
                    $temp.answer = findOpposite($session.opposites.last);
                a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["DontKnow"]["a1"]) }}
                a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["DontKnow"]["a2"]) }}
                go!: ../../Next                    

            state: Undefined
                q: * [$maybe|$sure] [$AnyWord] * || fromState = .., onlyThisState = true
                if: $session.barrier == "Undefined";
                    script:
                        $session.barrier = undefined;
                        $temp.answer = findOpposite($session.opposites.last);
                    a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Undefined"]["if"]) }}
                    go!: ../../Next
                else:
                    script:
                        $session.barrier = "Undefined";
                    a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Undefined"]["else"]) }}
                    go!: ../Repeat

            state: Repeat
                q: * ({повтори* * [слово]}/какое * слово) * || fromState = .., onlyThisState = true
                q: { (как/какой)} || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Play"]["Repeat"]) }}
                go: ../../Play

        state: Next
            q: * ($agree/$continue/игра*) * || fromState = ../Next, onlyThisState = true
            if: $session.opposites.correct > 4
                script:
                    prepareOpposite();
                    $session.opposites.correct = 0;
                a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Next"]["if"]["a1"]) }}
                a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Next"]["if"]["a2"]) }}
            else:
                a: {{ selectRandomArg($OppositesAnswers["Pudding"]["Next"]["else"]) }}
                go!: ../Play

    state: User

        state: Play || modal = true
            q!: * [давай] * [играть/сыграем/сыгрыть/играем/поиграем/поиграть/игра] * {[отгадай/разгадай/угадай/[я] загадаю/хочу загадать/буду загадывать/загадываю/(можешь/умеешь/будешь) (отгад*/разгад*/загадать/угад*)/попробую отгадать/отгадывать/угадывать] * [тебе] (противо*/антони*/переверт*)} *
            q: * [давай] * {[ты/сам] (отгадай/отгадывай/угадывай/[я] загадаю/{(буду/хочу) загадывать}/{(можешь/умеешь/будешь) отгад*}) * [против*/переверт*]} * || fromState = ../../Pudding/Play, onlyThisState = true  
            q: * [давай] * {[ты/сам] (отгадай/отгадывай/угадывай/[я]загадаю/{(буду/хочу) загадывать}/{(можешь/умеешь/будешь) отгад*}) * [против*/переверт*]} * || fromState = /Opposites
            q: * {[давай] [лучше] (наоборот/*меняемся)} * || fromState = ../../Pudding/Play, onlyThisState = true
            a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["a1"]) }}

            state: Correct
                q: (правильно/молодец/умница/умничка/да) || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Correct"]) }}
                go!: ../../Next

            state: Incorrect
                q: (врешь/неправильно/не правильно/нет) || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Incorrect"]) }}
                go!: ../../Next

            state: Opposite
                q: * $Opposite * || fromState = .., onlyThisState = true
                q: * $Opposite * || fromState = "/Games/How to play Opposites", onlyThisState = true
                q: * $Opposite * || fromState = /Opposites/Pudding, onlyThisState = true
                script:
                    if ($session.barrier) {
                        $session.barrier = undefined;
                    }
                    prepareOpposite();
                    $session.opposites.used.push($parseTree._Opposite.word);
                    $session.opposites.turn += 1;
                    $session.opposites.correct += 1;
                    $temp.opposite = findOpposite($parseTree._Opposite.word);
                if: $temp.opposite != ""
                    a: {{capitalize($temp.opposite)}}!
                    if: $session.opposites.turn > 2
                        script:
                            $session.opposites.turn = 0;
                        a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Opposite"]) }}
                        go!: ../../../Pudding/Play
                    else:
                        go!: ../../Next
                else:
                    script:
                        $session.opposites.last = $parseTree._Opposite.word;
                    go!: ../Unknown

            state: Unknown || modal = true
                q: {[я (говорю/скажу)]  $adjective} || fromState = .., onlyThisState = true
                q: {[я (говорю/скажу)]  $adjective} || fromState = "/Games/How to play Opposites", onlyThisState = true
                q: {[я (говорю/скажу)]  $adjective} || fromState = /Opposites/Pudding, onlyThisState = true
                script:
                    if ($session.barrier) {
                        $session.barrier = undefined;
                    }
                    prepareOpposite();  
                    $session.opposites.turn += 1;
                    if ($parseTree.adjective) {
                        $session.opposites.last = $parseTree.adjective[0].text;
                    }
                if: isQuestionLearned("oppositesLearned", $session.opposites.last)
                    a: {{capitalize(getAnswerLearned("oppositesLearned", $session.opposites.last))}}!
                    go!: ../../Next
                else:
                    a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Unknown"]["a1"]) }}
                    a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Unknown"]["a2"]) }}


                state: Answer
                    q: * [ой] * [$adjective::adjective1] * $adjective::adjective2 * || fromState = .., onlyThisState = true
                    script:
                        $temp.word1 = $parseTree.adjective1 ? $parseTree.adjective1[0].text : $session.opposites.last;
                    if: $temp.word1.toLowerCase() != $parseTree.adjective2[0].text.toLowerCase()
                        script:
                            learnAnswer("oppositesLearned", $temp.word1, $parseTree.adjective2[0].text);
                            learnAnswer("oppositesLearned", $parseTree.adjective2[0].text, $temp.word1);
                        a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Unknown"]["Answer"]["a1"]) }}
                        go!: ../../../Next
                    else:
                        if: $session.barrier == "Answer"
                            script:
                                $session.barrier = undefined;
                            a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Unknown"]["Answer"]["if"]) }}
                            go!: ../../../Next
                        else:
                            script:
                                $session.barrier = "Answer";
                            a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Unknown"]["Answer"]["else"]) }}
                            go: ../../Unknown

                state: Undefined
                    q: * $AnyWord * || fromState = .., onlyThisState = true
                    a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Unknown"]["Undefined"]) }}
                    go!: ../../../Next
                        
            state: Undefined
                q: * $AnyWord * || fromState = .., onlyThisState = true
                if: $session.barrier == "Undefined";
                    script:
                        $session.barrier = undefined;
                    a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Undefined"]["if"]) }}
                    go!: ../../Next                
                else:
                    script:
                        $session.barrier = "Undefined";
                    a: {{ selectRandomArg($OppositesAnswers["User"]["Play"]["Undefined"]["else"]) }}
                    go: ../../Play

        state: Next
            q: * ($agree/$continue/игра*) * || fromState = ../Next, onlyThisState = true
            if: $session.opposites.correct > 4
                script:
                    prepareOpposite();
                    $session.opposites.correct = 0;
                a: {{ selectRandomArg($OppositesAnswers["User"]["Next"]["if"]["a1"]) }}
                a: {{ selectRandomArg($OppositesAnswers["User"]["Next"]["if"]["a2"]) }}
            else:
                a: {{ selectRandomArg($OppositesAnswers["User"]["Next"]["else"]["a1"]) }}
                a: {{ selectRandomArg($OppositesAnswers["User"]["Next"]["else"]["a2"]) }}
                go: ../Play

    state: Stop
        q: * $stopGame *
        q: * $stopGame * || fromState = ../Pudding/Play
        q: * $stopGame * || fromState = ../User/Play
        q: * ($disagree|$notNow|$stopGame|$dontKnow) * || fromState = ../Pudding/Next
        q: * ($disagree|$notNow|$stopGame|$dontKnow) * || fromState = ../User/Next
        q: * {[давай] * [сделаем] (перерыв/прервемся)} * || fromState = ../Pudding/Play
        q: * {[давай] * [сделаем] (перерыв/прервемся)} * || fromState = ../User/Play
        q: * {[давай] * [сделаем] (перерыв/прервемся)} *                
        q: [я] устал*
        script:
            $session.opposites = undefined;
        a: {{ selectRandomArg($OppositesAnswers["Stop"]["a1"]) }}  
        a: {{ selectRandomArg($OppositesAnswers["Stop"]["a2"]) }}  