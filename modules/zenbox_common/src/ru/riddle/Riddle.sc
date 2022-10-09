require: Riddle.js

require: riddles.csv
  name = Riddles
  var = $Riddles

require: ../../common/common.sc

require: answers.yaml
  var = RiddleCommonAnswers

    
init:

    $global.themes = $global.themes || [];
    $global.themes.push("Riddle");

    $global.$RiddleAnsw = (typeof RiddleCustomAnswers != 'undefined') ? applyCustomAnswers(RiddleCommonAnswers, RiddleCustomAnswers) : RiddleCommonAnswers;

patterns:
    $riddleQuestion = ((что делал слон когда):1|
        (без окон * горница):2|
        ((кольца/конца) * гвоздик|(кольца/конца) [а] посередине/два кольца):3|
        (стреляет в пятку * попадает в нос):4|
        (летом одним *ветом):5|
        (трубе * на трубе):6|
        (что под яйцами гладко):7|
        (висит груша * [нельзя скушать]|груша * *кушать):8|
        (ушка * брюшка):9|
        ([все] без застежек):10|
        (ходит утром на * вечером на):11|
        (ивашка * рубашка):12|
        ((шесть/6) ног):13| 
        (сколько страусов осталось):14|    
        (летела стая * совсем):15|
        (напиток * животны*):16|
        (подошел сунул):17|
        (потол* * лампочку):18|
        (кто любит [есть] морков*):19|
        (у коровы впереди * у быка сзади):20|
        (рубашк* * золото*):21|
        (бусы* * медвед*):22|
        (блеск* * треск*):23|
        (скрипач* * фрак*):24|
        (птиц* * спиц*):25|
        (трогайте * без огня):26|
        (кусает * пускает|не лает не кусает):27|
        (девица * улице):28|
        (шуб * одет):29)

    $riddleAnswer = ((трав*|{стал есть}):1|
        (огур*):2|
        (ножниц*):3|
        (пук*):4|
        (ел*):5|
        ([букв*] и):6|
        (сковород*):7|
        (ламп*):8|
        (подушк*):9|
        (капуст*):10|
        (челове*):11|
        (карандаш*):12|
        ({человек* * стул*}):13| 
        (не [мо*/уме*] (лета*/летает/летают)):14|    
        ({(7/семь) * (сов/слов)}):15|
        (коньяк*/конь як):16|
        (тап*):17|
        (потол* * ламп*):18|
        (заяц*/зай*/крол*):19|
        ([букв*] (к/ка)):20|
        (ромашк*):21|
        (малин*):22|
        (гроз*):23|
        (кузне*):24|
        (комар*|кама):25|
        (крапив*):26|
        (замо*|замк*):27|
        (морков*):28|
        (лук*|луч*):29)

