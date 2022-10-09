require: AnimalGame.js
require: ../../common/animalGame/AnimalGame.js
require: ../../common/common.sc

require: animals.csv
  name = Animals
  var = $Animals

require: ../Games.sc

require: answers.yaml
  var = AnimalGameCommonAnswers

require: ../main.js

require: patterns.sc
  module = zb_common

require: number/number.sc
  module = zb_common

require: floatNumber/floatNumber.sc
  module = zb_common

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .AnimalTagConverter = function(parseTree) {
            var id = parseTree.Animals[0].value;
            return $Animals[id].value;
        };

    $global.themes = $global.themes || [];
    $global.themes.push("Animal quiz");

    $global.$AnimalGameAnswers = (typeof AnimalGameCustomAnswers != 'undefined') ? applyCustomAnswers(AnimalGameCommonAnswers, AnimalGameCustomAnswers) : AnimalGameCommonAnswers;

patterns:
    $quizSavagery = ((домашн*|не дик*|недик*):1|(дик*|не домашн*|недомашн*):2)
    $quizClass = ((звер*|свер):1|(птич*|птиц*):2|(насеком*):3|(рыб*):4)
    $quizNumExtremities = (({нет ног}|ни одной|без ног):0)
    $quizParts = ((крыл*):1|(плавни*):2|(рог*|рага):3|(копыт*):4|(пята*):5|(хобот*):6|(бив*):7)
    $quizCoating = ((шерст*|шесть):1|(пер*|пёр*):2|(панц*):3|(кож*):4|(чешу*):5|(игл*|игол*):6)
    $quizAll = ($Animal|$quizSavagery|$quizClass|$quizParts|$quizCoating)

    $Animal = $entity<Animals> || converter = $converters.AnimalTagConverter

    $orAnimalGame = или
    
    $NotThisAnimal = не [$preposition] $Animal || converter = function(parseTree) {return parseTree.Animal[0].value} 
    $NotThisPart = не $quizParts || converter = function(parseTree) {return parseTree.quizParts[0].value} 
    $NotThisSavagery = не $quizSavagery || converter = function(parseTree) {return parseTree.quizSavagery[0].value} 
    $NotThisCoating = не $quizCoating || converter = function(parseTree) {return parseTree.quizCoating[0].value} 
    $NotThisClass = не $quizClass || converter = function(parseTree) {return parseTree.quizClass[0].value} 

theme: /Animal quiz
    state: Animal game alternative enter
        q!: * {($tellMe|что там) * [земн*] (животн*|звер*)} *
        a: {{selectRandomArg($AnimalGameAnswers["Animal game alternative enter"])}}
        a: {{selectRandomArg($AnimalGameAnswers["Animal game suggest quiz"])}} || question = true
        go!: ../Animal game pudding start

    state: Animal game user start
        q!: * {[давай|может] * (сыграем|игра*|поигра*) * (зоо*|животн*|звер*)} *
        q!: * {(давай|хочу|хочется) * (зоо*|животн*|звер*)} *
        a: {{selectRandomArg($AnimalGameAnswers["Animal game user start"])}}
        go!: ../Animal game pudding start 

    state: Animal game suggest quiz
        q!: предложи викторину про животных
        q!: * {[я|мне] (люблю|нравит*) * (зоо*|животн*|звер*)} *
        a: {{selectRandomArg($AnimalGameAnswers["Animal game suggest quiz"])}} || question = true
        go!: ../Animal game pudding start    
            
    state: Animal game pudding start        

        state: Animal game no
            q: * ($disagree|$notNow) * || fromState = .., onlyThisState = true
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game no"])}}

        state: Animal game from rules no
            q: * ($disagree|$notNow) [* (о|об) $Text] * || fromState = "/PlayGames/Games/How to play Zoo", onlyThisState = true 
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game from rules no"]["disagree"])}}
            if: $parseTree.Text
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game from rules no"]["disagree with text"])}}

        state: Animal game yes
            q: * ($agree|начин*|начн*|помогу|стараться|попытаюсь|попробую) [начин*|начн*|поигра*|сыгра*|игра*|попробую|попробуем] * || fromState = "/Animal quiz/Animal game pudding start", onlyThisState = true
            q: * [$agree] ($agree|начин*|начн*|помогу|постараюсь|стараться|попытаюсь|попробую) * || fromState = "/Animal quiz/Animal game pudding start", onlyThisState = true
            q: * [$agree] ($agree|начин*|начн*|помогу|постараюсь|стараться|попытаюсь|попробую) * || fromState = "/PlayGames/Games/How to play Zoo", onlyThisState = true
            q!: * [хочу] (игра*|сыграть|поиграть) * в зоо* *
            q: * (зоо*|животн*|звер*|залог*|запа|парк) * || fromState = /PlayGames/Games, onlyThisState = true  
            q: * [$agree|$maybe|$sure] * (зоо*|залог*) * [$agree|$maybe|$sure] * || fromState = /PlayGames/Games, onlyThisState = true
            q!: {  зоопарк}     
            script:
                $session.lastQuiz = undefined;
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game yes"])}}
            go!: ../Animal game question    
        state: Unexpected reaction
            q: * || fromState = "../../Animal game pudding start", onlyThisState = true
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Unexpected reaction"])}}
            go!: ../../Animal game pudding start


