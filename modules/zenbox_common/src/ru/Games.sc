require: Games.js

init:
    $global.themes = $global.themes || [];
    $global.themes.push("PlayGames");

theme: /PlayGames
    state: Games
        q!: * {[давай|может] * [[в|во] $smth] (поигра*|сыгра*)} [для|в] [игр*] *
        q!: * {(давай/хочу) * [[в|во] $smth] (~играть|~игра) [для|в] [игр*]} *
        q!: * (хоч*/давай/можешь) {[с тобой/со мной] (игра*|поигра*)} *
        q!: * как* * (есть|$you) * игр* *
        q!: * { [в] как* [еще|ещё] [есть] игр* } *
        q: * {(игр*|поигра*|сыгра*) * [что-нибудь|~другая|~другое]} * || fromState = "/Gorodagame/Goroda game end", onlyThisState = true
        q: * {(игр*|поигра*|сыгра*) * [что-нибудь|~другая|~другое]} * || fromState = "/Food quiz/Food game pudding stop", onlyThisState = true
        q: * {(игр*|поигра*|сыгра*) * [что-нибудь|~другая|~другое]} * || fromState = "/Animal quiz/Animal game pudding stop", onlyThisState = true
        q: * {(игр*|поигра*|сыгра*) * [что-нибудь|~другая|~другое]} * || fromState = "/Math game/Math game pudding stop", onlyThisState = true
        q: * {(игр*|поигра*|сыгра*) * [что-нибудь|~другая|~другое]} * || fromState = "/Odd word game/Odd word game stop", onlyThisState = true
        q!: * ((что/че) * {будем * делать}/чем * (займемся/{будем * заниматься})) *
        q!: что мне делать
        q!: * ([давай/хочу] (играть/сыграем/сыграть/поиграть/поиграем)/давай/хочу) [в] другую игру *
        q: * ([давай/хочу] (играть/сыграем/сыграть/поиграть/поиграем)/давай/хочу) [в] другую игру * || fromState = /Opposites/Pudding/Play
        q: * ([давай/хочу] (играть/сыграем/сыграть/поиграть/поиграем)/давай/хочу) [в] другую игру * || fromState = /Opposites/User/Play
        script:
            $session.askedGames = true;
            $session.games = [];
            if (themeExists("Animal quiz")) {$session.games.push(["зоопарк", "в зоопарк", "/Animal quiz/Animal game pudding start/Animal game yes"]);}
            if (themeExists("Food quiz")) {$session.games.push(["буфет", "в буфет", "/Food quiz/Food game pudding start/Food game yes"]);}
            if (themeExists("Gorodagame")) {$session.games.push(["города", "в города", "/Gorodagame/Goroda game intro"]);}
            if (themeExists("Math game")) {$session.games.push(["математика", "в математику", "/Math game/Math game pudding start/Math game yes"]);}    
            if (themeExists("Geography quiz")) {$session.games.push(["география", "в географию", "/Geography quiz/Geography game pudding start/Geography game yes"]);}
            if (themeExists("Odd word game")) {$session.games.push(["найди лишнее", "в найди лишнее", "/Odd word game/Odd word game pudding start/Odd word game yes"]);}
            if (themeExists("Calendar quiz")) {$session.games.push(["календарь", "в календарь", "/Calendar quiz/Pudding start/Yes"]);}
            if (themeExists("Guess Number Game")) {$session.games.push(["угадай число", "в угадывание чисел", "/Guess Number Game/Guess/Yes"]);}   
            if (themeExists("Opposites")) {$session.games.push(["перевёртыши", "в перевёртыши", "/Opposites/Pudding/Play"]);}
            $temp.nomGames = "";
            $temp.accGames = "";
        if: ($session.games.length > 1)
            script:
                if ($session.games.length == 2) {
                    for (var i=0;i<$session.games.length;i++){
                        if (i > 0) {
                            $temp.nomGames  += " или ";
                            $temp.accGames  += " или ";
                        }
                        $temp.nomGames  += $session.games[i][0];
                        $temp.accGames  += $session.games[i][1];
                    } 
                } else {
                    for (var i=0;i<3;i++){
                        var id = selectRandomArg.apply(this, Object.keys($session.games));
                        if (i == 2) {
                            $temp.nomGames  += "или " + $session.games[id][0];
                            $temp.accGames  += "или " + $session.games[id][1];
                        } else if (i == 0){
                            $temp.nomGames += $session.games[id][0] + ', ';
                            $temp.accGames += $session.games[id][1] + ', ';
                        } else {
                            $temp.nomGames += $session.games[id][0] + ' ';
                            $temp.accGames += $session.games[id][1] + ' ';
                        }  
                        delete $session.games[id];
                    } 
                }
            if: $temp.canYouPlay
                a: Я умею играть {{$temp.accGames}}. Выбирай!
            else:
                if: $temp.tellOtherGames
                    random:
                        a: Ещё можно сыграть {{$temp.accGames}}. Выбирай!
                        a: Я знаю и другие интересные игры: {{$temp.nomGames}}. Выбирай!
                else:
                    random:
                        a: Обожаю играть! Во что сыграем: {{$temp.accGames}}?
                        a: Люблю играть! Выбирай игру: {{$temp.nomGames}}?
                        a: Я знаю интересные игры: {{$temp.nomGames}}. Выбирай! 
        else:
            go!: {{$session.games[0][2]}}

        state: WordsGame
            q: [давай/хочу] [в] слова || fromState = .., onlyThisState = true
            random:
                a: В слова я пока не умею.
                a: В слова я ещё не научился играть.
            go!: ../../Games

        state: CanYouPlay
            q!: * [во что] * (умеешь/можешь/знаешь как) играть *
            q!: * {игры  * (знаешь/умеешь/можешь)} *
            q: * [{[давай|может] * [[в|во] $smth] (поигра*|сыгра*)}] * [во что] * (умеешь/можешь/знаешь как/[в] какие/например) * || fromState = .., onlyThisState = true
            q: * [во что] * {[ещё|ещё] (умеешь/можешь/знаешь как)} *
            q: * как* [игры] * || fromState = "/Education/What can you do", onlyThisState = true
            script:
                $temp.canYouPlay = true;
            go!: ../../Games

        state: Other games you can play
            q: * { [а] ((во|в) что|в как* игр*) (еще|ещё) можно [игр*|поигра*|сыгра*] } * || fromState = ../../Games, onlyThisState = true
            q: * { [а]  (еще|ещё) [во что |[в] как*] [игр*|поигра*|сыгра*] [есть|можно|знаешь] } * || fromState = ../../Games, onlyThisState = true
            q: * {[давай] [игр*|поигра*|сыгра*] [во] (что-нибудь|~другая|~лругое) [что-нибудь|~другая|~лругое]} * || fromState = ../../Games, onlyThisState = true
            script:
                $temp.tellOtherGames = true;
            go!: ../../Games

        state: IDontWantToPlay
            q!: * (не (хоч*|буду)|некогда [мне]) (игра*|поигра*) [в] [город*|зоо*|животн*|математик*|арифмети*|географи*|столиц*|буфет|продукт*|найд* лишнее] *
            q: * ($disagree|$notNow) [игра*|поигра*] [в] [город*|зоо*|животн*|математик*|географи*|столиц*|буфет|продукт*|найд* лишнее] * || fromState = ../../Games, onlyThisState = true
            random:
                a: Как хочешь.
                a: Ладно.

        state: How to play
            q: * {как* * (игр*|правил*)} *
            a: Всё просто, надо только начать!
            go!: ../../Games

        state: How to play Zoo
            q!: * {зоо* * как* * (игр*|правил*)} *
            q!: * (что такое/про что) зоо* *
            a: Я буду задавать вопросы про животных, а ты отвечай! Начнём?

        state: How to play Calendar
            q!: * {календар* * как* * (игр*|правил*)} *
            q!: * (что такое/про что) календар* *
            a: Я буду задавать вопросы про даты и дни недели, а ты отвечай! Начнём?

        state: How to play GuessNumber
            q!: * {угад* * как* * (игр*|правил*)} *
            q!: * (что такое/про что [это]) угад* *
            a: Ты загадаешь число от одного до ста, а я его отгадаю. Начнём?

        state: How to play Opposites
            q!: * {против* * как* * (игр*|правил*)} *
            q!: * (что такое/про что [это]) против* *
            a: Я буду называть слова, а ты их противоположности. Например, длинный - короткий, горячий - холодный. Начнём?

        state: How to play Food
            q!: * {буфет* * как* * (игр*|правил*)} *
            q!: * (что такое/про что) буфет* *
            a: Я буду задавать вопросы про еду, а ты отвечай! Начнём?

        state: How to play Goroda
            q: * {город* * как* * (игр*|правил*)} *
            q: *  [дав* [в]] город*
            q: * (что такое/про что) город* *
            a: Будем по очереди называть города. Хочешь сыграть?

        state: How to play Math
            q: * {(~устный ~счет|~математика|арифмети*) * как* * (игр*|правил*)} *
            q: * (что такое/про что) (~устный ~счет|~математика|арифмети*) *
            a: Я буду задавать вопросы по математике, а ты отвечай! Начнём?

        state: How to play Geography
            q: * {~география * как* * (игр*|правил*)} *
            q: *  [дав* [в]] (~столица/~страна/~география)
            q: * (что такое/про что) ~география
            a: Я буду задавать тебе разные вопросы про города, страны и континенты. Начнём?

        state: How to play Odd word game
            q: * {([найди] лишн*) * как* * (*игр*|правил*)} *
            q: *  [дав* [в]] ([найди] лишнее)
            q!: * (что такое/про что) ([найди] лишнее)
            a: Я буду называть слова, а ты говори, какое среди них лишнее. Начнём?

        state: Any game
            q: * (люб*|{выб* * (ты|сам)}) *
            q: * (($you|как*) * (хочешь|нравится)) *
            q: давай || fromState = .., onlyThisState = true
            script:
                nextGame();
            go!: /{{$temp.nextTheme}}

        state: WhenNewGame
            q!: * {(когда буд*|хочу) ~новый * ~игра} *
            random:
                a: Я не перестаю учиться, так что скоро буду знать новые игры.
                a: Я всё ещё учусь - и скоро выучу новые игры.
                a: Я продолжаю учиться - скоро буду предлагать ещё какие-нибудь игры.