theme: /Riddle

    state: Offer
        q!: * (ты/тебе) (любишь/нрав*) * загадк* *
        script:
            $session.askedRiddle = true;
        a: {{ selectRandomArg($RiddleAnsw["Offer"]["a1"]) }}
        a: {{ selectRandomArg($RiddleAnsw["Offer"]["a2"]) }}
        go: ../WantMore

    state: Guess
        q!: * {(отгадай/разгадай/угадай/загадаю/хочу загадать/буду загадывать/(можешь/умеешь/будешь) (отгад*/разгад*/загадать/угад*)/попробую отгадать) * [тебе] загадк*} *
        q!: * $riddleQuestion *
        q: * $riddleQuestion * || fromState = ./UserRiddle
        q!: * хо* * мою загадку *
        q: * {[ты/сам] (отгадай/загадаю/буду загадывать/(можешь/умеешь) отгад*) * [загадк*]} *
        q: * {[ты/сам] (отгадай/загадаю/буду загадывать/(можешь/умеешь) отгад*) * [загадк*]} * || fromState = ../WantMore
        q!: {* хочешь загадку}
        q: * [отгадай/хочешь] еще * [одну/загадку] * || fromState = /Riddle/Guess
        q: * (давай [$AnyWord] я/я загадаю) * || fromState = /Riddle/WantMore
        script:
            $session.askedRiddle = true;
        if: $parseTree.riddleQuestion
            script:
                if (!$session.usedRiddles) { 
                    $session.usedRiddles = $session.usedRiddles || {
                        questions: [],
                        id: null,        
                        question: '',
                        answer: ''
                    };
                }
                if (!_.contains($session.usedRiddles.questions, $parseTree.riddleQuestion[0].value)) {
                    $session.usedRiddles.questions.push($parseTree._riddleQuestion);
                }
                $temp.riddle = $Riddles[$parseTree._riddleQuestion].value;
            a: {{$temp.riddle.answer}}
        else:
            go!: ./UserRiddle

        state: Correct
            q: * (правильн*/правильно/угадал/молодец/верно/точно/$agree) * || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($RiddleAnsw["Guess"]["Correct"]) }}
            go!: ../../WantMore

        state: Incorrect
            q: * (не (правильн*/угадал*)/нет/$disagree) * || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($RiddleAnsw["Guess"]["Incorrect"]) }}

        state: UserRiddle
            a: {{ selectRandomArg($RiddleAnsw["Guess"]["UserRiddle"]["a1"]) }}

            state: Unknown
                q: * ($AnyWord/{что * такое}) * || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($RiddleAnsw["Guess"]["UserRiddle"]["Unknown"]["a1"]) }}
                random:
                    a: {{ selectRandomArg($RiddleAnsw["Guess"]["UserRiddle"]["Unknown"]["a2"]) }}
                    a: 
                a: {{ selectRandomArg($RiddleAnsw["Guess"]["UserRiddle"]["Unknown"]["a3"]) }}

                state: Answer
                    q: * $AnyWord * || fromState = .., onlyThisState = true
                    a: {{ selectRandomArg($RiddleAnsw["Guess"]["UserRiddle"]["Unknown"]["Answer"]) }}

    state: Get
        q!: * {(загадай/загадывай/загадывать/разгадаю/расскажи/давай/загадать/загадаем/загадаешь/загадывает/играть/поиграем/поиграть/дай/задай/хочу/загадал/загадала/заграном/загадано) * [мне] [еще] (загадк*/загадку)} *
        q: * (еще/продолжай) * || fromState = ../Get
        q!: [тогда] загадк*
        q: * {[ты/сам] (загадай/отгадаю/угадаю/разгадаю/(могу/умею/буду) (отгад*/разгад*/угад*)) * [загадк*]} *
        q: (покажи/давай/загад*) || fromState = .., onlyThisState = true
        script:
            getRiddle();
        a: {{$session.usedRiddles.question}}

        state: Answer
            q: * [$dontKnow] $riddleAnswer *
            q: * $Text *
            if: ($parseTree.riddleAnswer && $parseTree._riddleAnswer == $session.usedRiddles.id)
                a: {{ selectRandomArg($RiddleAnsw["Get"]["Answer"]["if"]["a1"]) }}
                a: {{ selectRandomArg($RiddleAnsw["Get"]["Answer"]["if"]["a2"]) }} 
            else:
                a: {{ selectRandomArg($RiddleAnsw["Get"]["Answer"]["else"]["a1"]) }}
                a: {{$session.usedRiddles.answer}}
            go!: ../../WantMore

        state: DontKnow
            q: * ($dontKnow/$stopGame/следу*/какая отгадка/интересно) *
            q: (нет/что это/[ты] знаешь)
            a: {{ selectRandomArg($RiddleAnsw["Get"]["DontKnow"]["a1"]) }}
            a: {{ selectRandomArg($RiddleAnsw["Get"]["DontKnow"]["a2"]) }}
            a: {{$session.usedRiddles.answer}}
            go!: ../../WantMore

        state: Repeat the question
            q: * {(повтори*|ска*|зада*) (вопрос/загадк*) [(еще|ещё) раз] [пожалуйста|плиз]}*
            q: * (чего|что|(еще|ещё) раз) *
            q: * повтори *
            q: хорошо
            script:
                $temp.rep = true;
            if: ($session.usedRiddles)
                a: {{ selectRandomArg($RiddleAnsw["Get"]["Repeat the question"]) }}
                a: {{$session.usedRiddles.question}}
                go: ../../Get
            else:
                go!: ../../Get

    state: WantMore
        a: {{ selectRandomArg($RiddleAnsw["WantMore"]) }} 

        state: Yes
            q: * ($agree/загад*/попробуй/задать/говори/какая/пожалуйста/$continue/давай загадку/спроси/заказывать) * || fromState =.., onlyThisState = true
            go!: ../../Get

        state: No
            q: * ($disagree/хватит/не [хочу] (загад*)/$notNow/ давай (потом/в другой/в следующий) *) * || fromState =.., onlyThisState = true
            q: * ($disagree/не [хочу] (загад*)/$notNow/ давай (потом/в другой/в следующий) *) * || fromState =/Riddle, onlyThisState = true
            a: Хорошо.