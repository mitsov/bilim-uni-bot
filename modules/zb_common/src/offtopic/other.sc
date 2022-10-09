theme: /Offtopic

    state: WhatCanYouDo
        q!: [$AnyWord] [$AnyWord] (меню|помощь|справка) [$AnyWord] [$AnyWord]
        q!: * {как* вопрос* * [я] мо* [$you] зада*} *
        q!: * {$what * [я] мо* спрос* * $you} *
        q!: * {$what * $you * мо* (расска*|помо*)} *
        q!: * {$what * $you * (мож*|уме*)} *
        q!: * в чем ты разбираешься *
        a: {{selectRandomArg($global.OfftopicAnswers["WhatCanYouDo"])}}

    state: YouMisunderstoodMe
        q!: * $you * [меня] * (не|неправильно) (понимае*|понял*|поняли) * 
        q!: * {(недовол*|не удовлетвор*|не понравил*) [$AnyWord] [$AnyWord] (бесед*|диалог*|общение*)} *
        a: {{selectRandomArg($global.OfftopicAnswers["YouMisunderstoodMe"])}}

    state: Test || noContext = true
        q!: (тест|прием|есть контакт|проверка связи) [прием]
        a: {{selectRandomArg($global.OfftopicAnswers["Test"])}}
        go!: /Offtopic/HowCanIHelpYou?

    state: MyNameIs || noContext = true
        q!: {[$AnyWord] [$AnyWord] меня зовут [$AnyWord] [$AnyWord]}
        q!: * {[$AnyWord] [$AnyWord] $my (имя|прозвище|кличка|ник|никнейм|ник-нейм|ник нейм) [$AnyWord] [$AnyWord] }
        a: {{selectRandomArg($global.OfftopicAnswers["MyNameIs"])}}
        go!: /Offtopic/HowCanIHelpYou?


    state: WaitAMoment || noContext = true
        q!: {(*ждите|подожд*|погод*) [одну|пару] * (минут*|секунд*|немного|немнож*)}
        q!: {(одну|пару) * (минут*|секунд*)}
        q!: (минут*|секунд*|погоди*|погодь*)
        q!:  не (отключай*|переключай*|переключай*|отсодиняй*) 
        a: {{selectRandomArg($global.OfftopicAnswers["WaitAMoment"])}}