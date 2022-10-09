require: MathGame.js
require: ../../common/mathGame/MathGame.js
require: ../../common/common.sc

require: fruits.csv
  name = Fruits
  var = Fruits

require: number/number.sc
  module = zb_common

require: floatNumber/floatNumber.sc
  module = zb_common

require: namesRu/namesRu.sc
  module = zb_common

require: patterns.sc
  module = zb_common

require: ../Games.sc

require: ../main.js

require: answers.yaml
    var = MathGameCommonAnswers

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.themes = $global.themes || [];
    $global.themes.push("Math game");

    $global.$MathGameAnswers = (typeof MathGameCustomAnswers != 'undefined') ? applyCustomAnswers(MathGameCommonAnswers, MathGameCustomAnswers) : MathGameCommonAnswers;

theme: /Math game

    state: Math game alternative enter
        q!: * {(научи*|не умею) * *считать} *
        q!: [я] [хочу/буду] (~научить/~учить) тебя ~математика
        q!: * {(плохо|двойка|тройка) (считаю|(с|по) ~математика)} *
        q!: давай посчитаем           
     
        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_alternative_enter"])}}
        go!: ../Math game pudding start/Math game question

    state: Math game user start
        q!: * {[давай] * (сыграем|играть|поигра*|сыгр*) * [в] (~устный ~счет|математи*|арифмети*)} *
        q!: * {(давай|хочу|хочется) * [в] (~устный ~счет|математи*|арифмети*)} *
        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_user_start"]["forRandom"])}}
        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_user_start"]["followingQues"])}} || question = true
        go!: ../Math game pudding start

    state: Math game suggest quiz
        q!: предложи викторину про математику
        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_suggest_quiz"])}} || question = true
        go!: ../Math game pudding start

    state: Math game pudding start        
        
        state: Math game no
            q: * ($disagree|$notNow|не помогу) * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) * || fromState = "/Games/How to play Math", onlyThisState = true 
            a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_no"])}}

        state: Math game yes
            q: * ($agree|начин*|начн*|помогу|могу|постараюсь|попробую|поехали|поможешь) * || fromState = .., onlyThisState = true
            q: * ($agree|начин*|начн*|поехали) * || fromState = "/Games/How to play Math", onlyThisState = true
            q!: * [хочу] (играть|сыграть|поиграть|игра) * в (~устный ~счет|~математика) *
            q: * (~устный ~счет|~математика|тематик*) * || fromState = /Games, onlyThisState = true  
            q: * [$agree|$maybe|$sure] * (~устный ~счет|~математика) * [$agree|$maybe|$sure] * || fromState = /PlayGames/Games, onlyThisState = true        
            a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_yes"])}}
            go!: ../Math game question

