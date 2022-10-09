require: CalendarGame.js
require: months.csv
  name = Months
  var = $Months
require: weekdays.csv
  name = Weekdays
  var = $Weekdays

require: ../Games.sc
require: ../../common/common.sc

require: ../main.js

require: answers.yaml
    var = CalendarCommonAnswers

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .MonthsTagConverter = function(parseTree) {
            var id = parseTree.Months[0].value;
            return $Months[id].value;
        };

    $global.themes = $global.themes || [];
    $global.themes.push("Calendar quiz");

    $global.$CalendarAnswers = (typeof CalendarCustomAnswers != 'undefined') ? applyCustomAnswers(CalendarCommonAnswers, CalendarCustomAnswers) : CalendarCommonAnswers;

patterns:
    $monthGame = $entity<Months> || converter = $converters.MonthsTagConverter
    $quizAllCalendar = ($monthGame|$Number|$Season|$weekdayGame)
    $febDays = (28/29)
    $weekdayGame = (~понедельник:1 | ~вторник:2 | ~среда:3 | ~четверг:4 | ~пятница:5 | (~суббота|субот*):6 | (~воскресенье|воскресение):0)

theme: /Calendar quiz

    state: Alternative enter
        q!: * {($tellMe|что там) * [земн*] (календар*)} *    
        a: {{ selectRandomArg($CalendarAnswers["Alternative enter"]) }}
        go!: ../Pudding start

    state: User start
        q!: * {[давай|может] * (сыграем|игра*|поигра*) * (календар*)} *
        q!: * {(давай|хочу|хочется) * (календар*)} *
        q!: календарь
        a: {{ selectRandomArg($CalendarAnswers["User start"]) }}
        go!: ../Pudding start 

    state: Suggest quiz
        q!: предложи викторину про календарь
        q!: * {[я|мне] (люблю|нравит*) * (календар*)} *
        a: {{ selectRandomArg($CalendarAnswers["Suggest quiz"]) }}
        go!: ../Pudding start    
            
    state: From Ask user about game
        q: * [$maybe|$sure] (календар*) [$maybe|$sure] * || fromState = "/Ask user about game", onlyThisState = true
        a: {{ selectRandomArg($CalendarAnswers["From Ask user about game"]) }}
        go!: ../Pudding start
    

    state: Pudding start        

        state: No
            q: * ($disagree|$notNow) * || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["No start"]) }}
            go!: /StartDialog

        state: From rules no
            q: * ($disagree|$notNow) [* (о|об) $Text] * || fromState = "/PlayGames/Games/How to play Calendar", onlyThisState = true 
            a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["From rules no"]) }}
            if: $parseTree.Text
                random:
                    a: Давай подумаем об этом завтра.
                    a: Давай поговорим об этом потом.
                    a: Что-то мне пока не хочется...
            go!: /StartDialog

        state: Yes
            q: * ($agree|начин*|начн*|помогу|помогаю|стараться|попытаюсь|попробую|допустим|(почему/отчего) [бы] и не (поиграть/сыграть)) [начин*|начн*|поигра*|сыгра*|игра*|попробую|попробуем] * || fromState = "/Calendar quiz/Pudding start", onlyThisState = true
            q: * [$agree] ($agree|начин*|начн*|помогу|помогаю|постараюсь|стараться|попытаюсь|попробую|допустим|(почему/отчего) [бы] и не (поиграть/сыграть)) * || fromState = "/Calendar quiz/Pudding start", onlyThisState = true
            q: * [$agree] ($agree|начин*|начн*|помогу|помогаю|постараюсь|стараться|попытаюсь|попробую|допустим|(почему/отчего) [бы] и не (поиграть/сыграть)) * || fromState = "/PlayGames/Games/How to play Calendar", onlyThisState = true
            q!: * [хочу] (игра*|сыграть|поиграть) * в календар* *
            q: * (календар*) * || fromState = /PlayGames/Games, onlyThisState = true  
            q: * [$agree|$maybe|$sure] * (календар*) * [$agree|$maybe|$sure] * || fromState = /PlayGames/Games, onlyThisState = true        
            script:
                $session.lastQuiz = undefined;
            a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Yes start"]) }}
            go!: ../Question             


