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

require: names.csv
  name = NamesEn
  var = NamesEn
require: animalsEn.csv
  name = Animals
  var = $Animals

require: patternsEn.sc
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
        q!: * {(teach me|i ca n't| i do n't know) * (count|math*)} *
        q!: [I] [want/will/i ll/ wanna] teach you [mental] math*
        q!: let 's count
        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_alternative_enter"])}}
        go!: ../Math game pudding start/Math game question

    state: Math game user start
        q!: * [i] [want [to]| wanna | let 's|let us] play* * {[game|quiz] [mental] (math*|arith*)} [with] [me] *

        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_user_start"]["forRandom"])}}
        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_user_start"]["followingQues"])}} || question = true
        go!: ../Math game pudding start

    state: Math game suggest quiz
        q!: suggest maths quiz
        a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_suggest_quiz"]["a1"])}} || question = true
        go!: ../Math game pudding start

    state: Math game pudding start
        
        state: Math game no
            q: * ($disagree|$notNow|$no help) * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) * || fromState = "/Games/How to play Math", onlyThisState = true
            a: {{selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_no"])}}

        state: Math game yes
            q: * ($agree|* try|help|come on| (let 's|let us) (play|do it)|(i will/i ll) [help] [you]) *|| fromState = .., onlyThisState = true
            q: * [$agree] * ($agree|* try|help|come on| (let 's|let us) (play|do it))|| fromState = "/Games/How to play Math", onlyThisState = true
            q!: * [want] (game|play) * (math*|arith*)
            q: * ([mental] math*|arithmetic*) *|| fromState = /Games, onlyThisState = true
            q: * [$agree|$maybe] * ([mental] math*|arithmetic*) * [$agree|$maybe] *|| fromState = /PlayGames/Games, onlyThisState = true
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
                q: * [$maybe] [it 's|[it] is] $Number [$maybe]  *
                q: * [$maybe] [it 's|[it] is] $Number [$maybe]  * || fromState = "../Math game question undefined", onlyThisState = true

                script:
                    $temp.answer = $parseTree.CalcQuestion ?  $parseTree.CalcQuestion[0].CalcNumber[0].value : $parseTree._Number;
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
                        $temp.nextState = "Math game question"
                go!: ../../{{$temp.nextState}}

            state: Math game question answer or
                q: * [$maybe] $Number::Number * (or) * $Number::Number2 [$maybe] *
                q: * [$maybe]  (one or other|first or second|{former or latter}|neither|not one not other|nothing) [$maybe] *
                if: $parseTree._Number == $parseTree._Number2
                    go!: ../Math game answer number
                else:
                    if: ($session.barrier && $session.barrier == "Math game question answer or")
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
                q: * [very/not] much *
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
                q: * $dontKnow  * $Number or $Number *
                q: * $Number or $Number * $Number *
                q: * ($dontKnow|(ca n't/can not)|[too/very] difficult) [for] [me] *

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
                q: * (i (ca n't/can not) count/ (i 'm/i am) (no good/$bad) * (count*|math*|arith*))  *
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
                    q:  * [$agree] * (one more|(try|once) * again) [try|guess] * || fromState = .., onlyThisState = true
                    q: $Text || fromState = .., onlyThisState = true
                    q: *  * [$agree] * (one more|(try|once) * again) [try|guess] * || fromState = .., onlyThisState = true

                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["Math_game_undefined_try_again"]) }}
                    a: {{$session.lastQuiz.text}} || question = true
                    go: ../../../Math game question

                state: Math game undefined try again no
                    q: * {(wrong/incorrect/$bad/$stupid) * question} * || fromState = .., onlyThisState = true
                    q: *  (next/other/different/another|$disagree) [question*] *
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["Math_game_undefined_try_again_no"]["a1"]) }}
                    a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Math_game_question"]["Math_game_question_undefined"]["Math_game_undefined_try_again_no"]["a2"]) }}
                    go!: ../../../Math game question

            state: Math game question answer dontknow
                q: * ($agree/$disagree/(do not|do n't) want/hint/tip/{already * asked}/help me/you answer)  *
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
                q: * ($agree|next|move on|another|other|different|ready*|more) [question*] *
                q: * (let 's|let us|(let|give) me) [next|other|another|different|your*|more] [question*] *
                q: * ($agree|$maybe) [$agree|$maybe] * || fromState = .., onlyThisState = true
                q: (ask/quiz/question)
                q: * $dontKnow ($agree|$maybe) [$agree|next|move on|another|other|different|ready*|more] [question*] *
                q: * ($agree|next|move on|another|other|different|ready*|more) [question*] || fromState = "../../Math game question/Math game question undefined"
                q: * $dontKnow ($agree|$maybe) [$agree|next|move on|another|other|different|ready*|more] [question*] * || fromState = "../../Math game question/Math game question undefined"
                script:
                    $response.action = "ACTION_EXP_HAPPY";
                a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_start"]["Next_question_enquire"]["Next_question_enquire_YES"]) }}
                go!: ../../Math game question

###############################################################################
###############             Остановка игры                       ##############
###############################################################################

    state: Math game pudding stop
        q: * $stopGame * || fromState = "../Math game pudding start"
        q: * ($disagree|$stopGame|enough) * || fromState ="../Math game pudding start/Next question enquire"
        q: * [let 's|let us] * [make] [a] break *
        q: * [i] * (tired|bored|boring) *
        q: * (need/let 's/want/wanna) * (break/finish/end/stop) [* game] *
        q: * (give|want to|wanna|need) * [have] [me|us] * rest *
        a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["forRandom"]) }}
        script:
            $temp.right = $client.mathLevel.rightAnswers;
             
            $temp.ru__answers = $temp.right % 10 === 1 && $temp.right % 100 !== 11 ? 'question' : ($temp.right % 10 >= 2 && $temp.right % 10 <= 4 && ($temp.right % 100 < 10 || $temp.right % 100 >= 20) ? 'questions' : 'questions');
        if: ($temp.right == 0)
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["ifUserHas_NOT_RightAnswers"]) }}
        else:
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["ifUserHasRightAnswers"]["a1"]) }}
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["ifUserHasRightAnswers"]["forRandom"]) }}
        script:
            $session.lastQuiz = undefined;

        state: Math game again
            q: * [$agree] * [let 's/let us/i want [to]] * [play] (again/another game/one more game/more)
            q: * $agree *
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["Math_game_again"]) }}
            go!: ../../Math game pudding start/Math game question

        state: Math game again question
            q: * [$agree] * ([one] more|again) question* *
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_pudding_stop"]["Math_game_again_question"]) }}
            go!: ../../Math game pudding start/Math game question

    state: Math game repeat
        q: * {(repeat*|say|ask) * [question] [again] [please] } || fromState = "../Math game pudding start"
        q: * {(repeat*|say|ask) * [question] [again] [please] } || fromState = "../Math game pudding start"
        q: * (what|$no ~understand|come again) * || fromState = "../Math game pudding start"
        if: ($session.lastQuiz)
            a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_repeat"]) }}
            a: {{$session.lastQuiz.text}} || question = true
            go: ../Math game pudding start/Math game question
        else:
            go!: ../Math game pudding start/Math game question

    state: Math game howDoYouKnow
        q: * (how|why) do $you know * || fromState = "../Math game pudding start"
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
        q: * [too|very] difficult [for me] *
        q: * [$you] [have] [some*] (simpler|easier) [question*|task*|problem*|level*] *
        script:
            if ($client.mathLevel) {
                $client.mathLevel.wrongAnswers = 10;
            }
        a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_question_too_difficult"] )}}
        go!: ../Math game pudding start/Math game question
            
    state: Math game question too easy
        q: * [too|very] (simple|easy) [for me] *
        q: * [$you] [have] [some*] [more] (difficult|complicated) [question*|task*|problem*|level] *
        q: * next level [question*]
        script:
            if ($client.mathLevel) {

                $client.mathLevel.testIsNeeded = false;
                $client.mathLevel.level = $client.mathLevel.level + 1 <= 3 ? $client.mathLevel.level + 1 : $client.mathLevel.level;
            }
        a: {{ selectRandomArg($MathGameAnswers["Math_game"]["Math_game_question_too_easy"] )}}
        go!: ../Math game pudding start/Math game question          