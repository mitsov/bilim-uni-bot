(function() {
   
    function checkSameAnswer($context) {
        var session = $context.session;
        var response = $context.response;
        session.catchAll = session.catchAll || {};

        //проверяем, не собирается ли бот выдать тот же ответ, что в прошлый раз
        var repetition = false;

        if (session.lastAnswer && session.lastAnswer === response.answer) {
            repetition = true;
        }
        session.lastAnswer = response.answer;    

        var context = $context.currentState;
        session.lastState = context;

        if (repetition) {        
            if (!context.startsWith("/CatchAll") && !context.endsWith('CatchAll')) {
                // Переходы не работают в pre/postProcess
                // Потому делаем все нужные действия прямо здесь
                // 1. Стираем ответ выданый основным сценарием
                response.replies = [];
                response.answer = ""; //добавила, потому что Resterisk берет ответ из response.answer
                // 2. Перетираем контекс
                session.contextPath = "/CatchAll/SameAnswer"; // это путь контекста для разбора следующей фразы
                $context.currentState = "/CatchAll/SameAnswer"; // это путь текущего состояния, который подставляется в ответ и далее проверяется в тесте
                // 3. Формируем ответ
                $reactions.answer(getAnswer('SameAnswer'));
            }
        }
    }

    bind("postProcess", function($context){
        if(catchAll.CheckSameAnswer){
            checkSameAnswer($context);
        }
    });

})();
