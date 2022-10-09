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

require: patternsEn.sc
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
    $quizSavagery = ((domestic*|not wild*|not savage*|pet|farm):1|(wild|savage|not domestic):2)
    $quizClass = ((mammal*):1|(bird*):2|(insect*):3|(fish*):4)
    $quizNumExtremities = (({no legs}|none|zero):0)
    $quizParts = ((wing*):1|(fin*):2|(horn*):3|(hoof*):4|(snout):5|(trunk):6|(tusk*):7)
    $quizCoating = ((fur*|wool):1|(feather*):2|(carapace):3|(skin):4|(scales):5|(spines):6)
    $quizAll = [a/an/the] ($Animal|$quizSavagery|$quizClass|$quizParts|$quizCoating)

    $Animal = $entity<Animals> || converter = $converters.AnimalTagConverter

    $orAnimalGame = or
    
    $NotThisAnimal = (no/not) [a/an/the] $Animal || converter = function(parseTree) {return parseTree.Animal[0].value} 
    $NotThisPart = (no/not) [a/an/the] $quizParts || converter = function(parseTree) {return parseTree.quizParts[0].value} 
    $NotThisSavagery = (no/not) [a/an/the] $quizSavagery || converter = function(parseTree) {return parseTree.quizSavagery[0].value} 
    $NotThisCoating = (no/not) [a/an/the] $quizCoating || converter = function(parseTree) {return parseTree.quizCoating[0].value} 
    $NotThisClass = (no/not) [a/an/the] $quizClass || converter = function(parseTree) {return parseTree.quizClass[0].value} 