###############################################################################
###############                   Генерация вопросов             ##############
###############################################################################

        state: Question
            script:
                if ($session.barrier) {
                    $session.barrier = undefined;
                } 
                $response.ledAction = 'ACTION_EXP_QUESTION';
                $session.lastQuiz = getCalendarQuestion();
                $temp.nextState = "Question " + $session.lastQuiz.type;
            a: {{$session.lastQuiz.text}}
            go!: ./{{$temp.nextState}}


            state: Question nextMonth
 
                state: Answer nextMonth
                    q: * [$maybe|$sure] * $monthGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;
                    script:         
                        if ($session.lastQuiz.right == $parseTree._monthGame.name){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}  

            state: Question prevMonth
 
                state: Answer prevMonth
                    q: * [$maybe|$sure] * $monthGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:                   
                        if ($session.lastQuiz.right == $parseTree._monthGame.name){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}  

            state: Question days
 
                state: Answer days
                    q: * [$maybe|$sure] * $Number * [$maybe|$sure] *
                    q: * $febDays::Number * [или/иногда/когда/бывает/високосн*/то] * $febDays *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:                 
                        if ($session.lastQuiz.right == $parseTree._Number || ($session.lastQuiz.right == "28" && $parseTree._Number == "29")){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}  

            state: Question season
 
                state: Answer season
                    q: * [$maybe|$sure] * $Season * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:
                        if ($session.lastQuiz.right == dts_seasonName(parseInt($parseTree._Season))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}  

            state: Question seasonNumber
 
                state: Answer seasonNumber
                    q: * [$maybe|$sure] * $monthGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:                  
                        if ($session.lastQuiz.right == $parseTree._monthGame.name){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}  

            state: Question monthNumber
 
                state: Answer monthNumber
                    q: * [$maybe|$sure] * $Number * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:     
                        if ($session.lastQuiz.right == $parseTree._Number){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}  

            state: Question nextWeekday
 
                state: Answer nextWeekday
                    q: * [$maybe|$sure] * [будет] $weekdayGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:
                        if ($session.lastQuiz.right == dts_dayweekName(parseInt($parseTree._weekdayGame))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}} 

            state: Question prevWeekday
 
                state: Answer prevWeekday
                    q: * [$maybe|$sure] * [был*] $weekdayGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:                  
                        if ($session.lastQuiz.right == dts_dayweekName(parseInt($parseTree._weekdayGame))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}} 

            state: Question beforeYesterday
 
                state: Answer beforeYesterday
                    q: * [$maybe|$sure] * [был*] $weekdayGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:                 
                        if ($session.lastQuiz.right == dts_dayweekName(parseInt($parseTree._weekdayGame))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}

            state: Question afterTomorrow
 
                state: Answer afterTomorrow
                    q: * [$maybe|$sure] * [будет] $weekdayGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:                   
                        if ($session.lastQuiz.right == dts_dayweekName(parseInt($parseTree._weekdayGame))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}                    

            state: Question calcNextWeekday
 
                state: Answer calcNextWeekday
                    q: * [$maybe|$sure] * [будет] $weekdayGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:                  
                        if ($session.lastQuiz.right == dts_dayweekName(parseInt($parseTree._weekdayGame))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}    

            state: Question calcPrevWeekday
 
                state: Answer calcPrevWeekday
                    q: * [$maybe|$sure] * [был*] $weekdayGame * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:                   
                        if ($session.lastQuiz.right == dts_dayweekName(parseInt($parseTree._weekdayGame))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}                        

            state: Question nextSeason
 
                state: Answer nextSeason
                    q: * [$maybe|$sure] * $Season * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script:
                        if ($session.lastQuiz.right == dts_seasonName(parseInt($parseTree._Season))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}} 

            state: Question prevSeason
 
                state: Answer prevSeason
                    q: * [$maybe|$sure] * $Season * [$maybe|$sure] *
                    if: $temp.parseTree
                        script:
                            $parseTree = $temp.parseTree;                    
                    script: 
                        if ($session.lastQuiz.right == dts_seasonName(parseInt($parseTree._Season))){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}                     

