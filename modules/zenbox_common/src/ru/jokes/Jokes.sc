require: ../../common/common.sc
require: Jokes.js

require: patterns.sc
  module = zb_common

require: ../main.js

require: answers.yaml
    var = JokesCommonAnswers
init:
    $global.$JokesAnswers = (typeof JokesCustomAnswers != 'undefined') ? applyCustomAnswers(JokesCommonAnswers, JokesCustomAnswers) : JokesCommonAnswers;

patterns:
    $jokes = (анекдот*|~байка|~шутка|шуток|шуточк*|шутеечк*|шутейк*|юмор*|прикол/[что-то/что-нибудь] (смешное/веселое/забавное/прикольное)|(смешн*/шуточн*/прикольн*/весел*) (~история|~случай|~кейс)) 
    $laugh = (*смеяться|*ржать|*хихикать|*хохотать)
    $haha = (хаха*|ха-ха*|хихи*|хи-хи*|хехе*|хе-хе*|ахах*|лол|ха ха|хи хи|хе хе|хе|хех|ха|хах|хи|хих|ухах*|лол|лал|lol)

theme: /Jokes

    state: Get
        q!: * {[ты можешь|можешь] (развлеки*|рассмеши*|развесел*/повесели*/насмеши*) * $me} *
        q!: * {[$tellMe/хочу/хочется] [*слышать/*слушать] [еще|ещё] * $jokes } *
        q!: * (подн*|повыс*) ($me|свое*) настро* *
        q!: * [я] (хочу/хочется) * (посмеяться/повеселиться/весель*/юмор*) *
        q!: * (развлеки*|рассмеши*|насмеши*|развесели*|пошути*|шути*|повесели*|рассмеши*) * {[еще|ещё] [меня/нас]} *
        q: * $tellMe * || fromState= ../Get, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_HAPPY';
            chooseJoke();
        if: !$temp.nextState
            a: {{$temp.answer}}
        else:
            go!: {{$temp.nextState}}


    state: canYouLaugh
        q!: * {(умеешь/можешь/любишь) * $laugh} *
        q!: * (*смейся|похохочи|смейся|похихикай) *
        q!: * (улыбнись|[$you] (можешь/умеешь) улыбаться) * 
        q: * не стесняйся * || fromState = ../canYouLaugh, onlyThisState = true
        q!: колобок повесился
        script:
            $response.action = 'ACTION_EXP_HAPPY';
        a: {{ selectRandomArg($JokesAnswers["canYouLaugh"]) }}

    state: doYouKnowJokes
        q!: * {(я|мне) $like * ($jokes|*смеяться|*ржать)} *
        q!: * {(знаешь/помнишь/любишь) * $jokes} *
        q!: * {(умеешь/можешь/любишь) * ({шутить [$jokes]}/{рассказывать $jokes})} *
        a: {{ selectRandomArg($JokesAnswers["doYouKnowJokes"]) }}
        
    state: Funny
        q: * $haha * || fromState = ../Get, onlyThisState = true
        q: * [(очень|как|оч)] (смешн*|весело|забавно|$good|милая|мило|миленьк*|спасибо*) * || fromState = ../Get, onlyThisState = true
        a: {{ selectRandomArg($JokesAnswers["Funny"]) }}


    state: NotFunny
        q: [и|это] [тоже|опять|снова|у $you] (несмешн*/неуместн*/глуп*/странная/странный/сложн*/дурац*/не понятн*/[я [тебя]] не понял*/скучн*/~тупая/~тупой/неитересн*/не смешн*/неудачн*/не удачн*) [[~твой] $jokes] || fromState = ../Get, onlyThisState = true
        q: * разве это * [кажется] (смешн*|удачн*|забавн*|весел*|уместн*|прикольн*) *
        q: * в (чем|чём) * {[заключ*|был*] [твой|твоя] ($jokes|~суть|~соль)} *
        q: * {[у] $you (нет|не (имеешь|хватает)|отсутствует) * ~чувство юмора} *
        script:
            $response.action = 'ACTION_EXP_SPEECHLESS';
        a: {{ selectRandomArg($JokesAnswers["NotFunny"]) }}

    state: One more 
        q: * $continue [один|одну] * || fromState = ../Get, onlyThisState = true
        q: * $tellMe * || fromState = ../Funny, onlyThisState = true
        q: $agree * || fromState = ../Funny, onlyThisState = true
        q: * $tellMe * || fromState = ../doYouKnowJokes, onlyThisState = true
        q: $agree * || fromState = ../doYouKnowJokes, onlyThisState = true
        q: * $tellMe * || fromState = ../BadMood, onlyThisState = true
        q: $agree *|| fromState = ../BadMood, onlyThisState = true
        q: * $continue * || fromState = ../Funny, onlyThisState = true
        q: {[$tellMe|$continue|лучше] (~другой|~следующий|~новый)} || fromState = ../Get, onlyThisState = true
        go!: ../Get

    state: No more
        q: * (хватит/стоп/не надо/надоело/довольно/достаточно/прекрати/перестань/остановись) * || fromState = ../Get, onlyThisState = true
        q: * $disagree * [$thanks] * || fromState = ../Funny, onlyThisState = true
        q: * $disagree * [$thanks] * || fromState = ../doYouKnowJokes, onlyThisState = true
        q: * $disagree * [$thanks] * || fromState = ../BadMood, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_SPEECHLESS';
        a: {{ selectRandomArg($JokesAnswers["No more"]) }}
            
    state: Good jokes
        q!: * {[у] $you [очень|оч|такие|такая|такой] (смешн*|весел*|прикол*|$good) * $jokes} *
        q!: * {(мне|я) [очень|оч|так] (нравится|нравятся|понравил*|обожа*|люблю) * $you ($jokes|[чувство] юмор*)} *
        q!: * {$you (здорово|прикол*|$good) * (смеёшься|смеешься|ржешь|ржёшь|хохочешь|хихикаешь)} *
        q!: * $you [очень|оч|такие|такая|такой] * (смешн*|весел*|прикол*|забавн*) *
        q!: * {[у] $you (имеешь|есть|$good) * ~чувство юмора} *
        q: * (здорово|прикол*|$good) * || fromState = ../canYouLaugh, onlyThisState = true
        q: * {[это|этот|эта] [$you] [самая|самый] (лучш*|крут*|$good) * ($jokes|шутк*|байк*)} * || fromState = ../Get, onlyThisState = true
        q: {(это|этот|эта) [вот] (еще/вообще) (лучш*|крут*|круче) [$jokes]} || fromState = ../Get, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_CUTE';
        a: {{ selectRandomArg($JokesAnswers["Good jokes"]) }}
        if: $client.jokes
            go!: ../Funny
        else:
            go!: ../doYouKnowJokes

    state: Bad jokes
        q!: * {$you $bad * (смеёшься|смеешься|ржешь|ржёшь|хохочешь|хихикаешь)} *
        q!: * {[у] $you [очень|оч|такие|такая|такой] (не (смешн*|весел*|прикол*|$good)|$bad|~тупой|дибильн*/неитересн*|неудачн*|несмешн*/неуместн*/глуп*/странны*/сложн*/дурац*/не понятн*/непонятн*/так себе/толст*) * $jokes} *
        q!: * $you * не (смешн*|весел*|прикол*|забавн*) *
        q: * (не (смешн*|весел*|прикол*|$good)|$bad|~тупой|дибильн*/неитересн*|неудачн*|несмешн*/неуместн*/глуп*/странны*/сложн*/дурац*/не понятн*/непонятн*/так себе/толст*) * || fromState = ../Get, onlyThisState = true
        q: * $bad * || fromState = ../canYouLaugh, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_SAD';
        a: {{ selectRandomArg($JokesAnswers["Bad jokes"]["a1"]) }}
        a: {{ selectRandomArg($JokesAnswers["Bad jokes"]["a2"]) }}

    state: Old jokes
        q!: * {(~старый/~бородатый) * $jokes} *
        script:
            $response.action = 'ACTION_EXP_SAD';
        a: {{ selectRandomArg($JokesAnswers["Old jokes"]["a1"]) }}
        a: {{ selectRandomArg($JokesAnswers["Old jokes"]["a2"]) }}

    state: YouHaveJustToldThisJoke
        q!: * {[$you] уже ((говорил|рассказывал|рассказал|~быть) * $jokes|шутил|прикалывался)} *
        q: * {(это|этот|эта) $jokes [мне|мну] [уже] надоел*} *
        q: * [$you] {уже * [так] (говорил|рассказывал|шутил|рассказал|прикалывался|~быть)} [[это|этот|эта] $jokes] * || fromState = ../Get, onlyThisState = true
        q: * {[$you] повторяешься} * || fromState = ../Get, onlyThisState = true
        a: {{ selectRandomArg($JokesAnswers["YouHaveJustToldThisJoke"]) }}
        go!: ../Funny

    state: AreYouFunny?
        q!: * {тебе * [бывает] [очень|безумно|оч|так] (смешно|весело|забавно)} *
        a: {{ selectRandomArg($JokesAnswers["AreYouFunny?"]) }}

    state: JokesHaveEnded
        a: {{ selectRandomArg($JokesAnswers["JokesHaveEnded"]) }}
        script:
            delete $client.jokes;

    state: Regret
        q: * (жаль|жалко) * || fromState = ../JokesHaveEnded, onlyThisState = true
        a: {{ selectRandomArg($JokesAnswers["Regret"]) }}
        
    state: BadMood
        q!: * [я|мне|у меня] * [очень] (не $good|$bad|груст*|тосклив*|не весел*|печал*|мрачн*|тяжел*|не радост*|так себе|отстой|тоска|устал*|одинок*) *
        q!: * {у меня * (не $good|$bad|груст*|тосклив*|не весел*|печал*|мрачн*|тяжел*|не радост*) настроение*} *
        q!: {ты скучный}
        a: {{ selectRandomArg($JokesAnswers["BadMood"]["a1"]) }}
        a: {{ selectRandomArg($JokesAnswers["BadMood"]["a2"]) }}