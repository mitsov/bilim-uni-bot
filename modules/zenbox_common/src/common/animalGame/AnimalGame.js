function getAnimal(questionType) {
    var $session = $jsapi.context().session;
    var animal;
    var numberOfAnimals = getNumberOfAnimals();
    var animals = [];
    for (var j = 1; j <= numberOfAnimals; j++) {
        animal = $Animals[j].value;
        if ($session.lastQuiz && $session.lastQuiz.animal && $session.lastQuiz.animal.title == animal.title) { 
        } else {
            switch(questionType) {
                case "savagery":
                    if (animal.savagery != "") {
                        animals.push(animal);
                    }
                    break;
                case "class":
                    if (animal.class != "") {
                        animals.push(animal);
                    }
                    break;
                case "numExtremities":
                    if (animal.numExtremities != "") {
                        animals.push(animal);
                    }
                    break;  
                case "parts":
                    if (animal.wings != "" || animal.fins != "" || animal.horns != "" || animal.hoof != "" || animal.penny != "" || animal.trunk != "" || animal.tusks != "") {
                        animals.push(animal);
                    }
                    break;
                case "employment":
                    if (animal.employment != "") {
                        animals.push(animal);
                    }
                    break;   
                case "coating":
                    if (animal.coating != "") {
                        animals.push(animal);
                    }
                    break;    
                case "wings":
                    if (animal.wings != "") {
                        animals.push(animal);
                    }        
                    break;            
                case "fins":
                    if (animal.fins != "") {
                        animals.push(animal);
                    }        
                    break;     
                case "horns":
                    if (animal.horns != "") {
                        animals.push(animal);
                    }        
                    break;           
                case "hoof":
                    if (animal.hoof != "") {
                        animals.push(animal);
                    }        
                    break;            
                case "penny":
                    if (animal.penny != "") {
                        animals.push(animal);
                    }        
                    break;         
                case "trunk":
                    if (animal.trunk != "") {
                        animals.push(animal);
                    }        
                    break;          
                case "tusks":
                    if (animal.tusks != "") {
                        animals.push(animal);
                    }        
                    break; 
            }
        }
    } 
    var index = randomIndex(animals);
    animal = animals[index];
    return animal;
}

function getNumberOfAnimals() {
    var i = 1;
    var toBeContinue = true;
    var test;
    while (toBeContinue) {
        try {
          test = $Animals[i].value;
            i++;          
        } catch (err) {
            i--;            
            toBeContinue = false;
        }
    }
    return i;
}

function getPairAnimal(questionAnimal, questionType) {
    var animal;
    var numberOfAnimals = getNumberOfAnimals();
    var animals = [];
    for (var j = 1; j <= numberOfAnimals; j++) {
        animal = $Animals[j].value;
        if (questionAnimal.title == animal.title) { 
        } else {
            switch(questionType) {
                case "savagery":
                    if (animal.savagery != "" && animal.savagery != questionAnimal.savagery) {
                        animals.push(animal);
                    }
                    break;
                case "class":
                    if (animal.class != "" && animal.class != questionAnimal.class) {
                        animals.push(animal);
                    }
                    break;
                case "numExtremities":
                    if (animal.numExtremities != "" && animal.numExtremities != questionAnimal.numExtremities) {
                        animals.push(animal);
                    }
                    break;  
                case "employment":
                    if (animal.employment != "" && animal.employment != questionAnimal.employment) {
                        animals.push(animal);
                    }
                    break;   
                case "coating":
                    if (animal.coating != "" && animal.coating != questionAnimal.coating) {
                        animals.push(animal);
                    }
                    break;   
                case "wings":
                    if (animal.wings == "") {
                        animals.push(animal);
                    }        
                    break;            
                case "fins":
                    if (animal.fins == "") {
                        animals.push(animal);
                    }        
                    break;     
                case "horns":
                    if (animal.horns == "") {
                        animals.push(animal);
                    }        
                    break;           
                case "hoof":
                    if (animal.hoof == "") {
                        animals.push(animal);
                    }        
                    break;            
                case "penny":
                    if (animal.penny == "") {
                        animals.push(animal);
                    }        
                    break;         
                case "trunk":
                    if (animal.trunk == "") {
                        animals.push(animal);
                    }        
                    break;          
                case "tusks":
                    if (animal.tusks == "") {
                        animals.push(animal);
                    }        
                    break; 
            }
        }
    } 
    var index = randomIndex(animals);
    animal = animals[index];
    return animal;
}

///////////////////////////////////////////////////////////////////////////////
///////////////       Проверка наличия части у животного         //////////////
///////////////////////////////////////////////////////////////////////////////

// Не используется. Возможно пригодится в будущем
/*function checkAnimalParts(animal, partCode) {
    var res = false;
    switch(partCode) {
        case "1":
            res = animal.wings != "" ? true : false;
            break;
        case "2":
            res = animal.fins != "" ? true : false;
            break;
        case "3":
            res = animal.horns != "" ? true : false;
            break;
        case "4":
            res = animal.hoof != "" ? true : false;
            break;
        case "5":
            res = animal.penny != "" ? true : false;
            break;
        case "6":
            res = animal.trunk != "" ? true : false;
            break;                                   
        case "7":
            res = animal.tusks != "" ? true : false;
            break; 
    } 
    return res;
}*/