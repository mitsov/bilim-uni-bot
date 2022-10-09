function nextGame() {
    var $temp = $jsapi.context().temp;
    var games = [];
    if (themeExists("Animal quiz")) {games.push("Zoo");}
    if (themeExists("Food quiz")) {games.push("Food");}
    if (themeExists("Gorodagame")) {games.push("Goroda");}
    if (themeExists("Math game")) {games.push("Math");}    
    if (themeExists("Geography quiz")) {games.push("Geography");}
    if (themeExists("Odd word game")) {games.push("Odd word game");}
    if (themeExists("Calendar quiz")) {games.push("Calendar");}
    if (themeExists("Guess Number Game")) {games.push("GuessNumber");}   
    if (themeExists("Opposites")) {games.push("Opposites");}

    var randGame = games[randomIndex(games)];

    switch(randGame) {
        case "Zoo":
            $reactions.answer("Давай в зоопарк!");
            $temp.nextTheme = "Animal quiz/Animal game pudding start/Animal game question";
            break;
        case "Food":
            $reactions.answer("Давай в буфет!");
            $temp.nextTheme = "Food quiz/Food game pudding start/Food game question";
            break;
        case "Goroda":
            $reactions.answer("Давай в Города!");
            $temp.nextTheme = "Gorodagame/Goroda game intro";
            break;
        case "Math":
            $reactions.answer("Давай в Математику!");
            $temp.nextTheme = "Math game/Math game pudding start/Math game question";
            break;
        case "Geography":
            $reactions.answer("Давай в Географию!");
            $temp.nextTheme = "Geography quiz/Geography quiz get question";
            break;  
        case "Odd word game":
            $reactions.answer("Давай в Найди лишнее!");
            $temp.nextTheme = "Odd word game/Odd word game start";
            break;      
        case "Calendar":
            $reactions.answer("Давай в Календарь!");
            $temp.nextTheme = "Calendar quiz/Pudding start/Question";
            break;
        case "GuessNumber":
            $reactions.answer("Давай угадывать числа!");
            $temp.nextTheme = "Guess Number Game/Guess/Yes";
            break;
        case "Opposites":
            $reactions.answer("Давай играть в противоположности!");
            $temp.nextTheme = "Opposites/Pudding";
            break;                              
    }
}