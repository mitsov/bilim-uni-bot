theme: /Offtopic

    state: Greetings
           
        state: Hello
            q!: {(утро|утра|день|дня|вечер*|ночи) (добрый|доброй|доброе|доброго|бодрый|бодрое) [$bot]}
            q!: {(утро|утра|день|дня|вечер*|ночи) (добрый|доброй|доброе|доброго|бодрый|бодрое)} [$AnyWord] [$AnyWord] [$AnyWord] (поможешь|поможете|помочь)
            q!: давн* не виделись !
            q!: {(салют|салютик|салютики|salut|salute|привет|привеет|приивеет|приветик|приветики|при вед|здравствуй|здравствуй*|здрасьте|сдрасьте|сдравствуй*|здавст*|хэй|хай|хэллоу|халоу|здаров|шалом|hello|здарова|хелло|здаровеньки|здоровеньки|здорова|здорово|здрасте|здрасти|здрасьти|салют|hi|приветствую|доброго времени суток|хеллоу|п р и в е т|здраствуй*|добро пожаловать|здоров) [$bot|парень]}
            go!: /Offtopic/HowCanIHelpYou?

        state: GoodBye
            q!: [$AnyWord] [$AnyWord] [$AnyWord] (спокой* ночи|споки|споки-споки|споки споки|спокиспоки) [$AnyWord] [$AnyWord] [$AnyWord] !
            q!: [$AnyWord] [$AnyWord] [$AnyWord] [ладно|все|давай|ну] (до свидан*|до встречи|до завтра|до связи|конец связи|досвидани*|досвидос|дозавтра|прощай|бай бай|гудбай|чау|чао|чмоки чмоки|пока-пока|п о к а|всего (хорошего|доброго)) [$AnyWord] [$AnyWord] [$AnyWord]
            q!: [ладно|все|давай|ну] (увидимся|счастливо|удачи|бывай|пока) !
            q!: * (хорошего|приятного|доброго) * (дня|вечера) [вам|тебе] !
            a: {{selectRandomArg($global.OfftopicAnswers["GoodBye"])}}
            go: /

    state: HowCanIHelpYou?
        a: {{selectRandomArg($global.OfftopicAnswers["HowCanIHelpYou?"])}}
        
        state: Yes
            q!:  { (*можете|*можешь) [$AnyWord] [$AnyWord] [$AnyWord] помочь } 
            q: * [думаю] (да|*можете|*можешь|надеюсь|хотелось бы) *
            a: {{selectRandomArg($global.OfftopicAnswers["CanHelp"])}}

        state: No
            q: * [да] [уже/нет/неат/ниат/неа/ноуп/ноу/найн/не]  (ничем|ни [о] чем|нечем|не чем|не надо|не нужно) [спасибо] *
            q: [$AnyWord] [да] (нет/неат/ниат/неа/ноуп/ноу/найн/не) [$AnyWord]
            a: {{selectRandomArg($global.OfftopicAnswers["CanNotHelp"])}}

    state: CanIAskYouAQuestion || noContext = true
        q!: * {(могу|можно) [я] (зада*|зада*|спросить|спрошу|поинтерес*) * [вопрос*]} ?
        q!: * $you [може*|сможешь] (ответи*|справи*) (на|с) (люб*|произвольн*) вопрос* *
        q!: * $you [все|ещё] [ещё|еще] (тут|здесь|{[меня] (слушаете|слушаешь|слышите)}) ?
        q!: [$AnyWord] [$AnyWord] {([ещё|еще] один|новый|другой|еще) вопрос} [$AnyWord] [$AnyWord]
        q!: * [я] хотел* спросить
        q!: есть тут кто *
        go!: /Offtopic/HowCanIHelpYou?


    state: ThankYou
        q!:  [$AnyWord] [$AnyWord] [$AnyWord] {[окей] $thanks [$bot] [за] [$AnyWord] [ответ*|помощь|информацию]}
        q!: [$AnyWord] [$AnyWord] [$AnyWord] {мне (понятно|понятненько|ясно|ясненько|понял*) [все|всё]} [$AnyWord] [$AnyWord] [$AnyWord]
        q!: [$AnyWord] [$AnyWord] [$AnyWord] {(понятно|понятненько|ясно|ясненько|понял*) (все|всё)} [$AnyWord] [$AnyWord] [$AnyWord]
        q!: [$AnyWord] [$AnyWord] (понятно|понятненько|ясно|ясненько|понял*|хорошо) [$AnyWord] [$AnyWord]
        q!: * [премного] благодарн* *
        a: {{selectRandomArg($global.OfftopicAnswers["ThankYou"])}}
        a: {{selectRandomArg($global.OfftopicAnswers["MoreQuestions?"])}}

        state: HaveAQuestion
            q!: [$AnyWord] [$AnyWord] [$AnyWord] [у меня] {(вопрос*|проблем*) (имею|есть)} [$AnyWord] [$AnyWord] [$AnyWord]
            q: * ({вопрос* (имею|есть|у меня еще)}|есть|да|остались) *
            go!: /Offtopic/HowCanIHelpYou?

        state: NoQuestions
            #q!: [$AnyWord] [$AnyWord] [$AnyWord] {[больше] вопросов (не имею|нет)} [$AnyWord] [$AnyWord] [$AnyWord]
            q: * ({[больше] [пока] вопросов (не имею|нет)}|[$thanks] нет [$thanks]) *
            q: * {[$thanks] * (все понятно)} *
            q: * [да] (нет/неат/ниат/неа/ноуп/ноу/найн/не) *
            q: * не (остались/осталось/остался) *
            q!: [$AnyWord] да [$AnyWord] [$AnyWord] все 
            a: {{selectRandomArg($global.OfftopicAnswers["NoQuestions"])}}

    state: OK || noContext = true
        q!: [$AnyWord] [$AnyWord] (ok|ок|окей|ага|ладно)[$AnyWord] [$AnyWord]
        a: {{selectRandomArg($global.OfftopicAnswers["OK"])}}