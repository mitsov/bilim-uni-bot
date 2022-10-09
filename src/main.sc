require: common.js
    module = zb_common
require: patterns.sc
    module = zb_common
require: src/offtopic/offtopic.sc
    module = zb_common

theme: /

    state: start
        q!: *start
        a: Это служба поддержки ВШЭ. Пожалуйста, введите вопрос.
        buttons:
            "F.A.Q."
            
        
    state: qa
        q!: *
        script:
            var text = $jsapi.context().request.query;
            var rslt = $caila.inference({"phrase":{"text":text}, "nBest": 1, knownSlots: [{"name":"a", "value":"b"}]});
            $reactions.answer(rslt['variants'][0]['intent']['answer']);
        buttons:
            "F.A.Q."
    
    state: faq3
        q!: F.A.Q.
        a: Выберите подходящий вопрос или напишите свой.
        buttons:
            "Не могу авторизоваться в системе Экзамус"
            "Не вижу свой экзамен"
            "Нет доступа к экзамену (ограничен вход)"
            "Не могу поменять пароль"
            "Не могу войти через личный кабинет"
            "Изображение с веб-камеры - не проходит проверку"
            "Трансляция веб-камеры и рабочего стола не работает"
            "Нет доступа к экрану при проверке компьютера"
            "Система зависла на этапе Поделиться экраном"
            "Не могу отправить фото паспорта"
            "Фото паспорта отправлено, дальше ничего не происходит"
            "Появляется окно с запросом на повторный ввод логина/пароля" 
            "Экран показывает Установка связи"
            "Ваш сеанс, по-видимому, истек"
            "Потратил много времени на проверку компьютера"
            "Прервалась связь во время экзамена"
            "Не проходит проверка компьютера"
            "Сохранились ли ответы экзамена"
            "Плохое сетевое соединение"
            "Не загрузился файл с заданием"
            "Не сохранились ответы"