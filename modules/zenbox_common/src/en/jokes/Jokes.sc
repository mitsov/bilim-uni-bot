require: ../../common/common.sc
require: Jokes.js

require: common.js
    module = zb_common

require: patternsEn.sc
  module = zb_common

require: ../main.js

require: answers.yaml
    var = JokesCommonAnswers
init:
    $global.$JokesAnswers = (typeof JokesCustomAnswers != 'undefined') ? applyCustomAnswers(JokesCommonAnswers, JokesCustomAnswers) : JokesCommonAnswers;

patterns:
    $jokes = (joke* | funny [story/stories] | anecdote* | one liner* | jest* | banter* | gag* | jape* | jocularity | [something] (hilarious/funny) | humor*)
    $laugh = (laugh*/laughter/haha*/chuckle/giggle*/smil*)

theme: /Jokes

    state: Get
        q!: * [(can/could) you] * ((bring/get/make) [$me] * [a] $laugh | amuse [me]) *
        q!: * [$tellMe/want/wanna] * [hear] * [more] * $jokes *
        q!: * [$tellMe/want/wanna] * ($laugh/have fun) *
        q!: * (make fun/tell [a] $jokes) [for (me/us)] *
        q!: * [(can/could) you] * (cheer * up | perk up [my] mood | make [me] feel better | put * in [a] (good|better) mood | put * smile on [my] face) *
        q: * $tellMe * || fromState= ../Get, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_HAPPY';
            chooseJoke();
        if: !$temp.nextState
            a: {{$temp.answer}}
        else:
            go!: {{$temp.nextState}}


    state: canYouLaugh
        q!: * (can you | do you (like/love) | like | love) * $laugh *
        q!: * [want [you] [to]] * [see/hear] [you*] (smile/laugh/$laugh) *
        q: * (do n't be (shy/embarrassed) | feel free to) * || fromState = ../canYouLaugh, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_HAPPY';
        a: {{ selectRandomArg($JokesAnswers["canYouLaugh"]) }}

    state: doYouKnowJokes
        q!: * (i|me) * $like * ($jokes/$laugh) *
        q!: * (know/rebember/recall/memorize/love/like) * $jokes *
        q!: * (can you/could you/do you like) * [tell*] * $jokes *
        a: {{ selectRandomArg($JokesAnswers["doYouKnowJokes"]) }}
        
    state: Funny
        q: * (lol/lul/omg/omegal*/rofl/hah*/hoh*/xaxa*) * || fromState = ../Get, onlyThisState = true  
        q: * [very/much] (fun|funny|interesting|$good|nice|sweet|thank*) * || fromState = ../Get, onlyThisState = true
        a: {{ selectRandomArg($JokesAnswers["Funny"]) }}


    state: NotFunny
        q: * [$you] * [again|yet|too|$you] (not funny | inappropri* | improper* | silly | weird | strange | fool* | difficult | (no/not/n't) underst* | boring | dumbass | retard* | not interest* | fail* | poor*) [[your] $jokes] || fromState = ../Get, onlyThisState = true
        q: * is it * [seems] (fun*|good|hilarious|best|approri*|proper*) *
        q: * (what|where) * {[is|was] [you*] ($jokes|meaning|point)} *
        q: * no humor* *
        script:
            $response.action = 'ACTION_EXP_SPEECHLESS';
            delete $client.jokes;
        a: {{ selectRandomArg($JokesAnswers["NotFunny"]) }}

    state: One more 
        q: * $continue [one] * || fromState = ../Get, onlyThisState = true
        q: * $tellMe * || fromState = ../Funny, onlyThisState = true
        q: $agree * || fromState = ../Funny, onlyThisState = true
        q: * $tellMe * || fromState = ../doYouKnowJokes, onlyThisState = true
        q: $agree * || fromState = ../doYouKnowJokes, onlyThisState = true
        q: * $tellMe * || fromState = ../BadMood, onlyThisState = true
        q: $agree *|| fromState = ../BadMood, onlyThisState = true
        q: * $continue * || fromState = ../Funny, onlyThisState = true
        q: {[$tellMe|$continue|better] * (other|next|new|another)} || fromState = ../Get, onlyThisState = true
        q: [read/tell] [one] more * || fromState = ../../Jokes
        go!: ../Get

    state: No more
        q: * (stop/enough/do n't/bored/end/shut up/silence/break) * || fromState = ../Get, onlyThisState = true
        q: * $disagree * [$thanks] * || fromState = ../Funny, onlyThisState = true
        q: * $disagree * [$thanks] * || fromState = ../doYouKnowJokes, onlyThisState = true
        q: * $disagree * [$thanks] * || fromState = ../BadMood, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_SPEECHLESS';
            delete $client.jokes;
        a: {{ selectRandomArg($JokesAnswers["No more"]) }}
            
    state: Good jokes
        q!: * {[have] $you [very / lot / such / so [much]] (nice / fun* / hilarious / joyful / $good) * $jokes} *
        q!: * {(me / I / i 'm) [very] [much] (like* / fond / [in] love) * $you ($jokes / [sense of] humor)} *
        q!: * {$you (nice / fun* / $good / loud*) * (laugh / smil* / nicker* / whinn*)} *
        q!: * $you * [very / lot / such / so [much]] * (fun* / cheerful / merry / gay / glad / hilarious / happy / jolly / joyful / jovial) * 
        q!: * { $you (have / had / $good) * [sense of] humor} *
        q: * (nice / fun* / $good) * || fromState = ../canYouLaugh, onlyThisState = true
        q: * {[this|that|the] [one] [$you] * [best|most] (fun*|cool*|$good) * $jokes} * || fromState = ../Get, onlyThisState = true
        q: {(this|that|the) [one] even (better|cool*) [$jokes]} || fromState = ../Get, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_CUTE';
        a: {{ selectRandomArg($JokesAnswers["Good jokes"]) }}
        if: $client.jokes
            go!: ../Funny
        else:
            go!: ../doYouKnowJokes

    state: Bad jokes
        q!: * {$you $bad * (laugh / smil* / nicker* / whinn*)} *
        q!: * {[have] $you [very|such] (not (nice / fun* / hilarious / joyful / $good) | $bad/dumb/retard*/inappropri*/boring/improper*/not fun*/stupid/silly/weird/hard/lame/unfortunate) * $jokes} *
        q: * (not (nice / fun* / hilarious / joyful / $good) | $bad/dumb/retard*/inappropri*/improper*/not fun*/stupid/silly/weird/hard) * || fromState = ../Get, onlyThisState = true
        q: * $bad * || fromState = ../canYouLaugh, onlyThisState = true
        script:
            $response.action = 'ACTION_EXP_SAD';
            delete $client.jokes;
        a: {{ selectRandomArg($JokesAnswers["Bad jokes"]["a1"]) }}
        a: {{ selectRandomArg($JokesAnswers["Bad jokes"]["a2"]) }}

    state: Old jokes
        q!: * {(old/sophomoric) [one] * $jokes} *
        script:
            $response.action = 'ACTION_EXP_SAD';
            delete $client.jokes;
        a: {{ selectRandomArg($JokesAnswers["Old jokes"]["a1"]) }}
        a: {{ selectRandomArg($JokesAnswers["Old jokes"]["a2"]) }}

    state: YouHaveJustToldThisJoke
        q!: * {[$you] already ((told|said|joked|was) * $jokes)} *
        q: * {(this|that) $jokes * [i|me] * (bored/hate/fed up) *} *
        q: * {[$you] already ((told|said|joked|was) * $jokes)} * || fromState = ../Get, onlyThisState = true
        q: * {[$you] repeat*} * || fromState = ../Get, onlyThisState = true
        a: {{ selectRandomArg($JokesAnswers["YouHaveJustToldThisJoke"]) }}
        go!: ../Funny

    state: AreYouFunny?
        q!: * {$you * [have] * [ever [been/had]] [very|much|laughed [so hard] ] (laughed|fun|smiled)} *
        a: {{ selectRandomArg($JokesAnswers["AreYouFunny?"]) }}

    state: JokesHaveEnded
        a: {{ selectRandomArg($JokesAnswers["JokesHaveEnded"]) }}
        script:
            delete $client.jokes;

    state: Regret
        q: * ([too] bad|[what] [a] pity| sad | feel* bad* for| i 'm sorry) * || fromState = ../JokesHaveEnded, onlyThisState = true
        a: {{ selectRandomArg($JokesAnswers["Regret"]) }}
        
    state: BadMood
        q!: * [i|me] * [very] (not [in] $good|$bad|sad*|drear*|not fun*|gloomy | grim | dark | bleak | dismal | somber | sombre | dreary | glum | murky|hard|not happy|sucks|fuck* up|[feel*] lonely|alone|tired| unhappy) [mood] *
        q!: you (are/'re) boring [me]
        a: {{ selectRandomArg($JokesAnswers["BadMood"]["a1"]) }}
        a: {{ selectRandomArg($JokesAnswers["BadMood"]["a2"]) }}