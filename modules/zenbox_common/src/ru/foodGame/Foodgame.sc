require: Foodgame.js
require: ../main.js

require: ../Games.sc
require: ../../common/common.sc

require: food-ru.csv
  name = Foods
  var = $Foods

require: answers.yaml
  var = FoodGameCommonAnswers


init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .FoodTagConverter = function(parseTree) {
            var id = parseTree.Foods[0].value;
            return $Foods[id].value;
        };

    $global.themes = $global.themes || [];
    $global.themes.push("Food quiz");

    $global.$FoodGameAnsw = (typeof FoodGameCustomAnswers != 'undefined') ? applyCustomAnswers(FoodGameCommonAnswers, FoodGameCustomAnswers) : FoodGameCommonAnswers;

patterns:
    $foodType = ((фрукт*):1|(ягод*):2|(орех*|ореш*):3|(сухофрукт*|сух* фрукт*):4|(овощ*):5|(мяс*):6|(рыб*):7|(мясн* [блюд*]|[из] мяс*|блюд*):8|(суп*):9|
        (десерт*):10|(выпечк*):11)
    $foodSweetness = ((слад*|да):1|({[нет] (неслад*|не слад*)}|нет|не):2)
    $foodIngredient = (
       [из] (моло*):1|
       [из] (мяс*):2|
       [из] (яиц*|яйц*|яич*):3|
       [из] (мук*):4)
    $foodAll = ($foodType|$foodSweetness|$foodIngredient)
    $Food = $entity<Foods> || converter = $converters.FoodTagConverter

theme: /Food quiz

    state: Food game user start
        q!: * {[давай|хоч*] (поигра*|сыгра*|игра*) * [в] (ед*|продукт*|буфет*) *} * 
        q!: * {(давай|хоч*) [поигра*|сыгра*|игра*] * [в] (ед*|продукт*|буфет*) *} *
        q!: [в] (буфет/продукты)
        script:
            $session.lastQuiz = undefined;
        a: {{ selectRandomArg($FoodGameAnsw["FG user start"]) }}
        go!: ../Food game pudding start/Food game question         

    state: Food game pudding start
        script:
            $session.lastQuiz = undefined;
        a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["a1"]) }}

        state: Food game no
            q: ($disagree|не помогу|$notNow) || fromState = "/Food quiz/Food game pudding start", onlyThisState = true
            q: ($disagree|не помогу|$notNow) || fromState = "/PlayGames/Games/How to play Food", onlyThisState = true 
            q: ($disagree|$notNow) || fromState = "/Food quiz/Food game pudding start/Food game tell rules", onlyThisState = true
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG no"]) }}          
            go!: /StartDialog

        state: Food game yes
            q!: * [$agree|хочу] (играть|сыграть|поиграть) * (ед*|продукт*) *        
            q: * ($agree|помогу|постара*|буду стара*|(если|чем|как) смогу|попробу*|поехали) [тебе] [начин*|начн*|поигра*|сыгра*|игра*|помочь [тебе]] || fromState = "/Food quiz/Food game pudding start", onlyThisState = true
            q: * ($agree|начин*|начн*|поехали) * || fromState = "/PlayGames/Games/How to play Food", onlyThisState = true
            q: * ($agree|начин*|начн*|поехали) * || fromState = "/Food quiz/Food game pudding start/Food game tell rules", onlyThisState = true
            q: * [$agree] (поигра*|сыгра*|игра*|поехали) * || fromState = "/Food quiz/Food game pudding start/Food game tell rules", onlyThisState = true
            script:
                $session.lastQuiz = undefined;
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG yes"]) }}
            go!: ../Food game question   

        state: Food game yes from Games          
            q: * (буфет*|ед*|продукт*|бурфи|[булл] фиат|в уфе) * || fromState = /Games, onlyThisState = true
            script:
                $session.lastQuiz = undefined;
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG yes from Games"]) }}
            go!: ../Food game question

        state: Food game yes from Ask user about game
            q: * (ед*|продукт*|буфет*) * || fromState = "/Ask user about game", onlyThisState = true
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG yes from Ask user about game"]) }}
            go!: ../Food game tell rules

        state: Food game tell rules
            q!: * [*скажи*|повтори] (как|о|про|правил*) игр* [в] (буфет*|ед*|продукт*) *
            q!: * что (за|такое) [игра] [в] (буфет*|ед*|продукт*) *
            q: * { [*скажи*|повтори] (как|о|про|правил*) [игр*] [в] [буфет*|ед*|продукт*]  [еще|ещё] [один] [раз] } *  || fromState = "../Food game question"
            q: * { (*скажи*|повтори) (как|о|про|правил*) [игр*] [в] [буфет*|ед*|продукт*]  [еще|ещё] [один] [раз] } *  || fromState = "/Food quiz/Food game pudding start/Next question enquire"
            if: ($session.lastQuiz)
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG tell rules"]["if"]) }}
            else:
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG tell rules"]["else"]["a1"]) }}
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG tell rules"]["else"]["a2"]) }}

