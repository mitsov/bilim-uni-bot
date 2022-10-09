require: text/text.sc
require: namesEn/namesEn.sc

patterns:
    $catchAll = *
    
    $hello = (((hi|hello|hallo|salute|bonjour|salut|hey) [there])|hey there |heil to thee|g day|howdy|olleh|halo)

    $goodTIme = (good (morning|day|afternoon|evening)|g day)

    $bye = (bye|see you|g bye|goodbye|good bye|bye-bye|bye bye|so long|see you [later]|till later|talk to you later|i (must|have to) go|hasta la vista)

    $goodNight = (goodnight|good night|nighty night|nighty-night|sweat dreams|go to sleep)

    $howAreYou = how are (you|things) [doing|feeling] 


    $serviceWether = ([(check|tomorrow 's|(what|where|how) is|what 's|how 's|tell me [about]|show me)] [the] weather [like]|is it going to rain) [today] *
    $socialNetworks = (facebook|twitter|vkontakte|odnoklassniki|flickr|google plus|reddit|pinterest|linkedin|linked in|myspace|my space|DeviantArt|Orkut|Tagged|CafeMom|Ning|Meetup|myLife|ask fm)

    $Earth = ((this|our|my) planet|earth)

    $stopGame = ((stop|finish|$no want) [game|[to] play*]|[I ('m|am)] (tired|bored)|enough|I * give up)


    $you = (you|yours|thou|thee|U|$your)
    $your = (your|yor|yo)

    $me = (i|me|myself|me|us|our|my|mine|ours)

    $me2you = (me:you|my:your)

    $AnyWord = (a*|b*|c*|d*|e*|f*|g*|h*|i*|j*|k*|l*|m*|n*|o*|p*|q*|r*|s*|t*|u*|v*|w*|x*|y*|z*)
    $any = (whatever|any*|your choice|no matter what)


    $tellMe = (tell [me] [about]|I am interested in|[want|like] to know * about|do $you know about|have $you * heard about|what *
        $you * (think|thought*|opinion))

    $whatIs = ($tellMe|(what is|what 's) [about]| do $you have|is there $your) 

    $question = (what|who|where|why|how come|{(for|due to) wh*}|wh* reason*|wh* purpose*)

    $andYou = (and [(what|how) [about]] (do|did) $you [do]| and [what|how] [about] $you| [and] (what|how) about $you)

    $turnOn = ((turn|put|switch|get) * on)
    $turnOff = ((turn|put|switch|get|shut) * off|mute)

    $gender = (
        (boy|guy|male|man|not (female*|girl|woman)):0|
        (girl|woman|female|gal|not (male|man|guy|boy)):1)



    $doYouLike = (do|are|if) [not] you * [not] (like|love|fond|keen|fan)


    $stupid = (stupid |fool* |silly |(tiny |narrow |small |poor |ill) (brain* |mind*) |narrow-minded |idiot* |cretin* |dummy |dolt|freak|sily|stupid|fool*|*silly|idiot*|moron*|boring|dull|dumb)
    $clever = (smart|clever|intelligent |cute|wise|bright)
    $good = (good|nice|fine|cool|beautiful|wonderful|sweet|well|OK|okay|okey|all right|very well|alright*|allright*|not bad|great|not so bad |terrific |splendid |ex*el*ent|blooming|blossoming|excellent|perfect|the best|super|awesome|happy|glad|brilliant)

    $bad = (bad|crappy|rotten|wretched|shitty|like crap|like shit|lousy|terrible|horrible|horrendous|awful|not good|crappy|shitty|inferior|disgusting|$no * $good)

    $pretty = (beautiful|handsome|pretty|attractive|good-looking|cute|nice|gorgeous)
    $ugly = (ugly|weird|not $pretty)
  
    $nAffront = (wtf|pig|bastard|suck|idiot|fuck*|motherf*|mother-fuck*|shit|ass|ass-*|asshole|asswipe|gay|lesbian|homos*|fag|crap*|prostitute|arschloch|arsch|bitch|damn|kiss my ass)

    $big = (big|large|great|giant|huge|enormous|colossal|defective|terrible)
    $fast = (fast|speedy|agile|blistering|light*speed|quick)

    $perfect = (perfect|ideal|unflawed|faultless|errorless)
    $sad = (sad|unhappy|depressed|sorrowful|downhearted|miserable|joyless|melanchol*|saddened|upset)
    $anyColor = (red|blue|pink|yellow|black|white|teal|magenta|green|violet|orange|cyan)

    $funny = ($regexp<a?(ha)*h?>|lol|funny)

    $thanks = (thanks|thank you|thank u)

    $can = (can|could|will|would|may|(am|are|is) able to)
    $give = (give*|giving|yield*supply*)
    $like = (like*|love|lovi*|enjoy*|fancy*|prefer)
    $hate = (hate|dislike|loathe|detest|abhor|(do not|do n't) like)
    $understand = (understand*|comprehend*|grasp*)
    $want = (want*|wish*|desire*|like*|lust*|need*|demand*)
    $work = (work*|perform*|operate*|operating)
    $think = (think*|thought*|opinion*|suppose*|believ*|belief*)
    $sex = (fuck*|sex|screw|bonk|intercourse|make love|hump|eff|masturb*|lick * balls|have sex|suck * dick)
    $penis = (cock|dick|penis|phallos|prick)



    $iLiveIn = ( my native country is |my native city is |i (live in |am living in |reside in |am from |come from ) )
    $yes = (yes|yep|ye|y|OK|okay|yeah|yeh|i do|yup|ya|yea)
    $no = (no|non|none|n|never|not|nope|nop|[i] (do n't|did n't|do not|did not)|no way|have n't|do n't have|have not|have no|i 've no|ca n't|can not|could n't| it 's not|it is n't|it is not|are n't| ai n't|are not|nono|nah)
    $notTrue = (bullshit|lie|$no so|$no true)
    $noMatter = ($no (matter|important)|it 's ok|no problem|[i] (do n't|do not|could n't|could not) care [less]|i do n't care|(does n't|doesnt|does not) matter|never mind|nevermind)
    $maybe = (maybe|perhaps|could be|why not|could say so|probably|possibl*|may be|doubtful*)
    $sure = (must be|sure)
    $comeOn = ($yes|let us|let's|come on)
    $shutUp = (shut up|stop|cut it|enough|silence)
    $agree = ([$yes] ($comeOn|sure*|of course|certainly|definitely|easily|i (have|do|can|want)|well|exactly|absolutely|[i] (agree|(think|believe) so)|right|[that 's|that is] true|naturally|i (do n't|do not) mind|100 percents|by all means|okey|it is|you are)/yes you are/yes you do|fair enough| i will | (let us| let 's ) *)
    $disagree = [of course] ($no|never|not once|[i] (do not|do n't|would not| would n't) (agree|(think|believe) so)|impossible|false|hard to believe|not true|bullshit|(yo u're|you are) wrong)
    $repetition = (($you| you 've) (asked|said) (that|it)|not again|i [already] (told|answered)|(do not|do n't) (ask * (again|more)|bother)|[i 've|i have] {heard (that|it) already}|We* been * there (before|already)|$you [keep] repeat* *|$you * say* same [thing]|repeat yourself)
    $continue = (go on|continue|tell me more|interesting)
    $getLost = (get lost|go away|leave me alone|get out [of my sight]|clear off|hit the road)
    $changeTheme = ( [let 's|let us] ((talk|speak) about something (different|else)| change [the] (topic|theme))|$me ($no $like|$hate) * (topic|theme)|say something else)
    


    $youAreWelcome = ((you are|you* re|$you) welcome|please [feel] [free] [to]|(not|do not|do n't) (for that|at all|mention it)|[(is|have)] nothing to|no reason to|forget it)
    $dontKnow = ((could not|couldn't|how could (i|you)) (say|tell)|[have] no idea|(do n't|do not) know|not sure|(do not|do n't) (care|think about it)|(could not|could n't) care less|who knows|do n't remember|i forgot|not [have] a clue|[it (is|'s)] hard to (say|tell)|I wish I knew|I ('d|would) (like|love) to know [it|this])

    $whatsUp = (what 's (up|wrong)|what (is|went) (up|wrong|it)|what happened|what 's the problem|what is [(the|your)] problem|(what 's|what is) happening|wassup|wazzup|sup)
    $sorry = (sorry|excuse*|i did n't mean|forgive*)
    $helpYou = (what do $you (need|want)|how can I help $you|what {can i} do for $you|what do $you want)
    $cheerUp = ((brighten|cheer|jolly|spruce|chirk|buoy|perk|look|suck it) up|do n't (be sad|worry|sulk)|smile)
    $comfort = (all will be (fine|well)|(it's|it is) allright|it's fine|sort it out|do n't worry|forget it|never mind)
    
    $compliment = ($you * ($good|$clever|$pretty|funny|sweet|cute|nice)|i $like your (hair|dress|smile|eyes|face))
    $stop = ((stop|don 't|do not|quit|enough) * (whin*|beef*|lament*|crab*|nag*|bleat*snivel|bitch*))
    $notNow = (not now|[(let's|let us|maybe)] [talk] [about it] later|(i 'm|i am|too) busy)

    $mother = (mother|mothers|mother 's|mom|mom 's|moms|mum|mum 's|mums)
    $brother = (brother|brother*|broz|bro|broz 's)
    $father = (father*|dad|dad 's|daddy|daddys|daddy 's)
    $friend = (friend*|buddy|buddies)

    $nation = (american|russian|chinese|british|english|french|spanish|dutch|italian|greek|german|belgian|ukrainian|jew|jewish|canadian|austrian|japanese|polish|polack|bulgarian|hungarian|mexican|brazilian)
    $europeCountry = (France|Italy|Spain|Britain|Belgium|Netherlands|Holland|Chezch republic|Bulgary|Romania|United Kingdom|Scotland|Ireland)
    $americaCountry = (States|USA|Canada|Brazil|Mexico|Argentina|Cuba|Hawaii)
    $country = ($europeCountry|$americaCountry)

    $hashCountries = (albania|algeria|andorra|angola|anguilla|antigua and barbuda|argentina|armenia|aruba|australia|austria|azerbaijan|bahamas|bahrain|bangladesh|barbados|belarus|belgium|benin|bolivia|bosnia herzegovina|botswana|brazil|brunei darussalam|bulgaria|burundi|cambodia|cameroon|canada|chile|china|colombia|congo brazzaville|congo|costa rica|cote d ivoire|croatia|cuba|cyprus|czech republic|denmark|dominica|dominican republic|ecuador|egypt|el salvador|estonia|ethiopia|faroe islands|fiji|finland|france|french polynesia|gabon|georgia|germany|ghana|gibraltar|greece|grenada|guadeloupe|guam|guatemala|guinea|haiti|honduras|hong kong|hungary|iceland|india|indonesia|ireland|israel|italy|jamaica|japan|jordan|kazakhstan|kenya|korea|kuwait|latvia|lebanon|lesotho|lithuania|luxembourg|macau|macedonia fyrom |madagascar|malawi|malaysia|maldives island|mali|malta|mauritius island|mexico|moldova|monaco|morocco|mozambique|myanmar|namibia|nepal|netherlands|netherlands antilles|new caledonia|new zealand|nicaragua|nigeria|norway|oman|panama|papua new guinea|paraguay|peru|philippines|poland|portugal|puerto rico|qatar|romania|russia|rwanda|san marino|saudi arabia|senegal|seychelles islands|singapore|slovakia|slovenia|south africa|spain|sri lanka|st lucia|sudan|sweden|switzerland|syria|taiwan|tanzania|thailand|trinidad and tobago|tunisia|turkey|ukraine|united arab emirates|united kingdom|united states of america|uruguay|uzbekistan|vanuatu|venezuela|yugoslavia|zambia|zimbabwe)

    $problem = (problem*|issue*|difficult*|trouble*)
   
    
    $sweetFood = (sweet*|cand*|sundae*|ice-cream|choco*|cake*|dessert|tiramisu|brownie*|biscuit*|cookie*)
    $meatFood = (meat|kebab*|beef|veal|pork|mutton|cutlet|chicken|turkey|steak|sausage*)
    $carbFood = (pasta|spaghetti|macaroni|potato*|rice|cereal*|porridge|oatmeal*|bread|scone*|bagel*|pancake*)
    $fruitFood = (apple*|pear*|orange*|tangerine*|*melon|fruit*|berrie*|raisin*|grape*|mango*|cherr*|currant*|plum*)
    $fastFood = (pizza|hamburger|hotdog*|crisp*|doner*)
    
    $nFood = ($meatFood|fish|milk|cheese|honey|bread|meat|cola|macaron*|pasta|spaghetti|chips|fri*|sandwich*|hotdog|hot dog|hamburger|cheeseburger|potato*|rice|salad|soup*|dessert|omelette|vegetable*)


    $food = ($sweetFood|$meatFood|$carbFood|$fruitFood|$fastFood)

    $performer = (bieber|madonna|jackson|gaga|beyonce)
    $rarePet = (hamster*|ferret*|fish*|iguana*|rabbit*|snak*e|crocodile*|[guinea] (pig*)|rat*|parrot*|bunny*|bird*|turtle*|mouse*)
    $sport = (cheerlead*|rugby|biatlon*|acrobatic*|baseball|gymnastic*|athletic*|walk*|jog*|football|soccer|swimming|diving|yoga|bike|basketball|volleyball|aerobic*|cross training|weight lifting|power lifting|martial|karate|box|tennis|golf|bodybilding|danc*|ski*|kendo|ushu|hiking|bowling|extrem*|hockey|badminton|kerling|skating|chess|polo|horseback|riding|snowboard*|rock (climb*)|races)
    $spirit = (whiskey|whisky|vodk*|cognac|grappa|shnaps|spirit*|absinth|tequila|scotch|brandy)
    $alcoLight = (coctail|wine|jerez|cherry|vermouth|martini|champaign|sparkling|mojito|margarita|beer)
    $alcohol = ($spirit|$alcoLight|alcohol)

    $tits = (breast*|tits|tit|boot*|boobs|knockers|boobies|bosoms|titties|jugs|hooters|twins|cans|melons|titty)
    $ass = (buttocks|bottom|ass|cheeks|posterior|rear|buns|derriere|butt)
    $pussy = (vagina|pussy|cunt|beaver|cooter|bush|muff|slit|twat|quim|hoo-ha)
    $nudity = ($tits|$ass|$pussy|naked|nude|bare-assed|bare ass|nu)
    $strip = (undress|strip|take * off|get naked)

    $profession = (sales*|cashier|fastfood|clerc*|office|nurse|waiter|waitress|representative|labor*|janitor|cleaner|assistant|secretary|manager|legal|doctor|medical|retail|driv*|delivery|mover|service|kfc|macdonalds|subway|executive|boss|employed|programmist|doctor|mechanic|agronomist|lawyer|attorney|administrator|manager|actor|obstetrician|animator|artist|architect|astronomer|auditor|banker|barman|bartender|barkeeper|worker|librarian|businessman|biologist|attendant|bot*anist|foreman|broker|bookkeeper|accountant|maker-up|veterinarian|vet|esthetician|driver|plumber|serviceman|teacher|educator|physician|guide|gynaecologist|miner|mineworker|loader|workman|yardman|street cleaner|developer|dermatologist|dietitian|
        designer|announcer|dealer|director|conductor|controller|distributor|milker|dairymaid|railwayman|railroadman|railroader|livestock breeder|journalist|tailor's cutter|deputy|(sound|audio) (man|mixer)|image maker|engineer|collector|inspector|executor|[executing] officer|historian|mason|bricklayer|cardiologist|cashier|expert|cynologist|cameraman|merchant|composer|confectioner|
        designer|constructor|consultant|supervisor|copywriter|cosmetologist|astronaut|spaceman|cosmonaut|operator|smith|messenger|courier|pilot|broker|painter|model|marketeer|masseur|foreman|mathematician|merchandiser|metallurgist|policeman|moderator|fitter|rigger|erector|adjuster|assembler|mounter|musician|butcher|tax official|head|chief|superior|neuropathologist|notary public|
        nanny|nursemaid|baby-sitter|oculist|oncologist|specialist|trimmer|hunter|guard|executioner|hangman|hairdresser|barber|singer|*diatrician|baker|interpreter|writer|author|carpenter|cook|politician|aide|tailor|receptionist|dishwasher|postman|(letter|mail) carrier|entrepreneur|business owner|programmer|producer|prosecutor|prosecutor|promoter|superintendent|prostitute|streetwalker|
        psychiatrist|psychiater|psychologist|labourer|odd-job man|editor|repairman|repairer|tutor|realtor|fisherman|gardener|plumber|shoemaker|cobbler|welder|priest|administrator|sculptor|investigator|metalworker|sportsman|athlete|glazier|glass cutter|stylist|joiner|dentist|stomatologist|watchman|agent|fixer|builder|stewardess|airhostess|cabby|tattooist|bodyguard|therapeutist|physician|
        technician|technologist|weaver|turner|representative|traumatologist|trainer|coach|janitor|charwoman|scientist|pharmaceutist|chemist|farmer|physicist|philosopher|financier|photographer|surgeon|choreographer|painter|miner|seamstress|plasterer|ecologist|economist|electrician|endocrinologist|jeweller|jurist)

    $musicGenres = (rock|pop music|rap|jazz|blues|soul|rythm and blues|r n b|punk|industrial|latin|asian|folk|hip hop|country|classic*)

    $favorite = (favorite|favourite)

    $robotName = (Pudding*)