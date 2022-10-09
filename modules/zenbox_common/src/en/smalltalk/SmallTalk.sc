require: params.yaml
   var = SmallTalkParams
   name = SmallTalkParams

require: patternsEn.sc
  module = zb_common

require: common.js
  module = zb_common

require: ../../common/smalltalk/SmallTalk.js
require: ../../common/common.sc


patterns:
    $gender = (
        (boy|guy|male|man|not (female*|girl|woman)):0|
        (girl|woman|female|gal|not (male|man|guy|boy)):1)
    $nAffront = (wtf|pig |bastard |suck |idiot|fuck*|motherf*|mother-fuck*|shit|ass|ass-*|asshole|asswipe|gay|homos*|fag|crap*|prostitute|arschloch|arsch|bitch|damn/kiss my ass)
    

theme: /SmallTalk

    state: Unit

        state: Acquaintance
            q!: * (who|what) are you
            q!: * tell (me|us) * about (yourself|you) *
            q!: * (introduce|introduction) * yourself  *
            q!: * ({are you}|you're) * (robot|toy|human|android|thing) *
            if: SmallTalkParams.unitAcquaintance
                script:
                    $reactions.answer(SmallTalkParams['unitAcquaintance']);
            else:
                go!: ../../Fallback

        state: Age
            q!: * (when|what year) * $you born * * 
            q!: * how old {are you} * 
            q!: * are you (old|young)
            q!: * (what is| what 's) your age *
            q!: $you age
            if: SmallTalkParams.unitAge
                script:
                    $reactions.answer(SmallTalkParams['unitAge']);
            else:
                go!: ../../Fallback

        state: Author
            q!: * who * (made|created|developed|invented|produced) you *
            q!: * who (is|are) your * (author*|developer*|maker*|creator) *
            if: SmallTalkParams.unitAuthor
                script:
                    $reactions.answer(SmallTalkParams['unitAuthor']);
            else:
                go!: ../../Fallback

        state: Name
            q!: * (what is|what*) * $your * name *
            q!: * how * [i] * call you * 
            q!: * (what|how) * [do] you call yourself * 
            q!: * [can i] сhange $you name
            q!: * tell me $you name
            if: SmallTalkParams.unitName
                script:
                    $reactions.answer(SmallTalkParams['unitName']);
            else:
                go!: ../../Fallback

        state: Gender
            q!: * {$you * $gender} *
            if: SmallTalkParams.unitGender
                script:
                    $reactions.answer(SmallTalkParams['unitGender']);
            else:
                go!: ../../Fallback

        state: WhatAreYouDoing
            q!: * what * $you * doing * 
            if: SmallTalkParams.unitWhatAreYouDoing
                script:
                    $reactions.answer(SmallTalkParams['unitWhatAreYouDoing']);
            else:
                go!: ../../Fallback

        state: WhatCanYouDo
            q!: * what * [else] * [can|could|able|capable] * $you (do|say|answer) *
            q!: * what are your skills *
            q!: * what skills * {you have [got]} *
            q: ([that ['s/is]] all|* [what/something/anything] * else *) || fromState = ../WhatCanYouDo
            if: SmallTalkParams.unitWhatCanYouDo
                script:    
                    $reactions.answer(SmallTalkParams['unitWhatCanYouDo']);
            else:
                go!: ../../Fallback

        state: Family
            q!: * ($you|pudding*)  have * (famil*|relativ*) *
            q!: * (who * $your|do $you have) * (famil*|relatives|parents|sibling*|brother*|sister*) *
            if: SmallTalkParams.unitFamily                 
                script:    
                    $reactions.answer(SmallTalkParams['unitFamily']);
            else:
                go!: ../../Fallback

        state: YouAreStupid
            q!: * [$you] * $stupid *
            q!: * $you * $no * $clever *
            if: SmallTalkParams.unitYouAreStupid
                script:    
                    $reactions.answer(SmallTalkParams['unitYouAreStupid']);
            else:
                go!: ../../Fallback

        state: YouAreClever
            q!: * {[how] $clever [are] * $you} *
            q!: * $you are [very/so] $clever *
            q!: * (this|that) (is|was) [very/so] $clever *
            if: SmallTalkParams.unitYouAreClever  
                script:    
                    $reactions.answer(SmallTalkParams['unitYouAreClever']);
            else:
                go!: ../../Fallback

        state: WhatDoYouLike 
            q!: * what* * $you * $like *
            q!: * (what*|do $you have) * (hobby|hobbies|leisure* activit*) *
            q!: * (what are | what 're) $you * (into|keen on) *
            q!: * (what|how) * $you (do|spend) * ((free|spare|leisure) time| for fun) *
            if: SmallTalkParams.unitWhatDoYouLike
                script:    
                    $reactions.answer(SmallTalkParams['unitWhatDoYouLike']);
            else:
                go!: ../../Fallback

    state: Appraisal

        state: ThankYou
            q!: * (than* [you]|grateful) *
            q!: * nice of you *
            q!: * appreciate it *
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
            q!: [always/you* re/your] welcome
            q!: $youAreWelcome
            if: SmallTalkParams.appraisalYouAreWelcome 
                script:
                    $reactions.answer(SmallTalkParams['appraisalYouAreWelcome']);
            else:
                go!: ../../Fallback

    state: Dialog

        state: YouDontUnderstand
            q!: * ($you|i) * ($no|don 'nt|do not) * ($understand|get (it|me) [right]|see [the point]) * 
            q!: * you do n't get it right *
            q!: * what * $you (do not|do n't) $understand *           
            if: SmallTalkParams.dialogYouDontUnderstand
                script:
                    $reactions.answer(SmallTalkParams['dialogYouDontUnderstand']);
            else:
                go!: ../../Fallback

        state: Affront
            q!: * $nAffront *
            if: SmallTalkParams.dialogAffront
                script:
                    $reactions.answer(SmallTalkParams['dialogAffront']);
            else:
                go!: ../../Fallback

    state: Greetings

        state: Bye
            q!: * ($bye/good night/sleep/shut down/ciao/exit/quit) *
            q!: * (I|i 've) (need|have [got]|got) to (go|run) *
            if: SmallTalkParams.greetingsBye
                script:
                    $reactions.answer(SmallTalkParams['greetingsBye']);
            else:
                go!: ../../Fallback

        state: Hello
            q!: * $hello * [pudding|dear|robot] *
            q!: * (see you again| long time no see| been a while) *
            q: $me ['m/am/'re/are] * (back|here|home) || fromState = ../Bye, onlyThisState = true
            if: SmallTalkParams.greetingsHello
                script:
                    $reactions.answer(SmallTalkParams['greetingsHello']);
            else:
                go!: ../../Fallback

        state: How are you
            q!: * [$hello] * $howAreYou [today|now|here]
            q!: * [$hello] * (what is| what 's) up * 
            q!: * [$hello] * what mood are you in *
            q!: * [$hello] * (how|what) is your mood *
            q!: * (how|what) (are|do) you feel* *
            q!: * [$hello] * how (is| 's| are| 're) * (life|things|going) *
            if: SmallTalkParams.greetingsHowAreYou
                script:
                    $reactions.answer(SmallTalkParams['greetingsHowAreYou']);
            else:
                go!: ../../Fallback

    state: User

        state: ILikeYou
            q!: * [$me] * $like * $you *
            if: SmallTalkParams.userILikeYou
                script:
                    $reactions.answer(SmallTalkParams['userILikeYou']);
            else:
                go!: ../../Fallback
                
    state: Fallback
        q!: *
        script:
            log(">>>" + toPrettyString(SmallTalkParams));
            log(">>>" + SmallTalkParams['fallback']);
            $reactions.answer(SmallTalkParams['fallback']);