###############################################################################
###############                   Генерация вопросов             ##############
###############################################################################

        state: Food game question
            script:
                if ($session.barrier) {
                    $session.barrier = undefined;
                }
                $response.ledAction = 'ACTION_EXP_QUESTION';
                $session.lastQuiz = getFoodQuestion();
                $temp.nextState = "Food game question " + $session.lastQuiz.type;
            a: {{$session.lastQuiz.text}}
            go!: ./{{$temp.nextState}}

            state: Food game question ingredient
                
                state: Answer ingredient
                    q: * [$maybe|$sure|$dontKnow] * ($foodIngredient|$Food) * [$maybe|$sure|$dontKnow] *
                    script:
                        $temp.right = getIngredientCode($session.lastQuiz.food);
                        var answer;
                        if ($parseTree.foodIngredient) {
                            answer = $parseTree._foodIngredient;
                        } else {
                            answer = $parseTree._Food.title;
                            switch(answer) {
                                case "молоко":
                                    answer = "1";
                                    break;
                                case "мясо":
                                    answer = "2";
                                    break;
                                case "яйцо":
                                    answer = "3";
                                    break;
                                case "мука":
                                    answer = "4";
                                    break;
                                case "рыба":
                                    answer = "5";
                                    break;
                            } 
                        }
                        if ($temp.right == answer){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}

            state: Food game question sweetness
                
                state: Answer sweetness
                    q: * [$maybe|$sure|$dontKnow] * $foodSweetness * [$maybe|$sure|$dontKnow] *
                    script:
                        $temp.right = getSweetnessCode($session.lastQuiz.food);
                        if ($temp.right == $parseTree._foodSweetness){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}}

            state: Food game question type
                
                state: Answer type
                    q: * [$maybe|$sure|$dontKnow] * $foodType * [$maybe|$sure|$dontKnow] *
                    script:
                        $temp.right = getFoodTypeCode($session.lastQuiz.food);
                        if ($temp.right == $parseTree._foodType){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                        }
                    go!: ../../../{{$temp.nextState}} 

        state: Food game question answer dontknow
            q: * ($dontKnow/$agree/$disagree/не хочу/не буду/не надо/подскажи/помоги/уже спрашивал/повторяешься/(скажи/какой/назови) * ответ/сам (ответь/отвечай))  *
            q: * [давай] (дальше|след*|ещё|еще|игра*|поехали|задавай|другой) [дальше|след*|ещё|еще|игра*|поехали|задавай|другой] [вопрос|вопросик]*
            script:
                $response.ledAction = 'ACTION_EXP_SPEECHLESS';
                $temp.correct = $session.lastQuiz.right;
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question answer dontknow"]["a1"]) }}
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question answer dontknow"]["a2"]) }}
            go!: ../Next question enquire            
            
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
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["RightAnswer"]) }}
            go!: ../Next question enquire

        state: WrongAnswer
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["WrongAnswer"]) }}
            go!: ../Next question enquire

