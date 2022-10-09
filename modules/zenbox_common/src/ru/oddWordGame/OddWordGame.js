var OddWordGame = (function() {
    function getPlural(cls) {
        switch (cls) {
            case 'живое':
                return 'живое';
            case 'неживое':
                return 'неживое';
            case 'животное':
                return 'животные';
            case 'растение':
                return 'растения';
            case 'вещь':
                return 'вещи';
            case 'транспорт':
                return 'средства передвижения';
            case 'млекопитающее':
                return 'млекопитающие';
            case 'насекомое':
                return 'насекомые';
            case 'птица':
                return 'птицы';
            case 'рыба':
                return 'рыбы';
            case 'дерево':
                return 'деревья';
            case 'цветок':
                return 'цветы';
            case 'плод':
                return 'плоды';
            case 'музыкальный инструмент':
                return 'музыкальные инструменты';
            case 'рабочий инструмент':
                return 'рабочие инструменты';
            case 'кухонная принадлежность':
                return 'кухонные принадлежности';
            case 'средство гигиены':
                return 'средства гигиены';
            case 'дикое животное':
                return 'дикие животные';
            case 'домашнее животное':
                return 'домашние животные';
            case 'фрукт':
                return 'фрукты';
            case 'ягода':
                return 'ягоды';
            case 'овощ':
                return 'овощи';
            case 'гриб':
                return 'грибы';
        }
    }

    function getWordChain() {
        var $session = $jsapi.context().session;
        var $temp = $jsapi.context().temp;

        var curPair = selectRandomArg($session.oddWordGame.Levels[$session.oddWordGame.currentLevel]);
        $temp.rightClass = (selectRandomArg(0, 1) === 0) ? curPair[0] : curPair[1];
        $temp.wrongClass = ($temp.rightClass === curPair[0]) ? curPair[1] : curPair[0];
        var words = [];
        var values = $session.oddWordGame.Classes[$temp.rightClass].slice();
        for (var i = 0; i < $session.oddWordGame.questionType; i++) {
            var rightId = selectRandomArg(values);
            values.splice(values.indexOf(rightId), 1);
            var rightWord = $oddWordGameElements[rightId].value.name;
            words.push(rightWord);
        }

        var wrongId = selectRandomArg($session.oddWordGame.Classes[$temp.wrongClass]);
        var wrongWord = $oddWordGameElements[wrongId].value.name;
        $temp.rightClass = getPlural($temp.rightClass);
        $session.classForHint = $temp.rightClass;
        $session.oddWordGame.rightAnswer = wrongWord;
        $session.classOfAnswer = $temp.wrongClass;
        words.push(wrongWord);
        words = randomSort(words);
        if ($temp.start) {
            $reactions.answer(selectRandomArg($OddWordGameAnswers['getWordChain']['if']));
        } else {
            $reactions.answer(selectRandomArg($OddWordGameAnswers['getWordChain']['else']));
        }
        $session.oddWordGame.currentList = words;
        $session.oddWordGame.currentListText = '';
        for (var i = 0; i < words.length; i++) {
            $session.oddWordGame.currentListText += words[i];
            if (i !== words.length - 1) {
                $session.oddWordGame.currentListText += ' – ';
            } else {
                $session.oddWordGame.currentListText += '.';
            }
        }

        $session.oddWordGame.rightAnswerText = resolveVariables(selectRandomArg($OddWordGameAnswers['getWordChain']['rightAnswerText']));
        $session.oddWordGame.rightAnswerText += resolveVariables(selectRandomArg($OddWordGameAnswers['getWordChain']['rightAnswerText2']));
    }

    function processAnswer() {
        var $parseTree = $jsapi.context().parseTree;
        var $temp = $jsapi.context().temp;
        var $session = $jsapi.context().session;

        var elm = $parseTree.oddWordGameElement;
        var order = $parseTree.oddWordGameOrder;
        var curList = $session.oddWordGame.currentList;

        var twoElmInCurList = elm && elm[1] && (curList.indexOf(elm[0].value.name) > -1) && (curList.indexOf(elm[1].value.name) > -1);
        var twoElmByOrder = order && order[1];
        var elmAndOrder = elm && order && (elm[0].value.name !== curList[order[0].value]);

        var twoElmWithOneInCurList = elm && elm[0] && elm[1] && (((curList.indexOf(elm[0].value.name) > -1) && (curList.indexOf(elm[1].value.name) === -1)) ||
            ((curList.indexOf(elm[0].value.name) === -1) && (curList.indexOf(elm[1].value.name) > -1)));

        if (twoElmInCurList || twoElmByOrder || elmAndOrder) {
            $temp.askNow = true;
            $reactions.answer(selectRandomArg($OddWordGameAnswers['Odd Word game get answer']['if']));
        } else {
            $temp.userAnswer = null;
            if (twoElmWithOneInCurList) {
                $temp.userAnswer = (curList.indexOf(elm[0].value.name) > -1) ? elm[0].value.name : elm[1].value.name;
            } else if (elm && elm[0]) {
                $temp.userAnswer = elm[0].value.name;
            } else if (order) {
                if (order[0].value === 5 && curList.length === 4) {
                    $reactions.answer(selectRandomArg($OddWordGameAnswers['Odd Word game get answer']['answerOutOfRange']));
                } else if (order[0].value === 'lst') {
                    $temp.userAnswer = curList[curList.length - 1];
                } else if (order[0].value === 'blst') {
                    $temp.userAnswer = curList[curList.length - 2];
                } else {
                    $temp.userAnswer = curList[order[0].value - 1];
                }
            } else {
                throw "No oddWordGameElement or oddWordGameOrder in parseTree";
            }

            if ($temp.userAnswer) {
                if ($temp.userAnswer === $session.oddWordGame.rightAnswer) {
                    $reactions.transition('../../RightAnswer');
                } else if (curList.indexOf($temp.userAnswer) > -1 || $temp.beenHere) {
                    $reactions.transition('../../WrongAnswer');
                } else {
                    $reactions.answer(selectRandomArg($OddWordGameAnswers['Odd Word game get answer']['else']));
                    $temp.beenHere = true;
                    $temp.askNow = true;
                }
            }
        }
    }

    return {
        getPlural: getPlural,
        getWordChain: getWordChain,
        processAnswer: processAnswer
    };
})();
