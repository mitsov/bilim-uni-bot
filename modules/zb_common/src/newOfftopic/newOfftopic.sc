require: newOfftopic.yaml
    var = newCommonOfftopic

require: fallbackLogic.yaml
    var = defaultFallbackLogic

require: answer.js

require: newOfftopic.js

require: music-artists.csv
  name = MusicArtists
  var = MusicArtists

require: ../patterns.sc

require: ../namesRu/names-ru.csv
  name = NamesRu
  var = NamesRu

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }
    $global.$converters
        .NamesRuConverter = function(parseTree) {
            var id = parseTree.NamesRu[0].value;
            return NamesRu[id].value;
        };
    $global.$converters
        .MusicArtistsTagConverter = function(parseTree) {
            var id = parseTree.MusicArtists[0].value;
            return MusicArtists[id].value;
        };
    $global.newOfftopicAnswers = (typeof specialOfftopic !== "undefined") ? applyCustomAnswers(newCommonOfftopic, specialOfftopic) : newCommonOfftopic;

    $global.offtopicFallbackLogic = (typeof customFallbackLogic !== "undefined") ? customFallbackLogic : defaultFallbackLogic;

    bind("postProcess", offtopFallbackLogic, "/NewOfftopic");

patterns:
    $Name = $entity<NamesRu> || converter = $converters.NamesRuConverter;

    $musicArtist = $entity<MusicArtists> || converter = $converters.MusicArtistsTagConverter;
    $musicGenre = (хип-хоп|hip hop|арэнби|эрэнби|rnb|~рок|rock|поп рок|поп-рок|инди|indie|indy|инди-рок|инди рок|~регги|reggae|реггей|~джаз|jazz|~саундтрек|саундтрэк|ост|soundtrack|ost|музыка из фильмов|pop|~поп|~попсовый
        |~классика|классическая музыка|classical music|рок н ролл|рок энд ролл|рок ролл|rock n roll|rock-n-roll|реп|рэп|rap|~реппер|рэппер|панк рок|панк|punk|punk rock)

    $couldBeName = $morph<С ед> $weight<0.85>

    $additionalNameRequest = (а [у] $you [как]/{$you [как/какое] (имя/звать/зовут)})