###############################################################################
###############                   Генерация вопросов             ##############
###############################################################################

        state: Math game question
            script:
                $response.action = "ACTION_EXP_QUESTION";
                if ($session.barrier) {
                    $session.barrier = undefined;
                }
                $session.lastQuiz = getMathQuestion();
            a: {{$session.lastQuiz.text}} || question = true

            state: Math game answer number
                q: * [$CalcNumber] $repeat<$MathExpression> $CalcQuestion *
                q: * [$CalcNumber] $repeat<$MathExpression> $CalcQuestion * || fromState = "../Math game question undefined", onlyThisState = true            
                q: * [$maybe|$sure] [получится|ответ] $Number [$maybe|$sure]  *
                q: * [$maybe|$sure] [получится|ответ] $Number [$maybe|$sure]  * || fromState = "../Math game question undefined", onlyThisState = true
                script:
                    $temp.answer = $parseTree.CalcQuestion ? $parseTree.CalcQuestion[0].CalcNumber[0].value : $parseTree._Number;
                if: ($session.lastQuiz && typeof $session.lastQuiz.result != 'undefined')
                    if: ($temp.answer == $session.lastQuiz.result)
                        script:
                            $temp.nextState = "RightAnswer";
                    else:
                        script:
                            $temp.nextState = "WrongAnswer";
                else:
                    a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_answer_number"])}}
                    script:
                        $temp.nextState = "Math game question";
                go!: ../../{{$temp.nextState}}

            state: Math game question answer or
                q:  * [$maybe|$sure] $Number::Number * (или) * $Number::Number2 [$maybe|$sure] *
                q: * [$maybe|$sure]  (или то или то|то или другое|и то и другое|то и другое|ни то ни другое|ничего) [$maybe|$sure] *
                if: $parseTree._Number == $parseTree._Number2
                    go!: ../Math game answer number
                else:
                    if: ($session.barrier && $session.barrier == "Math game question answer or")
                        #a: Правильный ответ 
                        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_answer_or"]["numberOrNumberSecondTime"]["a1"])}}
                        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_answer_or"]["numberOrNumberSecondTime"]["a2"])}}
                        go!: ../../Math game question
                    else:
                        script:
                            $session.barrier = "Math game question answer or";
                            $response.action = "ACTION_EXP_DIZZY";
                        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_answer_or"]["numberOrNumberFirstTime"])}}

                        a: {{$session.lastQuiz.text}} || question = true
                        go: ../../Math game question

            state: Math game question cheating
                q: * ([на] [очень] много|намного) *
                if: ($session.barrier && $session.barrier == "Math game question cheating")
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_cheating"]["secondTimeCheating"]["a1"] )}}
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_cheating"]["secondTimeCheating"]["a2"] )}}
                    go!: ../../Math game question
                else:
                    script:
                        $session.barrier = "Math game question cheating";
                        $response.action = "ACTION_EXP_DIZZY";
                    a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_cheating"]["firstTimeCheating"])}}
                    a: {{$session.lastQuiz.text}} || question = true
                    go: ../../Math game question

            state: Math game question dontKnow
                q: * $dontKnow  * $Number или $Number *
                q: * $Number или $Number * $Number *
                q: * ($dontKnow|не (умею|проходили|прошли|понимаю)|плохо (умею считать|считаю)|сколько) *
                if: ($session.barrier && $session.barrier == "Math game question dontKnow")
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_dontKnow"]["secondTimeDontKnow"]["a1"] )}}
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_dontKnow"]["secondTimeDontKnow"]["a2"] )}}
                    go!: ../../Math game question
                else:
                    script:
                        $session.barrier = "Math game question dontKnow";
                    a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_dontKnow"]["firstTimeDontKnow"])}}
                    a: {{$session.lastQuiz.text}} || question = true
                    go: ../../Math game question                    

            state: Math game question negative numbers
                q: * $regexp<-\d+> *
                if: ($session.barrier && $session.barrier == "Math game question")
                    a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_negative_numbers"]["secondTimeNegativeNumber"]["a1"])}}
                    a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_negative_numbers"]["secondTimeNegativeNumber"]["a2"])}}
                    a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_negative_numbers"]["secondTimeNegativeNumber"]["a3"])}}
                    go!: ../../Math game question
                else:
                    script:
                        $response.action = "ACTION_EXP_DIZZY";
                        $session.barrier = "Math game question";
                    a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_negative_numbers"]["firstTimeNegativeNumber"])}} || question = true
                    go: ../Math game question undefined

            state: Math game question user complains
                q: * ([у меня] (не получается сосчитать|плохо с (математикой|устным счетом|сложением|вычитанием|делением|умножением))|[я] (плохо|не очень хорошо) (умею считать|считаю)) * 
                if: ($session.barrier && $session.barrier == "Math game question user ")
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_user_complains"]["secondTimeComlains"]["a1"] )}}
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_user_complains"]["secondTimeComlains"]["a2"] )}}
                    go!: ../../Math game question
                else:
                    script:
                        $session.barrier = "Math game question complains";
                    a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_user_complains"]["firstTimeComplains"])}} 
                    a: {{$session.lastQuiz.text}} || question = true
                    go: ../../Math game question 


            state: Math game question undefined
                q: (*|$agree|$disagree)
                if: ($session.barrier && $session.barrier == "Math game question")
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["secondTimeUndefined"]["a1"] )}}
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["secondTimeUndefined"]["a2"] )}}
                    go!: ../../Math game question
                else:
                    script:
                        $response.action = "ACTION_EXP_DIZZY";
                        $session.barrier = "Math game question";
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["firstTimeUndefined"]) }} || question = true
                      
                state: Math game undefined try again
                    q: * [давай] * (еще|ещё|попробую|$agree) [одну|один] [раз|попытку] * || fromState = .., onlyThisState = true
                    q: $Text || fromState = .., onlyThisState = true
                    q: * [давай] * [еще|ещё|попробую|$agree] (одну|один) (раз|попытку) * || fromState = .., onlyThisState = true
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["Math_game_undefined_try_again"]) }}
                    a: {{$session.lastQuiz.text}} || question = true                   
                    go: ../../../Math game question                   

                state: Math game undefined try again no
                    q: * (следующ*|друг*|дальше|продолжай*|продолж*|$disagree) [вопрос*] * || fromState = .., onlyThisState = true
                    q: * [давай] ($agree|дальше|след*|другой|еще|ещё|свой) [вопрос*] * || fromState = ../../, onlyThisState = true
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["Math_game_undefined_try_again_no"]["a1"]) }}
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["Math_game_undefined_try_again_no"]["a2"]) }}
                    go!: ../../../Math game question

            state: Math game question answer dontknow
                q: * ($agree/$disagree/не хочу/не буду/не понимаю/не надо/подскажи/помоги/уже спрашивал/повторяешься/(скажи/какой) * ответ/сам (ответь/отвечай))  *
                script:
                    $response.action = 'ACTION_EXP_SPEECHLESS';
                a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_answer_dontknow"]["a1"] )}}
                a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_answer_dontknow"]["a2"] )}}
                go!: ../../Next question enquire