###############################################################################
###############   Реакция на правильный/неправильный ответы      ##############
###############################################################################

        state: RightAnswer
            script:
                if ($session.lastQuiz && $session.lastQuiz.rightAnswers) {
                    $session.lastQuiz.rightAnswers = $session.lastQuiz.rightAnswers + 1;
                } else {
                    $session.lastQuiz.rightAnswers = 1;
                }
            a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["RightAnswer"]) }}
            go!: ../Next question enquire

        state: WrongAnswer
            a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["WrongAnswer"]["a1"]) }}
            if: $temp.rightAnswer
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["WrongAnswer"]["a2"]) }}
            go!: ../Next question enquire

###############################################################################
###############            Непонятный ответ                      ##############
###############################################################################

        state: Question answer or
            q: * [$maybe|$sure] $monthGame * $monthGame [$maybe|$sure] * || fromState = ../Question 
            q: * [$maybe|$sure] $Number * $Number [$maybe|$sure] * || fromState = ../Question
            q: * [$maybe|$sure] $Season * $Season [$maybe|$sure] * || fromState = ../Question
            q: * [$maybe|$sure] $weekdayGame * $weekdayGame [$maybe|$sure] * || fromState = ../Question
            q: * [$maybe|$sure] $monthGame * $monthGame [$maybe|$sure] * || fromState = "../Question dontKnow"
            q: * [$maybe|$sure] $Number * $Number [$maybe|$sure] * || fromState = "../Question dontKnow"
            q: * [$maybe|$sure] $Season * $Season [$maybe|$sure] * || fromState = "../Question dontKnow"
            q: * [$maybe|$sure] $weekdayGame * $weekdayGame [$maybe|$sure] * || fromState = "../Question dontKnow"
            q: * ([ни|или|и] [у] (то*|та|те) [ни|или|и] [у] (то*|та|те|друг*)|ничего [нет]) * || fromState = ../Question
            q: * ([ни|или|и] [у] (то*|та|те) [ни|или|и] [у] (то*|та|те|друг*)|ничего [нет]) * || fromState = "../Question dontKnow"
            q: * ([ни|нет] [у|из] (них|обоих|кого)|оба|обе|никто) * || fromState = ../Question
            if: ($session.barrier && $session.barrier == "Question dontKnow")
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question answer or"]["a1"]) }}
                go!: ../Question
            else:
                script:
                    $session.barrier = "Question dontKnow";
                    $temp.nextState = "Question " + $session.lastQuiz.type;                    
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question answer or"]["a2"]) }}
                a: {{$session.lastQuiz.text}}                    
                go!: ../Question/{{$temp.nextState}}   

        state: Question Negative
            q: * [$sure] не [$preposition] $quizAllCalendar * || fromState = ../Question
            if: ($session.barrier && $session.barrier == "Question Negative")
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question Negative"]["a1"]) }}
                go!: ../Question
            else:
                script:
                    $response.ledAction = 'ACTION_EXP_WINK';
                    $session.barrier = "Question Negative";
                    $temp.nextState = "Question " + $session.lastQuiz.type;
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question Negative"]["a2"]) }}
                a: {{$session.lastQuiz.text}}
                go!: ../Question/{{$temp.nextState}}  

        state: Question dontKnow
            q: * $dontKnow  * $quizAllCalendar или $quizAllCalendar *  || fromState = ../Question 
            q: * $quizAllCalendar или $quizAllCalendar * $dontKnow * || fromState = ../Question
            q: * $dontKnow * || fromState = ../Question
            q: (у кого/(какой/какое) [день недели/месяц/время года]/какой-то/какое-то/(какой/какое) то/день недели/месяц/время года) || fromState = ../Question
            q: * || fromState="../../../Calendar quiz"
            if: ($session.barrier && $session.barrier == "Question dontKnow")
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question dontKnow"]["a1"]) }}
                go!: ../Question
            else:
                if: ($session.lastQuiz)
                    script:
                        $response.ledAction = 'ACTION_EXP_WINK';
                        $session.barrier = "Question dontKnow";
                        $temp.nextState = "Question " + $session.lastQuiz.type;
                    a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question dontKnow"]["a2"]) }}
                    a: {{$session.lastQuiz.text}}                     
                    go!: ../Question/{{$temp.nextState}}
                else:
                    go!: ../Question
  
        state: Question undefined
            q: (*|$agree|$disagree) || fromState = ../Question
            q: * || fromState = ../Question
            if: ($session.barrier && $session.barrier == "Question")
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question undefined"]["a1"]) }}
                go!: ../Question
            else:
                script:
                    $response.ledAction = 'ACTION_EXP_SPEECHLESS';
                    $session.barrier = "Question";
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question undefined"]["a2"]) }}
         
            state: Undefined try again
                q: * [$agree] * { (еще|ещё) [одну|один] [раз*|попытку] [попробую|попробуем] } * || fromState = .., onlyThisState = true
                q: * [$agree] * { (попробую|попробуем|попытку) [еще|ещё] [одну|один] [раз*] } * || fromState = .., onlyThisState = true
                q: * [$agree] * { (еще|ещё) [одну|один] [раз*|попытку] [попробую|попробуем] } * || fromState = "../Undefined try again yes", onlyThisState = true
                q: * [$agree] * { (попробую|попробуем|попытку) [еще|ещё] [одну|один] [раз*] } * || fromState = "../Undefined try again yes", onlyThisState = true
                q: вопрос || fromState = .., onlyThisState = true                       
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question undefined"]["Undefined try again"]) }}
                script:
                    $temp.nextState = "Question " + $session.lastQuiz.type;
                a: {{$session.lastQuiz.text}}
                go!: ../../Question/{{$temp.nextState}}                    

            state: Undefined try again no
                q: * {(неправильный/плохой/что [еще] за) * вопрос} * || fromState = ../../Question
                q: * (*давай * (следующий/другой) вопрос/не (хочу/буду) пробовать) * || fromState = ../../Question
                q: далее || fromState = ../../Question
                q: * { [$agree] [следующ*|друг*|еще|ещё] вопрос* } * || fromState = "../Undefined try again yes", onlyThisState = true
                q: * не (хочу/буду) пробовать * || fromState = "../Undefined try again yes", onlyThisState = true                
                q: * { [$agree] (следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*|не (хочу/буду) пробовать) [вопрос*] } * || fromState = ../, onlyThisState = true
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question undefined"]["Undefined try again no"]) }}
                go!: ../../Question

            state: Undefined try again yes
                q: * $agree * || fromState = ../, onlyThisState = true
                q: * $disagree * || fromState = ../, onlyThisState = true
                script:
                    $temp.nextState = "Question " + $session.lastQuiz.type;
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Question undefined"]["Undefined try again yes"]) }}
                    
            state: Undefined try again quiz answer
                q: * $quizAllCalendar *
                script:
                    var context = "/Calendar quiz/Pudding start/Question/Question " + $session.lastQuiz.type;
                    var result = $nlp.match($parseTree.text, context);
                    $temp.nextState = result.targetState;
                    $parseTree = result.parseTree;
                    $temp.parseTree = result.parseTree;
                go!: {{$temp.nextState}}

        state: User wants next question
            q: * ($agree/$disagree/не хочу/не буду/не надо/подскажи/помоги/уже спрашивал/повторяешься/(скажи/какой/назови) * (ответ/сам/ты)/(сам/ты) (ответь/отвечай))  * || fromState = ../Question
            q: * [$agree] (дальше|след*|ещё|еще|игра*|задавай|другой) [дальше|след*|ещё|еще|игра*|задавай|другой] [вопрос*] * || fromState = ../Question
            q: * $agree (дальше|след*|ещё|еще|игра*|задавай|другой) [дальше|след*|ещё|еще|игра*|задавай|другой] вопрос* * || fromState = ../Question
            a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["User wants next question"]) }}
            go!: ../Question

        state: Do you know || noContext = true
            q: * { (ты|сам) знаешь [ответ] } *
            a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Do you know"]) }}

        state: Repeat the question
            q: * {(повтори*|ска*|зада*) вопрос [(еще|ещё) раз*] [пожалуйста|плиз]}* || fromState = "/Calendar quiz"
            q: * {(повтори*|ска*|зада*) * [пожалуйста|плиз]} * || fromState = ../Question, onlyThisState = true
            q: * (чего|что|(еще|ещё) раз*) * || fromState = ../Question, onlyThisState = true
            if: ($session.lastQuiz)
                script:
                    $temp.nextState = "Question " + $session.lastQuiz.type;
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Repeat the question"]) }}
                a: {{$session.lastQuiz.text}}
                go!: ../Question/{{$temp.nextState}}
            else:
                go!: ../Question        
          
