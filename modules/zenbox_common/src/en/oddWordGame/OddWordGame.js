var OddWordGame = (function() {

    function getPlural(cls) {
        switch(cls) {
            case "animate":
                return "animate";
            case "inanimate":
                return "inanimate";
            case "animal":
                return "animals";
            case "plant":
                return "plant";
            case "object":
                return "objects";
            case "transport":
                return "transportation devices";
            case "mammal":
                return "mammals";
            case "insect":
                return "insects";
            case "bird":
                return "birds";
            case "fish":
                return "fishes";
            case "tree":
                return "trees";
            case "flower":
                return "flowers";
            case "fruit":
                return "fruits";
            case "musical instrument":
                return "musical instruments";
            case "tool":
                return "tools";
            case "kitchen utensil":
                return "kitchen utensils";
            case "hygiene product":
                return "hygiene products";
            case "wild animal":
                return "wild animals";
            case "domestic animal":
                return "domestic animals";
            case "fruit":
                return "fruits";
            case "berry":
                return "berries";
            case "vegetable":
                return "vegetables";  
        }
    }

    function getWordChain() {
        var $session = $jsapi.context().session;
        var $temp = $jsapi.context().temp;


        var cur_pair = selectRandomArg($session.oddWordGame.Levels[$session.oddWordGame.currentLevel]);
        $temp.right_class = (selectRandomArg(0, 1) === 0) ? cur_pair[0] : cur_pair[1];
        $temp.wrong_class = ($temp.right_class === cur_pair[0]) ? cur_pair[1] : cur_pair[0];
        var words = [];
        var values = $session.oddWordGame.Classes[$temp.right_class].slice();
        for (var i = 0; i < $session.oddWordGame.questionType; i++) {
            var right_id = selectRandomArg(values);
            values.splice(values.indexOf(right_id), 1);
            var right_word = $oddWordGameElements[right_id].value.name;
            words.push(right_word);
        }

        var wrong_id = selectRandomArg($session.oddWordGame.Classes[$temp.wrong_class]);
        var wrong_word = $oddWordGameElements[wrong_id].value.name;
        $temp.right_class = getPlural($temp.right_class);
        $session.classForHint = $temp.right_class;
        $session.oddWordGame.rightAnswer = wrong_word;
        $session.classOfAnswer = $temp.wrong_class;
        words.push(wrong_word);
        words = randomSort(words);
        if ($temp.start){
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