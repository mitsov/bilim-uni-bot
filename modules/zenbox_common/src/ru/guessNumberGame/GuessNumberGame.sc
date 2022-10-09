require: GuessNumberGame.js

require: ../Games.sc

require: answers.yaml
  var = GuessNumberGameCommonAnswers

init:
    $global.themes = $global.themes || [];
    $global.themes.push("Guess Number Game");

    $global.$GuessNumbAnsw = (typeof GuessNumberGameCustomAnswers != 'undefined') ? applyCustomAnswers(GuessNumberGameCommonAnswers, GuessNumberGameCustomAnswers) : GuessNumberGameCommonAnswers;
theme: /Guess Number Game

    state: Guess
        a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["a"]) }}

        state: Incorrect
            q: * (это не (то/$Number)/не $Number/неправильно/не (правильно/верно/угадал)/{(загадывал/было) * (другое/не (это/то/$Number)) [числ*]}) * || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["Incorrect"] ) }}
            go!: /StartDialog

        state: Yes
            q!: * [играть/сыграем/сыгрыть/играем/поиграем/поиграть/игра] * {(отгадай*/разгадай*/угадай*/загадаю/хочу загадать/буду загадывать/загадываю/(можешь/умеешь/будешь) (отгад*/разгад*/загадать/угад*)/попробую отгадать/отгадывать/угадывать/я дала) * [тебе] (числ*/цифры)} *
            q: * ($agree/попробую/попробовать/отгадай/отгадывай/загадал/загадаю/(еще одно/другое/новое) [числ*]) * || fromState = .., onlyThisState = true
            q: * {[ты/сам] (отгадай*/отгадывай*/отгадаешь/загадаю/{(буду/хочу) загадывать}/{(можешь/умеешь/будешь) отгад*}) * [числ*]} * || fromState = "../../Make a guess", onlyThisState = true
            q: * {[ты/сам] (отгадай*/отгадывай*/отгадаешь/загадаю/{(буду/хочу) загадывать}/{(можешь/умеешь/будешь) отгад*}) * [числ*]} * || fromState = "../../Make a guess/Yes", onlyThisState = true            
            q: * [$agree] ($agree|начин*|начн*|помогу|постараюсь|стараться|попытаюсь|попробую) * || fromState = "/Games/How to play GuessNumber", onlyThisState = true   
            q: * {[ты/сам] (отгадай*/отгадывай*/отгадаешь/загадаю/{(буду/хочу) загадывать}/{(можешь/умеешь/будешь) отгад*}) * [числ*]} * || fromState = "/Guess Number Game", onlyThisState = true      
            q: число || fromState = "/Guess Number Game", onlyThisState = true
            q: число || fromState = /Games, onlyThisState = true
            q: меняемся || fromState = "../../Make a guess/Yes", onlyThisState = true
            script:
                $session.lastQuiz = {
                    number: null,
                    less: 101,
                    more: 0,
                    attempts: 0
                };
                guessNumber();
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["question"] ) }}

            state: Correct
                q: * (угадал/это оно/точно/правильно/верно/загад* это число/не больше и не меньше/в яблочко/тебе [просто] повезло/как ты (угадал/узнал)/откуда ты (знаешь/знал)/$clever) * || fromState = .., onlyThisState = true
                q: $agree [я загад* $Number] || fromState = .., onlyThisState = true
                script:
                    $temp.attempts = $session.lastQuiz.attempts % 10 === 1 && $session.lastQuiz.attempts % 100 !== 11 ? 'попытка' : ($session.lastQuiz.attempts % 10 >= 2 && $session.lastQuiz.attempts % 10 <= 4 && ($session.lastQuiz.attempts % 100 < 10 || $session.lastQuiz.attempts % 100 >= 20) ? 'попытки' : 'попыток');
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["Correct"]["random"] ) }}
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["Correct"]["a1"]) }}
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["Correct"]["a2"]) }}
                go: ../../../Guess

            state: MoreOrLess
                q: * ((*больше|{(прибавь|увеличь|добавь|приплюсуй) [еще] [$Number]}|не (так/настолько/такое) [же] (мало/маленькое/меньше)|не меньше):0|(*меньше|{(отними/вычти/убавь) [еще] [$Number]}|не (так/настолько/такое) [же] (много/большое/больше)|не больше):1|(намного *больше/слишком маленькое):2|(намного *меньше/слишком большое):3) * || fromState = .., onlyThisState = true
                q: ((большая|большие|боль|боли шея):0|(мент|мельниц*|меньшая|мень ши|милици*):1) || fromState = .., onlyThisState = true
                script:
                    checkGuessResult($parseTree._Root);
                go!: ./{{$temp.nextState}}

                state: CantBe
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["CantBe"]["random"] ) }}
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["CantBe"]["a1"]) }}
                    go: ../../../Yes

                state: NextAttempt
                    if: ($session.lastQuiz.less - $session.lastQuiz.more < 4)
                        a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["NextAttemp"]["if"] ) }}
                    else:
                        if: ($session.lastQuiz.less - $session.lastQuiz.more < 10)
                            random:
                                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["NextAttemp"]["else"] ) }}
                                a: 
                    script:
                        guessNumber();
                        getNumberFact($session.lastQuiz.number);  
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["NextAttemp"]["random"] ) }}
                    random:
                        a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["NextAttemp"]["moreOrLess"] ) }}
                        a: 
                        a: 
                        a: 
                    go: ../../../Yes

                state: IKnow
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["IKnow"]["random"] ) }}
                    script:
                        guessNumber();
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["IKnow"]["a1"] ) }}
                    script:
                        $temp.attempts = $session.lastQuiz.attempts % 10 === 1 && $session.lastQuiz.attempts % 100 !== 11 ? 'попытка' : ($session.lastQuiz.attempts % 10 >= 2 && $session.lastQuiz.attempts % 10 <= 4 && ($session.lastQuiz.attempts % 100 < 10 || $session.lastQuiz.attempts % 100 >= 20) ? 'попытки' : 'попыток'); 
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["IKnow"]["a2"]) }}
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["IKnow"]["a3"]) }}
                    go: ../../../../Guess

            state: CatchAll || noContext = true
                q: * [$disagree/еще/не (угадал/правильно/верно/это число)] * || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["CatchAll"] ) }}

            state: Repeat || noContext = true
                q: * {(какое/повтори*/что за/называл/говорил) * число} * || fromState = .., onlyThisState = true
                q: * {[повтори] * вопрос} || fromState = .., onlyThisState = true
                q: что || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["Repeat"]["a1"] ) }}
                random:
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["Repeat"]["a2"] ) }}
                    a: 
                    a: 
                    a:                            

            state: Stop
                q: * $stopGame * || fromState = .., onlyThisState = true
                q: * $me число $Number * || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["Stop"]["a1"] ) }}
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessYes"]["Stop"]["a2"] ) }}

        state: No
            q: * ($disagree/$notNow) * || fromState = .., onlyThisState = true
            q: * ($disagree|$notNow) [* (о|об) $Text] * || fromState = "/Games/How to play GuessNumber", onlyThisState = true             
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Guess"]["GuessNo"] ) }}

    state: Make a guess
        a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["a1"]) }}

        state: Yes
            q!: * [играть/сыграем/сыгрыть/играем/поиграем/поиграть/игра] * {(загадай*/загадывай*/загадывать/разгадаю/загадать/загадаем/гадаем/загадаешь/загадывает/дай/задай/буду (отгадывать/угадывать/разгадывать)/{(можешь/умеешь/будешь) загад*}) * [мне] [еще] (числ*/цифры)} *        
            q: * ($agree/попроб*/загадай*/загадывай*/другое/отгадаю) * || fromState = .., onlyThisState = true
            q: * {[ты/сам] ({хочу чтобы ты загадал}/загадай*/загадывай*/отгадаю/угадаю/разгадаю/{(могу/умею/буду) (отгад*/разгад*/угад*)}/{(можешь/умеешь/будешь) загад*}) * [числ*]} * || fromState = ../../Guess, onlyThisState = true
            q: * для меня * || fromState = ../../Guess, onlyThisState = true            
            q: * {[ты/сам] ({хочу чтобы ты загадал}/загадай*/загадывай*/отгадаю/угадаю/разгадаю/{(могу/умею/буду) (отгад*/разгад*/угад*)}/{(можешь/умеешь/будешь) загад*}) * [числ*]} * || fromState = ../../Guess/Yes, onlyThisState = true    
            q: * {[ты/сам] ({хочу чтобы ты загадал}/загадай*/загадывай*/отгадаю/угадаю/разгадаю/{(могу/умею/буду) (отгад*/разгад*/угад*)}/{(можешь/умеешь/будешь) загад*}) * [числ*]} * || fromState = "/Guess Number Game", onlyThisState = true
            q: меняемся || fromState = ../../Guess/Yes, onlyThisState = true
            script:
                makeGuessNumber();
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["GuessYes"]["a1"] ) }}

            state: Number
                q: * [[я] говорю] $Number * || fromState = .., onlyThisState = true
                script:
                    checkGuessNumber($parseTree._Number);
                go!: ../../{{$temp.nextState}}

            state: Negative Number, noContext = true
                q: * $regexp<-\d+> * || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["GuessYes"]["NegativeNumber"]["a1"] ) }}
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["GuessYes"]["NegativeNumber"]["a2"] ) }}

            state: CatchAll || noContext = true
                q: * || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["GuessYes"]["CatchAll"] ) }}

            state: Stop
                q: * $stopGame * || fromState = .., onlyThisState = true
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["GuessYes"]["Stop"] ) }}

        state: No
            q: * ($disagree/$notNow) * || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["GuessNo"]) }}

        state: Correct
            script:
                $temp.attempts = $session.lastQuiz.attempts % 10 === 1 && $session.lastQuiz.attempts % 100 !== 11 ? 'попытка' : ($session.lastQuiz.attempts % 10 >= 2 && $session.lastQuiz.attempts % 10 <= 4 && ($session.lastQuiz.attempts % 100 < 10 || $session.lastQuiz.attempts % 100 >= 20) ? 'попытки' : 'попыток');
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["Correct"]["a1"])}}
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["Correct"]["a2"]) }}
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["Correct"]["a3"]) }}
            go: ../../Make a guess

        state: MoreForSure
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["MoreForSure"]) }}
            go: ../Yes

        state: LessForSure
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["LessForSure"]) }}
            go: ../Yes

        state: More
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["More"]["a1"]) }}
            script:
                getNumberFact($parseTree._Number);
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["More"]["a2"]) }}
            go!: ../Check Close

        state: Less
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["Less"]["a1"]) }}
            script:
                getNumberFact($parseTree._Number);
            a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["Less"]["a2"]) }}                    
            go!: ../Check Close
                
        state: Check Close
            if: (Math.abs($session.lastQuiz.number - $parseTree._Number) < 2)
                a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["Check Close"]["if"]) }}
            else:
                if: (Math.abs($session.lastQuiz.number - $parseTree._Number) < 10)
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["Check Close"]["else"]["if"]) }} 
                else:
                    a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["Make a guess"]["Check Close"]["else"]["else"]) }}  
            go: ../Yes

    state: Exit
        q: * $stopGame *
        a: {{ selectRandomArg($GuessNumbAnsw["Guess Number"]["ExitGame"] ) }}