###############################################################################
###############       Предлагаем ещё один вопрос                 ##############
###############################################################################

        state: Next question enquire
            script:
                $temp.ask = selectRandomArg("yes","no","no","no","no","no","no");
            if: ($temp.ask == "yes")
                script:
                    $response.ledAction = 'ACTION_EXP_WINK';
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Next question enquire"]["a1"]) }}
            else:
                go!: ../Question

            state: Next question enquire yes
                q: * ($agree|дальше|след*|ещё|еще|игра*|зада*|другой|продолж*|я готов*) [$agree|дальше|след*|ещё|еще|игра*|задавай*] [вопрос*]*
                q: * давай [следующий|другой|еще|ещё|свой|дальше|задавай*] вопрос* *
                q: * [можешь] (задать|задавай*) [след*|ещё|еще|другой] [вопрос*] *
                script:
                    $response.ledAction = 'ACTION_EXP_HAPPY';
                a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Next question enquire"]["Next question enquire yes"]) }}
                go!: ../../Question

            state: Next question enquire undefined
                q: $Text || fromState = ../
                if: ($session.barrier && $session.barrier == "Question")
                    script:
                        $response.ledAction = 'ACTION_EXP_DIZZY';
                    a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Next question enquire"]["Next question enquire undefined"]["if"]) }}
                    go!: ../../Question
                else:
                    script:
                        $response.ledAction = 'ACTION_EXP_DIZZY';
                        $session.barrier = "Question";
                    a: {{ selectRandomArg($CalendarAnswers["Pudding start"]["Next question enquire"]["Next question enquire undefined"]["else"]) }}

