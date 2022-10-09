require: common.js
    module = zb_common

init:
    $global.themes = $global.themes || [];
    $global.themes.push("PlayGames");

theme: /PlayGames
    state: Games
        q!: * [i] [want|wanna|let 's|let us|let's|lets] [to] play * (game*|something) * [with] [you|me] [together] *
        q!: * [i] [want|wanna|let 's|let us|let's|lets] [to] play * [game*|something] * (with (you|me)|together) * 
        q!: * [how] * [about] * play* * game* *
        q!: * want * game* *
        q!: * (what/how many) [other] games * $you (know/have) * 
        q!: * do you (know/have) * other game* *
        q!: * what (shall/will/should) (i/we) do *
        script:
            $session.askedGames = true;
            $session.games = [];
            if (themeExists("Math game")) {$session.games.push(["maths", "/Math game/Math game pudding start/Math game yes"]);}
            if (themeExists("Extra game")) {$session.games.push(["odd one out", "/Extra game/Extra game pudding start/Extra game yes"]);}
            if (themeExists("Gorodagame")) {$session.games.push(["cities", "/Gorodagame/Goroda game intro"]);}
            if (themeExists("Animal quiz")) {$session.games.push(["zoo", "/Animal quiz/Animal game pudding start/Animal game yes"]);}
            if (themeExists("Geography quiz")) {$session.games.push(["geography", "/Geography quiz/Geography game pudding start/Geography game yes"]);}
            $temp.nomGames = "";
            

        if: ($session.games.length > 1)
            script:
                if ($session.games.length == 2) {
                    for (var i=0;i<$session.games.length;i++){
                        if (i > 0) {
                            $temp.nomGames  += " or ";
                        }
                        $temp.nomGames  += $session.games[i][0];
                    } 
                } else {
                    for (var i=0;i<3;i++){
                        var id = selectRandomArg.apply(this, Object.keys($session.games));
                        if (i == 2) {
                            $temp.nomGames  += "or " + $session.games[id][0];
                        } else if (i == 0){
                            $temp.nomGames += $session.games[id][0] + ', ';
                        } else {
                            $temp.nomGames += $session.games[id][0] + ' ';
                        }  
                        delete $session.games[id];
                    } 
                }
            if: $temp.canYouPlay
                a: I can play {{$temp.nomGames}}. Choose a game!
            else:
                if: $temp.tellOtherGames
                    random:
                        a: We can play {{$temp.nomGames}}. Choose a game!
                        a: I know more interesting games: {{$temp.nomGames}}. Choose a game!
                else:
                    random:
                        a: I love games! What shall we play: {{$temp.nomGames}}?
                        a: I like playing games! Choose a game: {{$temp.nomGames}}?
                        a: I know interesting games: {{$temp.nomGames}}. Choose a game! 
        else:
            go!: {{$session.games[0][1]}}        

        state: CanYouPlay
            q!: * (what/which) * games (can you play/do you know) *
            q!: * games  * (know/can/have) *
            q: * what (else/other/different)  *
            script:
                $temp.canYouPlay = true;
            go!: ../../Games    

        state: Other games you can play
            q: * [and] what (else/other/different) [games] $you (know/can/have) * || fromState = ../../Games, onlyThisState = true
            q: * [and] what (else/other/different) [games] $you (know/can/have) * || fromState = ../../Games, onlyThisState = true
            q: ** [and] what (else/other/different) [games] $you (know/can/have) * || fromState = ../../Games, onlyThisState = true
            script:
                $temp.tellOtherGames = true;
            go!: ../../Games  

        state: IDontWantToPlay
            q!: * ($no want/no time) * play* * [zoo*/math*/odd (one/1) out/cities/geo*] *
            q: * ($disagree|$notNow) * [play*] * (zoo*/math*/odd (one/1) out/cities/geo*) * || fromState = ../../Games, onlyThisState = true
            random:
                a: That's sad.
                a: A no is a no.

        state: How to play
            q: * (how to play/{(explain/what/which) * (rule/rules)}) *
            a: Thay's easy, let's start!
            go!: ../../Games

        state: How to play Zoo
            q!: * {(how to play/{(explain/what/which) * (rule/rules)}) * zoo*} *
            q!: * (what is/what 's) * zoo* *
            a: I'll ask you some questions about animals. Shall we start?

        #state: How to play Food
        #    q!: * {буфет* * как* * (игр*s|правил*)} *
        #    q!: * (что такое/про что) буфет* *
        #    scripa: Я буду задавать вопросы про еду, а ты отвечай! Начнём?

        state: How to play Goroda
            q: * {(how to play/{(explain/what/which) * (rule/rules)}) * cit*} *
            q: * cit*
            q: * (what is/what 's) * cit* *
            a: We'll call out the names of cities one after another - each starting with the last letter of the previous city. Do you want to play?
         
        state: How to play Math
            q: * {(how to play/{(explain/what/which) * (rule/rules)}) * math*} *
            q: * (what is/what 's) * math* *
            a: I'll ask you some math questions. Shall we start?

        state: How to play Geography
            q: * {(how to play/{(explain/what/which) * (rule/rules)}) * geo*} *
            q: *  geo*
            q: * (what is/what 's) * geo*
            a: I'll ask you some questions about cities, countries and continents. Shall we start?

        state: How to play Extra game
            q: * {(how to play/{(explain/what/which) * (rule/rules)}) * ((odd word*|odd (one|1) [out]) [game])} *
            q: *  ((odd word*|odd (one|1) [out]) [game])
            q!: * (what is/what 's) * ((odd word*|odd (one|1) [out]) [game])
            a: I will read some words and your task is to name the odd one. Shall we start?

        state: Any game
            q: * (any|{(select*/choose) * $you}) *
            q: * (($you/what*/which) * (want|$like)) *
            script:
                nextGame();
            go!: /{{ $temp.nextTheme }}