###############################################################################
###############                   Генерация вопросов             ##############
###############################################################################

        state: Animal game question || modal = true
            script:
                if ($session.barrier) {
                    $session.barrier = undefined;
                } 
                $response.action = 'ACTION_EXP_QUESTION';           
                $session.lastQuiz = getZooQuestion();
                $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                $temp.askedforNewQueStion = false;
            a: {{$session.lastQuiz.text}} || question = true   
            go!: ./{{$temp.nextState}}

            state: Animal game question tusks
 
                state: Answer tusks
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                   

            state: Animal game question trunk
 
                state: Answer trunk
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}               

            state: Animal game question penny
 
                state: Answer penny
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                      

            state: Animal game question hoof
 
                state: Answer hoof
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                 

            state: Animal game question horns
  
                state: Answer horns
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                  

            state: Animal game question fins
 
                state: Answer fins
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                                      

            state: Animal game question wings

                state: Answer wings
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                 

            state: Animal game question coating
 
                state: Answer coating
                    q: * {[$maybe|$sure] ($NotThisCoating|$quizCoating) * [($NotThisCoating|$quizCoating)]} * :1
                    q: * {[$maybe|$sure] ($NotThisCoating|$quizCoating) * [($NotThisCoating|$quizCoating)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2 
                    script:
                        var notThisCoating = $parseTree.NotThisCoating ? $parseTree.NotThisCoating : false,
                            usersAnswer = $parseTree.quizCoating ? $parseTree.quizCoating[0].value : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.quizCoating[1] !== 'undefined' ? $parseTree.quizCoating[1] : false, 
                            right = Number(getCoatingCode($session.lastQuiz.animal)),
                            wrong = Number(getCoatingCode($session.lastQuiz.otherAnimal));
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisCoating, usersAnswer, anotherAnswer, right, wrong);
                    go!: ../../../{{$temp.nextState}}  

            state: Animal game question employment
 
                state: Answer employment
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}

            state: Animal game question parts
 
                state: Answer parts
                    q: * {[$maybe|$sure] * ($NotThisPart|$quizParts) * [($NotThisPart|$quizParts)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisPart|$quizParts) * [($NotThisPart|$quizParts)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisPart = $parseTree.NotThisPart ? $parseTree.NotThisPart : false,
                            usersAnswer = $parseTree.quizParts ? $parseTree.quizParts[0].value : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.quizParts[1] !== 'undefined' ? $parseTree.quizParts[1] : false, 
                            right = $session.lastQuiz.parts.right.value,
                            wrong = $session.lastQuiz.parts.wrong.value; 
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;  
                        operateWithAnswer_AnimalGame(notThisPart, usersAnswer, anotherAnswer, right, wrong);
                    go!: ../../../{{$temp.nextState}}  

            state: Animal game question numExtremities
 
                state: Answer numExtremities
                    q: * {[$maybe|$sure] * ($Number|$FloatNumber|$quizNumExtremities) * [$orAnimalGame]} * :1
                    q: * {[$maybe|$sure] * ($Number|$FloatNumber|$quizNumExtremities) * [$orAnimalGame]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var currentAnswer = $parseTree.Number ? $parseTree._Number : $parseTree._quizNumExtremities;
                        var right = $session.lastQuiz.animal.numExtremities;
                        if (right == currentAnswer){
                            $temp.nextState = "RightAnswer";
                        } else {
                            $temp.nextState = "WrongAnswer";
                            $temp.rightAnswer = right
                        }
                        if ($parseTree.FloatNumber) {
                            if (parseInt($parseTree.FloatNumber[0].value) == right) {                            
                                $temp.entryReason = 'User Answered FloatNumber';
                                $temp.nextState = 'Animal game question Negative';
                            } else {
                                $temp.nextState = "WrongAnswer";
                                $temp.rightAnswer = right                                
                            }
                        }
                        if ($parseTree.orAnimalGame) {
                            $temp.nextState = 'Animal game question Negative';
                        }
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                    go!: ../../../{{$temp.nextState}}    

            state: Animal game question class

                state: Answer class
                    q: * {[$maybe|$sure] * ($NotThisClass|$quizClass) * [($NotThisClass|$quizClass)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisClass|$quizClass) * [($NotThisClass|$quizClass)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisClass = $parseTree.NotThisClass ? $parseTree.NotThisClass : false,
                            usersAnswer = $parseTree.quizClass ? $parseTree.quizClass[0].value : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.quizClass[1] !== 'undefined' ? $parseTree.quizClass[1] : false, 
                            right = Number(getClassCode($session.lastQuiz.animal)),
                            wrong = Number(getClassCode($session.lastQuiz.otherAnimal));  
                         
                        operateWithAnswer_AnimalGame(notThisClass, usersAnswer, anotherAnswer, right, wrong);     
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                    go!: ../../../{{$temp.nextState}}

            state: Animal game question savagery

                state: Answer savagery
                    q: * {[$maybe|$sure] * ($NotThisSavagery|$quizSavagery) * [($NotThisSavagery|$quizSavagery)]} * :1
                    q: * {[$maybe|$sure] * ($NotThisSavagery|$quizSavagery) * [($NotThisSavagery|$quizSavagery)]} * [давай] * ([следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*]|вопрос*) * :2
                    script:
                        var notThisSavagery = $parseTree.NotThisSavagery ? $parseTree.NotThisSavagery : false,
                            usersAnswer = $parseTree.quizSavagery ? $parseTree.quizSavagery[0].value : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.quizSavagery[1] !== 'undefined' ? $parseTree.quizSavagery[1] : false, 
                            right = Number(getSavageryCode($session.lastQuiz.animal)),
                            wrong = right === 1 ? 2 : 1;  
                        $temp.askedforNewQuestion = $parseTree._Root == 1 ? false : true;
                        operateWithAnswer_AnimalGame(notThisSavagery, usersAnswer, anotherAnswer, right, wrong);   
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
                $temp.nextState = $temp.askedforNewQuestion ? "Animal game question" : "Next question enquire";
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["RightAnswer"])}}
            go!: ../{{$temp.nextState}}

        state: WrongAnswer
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["WrongAnswer"])}}
            script:
                $temp.nextState = $temp.askedforNewQuestion ? "Animal game question" : "Next question enquire";
            if: ($temp.rightAnswer)
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["tell right answer"])}}
                script:
                    $temp.rightAnswer = ""
            go!: ../{{$temp.nextState}}


