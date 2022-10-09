require: params.yaml
   var = SmallTalkParams
   name = SmallTalkParams
   
require: patterns.sc
  module = zb_common

require: ../../common/smalltalk/SmallTalk.js
require: ../../common/common.sc


patterns:
    $gender = (
        (мальч*|парень|муж*|не (дев*|жен*)):0|
        (дев*|жен*|не (дев*|мальчик|муж*)):1)
    $nAffront = (сука:сукой|проститутка:проституткой|сучка:сучкой|шлюха:шлюхой|шалава:шалавой|ебанутая:ебанутой|тварь:тварью|блядь:блядью|стерва:стревой|шлюшка:шлюшкой|хуесоска:хуесоской|ебанашка:ебанашкой|пидараска:пидараской)

theme: /SmallTalk

    state: Unit

        state: Acquaintance
            q!: {(кто|что) (ты|вы)} [так*]
            q!: * [давай*/мож*] {(скаж*/расскаж*/рассказ*/покаж*/показ*/говор*/поговор*) [$you] [$me] [$pls]} * {кто * $you} [~такой] *
            q!: * [давай*/мож*] {(скаж*/расскаж*/рассказ*/покаж*/показ*/говор*/поговор*) [$you] [$me] [$pls]} * [что нибудь/что-нибудь] * [о/про] (себе/себя/тебе/тебя) *
            q!: * {[ты] * (пришелец/инопланетянин/космонавт/(из/с) (другой планеты/космоса))} *
            if: SmallTalkParams.unitAcquaintance
                script:
                    $reactions.answer(SmallTalkParams['unitAcquaintance']);
            else:
                go!: ../../Fallback

        state: Age
            q!: * (в каком году|когда) $you родил* * 
            q!: * как* * год рождения * 
            q!: * когда [$you] (родилась|родились) ?
            q!: * (ско*|как много) * (лет|годиков) * [тебе|вам|те] 
            q!: * {(ско*|сколько|как много) * (тебе|вам|те) * (лет|годиков|годков)} * 
            q!: * {[как*] * [у] ($you|твой|ваш) * возраст} * 
            q!: * [а] ско* (тебе|вам|те) 
            q!: [а] (лет|годиков)
            q!: * $you * (молод*|стар*|юн*) [робот] *
            q!: * $you много (годиков|лет) *
            q!: тебе лет
            if: SmallTalkParams.unitAge
                script:
                    $reactions.answer(SmallTalkParams['unitAge']);
            else:
                go!: ../../Fallback

        state: Author
            q!: * [а] кто * (тебя|вас) * (созда*|сделал|разработал*|написал) *
            q!: * кто * (тво*|ваш*|тебя|вас) * (автор*|созда*|сделал|разработал*|родител*|придумал*) * 
            q!: * кто * (автор*|созда*|твор*|сотвор*) * (тво*|ваш*|тебя|вас) * 
            if: SmallTalkParams.unitAuthor
                script:
                    $reactions.answer(SmallTalkParams['unitAuthor']);
            else:
                go!: ../../Fallback

        state: Name
            q!: * мо* [я] $you * (называть|звать) *
            q!: * мо* [я] * (называть|звать) $you * 
            q!: * как* [у] ($you|твоя|ваша) фамилия * 
            q!: * фамилия (как* твоя|какая у тебя|какая у вас|твоя|ваша) *
            q!: * а фамилия какая 
            q!: * {(как*|назови*) [мне] * ($you|твоё|твое|своё|свое) * (зовут|звать|завут|имя|называть|обращат*|обращя*|обращац*)} *
            q!: кто (будешь|будете) 
            q!: * как* * $you * себя * называе* *
            q!: [а] {как [тебя] (зовут|звать)}
            q!: * {(тебя|тво*|вас|ваше) * (отчество|имя)} *
            q!: * {как* * отчество} *
            q!: * давай * (знакомиться|познакомимся) *
            if: SmallTalkParams.unitName
                script:
                    $reactions.answer(SmallTalkParams['unitName']);
            else:
                go!: ../../Fallback

        state: Gender
            q!: * {$you $gender} *
            q!: * {~какой ([у] $you) (~пол/гендер*)} *
            q!: * ты {маль* или (девочк*/девч*)} *
            if: SmallTalkParams.unitGender
                script:
                    $reactions.answer(SmallTalkParams['unitGender']);
            else:
                go!: ../../Fallback

        state: WhatAreYouDoing
            q!: * {чем [ты] [$now] занимаешься} *
            q!: * {(что/че) [ты] [$now] делаешь} *
            if: SmallTalkParams.unitWhatAreYouDoing
                script:
                    $reactions.answer(SmallTalkParams['unitWhatAreYouDoing']);
            else:
                go!: ../../Fallback

        state: WhatCanYouDo
            q!: * [а] [что] $you [еще|ещё] * (умеешь*|умеете*|можешь*|можете*) [делать] *
            q!: * {чему [$you] * научился} *
            q!: * умее* делать *
            q!: * как* * $you * (сервисы/программы) *
            q: ([и] [это] (все|всё)|* (еще|ещё) *) || fromState = ../WhatCanYouDo
            if: SmallTalkParams.unitWhatCanYouDo
                script:    
                    $reactions.answer(SmallTalkParams['unitWhatCanYouDo']);
            else:
                go!: ../../Fallback

        state: Family
            q!: * (у тебя (есть/большая)|хочу зна*|кто|где|~какой) * [тво*|сво*] (сем*|родств*|родн*|сестр*|брат*) *
            q!: * (скаж*/расскаж*/рассказ*/показ*/говор*/поговор*) [$me] * [о/про] [тво*|сво*] (сем*|родств*|родн*|сестр*|брат*) *
            if: SmallTalkParams.unitFamily                 
                script:    
                    $reactions.answer(SmallTalkParams['unitFamily']);
            else:
                go!: ../../Fallback

        state: YouAreStupid
            q!: * {(недовол*|не удовлетвор*|не понравил*) * (бесед*|диалог*|общение*)} *
            q!: * [$you] * $stupid *
            q!: * (учи|выучи) * русск* *
            q!: * русск* *учи* *
            q!: * (что ты (имеешь в виду|хочешь сказать)| в каком смысле|поясни)
            q!: я [что] * непонятно (говорить/сказать) *
            q!: я [у] [теб*] друг* (~сказать/~спросить/~говорить)
            q!: я думал* ты умн*
            q!: * $you * ($no/не) * $clever *
            if: SmallTalkParams.unitYouAreStupid
                script:    
                    $reactions.answer(SmallTalkParams['unitYouAreStupid']);
            else:
                go!: ../../Fallback

        state: YouAreClever
            q!: {[$you] * [очень|такой|какой] * $clever}
            if: SmallTalkParams.unitYouAreClever  
                script:    
                    $reactions.answer(SmallTalkParams['unitYouAreClever']);
            else:
                go!: ../../Fallback

        state: WhatDoYouLike
            q!: * [как*] [у] $you [есть] * [любим*] (хобби|интерес*) 
            q!: * у $you хобби * есть *
            q!: * чем [$you] увлекае* 
            q!: * $question [$you] [нрав*|люб*]  (заним*|заниматься|интересно) * [в свободное время] * 
            q!: * (твоё|твое|ваше|какое|твои|тебя) (хобби|увлечени*)
            q!: * $question [$you] (увлекаешься|увлекаетесь|(делаешь в|проводишь) свободное время|интересуешься|интересуетесь) * 
            q!: что $you нравится 
            q!: * у $you * есть * хобби *  
            q!: хобби
            if: SmallTalkParams.unitWhatDoYouLike
                script:    
                    $reactions.answer(SmallTalkParams['unitWhatDoYouLike']);
            else:
                go!: ../../Fallback

    state: Appraisal

        state: ThankYou
            q!: * $thanks *
            if: SmallTalkParams.appraisalThankYou
                script:
                    $reactions.answer(SmallTalkParams['appraisalThankYou']);
            else:
                go!: ../../Fallback

        state: Good
            q!: * $good *
            if: SmallTalkParams.appraisalGood
                script:
                    $reactions.answer(SmallTalkParams['appraisalGood']);
            else:
                go!: ../../Fallback

        state: YouAreWelcome
            q!: [всегда] пожалуйста
            if: SmallTalkParams.appraisalYouAreWelcome 
                script:
                    $reactions.answer(SmallTalkParams['appraisalYouAreWelcome']);
            else:
                go!: ../../Fallback

    state: Dialog

        state: YouDontUnderstand
            q!: * $you * [меня] * не (понимае*|понял|поняла|поняли) * 
            q!: * $you * [меня] (понимае*|поняла|поняли) * 
            q!: * что * (тебе|ты) * (*яснить|не (понимаешь|понял|можешь понять)) *            
            if: SmallTalkParams.dialogYouDontUnderstand
                script:
                    $reactions.answer(SmallTalkParams['dialogYouDontUnderstand']);
            else:
                go!: ../../Fallback

        state: Affront
            q!: * ($obsceneWord/$nAffront) *
            if: SmallTalkParams.dialogAffront
                script:
                    $reactions.answer(SmallTalkParams['dialogAffront']);
            else:
                go!: ../../Fallback

    state: Greetings

        state: Bye
            q!: * [спасибо] ($bye / спи / усни / засни / поспи / засыпай/иди спать/*баюшки*) *
            q!: увидимся
            q!: * я (пошел/пошла/пойду/поеду/уезжаю/{прощаюсь (с тобой)})
            q!: пока
            q!: мне пора [~прощаться/~идти/~убегать/~спать/~есть]
            q!: * все отдыхай
            q!: попрощайся [с/со] *
            if: SmallTalkParams.greetingsBye
                script:
                    $reactions.answer(SmallTalkParams['greetingsBye']);
            else:
                go!: ../../Fallback

        state: Hello
            q!: * $hello *
            q!: давн* не виделись
            q!: это я
            q: $me * (пришла|пришел|вернулась|вернулся|тут|дома) || fromState = ../Bye, onlyThisState = true
            if: SmallTalkParams.greetingsHello
                script:
                    $reactions.answer(SmallTalkParams['greetingsHello']);
            else:
                go!: ../../Fallback

        state: How are you
            q!: * [$hello] * как* * [$you|твои|твоё|ваш*] * (дела|де ла|делишки|делаа|дила|себя (чувствуешь|чувствуете)|поживаешь|поживаете|настроение|жизнь|настрой) * 
            q!: * [$hello] * (кагдила|каг дила|хау а ю) * 
            q!: [$hello] ({как (ты|вы)}|как (живёшь|живёте)) 
            q!: * [$hello] * как * [твой|ваш|прошел|прошёл] * день 
            q!: * [$hello] * $you в порядке 
            q!: * [$hello] * как (житье|житуха|жись|оно|живется|живётся|сама|сами) 
            q!: * [$hello] * как* (жизнь|живет*|живёш*|жывет*|жывёш*|жевёш*|жевет*|жызнь)
            q!: [ну] (че|чо|что) как
            q!: * [а] {(у тебя|твои) дела}
            q!: * [$hello] * $you все ($good|$normal|$bad)
            q!: * $you * себя хорошо (чувствуешь|чувствуете) *
            q!: тебя дела
            if: SmallTalkParams.greetingsHowAreYou
                script:
                    $reactions.answer(SmallTalkParams['greetingsHowAreYou']);
            else:
                go!: ../../Fallback

    state: User

        state: ILikeYou
            q!: * [я] {[очень] (люблю|~влюбиться|обожаю) [в] тебя}
            q!: {я люблю тебя}
            q!: * $you $me [тоже] [очень] [сильно] (нрав*|[так] нравишься|возбуждаешь)
            q!: * {мне [тоже] нравишься ты}
            q!: [$you] {[$me] [самый] (любим*|любовь|зайка)} *
            q!: (ай лав ю|ай ловью)
            q!: {[я] лю тя}
            if: SmallTalkParams.userILikeYou
                script:
                    $reactions.answer(SmallTalkParams['userILikeYou']);
            else:
                go!: ../../Fallback
                
    state: Fallback
        q!: *
        script:
            $reactions.answer(SmallTalkParams['fallback']);