###############################################################################
###############            Непонятный ответ                      ##############
###############################################################################

        state: Food game question answer i think
            q: * { [думаю|считаю|предполагаю] [что] не ($foodType|$foodSweetness|[из] $foodIngredient) } * || fromState = "../Food game question"
            q: * { [думаю|считаю|предполагаю] [что] не ($foodType|$foodSweetness|[из] $foodIngredient) } * || fromState = "../Food game question dontKnow"
            if: ($session.barrier && $session.barrier == "Food game question answer i think")
                script:
                    $temp.correct = $session.lastQuiz.right;
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question answer i think"]["if"]) }}
                go!: ../Food game question
            else:
                script:
                    $session.barrier = "Food game question answer i think";
                    $temp.nextState = "Food game question " + $session.lastQuiz.type;
                if: ($session.lastQuiz.type == "type")
                    a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question answer i think"]["else"]["if"]) }}
                else:
                    if: ($session.lastQuiz.type == "sweetness")
                        a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question answer i think"]["else"]["else"]["if"]) }}
                    else:
                        a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question answer i think"]["else"]["else"]["else"]) }}
                go!: ../Food game question/{{$temp.nextState}}
                
        state: Food game question answer alternate
            q: * ([и|ни] [из] (то*|та|те|так*) (или|и|ни) [из] (то*|та|те|друг*|так*)) * || fromState = "../Food game question"
            q: * ([и|ни] [из] (то*|та|те|так*) (или|и|ни) [из] (то*|та|те|друг*|так*)) * || fromState = "../Food game question dontKnow"
            q: * [$maybe|$sure] [и|ни] $foodType [или|и|ни] $foodType [$maybe|$sure] * || fromState = "../Food game question"
            q: * [$maybe|$sure] [и|ни] $foodType [или|и|ни] $foodType [$maybe|$sure] * || fromState = "../Food game question dontKnow"
            q: * [$maybe|$sure] [и|ни] $foodSweetness [или|и|ни] $foodSweetness [$maybe|$sure] * || fromState = "../Food game question"
            q: * [$maybe|$sure] [и|ни] $foodSweetness [или|и|ни] $foodSweetness [$maybe|$sure] * || fromState = "../Food game question dontKnow"
            q: * [$maybe|$sure] [и|ни] [из] $foodIngredient [или|и|ни] [из] $foodIngredient [$maybe|$sure] * || fromState = "../Food game question"
            q: * [$maybe|$sure] [и|ни] [из] $foodIngredient [или|и|ни] [из] $foodIngredient [$maybe|$sure] * || fromState = "../Food game question dontKnow"
            q: * (по разному|по-разному|разн*|люб*|всяк*)  * || fromState = "../Food game question"
            q: * (по разному|по-разному|разн*|люб*|всяк*) * || fromState = "../Food game question dontKnow"
            if: ($session.barrier && $session.barrier == "Food game question answer dontKnow")
                script:
                    $temp.correct = $session.lastQuiz.right;
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question answer alternate"]["if"]) }}
                go!: ../Food game question
            else:
                script:
                    $session.barrier = "Food game question answer dontKnow";
                    $temp.nextState = "Food game question " + $session.lastQuiz.type;
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question answer alternate"]["else"]) }}
                a: {{$session.lastQuiz.text}} 
                go!: ../Food game question/{{$temp.nextState}}

        state: Food game question dontKnow
            q: * $dontKnow * || fromState = "../Food game question"
            if: ($session.barrier && $session.barrier == "Food game question dontKnow")
                script:
                    $response.ledAction = 'ACTION_EXP_WINK';
                    $temp.correct = $session.lastQuiz.right;
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question dontKnow"]["if"]) }}
                go!: ../Food game question
            else:
                script:
                    $session.barrier = "Food game question dontKnow";
                    $temp.nextState = "Food game question " + $session.lastQuiz.type;
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question dontKnow"]["else"]) }}
                a: {{$session.lastQuiz.text}}
                go!: ../Food game question/{{$temp.nextState}}

        
        state: Food game question tell answer
            q: * { (скажи*|назови*) [правильн*] ответ*} * || fromState = "../Food game question"
            if: ($session.barrier && $session.barrier == "Food game question tell answer")
                script:
                    $temp.correct = $session.lastQuiz.right;
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question tell answer"]["if"]["a1"]) }}
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question tell answer"]["if"]["a2"]) }}
                go!: ../Food game question
            else:
                script:
                    $session.barrier = "Food game question tell answer";
                    $temp.nextState = "Food game question " + $session.lastQuiz.type;
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question tell answer"]["else"]) }}
                a: {{$session.lastQuiz.text}}
                go!: ../Food game question/{{$temp.nextState}}

        state: Food game question undefined || noContext = true
            q: * || fromState = "../Food game question"
            q: * $disagree * || fromState = "../Food game question/Food game question ingredient"
            q: * $disagree * || fromState = "../Food game question/Food game question type"
                       
            if: ($session.barrier && $session.barrier == "Food game question")
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question undefined"]["if"]) }}
                go!: ../Food game question
            else:
                script:
                    $session.barrier = "Food game question";
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question undefined"]["else"]) }}
                   
            state: Food game undefined try again
                q: * (еще|ещё|попробую) [раз] * || fromState = "../../Food game question", onlyThisState = true
                q: * (еще|ещё|попробую|попыт*) * [раз] [попыт*] * || fromState = .., onlyThisState = true
                q: * { давай * [еще|ещё] [раз] (попробую|попытк*) }* || fromState = "../../Food game question"
                q: * { давай (еще|ещё) * [раз] [попробую|попытк*] } * || fromState = "../../Food game question"
                script:
                    $temp.nextState = "Food game question " + $session.lastQuiz.type;
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["FG question undefined"]["FG undefined try again"]) }}
                a: {{$session.lastQuiz.text}}
                go!: ../../Food game question/{{$temp.nextState}}                    

            state: Food game undefined try again no
                q: * (следу*|друг*|дальш*|продолж*|поехали) [вопрос] * || fromState = "../../Food game question", onlyThisState = true 
                q: * давай * (следу*|друг*|продолж*|дальше|поехали) [вопрос] * || fromState = "../../Food game question"
                a: {{ selectRandomArg( $FoodGameAnsw["FG pudding start"] ["FG question undefined"] ["FG undefined try again no"] ) }}
                go!: ../../Food game question

        state: I was right
            q: * [я] прав* ответ* [же] *
            q: [я] был* прав*
            q: * ты не прав *
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["I was right"]) }}
            go!: ../Next question enquire