###############################################################################
###############            Непонятный ответ                      ##############
###############################################################################

        state: Animal game question answer or
            q: * [$maybe|$sure] $Animal * $Animal [$maybe|$sure] * || fromState = "../Animal game question" 
            q: * [$maybe|$sure] $quizSavagery * $quizSavagery [$maybe|$sure] * || fromState = "../Animal game question"
            q: * [$maybe|$sure] $quizClass * $quizClass [$maybe|$sure] * || fromState = "../Animal game question" 
            q: * [$maybe|$sure] $quizParts * $quizParts [$maybe|$sure] * || fromState = "../Animal game question"
            q: * [$maybe|$sure] $quizCoating * $quizCoating [$maybe|$sure] * || fromState = "../Animal game question"
            q: * [$maybe|$sure] $Animal * $Animal [$maybe|$sure] * || fromState = "../Animal game question dontKnow"
            q: * [$maybe|$sure] $quizSavagery * $quizSavagery [$maybe|$sure] * || fromState = "../Animal game question dontKnow"
            q: * [$maybe|$sure] $quizClass * $quizClass [$maybe|$sure] * || fromState = "../Animal game question dontKnow"
            q: * [$maybe|$sure] $quizParts * $quizParts [$maybe|$sure] * || fromState = "../Animal game question dontKnow"
            q: * [$maybe|$sure] $quizCoating * $quizCoating [$maybe|$sure] * || fromState = ../
            q: * ([ни|или|и] [у] (то*|та|те) [ни|или|и] [у] (то*|та|те|друг*)|ничего [нет]) * || fromState = "../Animal game question"
            q: * ([ни|или|и] [у] (то*|та|те) [ни|или|и] [у] (то*|та|те|друг*)|ничего [нет]) * || fromState = "../Animal game question dontKnow"
            q: * ([ни|нет] [у|из] (них|обоих|кого)|оба|обе|никто) * || fromState = "../Animal game question"
            if: ($session.barrier && $session.barrier == "Animal game question dontKnow")
                script:
                    $temp.correct = getRightAnswer();
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["tell right answer and go next"])}}
                go!: ../Animal game question
            else:
                script:
                    $session.barrier = "Animal game question dontKnow";
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question answer or"])}}
                a: {{$session.lastQuiz.text}} || question = true
                go!: ../Animal game question/{{$temp.nextState}}   

        state: Animal game question Negative
            q: * [$sure] не [$preposition] $quizAll * || fromState = "../Animal game question"
            if: ($session.barrier && $session.barrier == 'Animal game question Negative')
                script:
                    $temp.correct = getRightAnswer();
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["tell right answer and go next"])}}
                go!: ../Animal game question
            else:
                script:
                    $response.action = 'ACTION_EXP_WINK';
                    $session.barrier = 'Animal game question Negative';
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;

                if: $temp.entryReason === 'User Answered FloatNumber'
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question FloatNumber"])}}
                    a: {{$session.lastQuiz.text}} || question = true  

                if: $temp.entryReason === 'User answered more then one answer'
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question More than one answer"])}}
                    a: {{$session.lastQuiz.text}} || question = true  

                if: $temp.entryReason === 'User answered not stated variant'
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["User answered not stated variant"])}}
                    a: {{$session.lastQuiz.text}} || question = true    

                if: !$temp.entryReason
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]['Animal game question Negative'])}}
                    a: {{$session.lastQuiz.text}} || question = true   

                go!: ../Animal game question/{{$temp.nextState}}  

        state: Animal game question dontKnow
            q: * $dontKnow  * $quizAll или $quizAll *  || fromState = "../Animal game question" 
            q: * $quizAll или $quizAll * $dontKnow * || fromState = "../Animal game question"
            q: * $dontKnow * || fromState = "../Animal game question"
            q: * $dontKnow * [давай] * [следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*] [вопрос*] * || fromState = "../Animal game question"
            q: у кого || fromState = "../Animal game question"
            q: * || fromState="../../../Animal quiz"

            if: ($session.barrier && $session.barrier == "Animal game question dontKnow")
                script:
                    $temp.correct = getRightAnswer();
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["tell right answer and go next"])}}
                go!: ../Animal game question
            else:
                if: $session.lastQuiz
                    script:
                        $response.action = 'ACTION_EXP_WINK';
                        $session.barrier = "Animal game question dontKnow";
                        $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question dontKnow"])}}
                    a: {{$session.lastQuiz.text}} || question = true                     
                    go!: ../Animal game question/{{$temp.nextState}} 

                else:
                    script:
                        var result = $nlp.match($parseTree.text, '/');
                        $temp.nextState = result.targetState;
                        $parseTree = result.parseTree;
                    go!: {{$temp.nextState}}                


        state: Animal game question many extremities 
            q: * (много|мало|нисколько) * || fromState = "../Animal game question/Animal game question numExtremities"
            if: ($session.barrier && $session.barrier == "Animal game question many extremities")
                script:
                    $temp.correct = getRightAnswer();
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["tell right answer and go next"])}}
                go!: ../Animal game question
            else:
                script:
                    $session.barrier = "Animal game question many extremities";
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question many extremities"])}}
                a: {{$session.lastQuiz.text}} || question = true
                go!: ../Animal game question/{{$temp.nextState}}  
    
        state: Animal game question undefined
            q: (*|$agree|$disagree) || fromState = "../Animal game question"
            q: * || fromState = "../Animal game question"
            if: ($session.barrier && $session.barrier == "Animal game question")
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question undefined"]["barrier"])}}
                go!: ../Animal game question
            else:
                script:
                    $response.action = 'ACTION_EXP_SPEECHLESS';
                    $session.barrier = "Animal game question";
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question undefined"]["no barrier"])}}
         
            state: Animal game undefined try again
                q: * [$agree] * { (еще|ещё) [одну|один] [раз|попытку] [попробую|попробуем] } * || fromState = .., onlyThisState = true
                q: * [$agree] * { (попробую|попробуем|попытку) [еще|ещё] [одну|один] [раз] } * || fromState = .., onlyThisState = true
                q: * [$agree] * { (еще|ещё) [одну|один] [раз|попытку] [попробую|попробуем] } * || fromState = "../Animal game undefined try again yes", onlyThisState = true
                q: * [$agree] * { (попробую|попробуем|попытку) [еще|ещё] [одну|один] [раз] } * || fromState = "../Animal game undefined try again yes", onlyThisState = true
                q: (вопрос/давай) || fromState = .., onlyThisState = true  
                script:
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question undefined"]["Animal game undefined try again"])}}
                a: {{$session.lastQuiz.text}} || question = true                    
                go!: ../../Animal game question/{{$temp.nextState}}                    

            state: Animal game undefined try again no
                q: * {(неправильный/плохой) * вопрос} * || fromState = "../../Animal game question"
                q: * *давай * (следующий/другой) вопрос * || fromState = "../../Animal game question"
                q: * {(неправильный/плохой) * вопрос} * 
                q: * *давай * (следующий/другой) вопрос * 
                q: далее || fromState = "../../Animal game question"
                q: * { [$agree] [следующ*|друг*|еще|ещё] вопрос* } * || fromState = "../Animal game undefined try again yes", onlyThisState = true
                q: * { [$agree] (следующ*|друг*|дальше|еще|ещё|продолжай*|продолж*) [вопрос*] } * || fromState = ../, onlyThisState = true
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question undefined"]["Animal game undefined try again no"])}}
                go!: ../../Animal game question

            state: Animal game undefined try again yes
                q: * $agree * || fromState = ../, onlyThisState = true
                q: * $disagree * || fromState = ../, onlyThisState = true
                script:
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question undefined"]["Animal game undefined try again yes"])}}
          
            state: Animal game undefined try again quiz answer
                q: * {[$maybe|$sure] * ($Number|$FloatNumber|$quizNumExtremities) * [$orAnimalGame]} *
                q: * {[$maybe|$sure] * ($NotThisPart|$quizParts) * [($NotThisPart|$quizParts)]} *
                q: * {[$maybe|$sure] * ($NotThisSavagery|$quizSavagery) * [($NotThisSavagery|$quizSavagery)]} *
                q: * {[$maybe|$sure] * ($NotThisClass|$quizClass) * [($NotThisClass|$quizClass)]} *
                q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                q: * {[$maybe|$sure] ($NotThisCoating|$quizCoating) * [($NotThisCoating|$quizCoating)]} *
                script:
                    var context = "/Animal quiz/Animal game pudding start/Animal game question/Animal game question " + $session.lastQuiz.type;
                    var result = $nlp.match($parseTree.text, context);
                    if(result === null) {
                        $temp.nextState = "/Animal quiz/Animal game pudding start/Animal game question undefined";
                    } else {
                        $temp.nextState = result.targetState;
                        $parseTree = result.parseTree;
                        $temp.parseTree = result.parseTree;
                    }
                go!: {{$temp.nextState}}

        state: Animal game user wants next question
            q: * ($agree/$disagree/не хочу/не буду/не надо/подскажи/помоги/уже спрашивал/повторяешься/(скажи/какой/назови) * (ответ/сам/ты)/(сам/ты) (ответь/отвечай))  * || fromState = "../Animal game question"
            q: * [$agree] (дальше|след*|ещё|еще|игра*|задавай|другой) [дальше|след*|ещё|еще|игра*|задавай|другой] [вопрос*] * || fromState = "../Animal game question"
            q: * $agree (дальше|след*|ещё|еще|игра*|задавай|другой) [дальше|след*|ещё|еще|игра*|задавай|другой] вопрос* * || fromState = "../Animal game question"
            script:
                $temp.correct = getRightAnswer();
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["tell right answer and go next"])}}
            go!: ../Animal game question

        state: Animal game do you know || noContext = true
            q: * { (ты|сам) знаешь [ответ] } *
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game do you know"])}}

        state: Animal game repeat the question
            q: * {(повтори*|ска*|зада*) вопрос [(еще|ещё) раз] [пожалуйста|плиз]}* || fromState = "/Animal quiz"
            q: * {(повтори*|ска*|зада*) * [пожалуйста|плиз]} * || fromState = "../Animal game question", onlyThisState = true
            q: * (чего|что|(еще|ещё) раз) * || fromState = "../Animal game question", onlyThisState = true
            if: ($session.lastQuiz)
                script:
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game repeat the question"])}}
                a: {{$session.lastQuiz.text}} || question = true
                go!: ../Animal game question/{{$temp.nextState}}
            else:
                go!: ../Animal game question     

        state: Animal game repeat the question how much try
            q:  * (сколько раз * пробов*| [сколько] попыт*) * || fromState = "../Animal game question", onlyThisState = true
            q:  * (сколько раз * пробов*| [сколько] попыт*) *|| fromState = "/Animal quiz"
            if: ($session.lastQuiz)
                script:
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                random:
                    a: Попытка одна. Давай повторю вопрос.
                    a: Попробовать можно один раз и ничего страшного, если ты ошибёшься. Давай попробую ещё раз задать вопрос.
                a: {{$session.lastQuiz.text}} || question = true
                go!: ../Animal game question/{{$temp.nextState}}
            else:
                go!: ../Animal game question        
          
