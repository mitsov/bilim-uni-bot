# Этот фрагмент сценария отвечает за обработку сообщения /start и создание новой сессии
# Сценарий имеет 2 параметра:
# * $injector.startState - стартовое состояние, которое выводится по /start, по умолчанию /
# * $injector.timeout - таймаут ожидания вопроса от клиента, по которому начинается новая сессия, по умолчанию 120минут
# Дополнительные паттерны могут быть определены в родительском сценарии с помощью toState

theme: /

    init:
        $global.startState = $injector.newSessionStartState || "/";
        $global.newSessionTimeout = ($injector.newSessionTimeout || 120) * 60 * 1000;

        $global.newSession = function($context) {
            var query = $context.request.query || "/start";
            $context.request.data.newSession = true;
            $context.request.data.targetState = $context.temp.targetState || $context.currentState;
            $reactions.newSession({message: query, data: $context.request.data});
        }

    init:
        bind("postProcess", function($context) {
            $context.session.lastActiveTime = $jsapi.currentTime();
        });

        bind("preProcess", function($context) {
            var $request = $context.request;
            var $temp = $context.temp;
            var $session = $context.session;
            if ($request.data.targetState) {
                $temp.targetState = $request.data.targetState;
            } else if ($request.query && $session.lastActiveTime) {
                var interval = $jsapi.currentTime() - $session.lastActiveTime.valueOf();
                if (interval > newSessionTimeout) {
                    newSession($context);
                }
            }
        });


    state: Start
        q!: * *start
        script:
            if (!$request.data.newSession && !$context.testContext) {
                newSession($context);
            }
        go!: {{ startState }}