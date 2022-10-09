
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
        text = selectRandomArg($MathGameAnswers["Math_game"]["getMoreLessQuestion"]["more"]) +
                (lessMore ? less : more) + " or " + (lessMore ? more : less);
        result = more;
    } else {
        text = selectRandomArg($MathGameAnswers["Math_game"]["getMoreLessQuestion"]["less"]) +
                (lessMore ? less : more) + " or " + (lessMore ? more : less);
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
    var ru__times = randNumber % 10 === 1 && randNumber % 100 !== 11 ? 'times' : (randNumber % 10 >= 2 && randNumber % 10 <= 4 && (randNumber % 100 < 10 || randNumber % 100 >= 20) ? 'times' : 'times');
    text = selectRandomArg("How many " + randAnimal.extremities + " would the " + randAnimal.genTitle + " have, if it had " + randNumber + " " + ru__times + " as many " + randAnimal.extremities + " as he does?",
        "If the " + randAnimal.genTitle + " had " + randNumber + " " + ru__times + " as many " + randAnimal.extremities + " as it does, how many would that be?");
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
            text = selectRandomArg("How much is " + number1 + " plus " + number2 + "?",
                "What is the sum of " + number1 + " and " + number2 + "?",
                "If we add together " + number1 + " and " + number2 + " how much would that be?", "task");
            
            if (text == "task") {
                var who, fruit, fruit1, fruit2, verb1, verb2;
                who = getRandomElement(NamesEn);
                fruit = getRandomElement(Fruits);
                fruit1 = number1 % 10 === 1 && number1 % 100 !== 11 ? fruit.accTitle : (number1 % 10 >= 2 && number1 % 10 <= 4 && (number1 % 100 < 10 || number1 % 100 >= 20) ? fruit.genTitle : fruit.genMany);
                fruit2 = number2 % 10 === 1 && number2 % 100 !== 11 ? fruit.accTitle : (number2 % 10 >= 2 && number2 % 10 <= 4 && (number2 % 100 < 10 || number2 % 100 >= 20) ? fruit.genTitle : fruit.genMany);
                verb1 = selectRandomArg("bought", "found"); 
                verb2 = selectRandomArg("bought", "found");
                text = who.name + " " + verb1 + " " + number1 + " " + fruit1 +
                    ", аnd then " + verb2 + " " + number2 + " more " + fruit2 +
                    ". How many " + ((who.sex == "female") ? "does she" : "does he") + " have now " + fruit.genMany + "?";
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
            text = selectRandomArg("How much is " + number2 + " minus " + number1 + "?",
                "What is " + number2 + " minus " + number1 + "?",
                "How much is " + number2 + " subtract " + number1 + "?", "task");
            if (text == "task") {
                var who, fruit, fruit1, fruit2, verb1, verb2;
                who = getRandomElement(NamesEn);
                fruit = getRandomElement(Fruits);
                fruit1 = number1 % 10 === 1 && number1 % 100 !== 11 ? fruit.accTitle : (number1 % 10 >= 2 && number1 % 10 <= 4 && (number1 % 100 < 10 || number1 % 100 >= 20) ? fruit.genTitle : fruit.genMany);
                fruit2 = number2 % 10 === 1 && number2 % 100 !== 11 ? fruit.accTitle : (number2 % 10 >= 2 && number2 % 10 <= 4 && (number2 % 100 < 10 || number2 % 100 >= 20) ? fruit.genTitle : fruit.genMany);
                verb1 = selectRandomArg("bought", "found");
                verb2 = selectRandomArg("lost", "gave to a friend");
                text = who.name + " " + verb1 + " " + number2 + " " + fruit2 +
                   ", and then " + verb2 + " " + number1 + " " + fruit1 +
                   ". How many  " + fruit.genMany + " does " + who.name + " have now?";
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
            text = selectRandomArg("How much is " + number1 + " plus " + number2 + " minus " + number3 + "?",
                "How much is if we sum " + number1 + " and " + number2 + " and then subtract " + number3 + "?",
                "How much is if we add " + number1 + " to " + number2 + ", and then subtract " + number3 + "?");
            break;         
        case "mul":
            number2 = randomInteger(2,6);
            result = number1 * number2;
            text = selectRandomArg("How much is " + number1 + " times " + number2 + "?",
                "How much is " + number1 + " multiplied by " + number2 + "?",
                "What is the " + number1 + " by " + number2 + "?");
            break;            
        case "div":
            result = randomInteger(2,6);
            number3 = number1 * result;
            text = selectRandomArg("How much is " + number3 + " divided by " + number1 + "?",
                "If we divide " + number3 + " by " + number1 + " how much do we get?",
                number3 + " divided by " + number1 + "?");    
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
    var text = selectRandomArg("What number comes between " + prev + " and " + next + "?",
            "What number is greater than " + prev + ", but less than " + next + "?",
            "What number is less than " + next + ", but greater than " + prev + "?");
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