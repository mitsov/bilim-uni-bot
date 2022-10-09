///////////////////////////////////////////////////////////////////////////////
///////////////        Генерация вопроса по календарю            //////////////
///////////////////////////////////////////////////////////////////////////////
function getCalendarQuestion() {
    var $temp = $jsapi.context().temp;
    var $session = $jsapi.context().session;
    var res, right;
    var questionType = getRandomQuestionType("nextMonth","prevMonth","days","season","seasonNumber","monthNumber",
        "nextWeekday","prevWeekday","beforeYesterday","afterTomorrow","calcNextWeekday","calcPrevWeekday",
        "nextSeason","prevSeason");
    switch(questionType) {
        case "nextMonth":
            $temp.month1 = getRandomElement($Months);
            if ($temp.month1.number == "12") {
                var month2 = $Months[1].value;
            } else {
                var month2 = $Months[parseInt($temp.month1.number)+1].value;
            }
            res = resolveInlineDictionary(selectRandomArg("Какой месяц {следует/следующий} за " + $nlp.inflect($temp.month1.name, 'ablt') + "?",
                "Если сейчас " + $temp.month1.name + ", то какой месяц следующий?",
                "{Скажи, какой месяц идёт/Какой месяц наступает} после " + $nlp.inflect($temp.month1.name, 'gent') + "?"));
            right = month2.name;
            break;
        case "prevMonth":
            var month1 = getRandomElement($Months);
            if (month1.number == "1") {
                var month2 = $Months[12].value;
            } else {
                var month2 = $Months[parseInt(month1.number)-1].value;
            }
            res = selectRandomArg("Какой месяц идёт перед " + $nlp.inflect(month1.name, "ablt") + "?",
                "Какой месяц предыдущий для " + $nlp.inflect(month1.name, "gent") + "?",
                "Если сейчас " + month1.name + ", то какой месяц был до этого?",
                "Скажи, какой месяц идёт перед " + $nlp.inflect(month1.name, "ablt") + "?",
                "Какой месяц закончился, если начался " + month1.name + "?");
            right = month2.name;
            break;
        case "days":
            var month = getRandomElement($Months);
            res = selectRandomArg("Сколько дней в " + $nlp.inflect(month.name, "loct") + "?",
                "Какое количество дней в месяце " + month.name + "?",
                "Скажи, сколько в " + $nlp.inflect(month.name, "loct") + " дней?");
            right = month.days;
            break;
        case "season":
            var month = getRandomElement($Months);
            res = selectRandomArg("Какое сейчас время года, если за окном " + month.name + "?",
                "К какому времени года относится " + month.name + "?",
                capitalize(month.name) + " - это месяц какого времени года?");
            right = month.season;
            break;
        case "seasonNumber":
            var month = getRandomElement($Months);
            res = selectRandomArg("Назови " + getOrdinalNumber(month.seasonNumber) + " месяц " + $nlp.inflect(month.season, "gent") + ".",
                "Какой месяц " + $nlp.inflect(month.season, "gent") + " " + getOrdinalNumber(month.seasonNumber) + " по счёту?",
                "Какое название " + $nlp.inflect(getOrdinalNumber(month.seasonNumber), "gent") + " месяца " + $nlp.inflect(month.season, "gent") + "?",
                "Как называется " + getOrdinalNumber(month.seasonNumber) + " месяц " + $nlp.inflect(month.season, "gent") + "?");
            right = month.name;
            break;
        case "monthNumber":
            var month = getRandomElement($Months);
            res = selectRandomArg("Каким по счёту месяцем года является " + month.name + "?",
                capitalize(month.name) + " - это какой по порядку месяц года?");
            right = month.number;
            break;
        case "nextWeekday":
            var weekday1 = getRandomElement($Weekdays);
            if (weekday1.number == "7") {
                var weekday2 = $Weekdays[1].value;
            } else {
                var weekday2 = $Weekdays[parseInt(weekday1.number)+1].value;
            }
            res = selectRandomArg("Если сегодня " + weekday1.name + ", то какой день недели завтра?",
                "Какой день недели следует за " + $nlp.inflect(weekday1.name, "ablt") + "?",
                "Какой следующий день недели после " + $nlp.inflect(weekday1.name, "gent") + "?");
            right = weekday2.name;
            break;
        case "prevWeekday":
            var weekday1 = getRandomElement($Weekdays);
            if (weekday1.number == "1") {
                var weekday2 = $Weekdays[7].value;
            } else {
                var weekday2 = $Weekdays[parseInt(weekday1.number)-1].value;
            }
            res = selectRandomArg("Если сегодня " + weekday1.name + ", то какой день недели был вчера?",
                "Какой день недели идёт перед " + $nlp.inflect(weekday1.name, "ablt") + "?",
                "Какой предыдущий день недели перед " + $nlp.inflect(weekday1.name, "ablt") + "?");
            right = weekday2.name;
            break;
        case "beforeYesterday":
            var weekday1 = getRandomElement($Weekdays);
            if (weekday1.number == "2") {
                var weekday2 = $Weekdays[7].value;
            } else if (weekday1.number == "1") {
                var weekday2 = $Weekdays[6].value;
            } else {
                var weekday2 = $Weekdays[parseInt(weekday1.number)-2].value;
            }
            res = "Если сегодня " + weekday1.name + ", то какой день недели был позавчера?";
            right = weekday2.name;
            break;
        case "afterTomorrow":
            var weekday1 = getRandomElement($Weekdays);
            if (weekday1.number == "6") {
                var weekday2 = $Weekdays[1].value;
            } else if (weekday1.number == "7") {
                var weekday2 = $Weekdays[2].value;
            } else {
                var weekday2 = $Weekdays[parseInt(weekday1.number)+2].value;
            }
            res = "Если сегодня " + weekday1.name + ", то какой день недели будет послезавтра?";
            right = weekday2.name;
            break;
        case "calcNextWeekday":
            var task = selectRandomArg("завтра","послезавтра","через 2 дня","через 3 дня");
            res = selectRandomArg("Какой день недели будет " + task + "?");
            switch(task) {
                case "завтра":
                    right = currentDate().add(1, "days").locale('ru').format('dddd');
                    break;
                case "послезавтра":
                case "через 2 дня":
                    right = currentDate().add(2, "days").locale('ru').format('dddd');
                    break;
                case "через 3 дня":
                    right = currentDate().add(3, "days").locale('ru').format('dddd');
                    break;                                        
            }
            break;
        case "calcPrevWeekday":
            var task = selectRandomArg("3 дня назад","2 дня назад","позавчера","вчера");
            res = selectRandomArg("Какой день недели был " + task + "?",
                capitalize(task) + " - какой был день недели?");
            switch(task) {
                case "3 дня назад":
                    right = currentDate().subtract(3, "days").locale('ru').format('dddd');
                    break;
                case "2 дня назад":
                case "позавчера":
                    right = currentDate().subtract(2, "days").locale('ru').format('dddd');
                    break;
                case "вчера":
                    right = currentDate().subtract(1, "days").locale('ru').format('dddd');
                    break;                                        
            }
            break;
        case "nextSeason":
            var season1 = selectRandomArg("зима","весна","лето","осень");
            switch(season1) {
                case "зима":
                    var season2 = "весна";
                    break;
                case "весна":
                    var season2 = "лето";
                    break;    
                case "лето":
                    var season2 = "осень";
                    break;
                case "осень":
                    var season2 = "зима";
                    break;                                       
            }
            res = selectRandomArg("Какое время года следует за " + $nlp.inflect(season1, "ablt") + "?",
                "Какое время года наступает после " + $nlp.inflect(season1, "gent") + "?");
            right = season2;
            break;
        case "prevSeason":
            var season2 = selectRandomArg("зима","весна","лето","осень");
            switch(season2) {
                case "зима":
                    var season1 = "весна";
                    break;
                case "весна":
                    var season1 = "лето";
                    break;    
                case "лето":
                    var season1 = "осень";
                    break;
                case "осень":
                    var season1 = "зима";
                    break;                                       
            }
            res = selectRandomArg("Какое время года идёт перед " + $nlp.inflect(season1, "ablt") + "?",
                "Какое время года предшествует " + $nlp.inflect(season1, "datv") + "?");
            right = season2;
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
        right: right,
        numQuestions: countQuestions,
        rightAnswers: countAnswers
    };

}

function getOrdinalNumber(number) {
    switch(number) {
        case "1":
            return "первый";
        case "2":
            return "второй";
        case "3":
            return "третий";         
    }
    return "неизвестно какой";
}