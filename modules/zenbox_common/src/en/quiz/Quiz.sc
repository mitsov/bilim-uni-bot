require: quiz.js

require: questions/quiz_football.csv
  name = QuizFootball
  var = $QuizFootball

require: number/number.sc
  module = zb_common

require: patternsEn.sc
  module = zb_common


theme: /Quiz

    state: Quiz user initiates
        q!: * [i] [want|wanna|let 's|let us] * (play/turn on/start/launch) * [football/soccer] quiz *
        q!: (football/soccer) quiz
        script: 
            $session.questionsCounter = 0;
            $session.rightAnswersCounter = 0;
            $session.lastRoundResults = 0;
        if: $session.currentQuestionInfo
            #если в пределах текущей сессии уже играли
            go!: ../Quiz game/Get question
        else: 
            go!: ../Quiz welcome
    state: Quiz welcome
        a: It's a quiz time! The rules are simple: I ask you a quiestion, give you response options, and your task is to chose the correct one. The quiz is divided into rounds of five questions. At the end of each round I will show your score. Let's start!
        #a: Давайте сыграем в викторину! Правила просты: я задаю вопрос, предлагаю варианты ответа, а вы отвечаете. Викторина поделена на раунды по 5 вопросов. В конце каждого раунда подсчитываются результаты. Ну что же, приступим!
        go!: ../Quiz game/Get question

    state: Quiz game
        state: Get question
            if: $session.questionsCounter !==0 && $session.questionsCounter % 5 === 0 && !$temp.fromResults && !$temp.fromSkipQuestion
                go!: /Quiz/Results/Round

            script:
                $session.quizUndefinedCounter = 0;
                var success = getQuestion();
                if (success && $session.currentQuestionInfo) {
                    $session.questionsCounter++;
                    $reactions.transition("../Ask question");
                } else {
                    $reactions.answer("Oops! Some error occured...");
                    $reactions.transition('/');
                }
        state: Ask question
            a: {{ $session.currentQuestionInfo.question }}
            random:
                a: Response options:
                #a: Варианты ответов:
                a: 
                a: 
                a: 
            a: {{ ($session.currentQuestionInfo.responseOptionsForQuestion) }}
            go: ../Get answer


        state: Get answer
            state: Number of answer
                # первый вариант; последний ответ; второй ответ и тд
                q: * {([is] [the] ( $Number | (last [one/option]):last)) * (right/option/answer/choice/maybe/number/probably)} *
                q: [the] ( $Number [one/option]| (last [one/option]):last)

                if: $session.currentQuestionInfo.answerIsNumber && $parseTree.value != "last"
                    # если ответ - это число. Напр., вопр.:"Сколько раз бразилия становилась чемпионом?" отв.: 5
                    if: 1 <= Number($parseTree.value) && Number($parseTree.value) <= $session.currentQuestionInfo.responseOptions.length
                        script:
                            $temp.results = checkAnswer($parseTree.value, $session);
                        script:
                            $temp.maybeIndexOrAnswer = $session.currentQuestionInfo.responseOptions.reduce(function(res, el){
                               if (res) return true
                               var indexStart = el.search(/.\)\s/g)+2;
                               var subStr = el.substring(indexStart);
                               subStr = subStr.substring(subStr.search(/\d/g));

                               return Number($parseTree.value) == parseInt(subStr)
                            }, false);

                        if: $temp.maybeIndexOrAnswer
                            go!: /Quiz/Clarify misunderstandings

                        if: $session.currentQuestionInfo.rightAnswerNumber == $parseTree.value
                            go!: /Quiz/The answer is correct
                            
                        script:
                            $temp.indexIsWrong = true;
                    else:


                        script:
                            $temp.results = checkAnswer($parseTree.value, $session);
    
                        if: $temp.results == 'right' || $session.currentQuestionInfo.answerIsNumber ===  $parseTree.value
                            go!: /Quiz/The answer is correct
                    if: $temp.results == 'wrong' || $temp.indexIsWrong
                        go!: /Quiz/The answer is incorrect

                    a: I don't understand. Please, chose one of given answers.
                    #a: Не понял. Выберите, пожалуйста, вариант ответа из предложенных.
                    go: ../../Get answer

                else:
                    if: $parseTree.value == "last" && ($session.currentQuestionInfo.rightAnswerNumber == $session.currentQuestionInfo.responseOptions.length)
                        # Пользователь:"последний вариант" И порядковый номер верного ответа РАВЕН кол-ву всех вариантов
                        go!: /Quiz/The answer is correct

                    if: $session.currentQuestionInfo.rightAnswerNumber === Number($parseTree.value)
                        go!: /Quiz/The answer is correct

                    go!: /Quiz/The answer is incorrect


            state: Probably answer
                q: *
                script:
                    $temp.results = checkAnswer($parseTree.text, $session);
                if: $temp.results == 'right'
                    go!: /Quiz/The answer is correct

                if: $temp.results == 'wrong'
                    go!: /Quiz/The answer is incorrect
                
                script: 
                    $session.quizUndefinedCounter++
                if: $session.quizUndefinedCounter == 1
                    a: I don't understand. Please, chose one of given answers.
                    #a: Не понял. Назовите, пожалуйста вариант ответа.
                if: $session.quizUndefinedCounter == 2
                    a: Try to chose one of these answer:
                    #a: Попробуйте выбрать ответ из предложенных:
                    a: {{ ($session.currentQuestionInfo.responseOptionsForQuestion) }}

                if: $session.quizUndefinedCounter == 3
                    go!: /Quiz/Skip question 
                go: ../../Get answer 



    state: Clarify misunderstandings
        a: Please indicate if {{ $parseTree.value }} is your answer or the number of answer. 
        #a: Уточните пожалуйста: {{ $parseTree.value }} – это ответ или номер ответа?
        script:
            $session.memorizeQuizNum = Number($parseTree.value);
        state: Answer
            q: * answer *
            if: $session.currentQuestionInfo.answerIsNumber && Number($session.currentQuestionInfo.answerIsNumber) === $session.memorizeQuizNum
                go!: /Quiz/The answer is correct
            go!: /Quiz/The answer is incorrect

        state: Number of answer
            q: * number* * [answer] *
            if: $session.currentQuestionInfo.rightAnswerNumber == $session.memorizeQuizNum
                go!: /Quiz/The answer is correct
            go!: /Quiz/The answer is incorrect
                
    state: Skip question
        q: * || fromState = "/Quiz/Clarify misunderstandings"
        q: * {(skip/next/another/other) question} * || fromState = /Quiz
        q: (skip/next) || fromState = /Quiz
        a: The correct answer is: {{ $session.currentQuestionInfo.rightAnswer }}. Let's move to the next question!
        script:
            $temp.fromSkipQuestion = true;
            $session.questionsCounter--;
        go!: /Quiz/Quiz game/Get question

    state: The answer is correct
        random:
            a: It's the right answer!
            a: Well done!
            a: Good job!

        script: 
            $session.rightAnswersCounter++;

        go!: /Quiz/Quiz game/Get question


    state: The answer is incorrect
        if: $temp.indexIsWrong
            a: The answer number {{ $parseTree.value }} is wrong.
        else:
            random:
                a: Unfortunately you're wrong.
                a: Not correct.
        if: $session.currentQuestionInfo.answerText
            a: {{ $session.currentQuestionInfo.answerText }}
        else:
            a: The correct answer is: {{ $session.currentQuestionInfo.rightAnswer }}.

        go!: /Quiz/Quiz game/Get question

    state: Dont know
        q: * ((how/why) (do/would) i know/idk/ (dont/do not/ do n't/ don 't) know ) * || fromState = "/Quiz"
        q: $dontKnow || fromState = "/Quiz"
        a: Try chose one answer at random!
        go!: /Quiz/Quiz game/Get answer

    state: Repeat
        q: * {((read/show) [me/us]/repeat/what was the) * [question*/option*]  [again]} * || fromState = "/Quiz"
        go!: /Quiz/Quiz game/Ask question

    state: Results

        state: Round
            a: The round is completed! {{ correctAnswersAmount($session.rightAnswersCounter - $session.lastRoundResults, 5) }}.
            script:
                $session.lastRoundResults = $session.rightAnswersCounter;

            a: Shall we go on?

            state: Continue the game
                q: * ($agree/go on/continue) *
                script:
                    $temp.fromResults = true
                go!: /Quiz/Quiz game/Get question
            
        state: Whole game
            q: * {(how many/what is my) * (ball*/(correct/right) answer*/result*/score)} * || fromState = "/Quiz"
            q: * (result*/score) * || fromState = "/Quiz"
            go!: ./Somewhere in a game
            state: End of the game
                a: The results of the whole game: {{ correctAnswersAmount($session.rightAnswersCounter, $session.questionsCounter) }}.
                go!: /

            state: Somewhere in a game
                a: The results of the whole game: {{ correctAnswersAmount($session.rightAnswersCounter, $session.questionsCounter) }}.
                a: Shall we go on?

                state: Continue the game
                    q: * ($agree/go on/continue) *
                    if: $session.questionsCounter !==0 && $session.questionsCounter % 5 === 0
                        script:
                            $temp.fromResults = true
                        go!: /Quiz/Quiz game/Get question

                    else:
                        a: Ok. So what is your answer?
                        go!: /Quiz/Quiz game/Get answer

                state: Finish the game
                    q: * ($disagree/(not/'t) (go on/continue)/bored/tired) *
                    script:
                        $temp.fromResults = true
                    go!: /Quiz/Stop the game

        state: CatchAll local
            q: * || fromState = /Quiz/Results
            a: I'm not quite following you. Shall we go on?
            if: $session.lastRoundResults = $session.rightAnswersCounter;
                go: ../Round
            go: ../Whole game/Somewhere in a game

    state: Stop the game
        q: * ($stopGame/(turn* off/stop/end) [the] (game/quiz)) * || fromState = "/Quiz/Quiz game"
        q: * ($disagree/(not/'t) (go on/continue)/bored/tired/enough) * || fromState = "../Results/Round"
        a: Okay. Let's finish!
        if: !$temp.fromResults
            go!:  ../Results/Whole game/End of the game
        go!: /

    #state: Test
    #    q!: test $Number::QuestionID
    #    script:
    #        $session.questionsIDs = [Number($parseTree._QuestionID),Number($parseTree._QuestionID),Number($parseTree._QuestionID)]
    #    go!:  /Quiz/Quiz game/Get question