###############################################################################
###############       Предлагаем ещё один вопрос                 ##############
###############################################################################

        state: Next question enquire
            script:
                $temp.ask = selectRandomArg("yes","no","no","no","no","no","no");
            if: ($temp.ask == "yes")
                script:
                    $response.action = 'ACTION_EXP_WINK';
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Next question enquire"]["questions"])}}
            else:
                go!: ../Animal game question

            state: Next question enquire yes
                q: * ($agree|дальше|след*|ещё|еще|игра*|зада*|другой|продолж*|я готов*) [$agree|дальше|след*|ещё|еще|игра*|задавай*] [вопрос*]*
                q: * давай [следующий|другой|еще|ещё|свой|дальше|задавай*] вопрос* *
                q: * [можешь] (задать|задавай*) [след*|ещё|еще|другой] [вопрос*] *
                script:
                    $response.action = 'ACTION_EXP_HAPPY';
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Next question enquire"]["Next question enquire yes"])}}
                go!: ../../Animal game question

            state: Next question enquire undefined
                q: $Text || fromState = ../
                if: ($session.barrier && $session.barrier == "Animal game question")
                    script:
                        $response.action = 'ACTION_EXP_DIZZY';
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Next question enquire"]["Next question enquire undefined"]["barrier"])}}
                    go!: ../../Animal game question
                else:
                    script:
                        $response.action = 'ACTION_EXP_DIZZY';
                        $session.barrier = "Animal game question";
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Next question enquire"]["Next question enquire undefined"]["no barrier"])}}