###############################################################################
###############   Реакция на правильный/неправильный ответы      ##############
###############################################################################

        state: RightAnswer
            script:
                if ($client.mathLevel && typeof $client.mathLevel.rightAnswers != 'undefined') {
                } else { defineMathLevel(); }
                $client.mathLevel.rightAnswers += 1;
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["RightAnswer"]) }}
            go!: ../Next question enquire

        state: WrongAnswer
            script:
                if ($client.mathLevel && typeof $client.mathLevel.wrongAnswers != 'undefined') {
                } else { defineMathLevel(); }
                $client.mathLevel.wrongAnswers += 1;
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["WrongAnswer"]["forRandom"]) }}
            if: ($session.lastQuiz && $session.lastQuiz.result)
                a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["WrongAnswer"]["a1"])}}
            go!: ../Next question enquire

###############################################################################
###############       Предлагаем ещё один вопрос                 ##############
###############################################################################

        state: Next question enquire
            script:
                $temp.ask = selectRandomArg("yes","no","no","no","no","no","no");
            if: ($temp.ask == "yes")
                script:
                    $response.action = 'ACTION_EXP_WINK';
                a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Next_question_enquire"]["forRandom"]) }} || question = true
            else:
                go!: ../Math game question

            state: Next question enquire yes
                q: * [давай] ($agree|дальше|след*|ещё|еще|игра*|поехали|задава*|готов|продолж*|попробу*) [вопрос|вопросик] *
                q: * [$dontKnow] [давай] [следующий|другой|еще|ещё|свой] вопрос* *
                q: * ($agree|$maybe|$sure) [$agree|$maybe|$sure] * || fromState = .., onlyThisState = true
                q: * [даже] $dontKnow ($agree|$maybe|$sure) [$agree|дальше|след*|другой|еще|ещё|свой] [вопрос*] *
                q: (вопрос*/спрашивай)
                q: * [давай] ($agree|дальше|след*|другой|еще|ещё|свой) [вопрос*] || fromState = "../../Math game question/Math game question undefined"
                q: * давай [следующий|другой|еще|ещё|свой] вопрос* * || fromState = "../../Math game question/Math game question undefined"
                script:
                    $response.action = "ACTION_EXP_HAPPY";
                a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Next_question_enquire"]["Next_question_enquire_YES"]) }}
                go!: ../../Math game question

