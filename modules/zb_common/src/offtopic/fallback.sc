theme: /Offtopic

    state: Fallback
        a: {{selectRandomArg($global.OfftopicAnswers["Fallback"])}}
        go!: /Offtopic/HowCanIHelpYou?


        state: HowAreYou
            q!: [привет] * как* * [$you|твои|твоё|ваш*] * (дела|де ла|делишки|делаа|дила|себя (чувствуешь|чувствуете)|поживаешь|поживаете|настроение|жизнь) [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [привет] * (кагдила|каг дила|хау а ю) [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [привет] ({как (ты|вы)}|как (живёшь|живёте)) ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  как [$AnyWord] [$AnyWord] [$AnyWord] (твой|ваш|прошел|прошёл) [$AnyWord] [$AnyWord] [$AnyWord]  день ?
            q!: * $you в порядке ?
            q!: * как (житье|житуха|жись|оно|живется|живётся|сама|сами) ?
            q!: * как* (жизнь|живет*|живёш*|жывет*|жывёш*|жевёш*|жевет*|жызнь) ?
            a: {{selectRandomArg($global.OfftopicAnswers["HowAreYou"])}}
            go!: /Offtopic/Fallback

        state: WhatsNew
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  ($what|какие|$tellMe) [у тебя|у вас] (нового|новенького|новости) [$AnyWord] [$AnyWord] [$AnyWord] ?
            a: {{selectRandomArg($global.OfftopicAnswers["News"])}}

        state: YouAreARobot || noContext = true
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  ($you|тыж) [не настоящ*|настоящ*] [правда] ((человек|девочка|мальчик|девушка|живая|женщина|реальн*|реальн* человек)|робот|чатбот|бот|компьютер*|желез*|настоящ*|не настоящ* ) *
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  [у] $you * искус* интел* [$AnyWord] [$AnyWord] [$AnyWord] 
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  ($you|тыж) (не настоящ*|ненастоящ*) [$AnyWord] [$AnyWord] [$AnyWord] 
            q!: [а] $you (машина|инф)
            q!: (привет|пока) (робот|бот|инф) !
            q!: * (место|где) [$you] * работ*
            q!: Кто [же] ты ?
            a: {{selectRandomArg($global.OfftopicAnswers["WhoAreYou"])}}
            go!: /Offtopic/HowCanIHelpYou?


        state: WhatsTheMeaningOfLife
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  в (чём|чем) смысл жизни [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  (какой|в чем) * смысл [в] жизни [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: Что такое люб* ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  жизнь * марс* [$AnyWord] [$AnyWord] [$AnyWord] 
            go!: /Offtopic/Fallback


        state: WhatIsYourName
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  мо* [я] $you * (называть|звать) *[$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  мо* [я] * (называть|звать) $you [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  как* [у] ($you|твоя|ваша) фамилия [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  фамилия (как* твоя|какая у тебя|какая у вас|твоя|ваша) [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  а фамилия какая ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(как*|назови*) [мне] * ($you|твоё|твое|своё|свое) * (зовут|звать|завут|имя|называть|обращат*|обращя*|обращац*)} * ?
            q!: кто (будешь|будете) ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  как* * $you * себя * называе* [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [а] {как [тебя] зовут} ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(тебя|тво*|вас|ваше) * (отчество|имя)} [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  {как* * отчество} [$AnyWord] [$AnyWord] [$AnyWord]  ?
            a: {{selectRandomArg($global.OfftopicAnswers["WhatIsYourName"])}}
            go!: /Offtopic/Fallback

        state: WhereAreYouFrom
            q!: {откуда $you} такая ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  $you  из россии ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  $you  из какой страны [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  в какой стране * (живешь|живёшь|живете|живёте) [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: * (в каком город*|где|откуда|место) [$AnyWord] [$AnyWord] [$you] [$AnyWord] [$AnyWord] (родился|живешь|родом|обитаешь|рождения) * ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  (откуда|от куда) * [$you] * (будешь|будете|родом) ?
            q!: где (твой|ваш) дом ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord] $you (откуда|от куда|в каком город*) ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord] (из|с|в) как* * $you город* * ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  (откуда|от куда|в каком город*) * $you ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  $you (из|с) какого города * ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  [и] {где $you} [сейчас|щас] [находи*] ?
            a: {{selectRandomArg($global.OfftopicAnswers["WhereAreYouFrom"])}}
            go!: /Offtopic/Fallback

        state: WhatDoYouDoForALiving
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(кем|где) [$you]} работ* [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  {[как*] ($you|твоя|ваша|ваши|твои) (работа|профессия|обязанности|функц*)} [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] (ваша|твоя) работа [$AnyWord] [$AnyWord] [$AnyWord] 
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  $you (студентка|работаешь|работаете) ?
            q!: {кто $you} [такая] ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(зачем|почему) $you (здесь|тут)} ?
            a: {{selectRandomArg($global.OfftopicAnswers["WhatDoYouDoForALiving"])}}
            go!: /Offtopic/Fallback

        state: HowOldAreYou
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  (в каком году|когда) $you родил* [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  какой [$AnyWord] [$AnyWord] [$AnyWord] год рождения [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  когда [$you] (родилась|родились) ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(сколько|как много) * (тебе|вам|те) * (лет|годиков)} [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  {[какой] [у] ($you|твой|ваш) возраст} [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  [а] сколько (тебе|вам|те) (лет|годиков) ?
            a: {{selectRandomArg($global.OfftopicAnswers["HowOldAreYou"])}}
            go!: /Offtopic/Fallback

        state: WhoMadeYou
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  [а] кто [$AnyWord] [$AnyWord] (тебя|вас) [$AnyWord] [$AnyWord] (созда*|сделал|разработал*|написал) [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  кто [$AnyWord] [$AnyWord] (тво*|ваш*|тебя|вас) [$AnyWord] [$AnyWord] (автор*|созда*|сделал|разработал*|родител*|придумал*|*программиров*) [$AnyWord] [$AnyWord] [$AnyWord]  ?
            q!: [$AnyWord] [$AnyWord] [$AnyWord]  кто [$AnyWord] [$AnyWord] (автор*|созда*|твор*|сотвор*) [$AnyWord] [$AnyWord] (тво*|ваш*|тебя|вас) [$AnyWord] [$AnyWord] [$AnyWord]  ?
            a: {{selectRandomArg($global.OfftopicAnswers["WhoMadeYou"])}}
            go!: /Offtopic/Fallback