###############################################################################
###############             Остановка игры                       ##############
###############################################################################

    state: Animal game pudding stop
        q: * $stopGame * || fromState = "../Animal game pudding start"
        q: * ($disagree|$stopGame|$dontKnow) * || fromState = "../Animal game pudding start/Next question enquire"
        q: * [давай] * перерыв *
        q: [я] устал*
        a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["stop phrases"])}}
        script:    
            var right = $session.lastQuiz.rightAnswers;
            var all_questions = $session.lastQuiz.numQuestions;
            
            $temp.ru__answers = right % 10 === 1 && right % 100 !== 11 ? 'вопрос' : (right % 10 >= 2 && right % 10 <= 4 && (right % 100 < 10 || right % 100 >= 20) ? 'вопроса' : 'вопросов');
            $temp.ru_questions = all_questions % 10 === 1 && all_questions % 100 !== 11 ? 'вопрос' : (all_questions % 10 >= 2 && all_questions % 10 <= 4 && (all_questions % 100 < 10 || all_questions % 100 >= 20) ? 'вопроса' : 'вопросов');
        if: ($session.lastQuiz.rightAnswers == 0)
            if: ($session.lastQuiz.numQuestions == 1)
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["one wrong answer"])}}
            else:
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["all wrong answers"])}}
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["bad result"])}}
        else:
            if: ($session.lastQuiz.rightAnswers == $session.lastQuiz.numQuestions)
                if: ($session.lastQuiz.numQuestions == 1)
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["one right answer"])}}
                else: 
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["all right answers"])}}
            else:
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["some right answers"])}}
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["good result"])}}
        
        script:
            $session.lastQuiz = undefined;

        state: Animal game again
            q: * { [$agree] * (еще|ещё|продолж*|повторим) [раз] [сыгр*|игр*|поигр*] [в (зоо*|животных)] } * 
            q: * { [$agree] * (еще|ещё) вопрос* } *   
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["Animal game again"])}}
            go!: ../../Animal game pudding start/Animal game question

    state: Animal game howDoYouKnow
        q: * {$you * откуда * знаешь} * || fromState = "../Animal game pudding start"
        a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game howDoYouKnow"])}}        
        if: ($session.lastQuiz && $session.lastQuiz.text)
            script:
                $temp.nextState = "Animal game question " + $session.lastQuiz.type;
            a: {{$session.lastQuiz.text}} || question = true
            go!: ../Animal game pudding start/Animal game question/{{$temp.nextState}}
        else:
            go!: ../Animal game pudding start/Animal game question   

    state: Count right answers || noContext = true
        q: * {ско* * правильн* * ответ*} *
        script:          
            var right = $session.lastQuiz.rightAnswers;
            var all_questions = $session.lastQuiz.numQuestions;
            
            $temp.ru__answers = right % 10 === 1 && right % 100 !== 11 ? 'вопрос' : (right % 10 >= 2 && right % 10 <= 4 && (right % 100 < 10 || right % 100 >= 20) ? 'вопроса' : 'вопросов');
            var ru_questions = all_questions % 10 === 1 && all_questions % 100 !== 11 ? 'вопрос' : (all_questions % 10 >= 2 && all_questions % 10 <= 4 && (all_questions % 100 < 10 || all_questions % 100 >= 20) ? 'вопроса' : 'вопросов');
        if: ($session.lastQuiz.rightAnswers == 0)
            if: ($session.lastQuiz.numQuestions == 1)
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["one wrong answer"])}}
            else:
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game pudding fstop"]["all wrong answers"])}}
        else:
            if: ($session.lastQuiz.rightAnswers == $session.lastQuiz.numQuestions)
                if: ($session.lastQuiz.numQuestions == 1)
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["one right answer"])}}
                else:
                    a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["all right answers"])}}
            else:
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["some right answers"])}}

    state: AnimalGame for tests || noContext = true
        q!: $Number AnimalGame test
        script: 
            $session.AnimalGameTestNumber = $parseTree.Number[0].value;
              