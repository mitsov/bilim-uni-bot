///////////////////////////////////////////////////////////////////////////////
///////////////           Генерация вопроса о еде                //////////////
function getFoodQuestion() {
    var $session = $jsapi.context().session;
    var res;
    var questionType = getRandomQuestionType("type","ingredient","sweetness");
    var randFood = getFood(questionType);
    var right;
   
    switch(questionType) {
        case "type":
            res = capitalize(randFood.title) + " - это " + getPairFood(randFood, questionType) + "?";
            right = randFood.type + ".";
            break;
        case "sweetness":
            res = capitalize(randFood.title) + " - это " + getPairFood(randFood, questionType) + " блюдо?";
            right = randFood.sweetness + ".";
            break;
        case "ingredient":
            res = "Из чего готовят " + randFood.acc + " - " + getPairFood(randFood, questionType) + "?";
            right = randFood.ingredient + ".";
            break;                                   
    }

    var countQuestions = 1;
    if ($session.lastQuiz && $session.lastQuiz.numQuestions) {
        countQuestions = $session.lastQuiz.numQuestions + 1;
    }

    var countAnswers = 0;
    if ($session.lastQuiz && $session.lastQuiz.rightAnswers) {
        countAnswers = $session.lastQuiz.rightAnswers;
    }

    return {
        type: questionType,
        text: res,
        food: randFood,
        numQuestions: countQuestions,
        rightAnswers: countAnswers,
        right: right
    };    

}


function getFood(questionType) {
    var $session = $jsapi.context().session;
    var food;
    var foods = [];

    for (var j = 1; j <= Object.keys($Foods).length; j++) {
        food = $Foods[j].value;
        if ($session.lastQuiz && $session.lastQuiz.food && $session.lastQuiz.food.title == food.title) { 
        } else {
            switch(questionType) {
                case "type":
                    if ((food.type == "фрукт" || food.type == "овощ" || food.type == "орех" || food.type == "сухофрукт" ||
                        food.type == "мясное блюдо" || food.type == "выпечка" || food.type == "десерт" || food.type == "суп")
                        && !/.* суп/.test(food.title)) {
                        foods.push(food);
                    }
                    break;
                case "sweetness":
                    if (food.sweetness != "") {
                        foods.push(food);
                    }
                    break;
                case "ingredient":
                    if (food.ingredient != "") {
                        foods.push(food);
                    }
                    break;
            }
        }
    } 
    var index = randomIndex(foods);
    food = foods[index];
    return food;
}

///////////////////////////////////////////////////////////////////////////////
///////////////          Подбор данных для вопросов "или" (еда)  //////////////
///////////////////////////////////////////////////////////////////////////////

function getPairFood(food, type) {
    var res;
    var otherFood = getOtherFood.apply(this, arguments);
    var first;
    var second;

    switch(type) {
        case "type":
            first = food.type;
            second = otherFood.type;
            break;
        case "sweetness":
            first = food.sweetness;
            second = otherFood.sweetness;
            break;
        case "ingredient":
            first = food.ingredient;
            second = otherFood.ingredient;
            break;            
    }

    var rand = selectRandomArg("1","2");  
    res = (rand == "1") ? first + " или " + second : second + " или " + first;
    return res;
}

function getOtherFood(questionFood, questionType) {
    var food;
    var foods = [];

    for (var j = 1; j <= Object.keys($Foods).length; j++) {
        food = $Foods[j].value;
        if (questionFood.title == food.title) { 
        } else {
            switch(questionType) {
                case "type":
                    if (food.type != "" && food.type != questionFood.type) {
                        switch (questionFood.type) {
                            case "фрукт":
                            case "овощ":
                            case "орех":
                            case "сухофрукт":
                                if (food.type == "фрукт" || food.type == "овощ" || food.type == "орех" || food.type == "сухофрукт") {
                                    foods.push(food); 
                                }
                                break;
                            case "мясное блюдо":
                            case "суп":
                                if (food.type == "мясное блюдо" || food.type == "выпечка" || food.type == "десерт" || food.type == "суп") {
                                    foods.push(food); 
                                }
                                break;
                            case "выпечка":
                            case "десерт":
                                if (food.type == "мясное блюдо" || food.type == "суп") {
                                    foods.push(food); 
                                }
                                break;                                                
                        }
                    }
                    break;
                case "sweetness":
                    if (food.sweetness != "" && food.sweetness != questionFood.sweetness) {
                        foods.push(food);
                    }
                    break;
                case "ingredient":
                    if (food.ingredient != "" && food.ingredient != questionFood.ingredient) {
                        switch (questionFood.ingredient) {
                            case "из муки":
                                if (food.ingredient == "из мяса" || food.ingredient == "из рыбы") {
                                    foods.push(food); 
                                }
                                break;
                            default:
                                foods.push(food);
                        }
                      
                    }
                    break;  
            }
        }
    } 
    var index = randomIndex(foods);
    food = foods[index];
    return food;
}

///////////////////////////////////////////////////////////////////////////////
///////////////      Получение кодов покрытий, классов и т.п.    //////////////
///////////////////////////////////////////////////////////////////////////////

function getIngredientCode(food) {
    var res = "0";
    switch(food.ingredient) {
        case "из молока":
            res = "1";
            break;
        case "из мяса":
            res = "2";
            break;
        case "из яиц":
            res = "3";
            break;
        case "из муки":
            res = "4";
            break;                                                                                                    
    }
    return res;
}

function getSweetnessCode(food) {
    var res = "0";
    switch(food.sweetness) {
        case "сладкое":
            res = "1";
            break;
        case "не сладкое":
            res = "2";
            break;                                                                                                 
    }
    return res;
}

function getFoodTypeCode(food) {
    var res = "0";
    switch(food.type) {
        case "фрукт":
            res = "1";
            break;
        case "ягода":
            res = "2";
            break;
        case "орех":
            res = "3";
            break;
        case "сухофрукт":
            res = "4";
            break;   
        case "овощ":
            res = "5";
            break; 
        case "мясо":
            res = "6";
            break; 
        case "рыба":
            res = "7";
            break;  
        case "мясное блюдо":
            res = "8";
            break;  
        case "суп":
            res = "9";
            break;  
        case "десерт":
            res = "10";
            break;  
        case "выпечка":
            res = "11";
            break;                                                                                                           
    }
    return res;
}