theme: /Animal quiz
    state: Animal game alternative enter
        q!: * {($tellMe|what about) * [earth*|terrestrial] (animal*|beast*)} *    
        a: {{selectRandomArg($AnimalGameAnswers["Animal game alternative enter"])}}
        a: {{selectRandomArg($AnimalGameAnswers["Animal game suggest quiz"])}} || question = true
        go!: ../Animal game pudding start

    state: Animal game user start
        q!: * [i] [want|wanna|let 's|let us|let's|lets] [to] play* * {[game|quiz] * (zoo* [park]|animal*)} [with] [$you|me] *
        q!: * (animal*|zoo* [park]) (game|quiz)
        a: {{selectRandomArg($AnimalGameAnswers["Animal game user start"])}}
        go!: ../Animal game pudding start 

    state: Animal game suggest quiz
        q!: offer me the animal quiz
        q!: * I $like (zoo* [park]|animal*) *
        a: {{selectRandomArg($AnimalGameAnswers["Animal game suggest quiz"])}} || question = true
        go!: ../Animal game pudding start    
            
    state: Animal game pudding start        

        state: Animal game no
            q: * ($disagree|$notNow) * || fromState = .., onlyThisState = true
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game no"])}}

        state: Animal game from rules no
            q: * ($disagree|$notNow) [* (about) $Text] * || fromState = "/PlayGames/Games/How to play Zoo", onlyThisState = true 
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game from rules no"]["disagree"])}}
            if: $parseTree.Text
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game from rules no"]["disagree with text"])}}

        state: Animal game yes
            q: * ($agree|* try|help|come on| (let 's|let us) (play|do it)) * || fromState = "/Animal quiz/Animal game pudding start", onlyThisState = true
            q: * [$agree] * ($agree|* try|help|come on| (let 's|let us) (play|do it)) * || fromState = "/Animal quiz/Animal game pudding start", onlyThisState = true
            q: * [$agree] * ($agree|* try|help|come on| (let 's|let us) (play|do it)) * || fromState = "/PlayGames/Games/How to play Zoo", onlyThisState = true
            q!: * [let 's|let us] [play] (zoo*|animal*) *
            q: * (zoo*|animal*) * || fromState = /PlayGames/Games, onlyThisState = true  
            q: * [$agree|$maybe] * (zoo*|animal*) * [$agree|$maybe|$sure] * || fromState = /PlayGames/Games, onlyThisState = true
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
            a: {{$session.lastQuiz.text}} || question = true   
            go!: ./{{$temp.nextState}}

            state: Animal game question tusks
 
                state: Answer tusks
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                    

            state: Animal game question trunk
 
                state: Answer trunk
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                    

            state: Animal game question penny
 
                state: Answer penny
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                    

            state: Animal game question hoof
 
                state: Answer hoof
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                    

            state: Animal game question horns
  
                state: Answer horns
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                    

            state: Animal game question fins
 
                state: Answer fins
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                    

            state: Animal game question wings

                state: Answer wings
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}                    

            state: Animal game question coating
 
                state: Answer coating
                    q: * {[$maybe|$sure] ($NotThisCoating|$quizCoating) * [($NotThisCoating|$quizCoating)]} *
                    script:
                        var notThisCoating = $parseTree.NotThisCoating ? $parseTree.NotThisCoating : false,
                            usersAnswer = $parseTree.quizCoating ? $parseTree.quizCoating[0].value : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.quizCoating[1] !== 'undefined' ? $parseTree.quizCoating[1] : false, 
                            right = Number(getCoatingCode($session.lastQuiz.animal)),
                            wrong = Number(getCoatingCode($session.lastQuiz.otherAnimal));

                        operateWithAnswer_AnimalGame(notThisCoating, usersAnswer, anotherAnswer, right, wrong);
                    go!: ../../../{{$temp.nextState}}  

            state: Animal game question employment
 
                state: Answer employment
                    q: * {[$maybe|$sure] * ($NotThisAnimal|$Animal) * [($NotThisAnimal|$Animal)]} *
                    script:
                        var notThisAnimal = $parseTree.NotThisAnimal ? $parseTree.NotThisAnimal : false,
                            usersAnswer = $parseTree.Animal ? $parseTree.Animal[0] : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.Animal[1] !== 'undefined' ? $parseTree.Animal[1] : false, 
                            right = $session.lastQuiz.animal.title,
                            wrong = $session.lastQuiz.otherAnimal.title;
                        operateWithAnswer_AnimalGame(notThisAnimal, usersAnswer, anotherAnswer, right, wrong, true);
                    go!: ../../../{{ $temp.nextState }}

            state: Animal game question parts
 
                state: Answer parts
                    q: * {[$maybe|$sure] * ($NotThisPart|$quizParts) * [($NotThisPart|$quizParts)]} *
                    script:
                        var notThisPart = $parseTree.NotThisPart ? $parseTree.NotThisPart : false,
                            usersAnswer = $parseTree.quizParts ? $parseTree.quizParts[0].value : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.quizParts[1] !== 'undefined' ? $parseTree.quizParts[1] : false, 
                            right = $session.lastQuiz.parts.right.value,
                            wrong = $session.lastQuiz.parts.wrong.value; 
                            
                        operateWithAnswer_AnimalGame(notThisPart, usersAnswer, anotherAnswer, right, wrong);
                    go!: ../../../{{$temp.nextState}}  

            state: Animal game question numExtremities
 
                state: Answer numExtremities
                    q: * ($Number|$FloatNumber|$quizNumExtremities) * || fromState = "../../../Animal game question undefined"
                    q: * {[$maybe|$sure] * ($Number|$FloatNumber|$quizNumExtremities) * [$orAnimalGame]} *
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
                    go!: ../../../{{$temp.nextState}}    

            state: Animal game question class

                state: Answer class
                    q: * {[$maybe|$sure] * ($NotThisClass|$quizClass) * [($NotThisClass|$quizClass)]} *
                    script:
                        var notThisClass = $parseTree.NotThisClass ? $parseTree.NotThisClass : false,
                            usersAnswer = $parseTree.quizClass ? $parseTree.quizClass[0].value : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.quizClass[1] !== 'undefined' ? $parseTree.quizClass[1] : false, 
                            right = Number(getClassCode($session.lastQuiz.animal)),
                            wrong = Number(getClassCode($session.lastQuiz.otherAnimal));  

                        operateWithAnswer_AnimalGame(notThisClass, usersAnswer, anotherAnswer, right, wrong);     

                    go!: ../../../{{$temp.nextState}}

            state: Animal game question savagery

                state: Answer savagery
                    q: * {[$maybe|$sure] * ($NotThisSavagery|$quizSavagery) * [($NotThisSavagery|$quizSavagery)]} *
                    script:
                        var notThisSavagery = $parseTree.NotThisSavagery ? $parseTree.NotThisSavagery : false,
                            usersAnswer = $parseTree.quizSavagery ? $parseTree.quizSavagery[0].value : false,
                            anotherAnswer = usersAnswer && typeof $parseTree.quizSavagery[1] !== 'undefined' ? $parseTree.quizSavagery[1] : false, 
                            right = Number(getSavageryCode($session.lastQuiz.animal)),
                            wrong = right === 1 ? 2 : 1;  

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
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["RightAnswer"])}}
            go!: ../Next question enquire

        state: WrongAnswer
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["WrongAnswer"])}}
            if: ($temp.rightAnswer)
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["tell right answer"])}}
                script:
                    $temp.rightAnswer = ""
            go!: ../Next question enquire

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
            q: * (neither|not one|no one) * || fromState = "../Animal game question"
            q: * ([they] both $no|not one|none|nobody) * || fromState = "../Animal game question dontKnow"
            q: * ([they] both $no|not one|none|nobody) * || fromState = "../Animal game question"
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
            q: * [$sure] not [a/the/an] $quizAll * || fromState = "../Animal game question"
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
            q: * $dontKnow  * $quizAll or $quizAll *  || fromState = "../Animal game question" 
            q: * $quizAll or $quizAll * $dontKnow * || fromState = "../Animal game question"
            q: * $dontKnow * || fromState = "../Animal game question"
            q: who has || fromState = "../Animal game question"
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
            q: * (many|little|few|not at all|none) * || fromState = "../Animal game question/Animal game question numExtremities"
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
            q: * [animal] || fromState = "../Animal game question"
            if: ($session.barrier && $session.barrier == "Animal game question")
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question undefined"]["barrier"])}}
                go!: ../Animal game question
            else:
                script:
                    $response.action = 'ACTION_EXP_SPEECHLESS';
                    $session.barrier = "Animal game question";
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question undefined"]["no barrier"])}}
         
            state: Animal game undefined try again
                q: * [$agree] * [let 's|let (us|me)] (one more|(try|once) * again) [try|guess] * || fromState = .., onlyThisState = true
                q: * [$agree] * [let 's|let (us|me)]  (one more|(try|once) * again) [try|guess] * || fromState = .., onlyThisState = true
                q: * [$agree] * [let 's|let (us|me)] (one more|(try|once) * again) [try|guess] * || fromState = "../Animal game undefined try again yes", onlyThisState = true
                q: * [$agree] * [let 's|let (us|me)] (one more|(try|once) * again) [try|guess] * || fromState = "../Animal game undefined try again yes", onlyThisState = true
                q: (question/quiz/go/start/begin/more/let's) || fromState = .., onlyThisState = true  
                script:
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game question undefined"]["Animal game undefined try again"])}}
                a: {{$session.lastQuiz.text}} || question = true                    
                go!: ../../Animal game question/{{$temp.nextState}}                    

            state: Animal game undefined try again no
                q: * {(wrong/incorrect/$bad/$stupid) * question} * || fromState = "../../Animal game question"
                q: * (next/other/different/another) question * || fromState = "../../Animal game question"
                q: next || fromState = "../../Animal game question"
                q: * [$agree] [next/other/different/another] * || fromState = "../Animal game undefined try again yes", onlyThisState = true
                q: * [$agree] (next/other/different/another/(go/move) on) [question] * || fromState = ../, onlyThisState = true
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
            q: * (next/another/different/other/[something] else/already asked/[$you] tell me/(what/which) * {is [the] (right/correct) answer}/stupid/skip/{[$me] [need] help}) * || fromState = "../Animal game question"
            q: * [$agree] ((next/another/different/other) [question] /[something] else) * || fromState = "../Animal game question"
            q: ($agree/$disagree) || fromState = "../Animal game question"
            script:
                $temp.correct = getRightAnswer();
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["tell right answer and go next"])}}
            go!: ../Animal game question

        state: Animal game do you know || noContext = true
            q: * [do] $you know * [the] [answer] [yourself] *
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game do you know"])}}

        state: Animal game repeat the question
            q: * {(repeat*|say|ask|try) * [question] [again] [please] } * || fromState = "/Animal quiz"
            q: * {(repeat*|say|ask|try) * [question] [again] [please] } * || fromState = "../Animal game question", onlyThisState = true
            q: * (what|$no ~understand|come again) * || fromState = "../Animal game question", onlyThisState = true
            if: ($session.lastQuiz)
                script:
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game repeat the question"])}}
                a: {{$session.lastQuiz.text}} || question = true
                go!: ../Animal game question/{{$temp.nextState}}
            else:
                go!: ../Animal game question     

        state: Animal game repeat the question how much try
            q:  * how many (tries/attempts) do i (get/have) * || fromState = "../Animal game question", onlyThisState = true
            q:  * how many (tries/attempts) do i (get/have) *|| fromState = "/Animal quiz"
            if: ($session.lastQuiz)
                script:
                    $temp.nextState = "Animal game question " + $session.lastQuiz.type;
                a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game repeat the question how much try"])}}
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
                q: * ($agree|next|(move|go) on|another|other|different|ready*|more) [question*] *
                q: * (let 's|let us|(let|give) me) [next|other|another|different|your*|more] [question*] *
                q: * [could/can] [you] * (ask|move to) [the/a] [next/another/other] [question] *
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
        q: * ($stopGame/$shutUp|[i [am]|i 'm] * (tired|bored|boring)) * || fromState = "../Animal game pudding start"
        q: * ($disagree|$stopGame|$dontKnow|[i [am]| i 'm] * (tired|bored|boring)) * || fromState = "../Animal game pudding start/Next question enquire"
        q: * [i (need|want|wanna)|(let 's|let us) (have|make) ] * break *
        q: [i [am]| i 'm] * (tired|bored|boring) *
        q: * (give|want to|wanna|need) * [have] [me|us] * rest *
        q: * (let 's|want to|wanna|need) * (end|stop|finish) [* game] *
        a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["stop phrases"])}}
        script:    
            var right = $session.lastQuiz.rightAnswers;
            var all_questions = $session.lastQuiz.numQuestions;
            
            $temp.ru__answers = right % 10 === 1 && right % 100 !== 11 ? 'question' : (right % 10 >= 2 && right % 10 <= 4 && (right % 100 < 10 || right % 100 >= 20) ? 'questions' : 'questions');
            $temp.ru_questions = all_questions % 10 === 1 && all_questions % 100 !== 11 ? 'question' : (all_questions % 10 >= 2 && all_questions % 10 <= 4 && (all_questions % 100 < 10 || all_questions % 100 >= 20) ? 'questions' : 'questions');
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
            q: * { [$agree] * [play] [zoo*] (again|more|repeat|one more time) [question]} * 
            a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding stop"]["Animal game again"])}}
            go!: ../../Animal game pudding start/Animal game question

    state: Animal game howDoYouKnow
        q: * {(how|why) do $you know} * || fromState = "../Animal game pudding start"
        a: {{selectRandomArg($AnimalGameAnswers["Animal game pudding start"]["Animal game howDoYouKnow"])}}        
        if: ($session.lastQuiz && $session.lastQuiz.text)
            script:
                $temp.nextState = "Animal game question " + $session.lastQuiz.type;
            a: {{$session.lastQuiz.text}} || question = true
            go!: ../Animal game pudding start/Animal game question/{{$temp.nextState}}
        else:
            go!: ../Animal game pudding start/Animal game question   

    state: Count right answers || noContext = true
        q: * {how many* * [right|correct] * answer*} *
        script:          
            var right = $session.lastQuiz.rightAnswers;
            var all_questions = $session.lastQuiz.numQuestions;
            
            $temp.ru__answers = right % 10 === 1 && right % 100 !== 11 ? 'question' : (right % 10 >= 2 && right % 10 <= 4 && (right % 100 < 10 || right % 100 >= 20) ? 'questions' : 'questions');
            var ru_questions = all_questions % 10 === 1 && all_questions % 100 !== 11 ? 'question' : (all_questions % 10 >= 2 && all_questions % 10 <= 4 && (all_questions % 100 < 10 || all_questions % 100 >= 20) ? 'questions' : 'questions');
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
              