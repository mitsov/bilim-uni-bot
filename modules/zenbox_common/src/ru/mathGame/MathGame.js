
///////////////////////////////////////////////////////////////////////////////
///////////////        Генерация вопроса по математике           //////////////
///////////////////////////////////////////////////////////////////////////////


//формируем вопрос типа "больше/меньше"
function getMoreLessQuestion() {
    var less = randomInteger(0,6);
    var more = less + randomInteger(1,3);
    var lessMore = randomInteger(0,10) > 5 ? true : false;
    var text, result;

    if (randomInteger(0,10) > 5) {
        text = selectRandomArg("Какое число больше: ",
                $MathGameAnswers["Math_game"]["getMoreLessQuestion"]["more"]) +
                (lessMore ? less : more) + " или " + (lessMore ? more : less);
        result = more;
    } else {
        text = selectRandomArg("Какое число меньше: ",
                $MathGameAnswers["Math_game"]["getMoreLessQuestion"]["less"]) +
                (lessMore ? less : more) + " или " + (lessMore ? more : less);
        result = less;
    }

    return {
        text: text,
        result: result
    };
}

//формируем вопрос о животных
function getMathAnimalsQuestion(level) {
    var text, result, randNumber, randAnimal;
    randAnimal = getAnimal("numExtremities");
    randNumber = randomInteger(2,10*level);
    var ru__times = randNumber % 10 === 1 && randNumber % 100 !== 11 ? 'раз' : (randNumber % 10 >= 2 && randNumber % 10 <= 4 && (randNumber % 100 < 10 || randNumber % 100 >= 20) ? 'раза' : 'раз');
    text = selectRandomArg("Сколько " + randAnimal.extremities + " было бы у " + randAnimal.genTitle + ", если бы их было в " + randNumber + " " + ru__times + " больше?",
        "Если бы у " + randAnimal.genTitle + " было в " + randNumber + " " + ru__times + " больше " + randAnimal.extremities + ", то сколько бы их было?",
        "Представим, что количество " + randAnimal.extremities + " у " + randAnimal.genTitle + " увеличилось в " + randNumber + " " + ru__times + ". Сколько теперь " + randAnimal.extremities + " у " + randAnimal.genTitle + "?");
    result = randAnimal.numExtremities * randNumber;
    return {
        text: text,
        result: result
    };    
}

//формируем вопрос о сложении
function getMathFuncQuestion(func, level) {
    var number1, number2, number3, number4, text, result;
    switch(level) {
        case 1:
            number1 = randomInteger(1,10);
            number2 = randomInteger(1,10);
            break;
        case 2:
            number1 = randomInteger(1,50);
            number2 = randomInteger(1,50);
            break;     
        case 3:
            number1 = randomInteger(1,250);
            number2 = randomInteger(1,250);
            break;        
    }
    switch(func) {
        case "add":
            result = number1 + number2;
            text = selectRandomArg("Сколько будет " + number1 + " плюс " + number2 + "?",
                "Какая сумма чисел " + number1 + " и " + number2 + "?",
                "Сколько получится, если сложить " + number1 + " и " + number2 + "?", "task");
            if (text == "task") {
                var who, fruit, fruit1, fruit2, verb1, verb2;
                who = getRandomElement(NamesRu);
                fruit = getRandomElement(Fruits);
                fruit1 = number1 % 10 === 1 && number1 % 100 !== 11 ? fruit.accTitle : (number1 % 10 >= 2 && number1 % 10 <= 4 && (number1 % 100 < 10 || number1 % 100 >= 20) ? fruit.genTitle : fruit.genMany);
                fruit2 = number2 % 10 === 1 && number2 % 100 !== 11 ? fruit.accTitle : (number2 % 10 >= 2 && number2 % 10 <= 4 && (number2 % 100 < 10 || number2 % 100 >= 20) ? fruit.genTitle : fruit.genMany);
                verb1 = (who.sex == "ж") ? selectRandomArg("купила", "нашла") : selectRandomArg("купил", "нашёл");
                verb2 = (who.sex == "ж") ? selectRandomArg("взяла", "достала") : selectRandomArg("взял", "достал");
                text = who.name + " " + verb1 + " " + number1 + " " + fruit1 +
                    ", а потом " + verb2 + " ещё " + number2 + " " + fruit2 +
                    ". Сколько всего у " + ((who.sex == "ж") ? "неё" : "него") + " теперь " + fruit.genMany + "?";
            }
            break;
        case "sub":
            if (number1 > number2) {
                number3 = number2;
                number2 = number1;
                number1 = number3;
            }
            if (number2 == number1) {
                number2 += 1;
            }
            result = number2 - number1;
            text = selectRandomArg("Сколько будет " + number2 + " минус " + number1 + "?",
                "На сколько " + number2 + " больше, чем " + number1 + "?",
                "Сколько получится, если от " + number2 + " отнять " + number1 + "?", "task");
            if (text == "task") {
                var who, fruit, fruit1, fruit2, verb1, verb2;
                who = getRandomElement(NamesRu);
                fruit = getRandomElement(Fruits);
                fruit1 = number1 % 10 === 1 && number1 % 100 !== 11 ? fruit.accTitle : (number1 % 10 >= 2 && number1 % 10 <= 4 && (number1 % 100 < 10 || number1 % 100 >= 20) ? fruit.genTitle : fruit.genMany);
                fruit2 = number2 % 10 === 1 && number2 % 100 !== 11 ? fruit.accTitle : (number2 % 10 >= 2 && number2 % 10 <= 4 && (number2 % 100 < 10 || number2 % 100 >= 20) ? fruit.genTitle : fruit.genMany);
                verb1 = (who.sex == "ж") ? selectRandomArg("купила", "нашла") : selectRandomArg("купил", "нашёл");
                verb2 = (who.sex == "ж") ? selectRandomArg("потеряла", "отдала другу") : selectRandomArg("потерял", "отдал другу");
                text = who.name + " " + verb1 + " " + number2 + " " + fruit2 +
                   ", а потом " + verb2 + " " + number1 + " " + fruit1 +
                   ". Сколько всего у " + ((who.sex == "ж") ? "неё" : "него") + " теперь " + fruit.genMany + "?";
            }            
            break;
        case "addSub":
            number3 = randomInteger(1,10);
            if (number3 > number1) {
                number4 = number3;
                number3 = number1;
                number1 = number4;
            } 
            result = number1 + number2 - number3;
            text = selectRandomArg("Сколько будет " + number1 + " плюс " + number2 + " минус " + number3 + "?",
                "Сколько будет, если от суммы чисел " + number1 + " и " + number2 + " отнять " + number3 + "?",
                "Сколько получится, если сложить " + number1 + " и " + number2 + ", а потом отнять " + number3 + "?");
            break;         
        case "mul":
            number2 = randomInteger(2,6);
            result = number1 * number2;
            text = selectRandomArg("Сколько будет " + number1 + " умножить на " + number2 + "?",
                "Какое произведение чисел " + number1 + " и " + number2 + "?",
                "Сколько получится, если перемножить " + number1 + " и " + number2 + "?");
            break;            
        case "div":
            result = randomInteger(2,6);
            number3 = number1 * result;
            text = selectRandomArg("Сколько будет " + number3 + " делить на " + number1 + "?",
                "Какое частное чисел " + number3 + " и " + number1 + "?",
                "Сколько получится, если разделить " + number3 + " на " + number1 + "?");            
            break;
    }
    return {
        text: text,
        result: result
    };
}