###############################################################################
###############       Предлагаем ещё один вопрос                 ##############
###############################################################################

        state: Next question enquire
            script:
                $temp.ask = selectRandomArg("yes","no","no","no","no","no","no");
            if: ($temp.ask == "yes")
                script:
                    $response.ledAction = 'ACTION_EXP_WINK';
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["Next question enquire"]["a1"]) }}
            else:
                go!: ../Food game question

            state: Next question enquire yes
                q: * ($agree|дальше|след*|ещё|еще|игра*) [$agree|дальше|след*|ещё|еще|игра*] [вопрос|вопросик] [пожалуйста|плиз]* || fromState = "../../Next question enquire", onlyThisState = true 
                q: * (следующий|другой|еще|ещё|свой) [один] (вопрос|вопросик) [пожалуйста|плиз] * || fromState = "../../Next question enquire", onlyThisState = true 
                q: { [я] (слушаю|готов*) } || fromState = "../../Next question enquire", onlyThisState = true 
                q: (продолжай|поехали|перейдем|перейдём|задавай|продолжаем|продолжим) || fromState = "../../Next question enquire", onlyThisState = true 
                script:
                    $response.ledAction = 'ACTION_EXP_HAPPY';
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["Next question enquire"]["Next question enquire yes"]) }}
                go!: ../../Food game question

            state: Next question enquire answer undefined
                q: $Text || fromState = "../../Next question enquire", onlyThisState = true 
                script:
                    $response.ledAction = 'ACTION_EXP_SPEECHLESS';
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding start"]["Next question enquire"]["Next question enquire answer undefined"]) }}
                go!: ../../Next question enquire
         