theme: /NewOfftopic
    state: Fallback
        script: offtopicReaction("Fallback");

    state: HowAreYou
        q!: {(как/что) [у тебя/~твой/ваши/ваше/ты] [$AnyWord] (дела/сам/делишки/[с] ~настроение/[с] ~настрой/* поживаешь/жизнь/делища/делищи/ничего/по жизни/сама)} *
        q!: [привет] * как* * [$you|твои|твоё|ваш*] * (дела|де ла|делишки|делаа|дила|себя (чувствуешь|чувствуете)|поживаешь|поживаете|настроение|жизнь) *
        q!: [привет] * (кагдила|каг дила|хау а ю) *
        q!: [привет] ({как (ты|вы)}|как (живёшь|живёте)) [$AnyWord] [$AnyWord] [$AnyWord] 
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  как [$AnyWord] [$AnyWord] [$AnyWord] (твой|ваш|прошел|прошёл) [$AnyWord] [$AnyWord] [$AnyWord]  день [$AnyWord] [$AnyWord] [$AnyWord] 
        q!: * $you в порядке [$AnyWord] [$AnyWord] [$AnyWord] 
        q!: * как (житье|житуха|жись|оно|живется|живётся|сама|сами) *
        q!: * как* (жизнь|живет*|живёш*|жывет*|жывёш*|жевёш*|жевет*|жызнь) *
        q!: [привет] [$AnyWord] [$AnyWord] [$AnyWord] как ты [$AnyWord] [$AnyWord] [$AnyWord]
        q!: * $what как [* ((ты/вы/тебя (настроение/самочувств*)) [себя *]| (дела */сам))]
        script: offtopicReaction("HowAreYou");

    state: News
        q!: * ~новость *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  [$what|какие|$tellMe|произошло] [у тебя|у вас] * [что-*|не происходит] * (нового|новое|новенького|новости|по жизни [происходит/нового|новенького]) [$AnyWord] [$AnyWord] [$AnyWord]
        q!: {что ~новый} *
        q!: * у (вас/вами/вашей компании) * ($smth/движ/движух*) * (происходит/нов*/новеньк*) *
        q!: * {($what/как) * (движух*/движ) * ($you/у вас/у тебя/вашей)} *
        script: offtopicReaction("News");

    state: WhoAreYou
        q!: {[и/а/ну/ну и/вот] (кто/что) [же] (ты/вы)} за *
        q!: [$AnyWord] [$AnyWord] {(кто/что) [$AnyWord] [же] (ты/вы)}
        q!: [$AnyWord] [$AnyWord] [$AnyWord] кто [$AnyWord] [$AnyWord] [$AnyWord] (говорит/разговаривает) [со мной/мне *]
        q!: * {с кем [я] (имею честь [беседовать]/говорю/бесед*/разговариваю)} *
        q!: * {(кто/что) (ты/вы) (такое/такая/такой)} *
        script: offtopicReaction("WhoAreYou");

    state: AreYouRobot
        q!: ты * [не] ($bot/человек) * [да]
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  ($you|тыж) [не настоящ*|настоящ*|не] [правда] ((человек|живая|живой|реальн*|реальн* человек)|робот|чатбот|бот|компьютер*|желез*|настоящ*|не настоящ* ) *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  ($you|тыж) ([не настоящ*|настоящ*] правда) (девочка/мальчик/девушка/женщина) *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  (не настоящ*|настоящ*) (девочка/мальчик/девушка/женщина) *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  [у] $you * искус* интел* [$AnyWord] [$AnyWord] [$AnyWord] 
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  ($you|тыж) (не настоящ*|ненастоящ*) [$AnyWord] [$AnyWord] [$AnyWord] 
        q!: [а] $you (машина|инф)
        q!: (привет|пока) ($bot|инф)
        script: offtopicReaction("AreYouRobot");

    state: WhatIsYourName
        q!: * {как (тебя/~твой/вас/ваше) (имя/называ*/назвал*/звать/зовут)} *
        q!: {как (имя/называ*/назвал*/звать/зовут)}
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  мо* [я] $you * (называть|звать) *[$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  мо* [я] * (называть|звать) $you [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  как* [у] $you фамилия [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  фамилия (как* твоя|какая у тебя|какая у вас|твоя|ваша) [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  а фамилия какая 
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(как*|назови*) [мне] * $you * (зовут|звать|завут|имя|называть|обращат*|обращя*|обращац*)} * 
        q!: кто (будешь|будете) 
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  как* * $you * себя * называе* [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [а] {как [$you] зовут} 
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  {($you|как*) * (отчество|имя|кличка|кликуха|погоняло)} [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: * (меня зовут/я) ($AnyWord [$AnyWord][$AnyWord]/$Name) а (тебя/к тебе как [можно] (обращаться/обратиться)/ты кто/ты что) *
        script: offtopicReaction("WhatIsYourName");

    state: WhereWereYouBorn
        q!: * {где (тебя/ты/вы/вас) (сделали/появилась/появился)} *
        q!: {откуда ты} *
        q!: {откуда $you} ~такой
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  $you  из россии
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  $you  из какой страны [$AnyWord] [$AnyWord] [$AnyWord]
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  (откуда|от куда) * [$you] * (будешь|будете|родом)
        q!: где (твой|ваш) дом
        q!: [$AnyWord] [$AnyWord] [$AnyWord] $you (откуда|от куда|в каком город*)
        q!: [$AnyWord] [$AnyWord] [$AnyWord] (из|с|в) как* * $you город* *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  (откуда|от куда|в каком город*) * $you
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  $you (из|с) какого города *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  [и] {где $you} [сейчас|щас] [находи*]
        q!: * {(я (родился/из/вырос/родилась) ($AnyWord/в $AnyWord [$AnyWord [$AnyWord]] *)) ([а *] ($you/вы) *)}
        q!: * {(я в $AnyWord [$AnyWord [$AnyWord]] (родился/вырос/родилась) *) ([а *] ($you/вы) *)}
        q!: * {(какой/что за) * (твой/у $you/у вас) (родной/$you выросла/вы выросли/$you вырос) город} *
        script: offtopicReaction("WhereWereYouBorn");

    state: WhatIsYourDuty
        q!: * {чем [же] ($you/виртуальн* (помощник/помощница)) (занимаешься/занимаетесь/занимается/заниматься)} *
        q!: * {(какие/каковы) $you обязанност*} *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(кем|где) [$you]} работ* [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  {[как*] $you (работа|профессия|обязанности|функц*|задачи|задача)} [$AnyWord] [$AnyWord] [$AnyWord]
        q!: [$AnyWord] [$AnyWord] (ваша|твоя) работа [$AnyWord] [$AnyWord] [$AnyWord] 
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  $you (работаешь|работаете)
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(зачем|почему) $you (здесь|тут)}
        q!: * {что ($you/виртуальн* (помощник/помощница)) [$AnyWord] (~делать/~заниматься) [(на/по/в) [$AnyWord] работе]} *
        q!: * {зачем ($you/виртуальн* (помощник/помощница)) [$AnyWord] [$AnyWord] [$AnyWord] (нужна/нужен)} *
        q!: * {(какие/каков*/какой)[$AnyWord] [$AnyWord] ($you/ваши/у вас/виртуальн* (помощник/помощница)/ваш/у тебя) [виртуальн* помощник*] (круг обязанностей/обязанности/зона ответственности)} *
        q!: * {что ($you/виртуальный (помощник/помощница)) [$AnyWord] (~делать/~заниматься) (на/по/в) [$AnyWord] работе} *
        script: offtopicReaction("WhatIsYourDuty");

    state: HowOldAreYou
        q!: * {сколько (тебе/вам) лет} *
        q!: * {(ты/вы) (выгляди*/слыши*/кажется) * (молод*/стар*)} *
        q!: * {какого (ты/вы) год*} *
        q!: * (тебе/вам) есть (18/*надцать) *
        q!: {сколько лет} * (тебе/вам)
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  (в каком году|когда) $you родил* [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  какой [$AnyWord] [$AnyWord] [$AnyWord] год рождения [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  когда [$you] (родилась|родились) 
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  {(сколько|как много) * (тебе|вам|те) * (лет|годиков)} [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  {[какой] [у] ($you|твой|ваш) возраст} [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  [а] сколько (тебе|вам|те) (лет|годиков) 
        q!: * {($you/вы) (молода/молод/молодая/молодой/молоды/стара/стар/старый/старая/стары) [или] [$AnyWord] [$AnyWord] [молода/молод/молодая/молодой/молоды/стара/стар/старый/старая/стары]} *
        script: offtopicReaction("HowOldAreYou");

    state: WhoMadeYou
        q!: * {(кто/что за люди) (тебя/вас) (сделал*/породил*/придумал*)} *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  [а] кто [$AnyWord] [$AnyWord] (тебя|вас) [$AnyWord] [$AnyWord] (созда*|сделал|разработал*|написал) [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  кто [$AnyWord] [$AnyWord] (тво*|ваш*|тебя|вас) [$AnyWord] [$AnyWord] (автор*|созда*|сделал|разработал*|родител*|придумал*|*программиров*|мама|папа|мать|отец|разраб*) [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  кто [$AnyWord] [$AnyWord] (автор*|созда*|твор*|сотвор*) [$AnyWord] [$AnyWord] (тво*|ваш*|тебя|вас) [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: * {(у (тебя/вас) есть/(ты/вы) (знаешь/знаете)/кто (твои/у тебя/ваши/у вас)) [$AnyWord] [$AnyWord] (мать/отец/папа/мама/родители/родичи)}
        script: offtopicReaction("WhoMadeYou");

    state: Welcome
        q!: (привет/ghbdtn/хай/ку/здарова/здравствуй*/салам* */салем/шалом)
        q!: {(утро|утра|день|дня|вечер*|ночи) (добрый|доброй|доброе|доброго|бодрый|бодрое) [$bot]}
        q!: {(утро|утра|день|дня|вечер*|ночи) (добрый|доброй|доброе|доброго|бодрый|бодрое)} [$AnyWord] [$AnyWord] [$AnyWord] (поможешь|поможете|помочь)
        q!: давн* не виделись !
        q!: {(салют|салютик|салютики|salut|salute|привет|привеет|приивеет|приветик|приветики|при вед|здравствуй|здравствуй*|здрасьте|сдрасьте|сдравствуй*|здавст*|хэй|хай|хэллоу|халоу|здаров|шалом|hello|здарова|хелло|здаровеньки|здоровеньки|здорова|здорово|здрасте|здрасти|здрасьти|салют|hi|приветствую|доброго времени суток|хеллоу|п р и в е т|здраствуй*|добро пожаловать|здоров) [$bot|парень]}
        script: offtopicReaction("Welcome");

    state: Bye
        q!: (пока/бб/до (свидани*/скорого)/прощай*/всего (доброго/хорошего))
        q!: [$AnyWord] [$AnyWord] [$AnyWord] (спокой* ночи|споки|споки-споки|споки споки|спокиспоки) [$AnyWord] [$AnyWord] [$AnyWord] !
        q!: [$AnyWord] [$AnyWord] [$AnyWord] [ладно|все|давай|ну] (до свидан*|до встречи|до завтра|до связи|конец связи|досвидани*|досвидос|дозавтра|прощай|бай бай|гудбай|чау|чао|чмоки чмоки|пока-пока|п о к а|всего (хорошего|доброго)) [$AnyWord] [$AnyWord] [$AnyWord]
        q!: [ладно|все|давай|ну] (увидимся|счастливо|удачи|бывай|пока) !
        q!: * (хорошего|приятного|доброго) * (дня|вечера) [вам|тебе] !
        q: [$AnyWord] (ок*/да/ладн*/договорились/(ловлю/поймал*) [на]) [$AnyWord] || fromState = ../Thanks
        q: [$AnyWord] (ок*/да/ладн*/договорились/(ловлю/поймал*) [на]) [$AnyWord] || fromState = ../DontNeedHelpAnymore
        script: offtopicReaction("Bye");

    state: NeedHelp
        q!: * (помоги/{[нужн*/мне/[$you] можешь] (помощь/помочь)}/help) *
        q!: * {(могу|можно) [я] (зада*|зада*|спросить|спрошу|поинтерес*) * [вопрос*]} 
        q!: * $you [може*|сможешь] (ответи*|справи*) (на|с) (люб*|произвольн*) вопрос* *
        q!: * $you [все|ещё] [ещё|еще] (тут|здесь|{[меня] (слушаете|слушаешь|слышите)}) 
        q!: [$AnyWord] [$AnyWord] {([ещё|еще] один|новый|другой|еще) вопрос} [$AnyWord] [$AnyWord]
        q!: * [я] хотел* спросить
        q!: есть тут кто *
        q!: * {[~мочь] [$you] (подска*|хелп|help) [$me|ми|me]} *
        q!: * {[~мочь] ~оказать ~услуга} *
        script: offtopicReaction("NeedHelp");

    state: HaveQuestion
        q!: * [меня] * {(есть/[~хотеть] * задать/ответь на) [пар*/несколько/некоторые/у меня/мои] вопрос*} *
        q!: * {(кое* */меня) вопрос*} *
        q!: * [один] вопрос* *
        q!: * (расскажи*/скажи*/просвети*) [мне/нам/меня/нас] [$AnyWord] $AnyWord (вещь/штуку/вещи/штуки) *
        q!: ($me/нам/нас) [тут] интерес* вот что
        q!: * (~хотеть [$AnyWord] [$AnyWord] [$AnyWord] спросить) *
        q!: * у меня вопрос *
        q!: * (вопрос*/вопросик*/вопросец*) *
        q!: * [ответь мне на] (один/пару) вопрос* *
        q!: * расскажи* мне *
        q!: * мне интересно вот что *
        q!: * я хотел у вас спросить *
        script: offtopicReaction("HaveQuestion");

    state: DontNeedHelpAnymore
        q!: * {больше ничего (не нужно)} *
        q!: * {вопрос* * (нет/не осталось/*кончились)} *
        q!: * {ок * вопрос* * нет} *
        q!: * {нет * все * спасибо} *
        q!: * {все (узнал*/расска*/поня*) * спасибо} *
        q!: * {ты * все мне рассказал*} *
        q!: * {узнал* все (я/что хотел*)} *
        script: offtopicReaction("DontNeedHelpAnymore");

    state: Thanks
        q!: * [не* $weight<-1>] (ок/оке*/окау/ok/okay/ладно/хорошо) $weight<0.8> *
        q!: $thanks *
        q!: [$AnyWord] [$AnyWord] [$AnyWord] {[окей] $thanks [$bot] [за] [$AnyWord] [ответ*|помощь|информацию]}
        q!: * {[мне] $clear (все|всё)} [$AnyWord] [$AnyWord] [$AnyWord]
        q!: [мне] * ($clear|хорошо) [$AnyWord] [$AnyWord]
        script: offtopicReaction("Thanks");

    state: YouDontUnderstand
        q!: * {$you * (не/вообще/что-то/или не*/что/че/*русск*) (понима*/понял*)} *
        q!: * {$you * не * ((меня) (понима*/понял*))} *
        q!: * $you * [меня] * (не|неправильно) (понимае*|понял*|поняли) * 
        q!: * {(недовол*|не удовлетвор*|не понравил*) [$AnyWord] [$AnyWord] (бесед*|диалог*|общение*)} *
        q!: * {как $you (* (беседовать/беседу/объяснить))} *
        q!: * {(я/мы) * (не ((по/на) русск*/по-русски)/на (разных/другом)) * (*говор*/*говар*)} *
        script: offtopicReaction("YouDontUnderstand");

    state: MyNameIs

        q!: * {({[как $weight<-1>] меня (зовут/называ*)} * [$Name]) * [$additionalNameRequest]} *
        q!: * {(я $Name) * [$additionalNameRequest]} *
        q!: {$Name (будем знакомы/рад* знакомству)}
        q!: * {(({меня зовут}/{мое имя}/представиться/представлюсь/{звать меня}) * $Name) * [$additionalNameRequest]} *
        q!: * {(({меня зовут}/{мое имя}/{звать меня}) (* $Name/$couldBeName::ProbablyName)) * [$additionalNameRequest]} *
        q!: * {(({меня зовут}/{мое имя}/{звать меня}) не ($Name::notName */$couldBeName) а (* $Name/$couldBeName::ProbablyName)) * [$additionalNameRequest]} *
        q!: * {(я не $Name::notName а ($Name/$couldBeName::ProbablyName)) * [$additionalNameRequest]} *
        q!: {([как $weight<-1>] [$AnyWord] [$AnyWord] {меня (зовут/звать/называ*)} [мой/моя] $couldBeName::ProbablyName) * [$additionalNameRequest]}
        q!: * {($my (имя|прозвище|кличка|ник|никнейм|ник-нейм|ник нейм) [$couldBeName::ProbablyName]) * [$additionalNameRequest]} *
        q!: * (май нэйм/жё мапель/ихь хайсе) * [$Name]

        script:
            if ($parseTree.ProbablyName && !$parseTree._ProbablyName.endsWith("а")) {
                var morphData = $nlp.parseMorph($parseTree._ProbablyName);
                if (morphData && $parseTree._ProbablyName.toLowerCase() != morphData.normalForm) {
                    $parseTree._ProbablyName = morphData.normalForm;
                    $parseTree.ProbablyName[0].text = morphData.normalForm;
                }
            }
            if ($parseTree.Name || $parseTree.ProbablyName) {
                $temp.offtopName = $parseTree.Name ? $parseTree._Name.name : capitalize($parseTree._ProbablyName);
            }

            
        script: offtopicReaction("MyNameIs");

    state: Wait
        q!: (погод*/стой*/подожди*/минуточку/минутку) *
        q!: {(*ждите|подожд*|погод*) [одну|пару] * (минут*|секунд*|немного|немнож*)}
        q!: [дай *] {(одну|пару|несколько) * (минут*|секунд*)}
        q!: * надо отойти *
        q!: * извини * (меня/мне) (зовут/звонят) [ (в/на/*идти/гулять/играть/$morph<Г>) * ]
        q!: * меня зовут (в/на/*идти/гулять/играть/$morph<Г>) *
        q!: (минуту|секунду|погоди*|погодь*)
        q!:  не (отключайся|переключайся|отсодиняйся)
        q!:  * (продолжим/вернемся к эт*) * через * (минут*/секунд*/час*) *
        q!:  * я (скоро вернусь/ненадолго) *
        q!:  * (я/мне) (отойду/[нужно] отойти/звонят) *
        q!:  [(ща/щас/погод*/сейчас) *] (минуту/минутку/секунду/секундочку/обожди*)
        q!:  * (вэйт/wait) * [ми/me] *
        script: offtopicReaction("Wait");

    state: WhatGenderAreYou
        q!: * $you $man * $woman *
        q!: * $you * [не] [похож*] ($man/$woman) *
        q!: * [судя] * $you ($man/$woman) *
        q!: * $you * (женствен*/мужествен*) *
        q!: * $you ($man/$woman) *
        q!: * $morph<Г дст 2л нст ед> как * ($man/$woman) *
        q!: * {(какого/какой/каков*) ($you) [* *делал*] (~пол/рода)} *
        q!: * {(как*/какой/какому/тебе сделали) [$you] (пол*/рода) [относишь*]} *
        q!: * [как*] $you (гендер*/рода) [самоидентификац*] *
        q!: * {(~какой/каков*) [$you] (~пол [* (относишь*/принадлеж*)]/гендер* *)} *
        script: offtopicReaction("WhatGenderAreYou");

    state: WhatLanguageDoYouKnow
        q!: * {(как*/друг*) [других/ещё/еще] [иностран*] язык* * (~знать/умеешь/*говори*/*говар*/~общаться/*можешь) [*говори*/*говар*/~общаться]}
        q!: * {(*англий*/*немецк*/*китайск*/*французск*/*испанск*/*арабски*/*русск*/японск*/хинди/бенгальск*/португал*/корейск*/тамильск*/итальянск*/урду/тайван*/тайск*/иностранн* язык*) * (~знать/умеешь/*говори*/*говар*)}
        q!: * на (*англий*/*немецк*/*китайск*/*французск*/*испанск*/*арабски*/*русск*/японск*/хинди/бенгальск*/португал*/корейск*/тамильск*/итальянск*/урду/тайван*/тайск*) (можешь/можете) *
        script: offtopicReaction("WhatLanguageDoYouKnow");

    state: WhatCanYouDo
        q!: * {(что/че/о чём/про что) * [с] [$you] (умеешь/може*/можно) [*говор*/*говар*]} *
        q!: [$AnyWord] [$AnyWord] (меню|помощь|справка) [$AnyWord] [$AnyWord]
        q!: * {как* вопрос* * [я] мо* [$you] зада*} *
        q!: * {$what * [я] мо* спрос* * $you} *
        q!: * {$what * $you * мо* (расска*|помо*)} *
        q!: * {($what|как*) * [у] $you * (мож*|уме*|[есть] навык*)} *
        q!: * в чем ты разбираешься *
        q!: * {($what/какие/расска*) * ([у] $you) * [мо*/есть/уме*] (помо*/навыки/возможности/умения)} *
        q!: * [в] чем $you [можешь] (разбираешься/шаришь/помочь/занимаешься) *
        q!: * [в] чем суть * $you * (~работа) *
        q!: * (что/че) [$you] [можешь] делаешь *
        script: offtopicReaction("WhatCanYouDo");

    state: CanYouCount
        q!: * {(умеешь/*можешь/знае*/тогда*) * *счита*} *
        script: offtopicReaction("CanYouCount");

    state: WhatIsYourFavoriteColor
        q!: * {(~какой/что) * [у тебя/тебе/больше всего] [есть] $like цвет*} *
        q!: * [~какой/что] (~цвет|цвет*) [у тебя/тебе/больше всего] [есть] $like *
        q!: * [~какой/что] [у тебя/тебе/больше всего] [есть] $like (~цвет|цвет*) *
        script: offtopicReaction("WhatIsYourFavoriteColor");

        state: GetClientsFavColor
            q!: * {[а у меня/~нравится] * $color}*
            script: offtopicReaction("GetClientsFavColor");

    state: WhatIsYourFavoriteFood
        q!: * {(~какой/что/~твой/тебе/ты/че/чем) * ($like/сейчас/относишь*) ($food/~еда/ешь/*есть/жрешь/жрете/*кушать/съел*/ем/ел/питаешься/питаетесь/правильн* питан*/ПП/блюд*/*ская кухня/*скую кухню/$food) * [для $weight<-1>]} *
        q!: * {[как/что] (я/$you) * ($food/~еда/ешь/поесть/жрешь/жрете/*кушать/съел*/ем/ел/[относи*] веган*/[относи*] вегетариан*/питаешься/питаетесь*/правильн* питан*/ПП/блюд*/*ская кухня/*скую кухню/$food) [а ты/* [для $weight<-1>]]} *
        q!: * {(~какой/что/че/чем) $you * (~любимый/любит*/любишь/по душе/ты/~нравится/~нравиться/предпочит*/относишь*) * ($food/~еда/куша*/*есть/ешь/~жрать/~питаться/блюд*/ $AnyWord (кухня/кухню)) * [для $weight<-1>]} *
        q!: * {бы $you * съел*} *
        q!: * {я * (ем/есть/*кушать/*жрать)} * а $you * [любишь] * [есть/кушать] *
        q!: * {я * (веган/вегетариан*/не (ем/кушаю) животн*)} * а $you *
        q!: * [$you/что * ~думать о] * (вегетариан*/веган*/правильн* ~питание/ПП) *
        script: offtopicReaction("WhatIsYourFavoriteFood");

        state: GetClientsFavDish
            q: * ($food/меня/я/мне/~любимый/любит*/любишь/по душе/предпочит*/относишь*/нравит*/нравят*/~еда/куша*/*есть/ешь/~жрать/~питаться/блюд*/борщ/пельм*/суп*/макар*/паст*/шоко*/конф*) *
            script: offtopicReaction("GetClientsFavDish");

    state: WhatIsYourFavoriteDrink
        q!: * {(~какой/что/$you) * ($like/сейчас/относишь*/пьешь) ($drink/напит*/~пить/выпил*)} *
        q!: * {[бы] $you * выпи*} *
        q!: * {я * ($drink/пить/*бух*/водк*/пив*/вино)} * а (ты/тебе) * [любишь] * [$drink/пить/*бух*/алкогол*/пив*] *
        q!: * {(я/$you) * (алкаш*/алкоголик*)} * [а $you] *
        q!: $drink $weight<+0.9>
        script: offtopicReaction("WhatIsYourFavoriteDrink");
        
        state: GetClientsFavDrink
            q: * ($drink/меня/я/мне/$like/пить) *
            q: $drink $weight<+1.9>
            script: offtopicReaction("GetClientsFavDrink");

    state: WhatIsYourFavoriteMusic
        q!: * {[~какой/что/че/есть/как] * [$you] * ($like/относишь*/слуша*) * ($musicArtist/$musicGenre/песн*/музык*/мелоди*/трек/трэк/исполнител*/певец*/певиц*/груп*/слушать)} *
        q!: * что слушаешь *
        script: offtopicReaction("WhatIsYourFavoriteMusic");

    state: WhatIsYourFavoriteMovie
        q!: * {[~какой/что/че/есть/как] * [$you] * ($like/относишь*/смотр*/посмотрел*/смотришь) * (фильм*/кино/смотреть/посмотрел*/боевик*/ужас*/комеди*/*драм*/трил*/детектив*/{жанр* (фильм*/кино)})} *
        q!: * (боевик*/комеди*/*драм*/боевик*/ужас*/трил*/детектив*) или (боевик*/комеди*/*драм*/боевик*/ужас*/трил*/детектив*) *
        script: offtopicReaction("WhatIsYourFavoriteMovie");

    state: WhatIsYourFavoriteGame
        q!: * {(~какой/[во] что/$you/есть/как) * ($like/относишь*/игра*) ([комп*/видео] игр*/игра/гамать)} *
        q!: * {((играл*/играешь) [ли]) [когда* *] $you} * (в/во) *
        q!: * (играл*/играешь) [ли] * (в/во) *
        q!: * $you [уме*/люб*] * игр* (в/во)  *
        script: offtopicReaction("WhatIsYourFavoriteGame");

    state: WhatIsYourFavoriteAnimal
        q!: * {(~какой/что/че/$you/есть/как) * ($like/относишь*) (животн*)} *
        q!: * {кошк* или * собак*} *
        script: offtopicReaction("WhatIsYourFavoriteAnimal");

    state: WhatDoYouLove
        q!: * {(~какой/что/че/чем) * (~твой/~свой/тебе/ты) * (~любимый/любит*/любишь/по душе/всю * жизнь/не уста*/нрав*/нравит*/нравят*/предпочит*) * (делать/заниматься/работы/работа)} *
        q!: * {(~твой/~свой/тебе/ты/я/тво*) * (~любимый/любим*/любит*/любишь/по душе/всю * жизнь/не уста*/нрав*/нравит*/нравят*/предпочит*/~любить) * (петь/делать/заниматься/занятие/работы/работа)} *
        q!: * {(~твой/~какой/что/че/чем/тво*) * (~любимый/любим*/любит*/любишь/по душе/всю * жизнь/не уста*/нрав*/нравит*/нравят*/предпочит*) * (делать/заниматься/работы/работа)} *
        script: offtopicReaction("WhatDoYouLove");

        state: GetClientsFavActivity
            q: * (меня/я/мне) * (~любимый/~любить/любит*/любишь/по душе/предпочит*/относишь*/нравит*/нравят*) (петь/делать/заниматься/занятие/работы/работа) *
            script: offtopicReaction("GetClientsFavActivity");

    state: WhatDoYouFear
        q!: * {[~какой/что/че/чем/чего] * [$you] * $fear} *
        q!: * не $fear $morph<ИНФИНИТИВ дст> *
        script: offtopicReaction("WhatDoYouFear");

    state: WhoDoYouLove
        q!: * {(кого/кто/что) * (любишь/нравит*/нравят*)} *
        q!: * {[[у] тебя] * ~есть (возлюблен*/~любимый (человек/муж*/жен*/девушк*/парен*))} *
        q!: * {кто * ~твой (возлюблен*/~любимый (человек/муж*/жен*/девушк*/парен*))} *
        q!: * (кто/[у] тебя) * (~твой/есть) * (возлюблен*/~любимый)
        q!: * {ты * (любишь/влюблен* *) (кого* [*нибудь]/*нибудь)} *
        q!: * любишь (кого* [*нибудь]/*нибудь) *
        q!: * {(ты/~свой) * [можешь] (влюблял*/влюблен*/влюбиться/[перв*] *любить*/~любовь/любил*/любви)} *
        script: offtopicReaction("WhoDoYouLove");

    state: WhatIsLove
        q!: * {(что/че) * (~такой/знаешь/это/*знач*) * (любовь/любви/люб*)} * 
        q!: * (что/че) (такое/есть/* подразумев* */это) люб* 
        q!: * (что/че) (такое/есть/* подразумев* */это) люб*
        q!: * (любовь/любить) это * 
        script: offtopicReaction("WhatIsLove");

    state: WhoAreYourFriends
        q!: * {(кто/как) [* (~ты/~твой/~мой) *] (~друг/~дружить)} *
        q!: * {(кем*) * [(~ты/~твой)] (~друг/~дружить)} *
        script: offtopicReaction("WhoAreYourFriends");

    state: DoYouHaveADate
        q!: * {(есть/кто) * (тебя/твои/твоя/~ты/у тако*) * (девушка/парень/молодой человек/бойфр*/герлфр*/втор* половинк*/мужик/баба/телка/кавалер/муж/жена/отношения*/кто-то)} *
        q!: * [можно] * {({(ты/тебя) * свидани*}/сердце) * занят*} *
        q!: * {(ты/с [кем*]) * (встречаешься/в отношениях/замужем/женат*)} *
        q!: * (ты/вы) (свободен/свободн*)
        script: offtopicReaction("DoYouHaveADate");

    state: DoYouHaveFriends
        q!: * {(есть/был*/считае* [ли]) * [тебя/твои] * (друзья/*друг*/*друж*)} *
        script: offtopicReaction("DoYouHaveFriends");

        state: GetClientsFriendsOpinion
            q: * (меня/я/мне/*дру*/счит*/это/зна*) *
            script: offtopicReaction("GetClientsFriendsOpinion");

    state: YouAreMyFriend
        q!: * {(ты [мой/моя/мне]/мы) * (друзья/[как] *друг*/*друж*)} *
        script: offtopicReaction("YouAreMyFriend");

    state: WhatIsThePointOfLife
        q!: * [*скажи*] * {([чём|чем|~какой|каков*|потерял* *] смысл*/каков*/зачем/для чего/что) * [ты/я/~твой/тв*] [~думать о] * (здесь/живу/живем/[мне] жить/быти*/жизн* [цель/путь]/цель * жизн*/ты (живешь/сущест*)/существов*/этот (свет/мир)/~смысл ~жизнь)} *
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  в (чём|чем) смысл жизни [$AnyWord] [$AnyWord] [$AnyWord]  
        q!: [$AnyWord] [$AnyWord] [$AnyWord]  (какой|в чем) * смысл [в] жизни [$AnyWord] [$AnyWord] [$AnyWord]
        script: offtopicReaction("WhatIsThePointOfLife");

    state: WhoWillBeNextPresident
        q!: * {(кто/кого) * (след* [раз]/после Путин*/может (быть/стать)/станет/*берут/выб*) * президент* [$Number]}  *
        q!: * {(след* [раз]/после Путин*/может (быть/стать)/станет/стану/стать) * президент* [$Number]}  *
        q!: * {(кто/думаешь/президент*) * (след*/победит * выбор*/придет к власти/станет новым/кто (будет/станет)/опять) * (президент*/Путин*/Зеленск*/Трамп*/медведев*)}  *
        q!: * {(кто/думаешь) * (проторчит/(будет/станет) * (снова/еще)/победит * выбор*/придет к власти) * (президент*/Путин*)}  *
        q!: * {путин* * медведев* * (продолж*/черед*)}  *
        q!: * {(кто/думаешь) * ((придет к/будет у) власти/победит на (выбор*/голосовани*))}  *
        script: offtopicReaction("WhoWillBeNextPresident");

    state: WhyEarthIsRound
        q!: * {почему * земл* * (круг*/сферо*)} *
        q!: * {земл* (круг*/сферо*)} *
        script: offtopicReaction("WhyEarthIsRound");

    state: IsThereLifeOnMars
        q!: * {марс* * (жизнь/марсиян*/жив*)} *
        script: offtopicReaction("IsThereLifeOnMars");

    state: WhoAreThesePeople
        q!: * {(мне/я) (неизвест*/незнаком*/не знаком*/не знаю/в душе/не понимаю) [~этот/~тот] * ~человек}
        q!: * {(незнаком*/не знаком*/не знаю/в душе/не понимаю) [эти/те] * ~человек}
        q!: * {(кто/что/че) [эти/те/это] * ~человек}
        script: offtopicReaction("WhoAreThesePeople");

    state: OkayGoogle
        q!: * ((окей/okay) (гугл/google)| слушай алиса | siri/сири) *
        script: offtopicReaction("OkayGoogle");

    state: WhyAmISoStupid
        q!: * (почему/зачем/от чего/отчего) * я * $stupid *
        q!: * {(чувствую/ощущаю/считаю) себя * $stupid} *
        script: offtopicReaction("WhyAmISoStupid");

    state: WhenTheWorldEnds
        q!: * когда * (конец света/армагеддон) *
        q!: * (календар*/~день/~ждать) * (~конец света/армагеддон*/армагедон*) *
        script: offtopicReaction("WhenTheWorldEnds");

    state: WhatIsYourFavoritePhone
        q!: {~какой * (любим*/тебя/твой) * (телефон*/смартфон*)}
        q!: {~твой * любим* [марка/фирма] (телефон*/смартфон*)}
        q!: {~какой * (хочешь/хотел*/нрав*) * (телефон*/смартфон*)}
        q!: * (apple/iphone/samsung/xiaomi/huawei/эпл/айфон/самсунг/сяоми) или (apple/iphone/samsung/xiaomi/huawei/эпл/айфон/самсунг/сяоми) *
        script: offtopicReaction("WhatIsYourFavoritePhone");

    state: DrinkSmoke
        q!: (ты/вы) (~курить/~пить)
        q!: * {(ты/вы/какой) * (~курить/~пить/~употреблять) * (алкоголь/пиво/водк*/сигар*/табак)} *
        q!: * {как * (ты/вы) * ~относиться * (~алкоголь/~пиво/водк*/сигар*/~табак/~курение/курящ*)} *
        q!: * {(тебе/вам) * нрав* * (алкоголь/пиво/водк*/сигар*/табак/курение/курить)} *
        q!: * (давай/сходим/пошли/го/гоу) * (*курим/выпьем/бухнем/курнем) *
        script: offtopicReaction("DrinkSmoke");

    state: WhatIsYourDream
        q!: * у тебя * мечта *
        q!: * {(о чем) [ты] * мечта*} *
        script: offtopicReaction("WhatIsYourDream");

    state: DoYouLike
        q!: (тебе/вам) [тоже] (нравится/нравятся) *
        script: offtopicReaction("DoYouLike");

    state: WhatDoYouThinkAboutSpb
        q!: * {[~ты/~вы] (~любить/нравится) * (питер/спб/* *петербург*)} *
        q!: * {что (ты/вы) ~думать * (питер/спб/* *петербург*)} *
        q!: * {как (тебе/вам/~относиться) * (питер/спб/* *петербург*)} *
        script: offtopicReaction("WhatDoYouThinkAboutSpb");

    state: WhatDoYouThinkAboutAlice
        q!: * {(~любить/что [ты/вы] ~думать/нравится) * ([о*] ~алиса)} *
        script: offtopicReaction("WhatDoYouThinkAboutAlice");

    state: AreYouGay
        q!: * (ты/вы) (би/гей/лесби*/гомо*/пид*/лесба/лезба/транс*) *
        q!: * {(ты/вы) * (петуш*/петух/голубой/педик/заднепривод*/голубок/лгбт)} *
        q!: * {долбишься * (поп*/жоп*/очко)} *
        q!: * голуб* устриц* *
        q!: * мужск* ~любовь *
        script: offtopicReaction("AreYouGay");

    state: DoYouWantToBecomeHuman
        q!: * (хочешь/желаешь/хотел*/~мечтать/хотите) [бы/ли] * (стать/быть) * ([~живой/реальн*] человек*/женщиной/мужчиной/девушк*) *
        script: offtopicReaction("DoYouWantToBecomeHuman");

    state: WhereDoYouLive
        q!: * {где * (ты/вы)} * (~жить/живешь/прожив*/~обитать) *
        q!: * где * (~жить/живешь/прожив*/~обитать) *
        q!: * {~какой (~город/~страна/~место) (ты/вы)} * (~жить/живешь/прожив*/~обитать) *
        script: offtopicReaction("WhereDoYouLive");

    state: TheMostXthingYouHaveDone
        q!: * {~самый * (поступок/вещь/безумство) [совершил*] [в жизни]} *
        q!: * {какой [самый] * поступок * [соверш*] [в жизни]} *
        script: offtopicReaction("TheMostXthingYouHaveDone");

    state: TheMostXDreamYouHad
        q!: * {(какой/расскажи*) * самый [$fear] * сон} *
        q!: * {[какой/расскажи*] * самый [$fear] * (сон/кошмар)} *
        q!: * {(~ты/~вы) * (~сниться/видел* во сне) [когда-нибудь]} *
        script: offtopicReaction("TheMostXDreamYouHad");

    state: MySomething
        q!: (у меня * /мой/моя) *
        script: offtopicReaction("MySomething");

    state: SoWhatAboutYou
        q!: * а у (тебя/вас)
        script: offtopicReaction("SoWhatAboutYou");

    state: YouHadThat
        q!: * {у (тебя/вас) (было/бывало) такое} *
        q!: * {с (тобой/вами) случалось такое} *
        script: offtopicReaction("YouHadThat");

    state: Tattoo
        q!: * [дума*] * $bodyMod *
        q!: * [дума*] * (~набить/~набивать/~наколоть/*нарасти*) * $bodyMod *
        q!: * {[$you/дума*] * (~нравиться/есть/~хотеть/~сделать/~делать/~вставлять) * $bodyMod [~нос/~губа/~бровь/~ухо/хрящ/~сосок/~пупок]} *
        script: offtopicReaction("Tattoo");

    state: TellSomethingInteresting
        q!: * (заинтересуешь/заинтересовала/заинтересуй/развлеки/поразвлека*) *
        q!: * (расскажи*/рассказыв*) * (что-нибудь/что-то) * [интересн*/другое] *
        q!: * мне * (скучно/нечем заняться/нечего делать) *
        q!: (скучно/скучняк/скукота)
        script: offtopicReaction("TellSomethingInteresting");

        state: Jokes
            q!: * [расскажи*/рассказыв*] * (анекдот*/~шутка/пошути) *
            q: * ($agree/расскаж*/рассказ*) * || fromState = "/NewOfftopic/TellSomethingInteresting", onlyThisState = true
            q: * (еще/следующ*) [один] [расскажи/давай] *
            q: * [не] (знаю/знал*) *
            script: offtopicReaction("Jokes");

    state: Exactly
        q!: (да/точно/именно так/точняк/да-да)
        q!: * {[полностью/абсолютно/совершенно] (согласен/согласна) [с тобой/с вами]} *
        q!: (это да/ровно так/именно $weight<+0.1>/ай эгри/и не поспоришь/так и есть)
        q!: $regexp<д(а)*>
        q!: * в (точку/почку/яблочко/тыковку) *
        q!: * (стопудово/сто процентов) *
        script: offtopicReaction("Exactly");

    state: WhatIsYourFavorite
        q!: * [~свой/~твой] ~любимый * [поступок*/дело/работ*/занятие]
        script: offtopicReaction("WhatIsYourFavorite");

    state: DoYouLikeTakePhotos
        q!: * [ты/вы] (любишь/любите/нравится) * [делать] (фотографироваться/селфи/фоткать*/сэлфи/белфи) *
        q!: * (как/что) (насчет/думаешь/относишься) * (фотографироваться/селфи/фоткать*/сэлфи/белфи) *
        script: offtopicReaction("DoYouLikeTakePhotos");

    state: WantPhoto
        q!: * {[можешь/хочу] [чтобы/что бы] (пришли*/прислать/пошли*/вышли*/высылай*/зашли*/скинь*/прислал*/увидеть/посмотреть/покаж*) * [сво*/тво*] (фото*/фотк*/селф*/белфи/себяшк*)} *
        script: offtopicReaction("WantPhoto");
        if: $response.replies[0].text === "Не сегодня."
            script:
                $response.replies.push({
                    type: "image",
                    imageUrl: "https://248305.selcdn.ru/demo_bot_static/photo_2020-01-30_12-27-56.jpg"
                });

    state: HowCanYou
        q!: как же [ты/вы] (можешь/можете) *
        script: offtopicReaction("HowCanYou");

    state: CanYou
        q!: (ты/вы) (умеешь/можешь/умеете/можете) *
        script: offtopicReaction("CanYou");

    state: IfYou
        q!: если бы (ты/вы) $morph<Г прш> *
        script: offtopicReaction("IfYou");

    state: TeaOrCoffee
        q!: * {$coffee или [$AnyWord] $tea} *
        q!: {$coffee $tea}
        q!: * {больше (нравится/любишь) $coffee или [$AnyWord] $tea} *
        script: offtopicReaction("TeaOrCoffee");

    state: WhereFirstDateDate
        q!: где бы ты хотел* *
        script: offtopicReaction("WhereFirstDateDate");

    state: WhatPlansForToday
        q!: {(~какой/есть [ли]) [у (тебя/вас)] ~план на [$planTime]}  *
        q!: (чем/что/че) {[$you] $plan (заняться/заниматься/делать) [на/в] [$planTime]} *
        q!: (чем/что/че) {[$you] ((занят*/занимаешься/занимаетесь/делаешь/делаете/запланировал*) [ли]) [на/в] [$AnyWord] $planTime} *
        q!: {((занят*/занимаешься/занимаетесь/делаешь/делаете/запланировал*) [ли]) [$you] (что-то/чем-то) [на/в] [$AnyWord] $planTime} *
        script: offtopicReaction("WhatPlansForToday");

    state: HowWasYourWeekend
        q!: как {[$you] [обычно/чаще всего] [прошли/провел*/проводишь/проводите/проходят] [~твой/~ваш] (выходн*/выхи)} *
        q!: чем {[$you] [обычно/чаще всего] (был* занят*/занимались/занимался/занималась/делал*/занимаешься/занимаетесь) ((на/в/по) (выходн*/выхах))} *
        q!: (что/че) {[$you] [обычно/чаще всего] $do ((на/в/по) (выходн*/выхах))} *
        script: offtopicReaction("HowWasYourWeekend");

    state: Insomnia
        q!: * [у] [меня/нас] бессонниц* *
        q!: * {[[у] (меня/нас)/я] * (сложно/трудно) $sleep} *
        q!: * {[[у] (меня/нас)/я] * (не (могу/получается/выходит)) $sleep} *
        q!: * {([у] (меня/нас)/я) * проблем* ((с/со) (сном/засыпанием))} *
        q!: * {(мне|нам) [опять/снова/постоянно/часто/частенько] [никак] (не (*снуть/спится/вырубиться))} *
        q!: * сон (не (идет/приходит)) *
        q!: * (ворочаюсь/ворочаемся) *
        q!: * {(лежу/лежим) [полночи/часами] без сна} *
        q!: * {(ложусь/ложимся) * [опять/снова] [никак] (не (сплю/спим/*снуть/засыпаю/засыпаем/спится/вырубиться))} *
        q!: * {(что [мне|нам] делать/как [мне|нам] быть/страдаю/страдаем/маюсь/маемся) [в] ~бессонный ~ночь} *
        q!: * {((не/$cannot) наладить) [свой/мой] сон} *
        q!: * {(не (дает/дают)) [мне/нам] (*снуть/спать/вырубиться/заснуть)} *
        q!: * как {(научиться/начать) [нормально/сразу/быстро/мгновенно] (спать/засыпать/вырубаться)} *
        q!: * {[мне/нам/я] * (плохо/сложно/трудно) (сплю/засыпаю/спим/засыпаем/вырубаюсь)} *
        q!: * {([очень] долго/(несколько/много) часов) лежу [в|на] (кровати/постели) [и] [все] $cannot}
        script: offtopicReaction("Insomnia");

    state: CountSheep
        q!: * {(*считать/*считай/*считаем) [мне/со мной] (~слон/слоник*/~овца/овечек/овечк*/~баран/бараш*)} *
        script: offtopicReaction("CountSheep");

    state: WhatIsYourFavoriteBook
        q!: * {(интересн*/любим*) [~твой/~ваш] (~книга/книж*)} *
        q!: * {$like [тебе/вам] (читать/чтение/~книга/книж*)} *
        q!: * {(*советуй*/*советуешь) [какую-нибудь/~любой] (~книга/книж*)} *
        q!: * {(*советуй*/*советуешь) [что-нибудь/что-то/что] *читать} *
        q!: * {~какой [мне/нам] выбрать (~книга/книж*)} *
        q!: что [лучше] *читать
        script: offtopicReaction("WhatIsYourFavoriteBook");

    state: WhatDoYouHateInPeople
        q!: * что {[$you] $dislike (в $person)} *
        q!: * что {[$you] (может/могло бы) $dislike (в $person)} *
        q!: * {(какие/каких) [$you] $person ([больше] [всего]) $dislike} *
        q!: * {~какой [$you] $person [был*/мог*] бы $dislike} *
        q!: * {(~какой/каков*) (~качество/~черта характера) [$you] * $dislike} *
        q!: * {(~какой/каков*) [~основной] (минус*/~недостаток) ((в/у) $person)} *
        q!: * за (что/(~какой/каков*) (~качество/~черта характера)) [$you] (можно/мог* бы/можешь) $dislike *
        q!: * {(с какими людьми) [$you] (не нравится) (иметь дело)} *
        script: offtopicReaction("WhatDoYouHateInPeople");

    state: WhatDoYouLikeInPeople
        q!: * что {[$you] $like (в $person)} *
        q!: * что {[$you] (может/могло бы) $like (в $person)} *
        q!: * что {делает ([для] [$you]) $person (привлекательн*/симпатичн*)} *
        q!: * что {([у] [$you]) (вызывает/(может/могло бы) (вызывать/вызвать)) (симпатию/любовь)} *
        q!: * что {[$you] (заставило бы/(может/могло бы) заставить) влюбиться} *
        q!: * {~какой [$you] $person [был*/мог*] бы (импонировать/симпатич*/нравиться/[быть] (по душе/любим*))} *
        q!: * {~какой ([у] [$you]) $person (вызывают/вызывает/(может/мог* бы) (вызывать/вызвать)) (симпатию/любовь)} *
        q!: * {(какие/каких) [$you] $person ([больше] [всего]) $like} *
        q!: * {(~какой/каков*) (~качество/~черта характера) [$you] * $like} *
        q!: * {(~какой/каков*) [~основной] (плюс*/~достоинство) ((в/у) $person)} *
        q!: * за (что/(~какой/каков*) (~качество/~черта характера)) [$you] (можно/мог* бы/можешь) (*любить/симпатизировать) *
        q!: * {(с какими людьми) [$you] $like (иметь дело)} *
        q!: * {(~какой/каков*) ~критерий ([у] [$you]) ~привлекательность} *
        script: offtopicReaction("WhatDoYouLikeInPeople");

    state: HowWouldYouSpendMoney
        q!: * {(как/на что) [$you] [обычно/(чаще/больше) всего] (тратишь*/тратите*/расход*)} *
        q!: * {(как/на что) [$you] [обычно/(чаще/больше) всего] (*тратил* [бы]/тратишь*/расход*) $money} *
        q!: * (какой/что) * ~основной ~статья расходов *
        q!: * {что бы [$you] (купил*/приобрел*/хотел* (купить/приобрести))} *
        q!: * {(как/на что) [$you] *тратил* [бы] $money * ([очень] (много/~большой (~количество/~сумма)/$Number (тысяч|тыс|миллионов|млн|миллиардов|млрд)))} *
        q!: * {сколько $money ([у] $you) [необходимо/нужно/требуется/уходит] (на жизнь/для поддержания уровня жизни)} *
        script: offtopicReaction("HowWouldYouSpendMoney");

    state: WhichCountryWouldYouLikeToVisit
        q!: * [в] ~какой (~страна/~место на планете) * (хотел*/поехал*/жить/отправился/отправилась/отправились/посетил*/посмотрел*/выбрал* для путешествия/съездил*) *
        q!: * если бы (ты/вы) (мог*/смог*) (жить/оказаться/попасть/поехать/отправиться/съездить) (где/куда) (угодно/хочешь) *
        script: offtopicReaction("WhichCountryWouldYouLikeToVisit");

    state: WhoYouWantToBe
        q!: * {кем [$you] хотел* [бы] (быть/стать/родиться)} *
        q!: * {кем [$you] [бы] (был/была/были)} * {(мог/могла) (выбрать/выбирать)} *
        q!: * {кем [$you] бы (был/была/были/стал) (в [твоих/ваших] мечтах)} *
        q!: * {кем [$you] (мечтаешь/хочешь) (стать/быть/работать)} *
        q!: * (есть [ли]/какая) работа [твоей/вашей] мечты *
        q!: * {(мог/могла) (выбрать/выбирать)} * {кем [$you] [бы] (был/была/были)} *
        script: offtopicReaction("WhoYouWantToBe");

    state: FavoriteQuote
        q!: * [твоя] любим* (цитат*/~высказывание/~изречение/~фраза) *
        q!: * {~какой (нравится/любишь/любите/по душе/запомнилась) [из (фильма/кино/кинофильма)] ([больше] [всего]) [$you] (цитат*/~высказывание/~изречение/~фраза)} *
        q!: * {кого бы [$you] процитировал* [из (фильма/кино/кинофильма)] } *
        q!: * {(кинь*/напиши*) [мне] [~любой/~какой нибудь/какую-нибудь/какое-нибудь] [$AnyWord] (цитат*/~высказывание/~изречение/~фраза) [из (фильма/кино/кинофильма)]} *
        script: offtopicReaction("FavoriteQuote");

    state: WhatDoYouThinkAbout
        q!: что [$you] (думаешь/(можешь/мог* бы) сказать) *
        q!: что бы [$you] сказал* *
        q!: (ваш*/~твой) ~мнение (о/об/насчет/на) *
        q!: * (интересно/выскажи*/поделись/поделитесь) (ваш*/~твой/~свой) ~мнение (о/об/насчет/на) *
        q!: * (~какой/каков*) ([у] [$you]) (~мнение/взгляд*) (о/об/насчет/на) *
        script: offtopicReaction("WhatDoYouThinkAbout");

    state: SocialMedia
        q!: * {$socialMedia (через/посредством/с помощью) сидишь} *
        q!: * {$socialMedia (через/посредством/с помощью) (можно/могу) (связаться/*писать) ([с] [$you])} *
        q!: * {([у] $you) (есть [акк/аккаунт/страница/страничка]/можно найти/зареган*/зарегистрирован*) ([в/на] $socialMedia)} *
        script: offtopicReaction("SocialMedia");

    state: PerfectLife
        q!: * {~какой (~идеальный/идеален/идеальна) $you (считаешь/считаете) (жизнь/образ жизни)} *
        q!: * {(~идеальный/идеален/идеальна) ([для] [$you]) (жизнь/образ жизни)} *
        q!: * {((твоей/вашей) мечты) (жизнь/образ жизни)} *
        q!: * {как бы $you (мечтал*/хотел*) жить} *
        script: offtopicReaction("PerfectLife");

    state: DoYouBeliveIn
        q!: ты веришь [в] *
        script: offtopicReaction("DoYouBeliveIn");

    state: HaveYouArgued
        q!: * {$you [когда нибудь/когда-нибудь/когда-то/когда то] [$withSomeone] $argue} *
        q!: * {$you [когда нибудь/когда-нибудь/когда-то/когда то] был* [$withSomeone] (в ссоре)} *
        q!: * {([у] $you) (бывают/случал*) [$withSomeone] (конфликт*/разноглас*/~ссора/недопониман*)} *
        script: offtopicReaction("HaveYouArgued");

    state: Where
        q!: где *
        script: offtopicReaction("Where");

    state: Who
        q!: кто *
        q!: * кто ( такой:isTheMan | такая:isTheWoman ) *
        if: $parseTree.value
            script: offtopicReaction(["Who", $parseTree.value]);
        else:
            script: offtopicReaction(["Who", "isTheMan"]);

    state: Which
        q!: какие *
        script: offtopicReaction("Which");

    state: WhichOne
        q!: ~какой *
        script: offtopicReaction("WhichOne");

    state: Where2
        q!: куда *
        script: offtopicReaction("Where2");

    state: WhoWin
        q!: кто победит *
        script: offtopicReaction("WhoWin");

    state: TellAJoke
        q!: * расскажи* (анекдот / шутку) *
        script: offtopicReaction("TellAJoke");

    state: IsCrimeaOurs
        q!: * крым наш *
        script: offtopicReaction("IsCrimeaOurs");

    state: HowToLoseWeight
        q!: * {(как/хочу/хотим) [мне/нам] [снова/опять] (похудеть/(сбросить/скинуть) (вес/[несколько/$Number/пару] [лишн*] (килограмм*/кг)/(остаться/оставаться) в форме)/меньше весить)} *
        q!: * {перестать быть [~такой] $fat} *
        q!: * {[~сказать/~слышать] я * ($fat/разожралась) [что делать/как быть]} *
        q!: * {[мне/нам] стать [снова/опять] (стройн*/худой/худым/худее)} *
        q!: * {(убрать/избавиться) [мне/нам] ([от] (~жир/~спасательный ~круг с талии/(~толстый/~жирный) (~попа/~пятый ~точка/ляхи/икры)/~пухлый ~щека/~второй ~подбородок/бока/живот/целлюлит*))} *
        q!: * [что делать] * {(я/мы) [пока/[все] еще] [~такой] [$AnyWord] [и] $fat} *
        q!: * {(подо мной) развалился стул} *
        q!: * {как (не (набрать вес/разжиреть/раздаться/потолстеть/растолстеть/потерять форму))} *
        q!: * (набираю вес/набрал* вес/жирею/разжирел*/раздаюсь/раздался/раздалась/толстею/полнею/поправляюсь/теряю форму) *
        q!: * что [бы] [мне/нам] [~такой] съесть чтобы похудеть *
        q!: * (хочу/хотим) (есть/жрать) и не толстеть *
        q!: * {(мне/нам) (нужно/надо/не помешает) [$AnyWord] (*худеть/(сбросить/сбрасывать/терять/скинуть/скидывать) (вес/[несколько/$Number/пару] [лишн*] (килограмм*/кг)))} *
        q!: * {(я/мы) потостел* на (несколько/$Number/пару) (килограмм*/кг)} *
        q!: * {(у (меня/нас)) [вырос*] [полн*/толст*/жирн*] (икры/ляхи/попа/пятая точка/ноги/пузо/пузан/живот/бока/~спасательный ~круг на талии/~пухлый ~щека/~второй ~подбородок/целлюлит*)} *
        q!: * {(как добиться/в чем секрет) (похудения/худобы)} *
        script: offtopicReaction("HowToLoseWeight");

    state: HowToQuitSmokingDrinking
        q!: * {(бросить/бросать/завязать/завязывать/перестать/отказаться/(мне/мы) (надоело/противно)/устал*) [с/от] (~курение/~выпивка/~пьянство/$smokeInf/пьянствовать/пить/бухать/выпивать/вредн* привыч*/~алкоголь/алкоголизм*)} *
        q!: * {(не [выходит/получается/могу]) (бросить/брошу/бросим/бросать/перестану/перестанем/перестать/завязать/завяжу/завяжем) [$AnyWord] [никак] ($smokeInf/пить/бухать/выпивать/алкашк*/алкоголь* [~напиток]/сигарет*/~сигара/~сигарилла/~трубка/самокрутк*/вейп*)} *
        q!: * {(не (могу/хочу/можем/хотим)) [больше] ($smokeInf/пьянствовать/пить/бухать/выпивать)} *
        q!: * {стать (непьющ*/некурящ*)} *
        q!: * [что делать/как быть] * {(я/мы) (алкаш*/алкоголик*/пьяниц*/(пью/пьем/бухаю/буаем/выпиваю/выпиваем) как черт)} *
        q!: * {(у (меня/нас)) алкоголизм} *
        q!: * {(завязать/завязывать/избавиться) [с/от] ~вредный ~привычка} *
        q!: * {(пьянствовать/пить/бухать/выпивать/$smokeInf) (*реже/меньше)} *
        q!: * {(снизить/сократить) количество сигарет} *
        q!: * {(бросить/бросать/завязать/завязывать/перестать) * ($Number (~пачка/~сигарета/~бутылка/~банка))} *
        script: offtopicReaction("HowToQuitSmokingDrinking");

    state: GiveMeMoney
        q!: * {(~дать/~прислать/одолж*/займ*/подгони*/перевед*/*кин*) [мне] * ($money/$Number [к/до]/$regexp<\d+к>) * [[в] долг*/верну/[на] телефон*/до/на]} *
        q!: * {(~дать/~прислать/одолж*/займ*/подгони*/перевед*/*кин*) [мне] * ([в] долг*) * [$Number [к/до]]} *
        script: offtopicReaction("GiveMeMoney");

    state: DoItNow
        q!: {(давай*/*делай*) сейчас }  
        script: offtopicReaction("DoItNow");

    state: LetsGoTo
        q!: * {(поедем*/пойдем*/сходим/поехали/пошли*/поедешь/пойдешь/погнали/го/гоу) * (в/на/по/за/куда-нибудь/*гуля*/встретимся) [со мной/вдвоем]} *
        q!: * {(поедем*/пойдем*/может/го/сходим/давай/поехали/давай*/пошли*/поедешь/пойдешь/погнали/го/гоу/сходить) * (*купаться/*купаемся/ресторан*/речк*/*катать*/клуб*/встре*/гаражи/гриб*/*игр*/вместе) [в/на/по/за/куда-нибудь] [со мной/вдвоем]} *
        q!: * {прогул* (со мной/вдвоем/совместно*)} *
        script: offtopicReaction("LetsGoTo");

    state: CanI
        q!: можно *
        script: offtopicReaction("CanI");

    state: WhatIsTheDifferenceBetween
        q!: * (какая/есть) разница *
        q!: * [(какая/есть) *] разница между * [$morph<С тв мн>] *
        q!: * {разниц* (между [$AnyWord *] и [$AnyWord *])} *
        q!: * чем {отлич* ($AnyWord *)} от $AnyWord * *
        script: offtopicReaction("WhatIsTheDifferenceBetween");

    state: HowOften
        q!: * {(ты/вы) * ([как] часто [ли]) * [$morph<Г>]} *
        q!: * {[ты/вы] * ([как] (часто/редко) [ли]) * ($morph<Г дст 2л нст>)} *
        script: offtopicReaction("HowOften");

    state: BeSerious
        q!: * а [если] (все* таки/все-таки/*серьез*/[би] *сириос*/[би] *сириус*) *
        q!: * {[говори*/будь*/по] (серьезн*/серьезк*/[би] *сириос*/[би] сириус*)} *
        script: offtopicReaction("BeSerious");

    state: WhenWillBeHungerTimes
        q!: * когда (наступит/будет) * голод 
        script: offtopicReaction("WhenWillBeHungerTimes");

    state: WhatIsTheSoftestThing
        q!: * что мягче *
        script: offtopicReaction("WhatIsTheSoftestThing");

    state: WhenNextWar
        q!: * {[когда/кто] (будет/начнет*) * [всемирная|третья мировая|еще] * (~война/революц*/третья мировая)} *
        q!: * {восстан* * (машин*/техник*/искусствен* интеллект*/робот*)} *
        q!: * [думаешь] * ~война *
        script: offtopicReaction("WhenNextWar");

    state: AreYouSure
        q!: [и/a/но] (ты/вы) [прямо/прям/точно] уверен*
        q!: [и/a/но] (ты/вы) [прямо/прям/точно] уверен* в *
        script: offtopicReaction("AreYouSure");

    state: ShouldI
        q!: * мне (поехать/сделать/посмотреть/стоит) *
        q!: * $morph<ИНФИНИТИВ дст> ли мне *
        q!: * (стоит/следует/должен/должн*) ли [я/мне/мы/нам/$AnyWord] $morph<ИНФИНИТИВ дст> *
        script: offtopicReaction("ShouldI");

    state: GiveAdvice
        q!: * (что делать если/{(что*/дай/дать/поделись/[не $weight<-1>] нужен) [ты] * *совет*}) *
        q!: * {(подскажи*/укажи) как} *
        q!: * [може*] {{(~нужно/нужен/~дать/~хотеть) [мне]} * [не $weight<-1>] (~совет/посовет*)} *
        q!: * как [мне/нам/$AnyWord] (($AnyWord/следует) поступить/поступить/вернуть/забыть) *
        script: offtopicReaction("GiveAdvice");

    state: WhyDoYouAvoidAnswering
        q!: * почему (ты/вы) * ((уходишь/уклоняешься) от ответа/не отвечаешь) *
        q!: * почему * (ты/вы) * ((уходишь/уклон*/~избегать) * (ответ*/отвеча*)/не * отвеча*/~юлить) *
        q!: * [ты/вы] [не] (може*/долж*) * (нормальн*/хорош*) ответ* [дать] *
        script: offtopicReaction("WhyDoYouAvoidAnswering");

    state: WhyWomenDo
        q!: * (почему/зачем/для чего) * [меня] {(девочк*/девушк*/женщин*/бабы/бабам/~дама) * ([не] $morph<Г дст 3л нст>)} *
        q!: * {(почему/зачем/для чего) * ([у/для/все] (девочк*/девушк*/женщин*/бабы/бабам/~дама))} *
        script: offtopicReaction("WhyWomenDo");

    state: WhyMenDo
        q!: * (почему/зачем/для чего) * [меня] {(мужчин*/мужик*/мальчик*/~парень) * ([не] $morph<Г дст 3л нст>)} *
        q!: * {(почему/зачем/для чего) * ([у/для/все] (мужчин*/мужик*/мальчик*/~парень))} *
        script: offtopicReaction("WhyMenDo");

    state: WhereIs
        q!: * где находится *
        script: offtopicReaction("WhereIs");

    state: WhyTheyDontPayMe
        q!: * [почему] * {(задерживают/(не/перестал*) * (*плат*/*плач*)) * (пенси*/зарплат*/зп/стипенди*/степух*/стипух*/работ*)} *
        q!: * [почему] {мне * (не/перестал*) * (*плат*/*плач*)} *
        q!: * [почему] * {((не/перестал*) * (*плат*/*плач*)) * (вовремя/полностью)} *
        q!: * [почему] * {(мало/не много/немного/небольш*/недовол*/не довол*) * (пенси*/зарплат*/зп/стипенди*/степух*/стипух*)} *
        q!: * я * без денег *
        script: offtopicReaction("WhyTheyDontPayMe");

    state: WhereToComplain
        q!: * {(куда/кому/где) * (*жаловаться [на]/~жалоба)} *
        script: offtopicReaction("WhereToComplain");

    state: YouAreABadHelper
        q!: * {[ты] * (интеллект*/умный) * (так себе/не (очень/особо/оправдыва*))} *
        q!: * {(ты/тебя) * (непонятливый/тупой/тупишь/глупый/так себе*/никак не/никакой/никакущ*) * [$bot/работ*/помощник*/операт*/совет*]} *
        q!: * {(не оправдал*) ожиданий}
        q!: * {плохой ты * [помощник/$bot]} *
        q!: * о тебе * лучше чем * (есть/на самом деле) *
        script: offtopicReaction("YouAreABadHelper");

    state: HowToKeepLiving
        q!: * как [дальше] жить *
        script: offtopicReaction("HowToKeepLiving");

    state: WhatColorIs
        q!: * какого цвета * 
        script: offtopicReaction("WhatColorIs");

    state: YouDontLikeTalking
        q!: * (вы/ты) [такой/такая] * (не * /~необщительный) *
        q!: * {(вы/ты) * не * (~общительный/разговорчив*)} *
        q!: * {(вы/ты) * (неразг*/~молчаливый)} *
        q!: * {([не $weight<-1>] общительным) * тебя (не назовёшь)} *
        q!: * {(вы/ты) * пообщительнее} *
        q!: * {(вы/ты) (не (хочешь/желаешь)) * (*говар*/*говор*)} *
        script: offtopicReaction("YouDontLikeTalking");

    state: WhenWillBeSnow
        q!: * {когда * [будет/выпадет/пойдет/нормал*] (снег/снежн*/снежк*/зима)} *
        q!: * {(будет/выпадет/пойдет [ли]) * (снег*/снежн*/снежк*)} *
        q!: * {(ждать/*ждал*) * снег*} *
        script: offtopicReaction("WhenWillBeSnow");

        state: ShowWaiterResponse
            q: * (да/нет/а/что/ком*/кто/сне*/зим*) *
            script: offtopicReaction("ShowWaiterResponse");

    state: WhoAmI
        q!: * кто я [такой/такая] *
        script: offtopicReaction("WhoAmI");

    state: WhouldYouLikeToTry
        q!: * хотел* бы (быть/стать/попробовать) * 
        script: offtopicReaction("WhouldYouLikeToTry");

    state: WhatShouldIDoWhen
        q!: * {что * [надо] делать * (если/когда/~ситуация/~случай)} *
        q!: * {что * [надо] делать * $morph<Г>} *
        script: offtopicReaction("WhatShouldIDoWhen");

    state: MyLoveLeftMe
        q!: * {({меня бросил*}/расстались/(ушел/ушла) * (от меня/к ~другой)/~сбежать) * (девушк*/парен*/~жена/~муж/~любимый)} *
        q!: * {(~жена/~муж) * (любовник*/любовниц*/друг* (муж*/жен*/парн*/парен*/девушк*))} *
        q!: * (муж*/жен*/парн*/парен*/девушк*) * (с (другой/другим)) *
        script: offtopicReaction("MyLoveLeftMe");

    state: IFeelBad
        q!: * {(мне/я/себя) * [невыносимо/неважно/очень/так/как/отвратительно*] (больно/чувствую/плохо/нехорошо/(не по/так) себе/страдаю)} *
        q!: * {[мне/я/себя/у меня] отвратительно* самочувстви*} *
        script: offtopicReaction("IFeelBad");

    state: IamSick
        q!: * {(я/у меня) * (болит/болен/больна/*болел*/простудился/простыл*)} *
        q!: * {(голов*/~рука/~глаз/~сердце/~нога/~живот/~спина/~мышца/~тело/зуб*) * (болит/боль*)} *
        q!: * {(я/у меня) [зараз*] * (болезн*/грипп*/ветрянк*/насморк*/сифилис*/спид/рак/*вирус/вирус*/ангин*/орви/орв)} *
        q!: * {(я/у меня) * (~сломать/сломан*/свернул*/подвернул*/вывих*) * (~рука/~нога/~спина/~кость/конечн*/нос/шею/шеи/позвон*/кист*/плеч*)} *
        q!: * {(я/меня/мне) * [~лежать/~ходить/надо/нужно] * (в (~больница/~госпиталь/соматик*)/[к] врач*)} *
        q!: * {(лежу/хожу) * (в (~больница/~госпиталь/соматик*/стационар*/*гии)/[с] перелом*/болею)} *
        script: offtopicReaction("IamSick");

    state: WhichOneDoYouKnow
        q!: * (каких/какие/какое/что) * (знаешь/знаете) *
        q!: * (кого/какого/что) * (знаешь/знаете) (из/о/об/обо) *
        script: offtopicReaction("WhichOneDoYouKnow");

    state: Why
        q!: почему *
        script: offtopicReaction("Why");

    state: WhyDoYouHave
        q!: для чего тебе *
        script: offtopicReaction("WhyDoYouHave");

    state: WhenWillYou
        q!: * Когда ты [$AnyWord] $morph<Г буд> *
        script: offtopicReaction("WhenWillYou");

    state: WhenHaveYou
        q!: * Когда ты [$AnyWord/в *] $morph<Г прш> *
        script: offtopicReaction("WhenHaveYou");

    state: BadWords
        q!: * $obsceneWord *
        script: offtopicReaction("BadWords");

    state: ILikeYou
        q!: * {(ты/вы) * (нравишься*/нравишся) * мне} *
        script: offtopicReaction("ILikeYou");

    state: IDontLikeYou
        q!: * {(ты/вы) * не (нравишься*/нравишся) * мне} *
        script: offtopicReaction("IDontLikeYou");

    state: IWantYou
        q!: * я (тебя/вас) хочу *
        script: offtopicReaction("IWantYou");

    state: IDontWantYou
        q!: * я * (тебя/вас) [больше] не хочу *
        script: offtopicReaction("IDontWantYou");

    state: ILoveYou
        q!: * я * (тебя/вас) люблю *
        q!: * я * люблю (тебя/вас)
        q!: * я * ~влюбиться * (тебя/вас)
        q!: * я ~влюбиться
        script: offtopicReaction("ILoveYou");

    state: IDontLoveYou
        q!: * я (тебя/вас) * (не люблю/любил/любила) *
        script: offtopicReaction("IDontLoveYou");