###############################################################################
###############             Остановка игры                       ##############
###############################################################################

    state: Pudding stop
        q: * $stopGame * || fromState = "../Pudding start"
        q: * ($disagree|$stopGame|$dontKnow) * || fromState = "../Pudding start/Next question enquire"
        q: * [давай] * перерыв *
        q: [я] устал*
        a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["a1"]) }}
        script:
            var right = $session.lastQuiz.rightAnswers;
            var all_questions = $session.lastQuiz.numQuestions;
            
            $temp.ru__answers = right % 10 === 1 && right % 100 !== 11 ? 'вопрос' : (right % 10 >= 2 && right % 10 <= 4 && (right % 100 < 10 || right % 100 >= 20) ? 'вопроса' : 'вопросов');
            $temp.ru_questions = all_questions % 10 === 1 && all_questions % 100 !== 11 ? 'вопрос' : (all_questions % 10 >= 2 && all_questions % 10 <= 4 && (all_questions % 100 < 10 || all_questions % 100 >= 20) ? 'вопроса' : 'вопросов');
        if: ($session.lastQuiz.rightAnswers == 0)
            if: ($session.lastQuiz.numQuestions == 1)
                a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["if there were no right answers"]["if there was only one question"]) }}
            else:
                a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["if there were no right answers"]["else"]) }}
            a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["if there were no right answers"]["a1"]) }}

        else:
            if: ($session.lastQuiz.rightAnswers == $session.lastQuiz.numQuestions)
                if: ($session.lastQuiz.numQuestions == 1)
                    a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["else"]["if there were no mistakes"]["if there was only one question"]) }}
                else:
                    a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["else"]["if there were no mistakes"]["else"]) }}
                a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["else"]["if there were no mistakes"]["a1"]) }}
                
            else:
                a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["else"]["else"]["a1"]) }}
                a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["else"]["else"]["a2"]) }}
        script:
            $session.lastQuiz = undefined;

        state: Again
            q: * { [$agree] * (еще|ещё|продолж*|повторим) [раз*] [сыгр*|игр*|поигр*] [в (календар*)] } *  
            a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["Again"]) }}
            go!: ../../Pudding start/Question

        state: Again question
            q: * { [$agree] * (еще|ещё) вопрос* } *  
            script:
                $response.ledAction = 'ACTION_EXP_HAPPY';
            a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["Again question"]) }}
            go!: ../../Pudding start/Question    

    state: HowDoYouKnow
        q: * {$you * откуда * знаешь} * || fromState = "../Pudding start"
        a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["HowDoYouKnow"]) }}
        if: ($session.lastQuiz && $session.lastQuiz.text)
            script:
                $temp.nextState = "Question " + $session.lastQuiz.type;
            a: {{$session.lastQuiz.text}}                    
            go!: ../Pudding start/Question/{{$temp.nextState}}
        else:
            go!: ../Pudding start/Question   

    state: Count right answers || noContext = true
        q: * {ско* * правильн* * ответ*} *
        script:          
            var right = $session.lastQuiz.rightAnswers;
            var all_questions = $session.lastQuiz.numQuestions;
            
            $temp.ru__answers = right % 10 === 1 && right % 100 !== 11 ? 'вопрос' : (right % 10 >= 2 && right % 10 <= 4 && (right % 100 < 10 || right % 100 >= 20) ? 'вопроса' : 'вопросов');
            $temp.ru_questions = all_questions % 10 === 1 && all_questions % 100 !== 11 ? 'вопрос' : (all_questions % 10 >= 2 && all_questions % 10 <= 4 && (all_questions % 100 < 10 || all_questions % 100 >= 20) ? 'вопроса' : 'вопросов');
        if: ($session.lastQuiz.rightAnswers == 0)
            if: ($session.lastQuiz.numQuestions == 1)
                a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["Count right answers"]["if there were no right answers"]["if there was only one question"]) }}
            else:
                a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["Count right answers"]["if there were no right answers"]["else"]) }}

        else:
            if: ($session.lastQuiz.rightAnswers == $session.lastQuiz.numQuestions)
                if: ($session.lastQuiz.numQuestions == 1)
                    a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["Count right answers"]["else"]["if there were no mistakes"]["if there was only one question"]) }}
                else:
                    a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["Count right answers"]["else"]["if there were no mistakes"]["else"]) }}
                
            else:
                a: {{ selectRandomArg($CalendarAnswers["Pudding stop"]["Count right answers"]["else"]["else"]["a1"]) }}