//формируем вопрос "между"
function getBetweenNumberQuestion() {
    var result = randomInteger(1,9);
    var prev = result - 1;
    var next = result + 1;
    var text = selectRandomArg("Какое число находится между " + prev + " и " + next + "?",
            "Какое число больше, чем " + prev + ", но меньше, чем " + next + "?",
            "Какое число меньше, чем " + next + ", но больше, чем " + prev + "?");
    return {
        text: text,
        result: result
    };
}

//формируем вопрос с последовательностью чисел
function getMissingNumberQuestion(level) {
    var start = randomInteger(0,6);
    var sequence = '' + start;
    var missing, nextNumber, text, i;
    missing = start + (level+1)*randomInteger(1, 3);
    for (i=1; i<5; i++) {
        nextNumber = start + (level+1)*i;
        if (nextNumber != missing) {
            sequence += ', ' + nextNumber;
        }
    }
    text = selectRandomArg($MathGameAnswers["Math_game"]["getMissingNumberQuestion"]) + sequence;
    return {
        text: text,
        result: missing
    };
}

//определяем уровень игрока
function defineMathLevel() {
    var $client = $jsapi.context().client;
    //если какой-то уровень был
    if ($client.mathLevel) {
        //проверка прохождения экзамена (первого теста)
        if (!$client.mathLevel.examPassed) {
            if ($client.mathLevel.wrongAnswers > 0) {
                $client.mathLevel.wrongAnswers = 10;
                $client.mathLevel.examPassed = true;
            } else if ($client.mathLevel.rightAnswers > 0) {
                if ($client.mathLevel.level >= 3) {
                    $client.mathLevel.examPassed = true;
                } else {
                    $client.mathLevel.rightAnswers = 10;                
                    $client.mathLevel.testIsNeeded = true;
                    $client.mathLevel.testPassed = true;                    
                }
            }   
        }
        //проверяем, не пора ли повышать уровкень
        if ($client.mathLevel.level < 3 && $client.mathLevel.rightAnswers >= 10) {
            //если пора и не было такой отметки, ставим её
            if (!$client.mathLevel.testIsNeeded) {
                $client.mathLevel.testIsNeeded = true;
                $client.mathLevel.testPassed = false;
            } else if ($client.mathLevel.testPassed) {
                //если была отметка, и тест пройден, повышаем уровень
                $client.mathLevel.level += 1;                
                $client.mathLevel.testIsNeeded = false;
                $client.mathLevel.testPassed = false;
                $client.mathLevel.rightAnswers = 0;
                $client.mathLevel.wrongAnswers = 0;
                if ($client.mathLevel.examPassed) {
                    $reactions.answer(selectRandomArg($MathGameAnswers["Math_game"]["defineMathLevel"]["lvlUp"]));
                }            
            } else {
                //если была отметка, но тест не пройден, снимаем отметку, обнуляем счётчики
                $client.mathLevel.testIsNeeded = false;              
                $client.mathLevel.rightAnswers = 0;
                $client.mathLevel.wrongAnswers = 0;                  
            }
        } else if ($client.mathLevel.wrongAnswers >= 10) {
            if ($client.mathLevel.level > 0) {
                $client.mathLevel.level -= 1; 
            }       
            $client.mathLevel.testIsNeeded = false;
            $client.mathLevel.testPassed = false;
            $client.mathLevel.rightAnswers = 0;
            $client.mathLevel.wrongAnswers = 0;
            $reactions.answer(selectRandomArg($MathGameAnswers["Math_game"]["defineMathLevel"]["lvlDown"]));            
        }
    } else {
        //первый вопрос
        $client.mathLevel = {
            level: 0,
            testIsNeeded: true,
            testPassed: false,
            examPassed: false,
            rightAnswers: 0,
            wrongAnswers: 0
        }
    }
}