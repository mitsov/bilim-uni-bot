// инициализация необходимых переменных
function oddWordGameInit() {
    var $session = $jsapi.context().session;
    $session.oddWordGame = {
        currentLevel: 1,
        questionType: 3, // текущий тип вопроса
        rightAnswersCounter: 0, // количество правильных ответов
        answersCounter: 0, // количество всех ответов
        again: false, // указывает, повторная ли эта игра
        currentList: [],
        currentListText: "",
        rightAnswer: "",
        rightAnswerText: ""
    };

    if (!($session.oddWordGame.Levels && $session.oddWordGame.Classes)) {
        $session.oddWordGame.Levels = {
            4: {"base": []}, 3: {}, 2: {}, 1: {}
            }; // словарь найди лишнее по уровням
        $session.oddWordGame.Classes = {};
        // пробегаем по словарю элементов, берём класс текущего элемента, добавляем id элемента во все классы и над-классы
        var ElementDict, cls, value, curLevel, curLevelUp, curClass;
        for (var id = 1; id < Object.keys($oddWordGameElements).length; id++) {
            ElementDict = $oddWordGameElements[id].value;
            cls = ElementDict.class;
            value = $oddWordGameClasses[cls].value;
            for (var i = 1; i <= 4; i++) {
                curLevel = parseInt(i);
                curLevelUp = parseInt(i+1);
                if (curLevel in value) {
                    curClass = value[curLevel];
                    if (!(curClass in $session.oddWordGame.Classes)) {
                        $session.oddWordGame.Classes[curClass] = [];
                    }
                    $session.oddWordGame.Classes[curClass].push(id);
                    // делаем предобработку для составления пар классов на каждом уровне
                    if (curLevelUp !== 5) {
                        if (!(value[curLevelUp] in $session.oddWordGame.Levels[curLevel])) {
                            $session.oddWordGame.Levels[curLevel][value[curLevelUp]] = [];
                        }
                        if ($session.oddWordGame.Levels[curLevel][value[curLevelUp]].indexOf(curClass) === -1) {
                            $session.oddWordGame.Levels[curLevel][value[curLevelUp]].push(curClass);
                        }
                    } else {
                        if ($session.oddWordGame.Levels[4]["base"].indexOf(curClass) === -1) {
                            $session.oddWordGame.Levels[4]["base"].push(curClass);
                        }
                    }
                }
            }
        }

        // составляем пары классов для каждого уровня
        var pairs, i, i2, j, k, m;
        for (i = 1; i <= 4; i++) {
          pairs = [];
            for (i2 = 0; i2 < Object.keys($session.oddWordGame.Levels[i]).length; i2++) {
                j = Object.keys($session.oddWordGame.Levels[i])[i2];
                for (k = 0; k < $session.oddWordGame.Levels[i][j].length-1; k++) {
                    for (m= k+1; m < $session.oddWordGame.Levels[i][j].length; m++) {
                        pairs.push([$session.oddWordGame.Levels[i][j][k], $session.oddWordGame.Levels[i][j][m]]);
                    }
                }
            }
            $session.oddWordGame.Levels[i] = pairs;
        }
    }
}

function randomSort(arr) {
    var tempArr = [];
    var word;
    while (arr.length !== 0) {
        word = selectRandomArg.apply(this, arr);
        tempArr.push(word);
        arr.splice(arr.indexOf(word), 1);
    }

    return tempArr;
}