###############################################################################
###############             Остановка игры                       ##############
###############################################################################

    state: Math game pudding stop
        q: * $stopGame * || fromState = "../Math game pudding start"
        q: * ($disagree|$stopGame|достаточно) * || fromState ="../Math game pudding start/Next question enquire"
        q: * [давай] * перерыв *
        q: * [я] *устал* * 
        a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["forRandom"]) }}
        script:          
            $temp.right = $client.mathLevel.rightAnswers;
             
            $temp.ru__answers = $temp.right % 10 === 1 && $temp.right % 100 !== 11 ? 'вопрос' : ($temp.right % 10 >= 2 && $temp.right % 10 <= 4 && ($temp.right % 100 < 10 || $temp.right % 100 >= 20) ? 'вопроса' : 'вопросов');
        if: ($temp.right == 0)
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["ifUserHas_NOT_RightAnswers"]) }}
        else:
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["ifUserHasRightAnswers"]["a1"]) }}
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["ifUserHasRightAnswers"]["forRandom"]) }}                
        script:
            $session.lastQuiz = undefined;

        state: Math game again
            q: * [$agree] * (еще|ещё|продолж*) [сыгр*|игр*|поигр*] * 
            q: * {[хоч*|давай*] (еще|ещё|снова|заново) [раз] [сыгра*|*игра*] [дальше]} * || fromState = .., onlyThisState = true
            q: * {(хоч*|давай*) (сначала|[c] начала|продолжим|продолжить|дальше) [сыгра*|*игра*]} * || fromState = .., onlyThisState = true
            q: * { [хоч*|давай*] (сыгра*|*игра*) } * || fromState = .., onlyThisState = true           
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["Math_game_again"]) }}
            go!: ../../Math game pudding start/Math game question

        state: Math game again question
            q: * [$agree] * (еще|ещё) вопрос* *  
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["Math_game_again_question"]) }}
            go!: ../../Math game pudding start/Math game question    

    state: Math game repeat
        q: * {(повтори*|ска*|зада*) вопрос [(еще|ещё) раз] [пожалуйста|плиз]}* || fromState = "../Math game pudding start"
        q: * {(повтори*|ска*|зада*) * [пожалуйста|плиз]} * || fromState = "../Math game pudding start"
        q: * (чего|что|(еще|ещё) раз) * || fromState = "../Math game pudding start"
        if: ($session.lastQuiz)
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_repeat"]) }}
            a: {{$session.lastQuiz.text}} || question = true
            go: ../Math game pudding start/Math game question
        else:
            go!: ../Math game pudding start/Math game question

    state: Math game howDoYouKnow
        q: * {$you * откуда * знаешь} * || fromState = "../Math game pudding start"
        a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_howDoYouKnow"]) }}
        if: ($session.lastQuiz && $session.lastQuiz.text)
            a: {{$session.lastQuiz.text}}            
            go: ../Math game pudding start/Math game question
        else:
            go!: ../Math game pudding start/Math game question

###############################################################################
###############             Смена уровня сложности               ##############
###############################################################################

    state: Math game question too difficult
        q: * (очень|слишком) сложн* *
        q: * (полегче|попроще|{(есть|хочу|давай|еще) * (*проще/*легче)}|более прост*) *
        q: жесть
        script:
            if ($client.mathLevel) {
                $client.mathLevel.wrongAnswers = 10;
            }
        a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_question_too_difficult"] )}}
        go!: ../Math game pudding start/Math game question
            
    state: Math game question too easy
        q: * (очень|слишком) прост* *
        q: * (посложнее|{(есть|хочу|давай|еще) * *сложнее}|более сложн*|{сложност* * (выше|повыше)}) *
        script:
            if ($client.mathLevel) {

                $client.mathLevel.testIsNeeded = false;
                $client.mathLevel.level = $client.mathLevel.level + 1 <= 3 ? $client.mathLevel.level + 1 : $client.mathLevel.level;
            }
        a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_question_too_easy"] )}}
        go!: ../Math game pudding start/Math game question