###############################################################################
###############             Остановка игры                       ##############
###############################################################################

    state: Food game pudding stop
        q: * ($stopGame|$notNow) *
        q: * (хочу отдохнуть/я устал*/давай * перерыв) *
        q: * { не хоч* [больше] [игр*] [в] [буфет] } *
        q: достаточно
        q: * $disagree * || fromState = "../Food game pudding start/Next question enquire"
        q: достаточно || fromState = "../Food game pudding start/Next question enquire"
        script:
            $response.ledAction = 'ACTION_EXP_SPEECHLESS';
        a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["a1"] )}}
        script:
            var right = $session.lastQuiz.rightAnswers;
            var all_questions = $session.lastQuiz.numQuestions;

            $temp.ru__answers = right % 10 === 1 && right % 100 !== 11 ? 'вопрос' : (right % 10 >= 2 && right % 10 <= 4 && (right % 100 < 10 || right % 100 >= 20) ? 'вопроса' : 'вопросов')
            $temp.ru_questions = all_questions % 10 === 1 && all_questions % 100 !== 11 ? 'вопрос' : (all_questions % 10 >= 2 && all_questions % 10 <= 4 && (all_questions % 100 < 10 || all_questions % 100 >= 20) ? 'вопроса' : 'вопросов');
        if: ($session.lastQuiz.rightAnswers == 0)
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["if"]["a1"]) }}
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["if"]["a2"]) }}
        else:
            if: ($session.lastQuiz.rightAnswers == $session.lastQuiz.numQuestions)
                if: ($session.lastQuiz.numQuestions == 1)
                    a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["else"]["if"]["if"]) }}
                else:
                    a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["else"]["if"]["else"]) }}
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["else"]["if"]["a1"]) }}
            else:
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["else"]["else"]["a1"]) }}
                a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["else"]["else"]["a2"]) }}
        script:
            $session.lastQuiz = undefined;
            
        state: Food game pudding stop thank you
            q: * (спасиб*|благодар*) [за] [игр*] *
            script:
                $response.ledAction = 'ACTION_EXP_CUTE';
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["FG pudding stop thank you"]) }}

        state: Food game pudding stop joy
            q: * (ура|урра|здорово|замечательно|молодцы|молодец|класс|отличн*) *   
            script:
                $response.ledAction = 'ACTION_EXP_HAPPY';
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["FG pudding stop joy"]) }}
             
        state: Food game pudding stop sad
            q: * (плох*|ужасн*|эх|жалко|жаль) *
            script:
                $response.ledAction = 'ACTION_EXP_CUTE';
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["FG pudding stop sad"]) }}

        state: Food game again
            q: * (еще|ещё|продолж*) [сыгр*|игр*|поигр*|раз*|вопрос*] *
            q: * давай* (еще|ещё) [сыгр*|игр*|поигр*|раз*|вопрос*] *
            script:
                $response.ledAction = 'ACTION_EXP_HAPPY';
            a: {{ selectRandomArg($FoodGameAnsw["FG pudding stop"]["FG again"]) }}
            go!: ../../Food game pudding start/Food game question

    state: Food game repeat question
        q: * { (повтори*|ска*|зада*) * (вопрос|задание|что * сказал*) [еще|ещё] [раз] [пожалуйста|плиз]} * || fromState = "../Food game pudding start"
        q: * { (повтори*|ска*|зада*) * (вопрос|задание|что * сказал*) [еще|ещё] [раз] [пожалуйста|плиз]} * || fromState = "/Food quiz/Food game pudding start/Food game tell rules"
        q: * (чего|что|(еще|ещё) раз|[я] не понял* [вопрос|задание|что * сказал*]) * || fromState = "../Food game pudding start"
        q: вопрос || fromState = "../Food game pudding start"
        if: ($session.lastQuiz && $session.lastQuiz.text)
            script:
                $temp.nextState = "Food game question " + $session.lastQuiz.type;
            a: {{ selectRandomArg($FoodGameAnsw["FG repeat question"]) }}
            a: {{$session.lastQuiz.text}}
            go!: ../Food game pudding start/Food game question/{{$temp.nextState}}
        else:
            go!: ../Food game pudding start/Food game question

    state: Food game howDoYouKnow
        q: * {$you * откуда * знаешь} * || fromState = "../Food game pudding start"          
        a: {{ selectRandomArg($FoodGameAnsw["FG howDoYouKnow"]) }}
        if: ($session.lastQuiz && $session.lastQuiz.text)
            script:
                $temp.nextState = "Food game question " + $session.lastQuiz.type;
            a: {{$session.lastQuiz.text}}
            go!: ../Food game pudding start/Food game question/{{$temp.nextState}}
        else:
            go!: ../Food game pudding start/Food game question            