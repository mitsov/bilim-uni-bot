require: ../../common/common.sc
require: ../../common/radio/Radio.js

require: radio-genres.csv
  name = RadioGenres
  var = $RadioGenres

require: radio-stations.csv
  name = RadioStations
  var = $RadioStations

require: ../main.js

require: patternsEn.sc
  module = zb_common
require: text/text.sc
  module = zb_common
require: common.js
    module = zb_common

require: params.yaml
   var = RadioParams

require: answers.yaml
    var = RadioCommonAnswers

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .RadioGenreTagConverter = function(parseTree) {
            var id = parseTree.RadioGenres[0].value;
            return $RadioGenres[id].value;
        };

    $global.$converters
        .RadioStationTagConverter = function(parseTree) {
            var id = parseTree.RadioStations[0].value;
            return $RadioStations[id].value;
        };

    $global.$RadioAnswers = (typeof RadioCustomAnswers != 'undefined') ? applyCustomAnswers(RadioCommonAnswers, RadioCustomAnswers) : RadioCommonAnswers;

patterns:
    $RadioGenre = $entity<RadioGenres> || converter = $converters.RadioGenreTagConverter
    $RadioStation = $entity<RadioStations> || converter = $converters.RadioStationTagConverter


theme: /Radio

    state: TurnOffRadio
        q!: * {($turnOff|stop) * (radio*|wireless|reciever|radio wave|$RadioStation)} *
        q: * ($turnOff|$shutUp|silen*|quiet|hush|mute*|cut|enough|stop|pause) *
        script:
            $response.intent = "radio_off";
        a: {{ selectRandomArg($RadioAnswers["TurnOffRadio"]) }}

    state: TurnOnRadioWithoutStation
        q!: * {($turnOn|switch|set|let*|lets|want|wanna) * ((radio* [wave]|wireless|receiver|fm) [$Text]) }
        q!: * {radio [please]} *
        q!: * {turn * [the] radio * on} *
        q: * (turn on/return) * || fromState = /Radio/TurnOffRadio   
        a: Sorry, radio is currently unavailable.
        #if: ($parseTree.Text)
            #a: {{ selectRandomArg($RadioAnswers["TurnOnRadioWithoutStation"]["unknown station"]) }}
        #else:
            #if: $session.currentRadio
                #go!: /Radio/TurnOnRadioByStation
            #else:
                #a: {{ selectRandomArg($RadioAnswers["TurnOnRadioWithoutStation"]["require station name"]) }}

        #state: TurnOnUnknownRadio
            #q: $Text || fromState = .., onlyThisState = true
            #a: {{ selectRandomArg($RadioAnswers["TurnOnRadioWithoutStation"]["TurnOnUnknownRadio"]) }}
            #go: /Radio

    state: TurnOnRadioByStation
        q!: * {($turnOn|switch|change [it]|set|want|let*|lets|wanna) * [radio*|wireless|receiver|radio wave] * $RadioStation} *
        q!: {radio * $RadioStation}
        q: * $RadioStation
        q: * [some|any|just] music * || fromState = ../TurnOnRadioWithoutStation, onlyThisState = true  
        a: Sorry, radio is currently unavailable.      
        #script:
            #if ($parseTree._RadioStation) {
                #$temp.radioStationQuery = $parseTree._RadioStation.title;
            #} else if ($session.currentRadio) {
                #$temp.radioStationQuery = $session.currentRadio;
            #}

            #if ($temp.radioStationQuery) {
                #var res = Radio.dirbleSearch($temp.radioStationQuery);

                #if (res !== []) {
                    #$session.radioList = res;
                    #$temp.stream = getRadioStream($session.radioList, "first", selectRandomArg($RadioAnswers["unknown radio"]));
                    #if ($temp.stream === "") {
                        #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByStation"]["station is out of reach"])); 
                    #} else {
                        #$response.stream = $temp.stream;
                        #$response.action = "radioOn";
                        #if ($parseTree._RadioStation) {
                            #if ($parseTree._RadioStation.title.toLowerCase() === $session.currentRadio.toLowerCase()) {
                                #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByStation"]["user requires previous station"]));
                            #} else {
                                #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByStation"]["user requires new station"]));
                            #}
                        #} else {
                            #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByStation"]["user requires previous station"]));
                        #}
                    #}
                #} else {
                    #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByStation"]["station is out of reach"])); 
                #}
            #} else {
                #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByStation"]["scenario error"])); 
            #}

    state: TurnOnRadioByGenre
        q!: * {($turnOn|switch*|set|find) * [radio* [station]|wireless|receiver|radio wave|fm] * $RadioGenre} *
        q!: * {(what*|which|where) [radio*|station*] * (sound*|play*|with) $RadioGenre} *
        q!: * (want*|wanna|lets|let's|let is) (listen to|hear) * [radio*|station] $RadioGenre *
        q!: {radio * $RadioGenre}
        q: * $RadioGenre *
        a: Sorry, radio is currently unavailable.
        #script:
            #var res = Radio.dirbleSearch($parseTree._RadioGenre.title);

            #if (res !== []) {
                #$session.radioList = res;
                #$temp.stream = getRadioStream($session.radioList, "first", selectRandomArg($RadioAnswers["unknown radio"]));
                #if ($temp.stream !== "") {
                    #$response.action = "radioOn";
                    #$response.stream = $temp.stream;
                    #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByGenre"]["succeed"]));
                #} else {
                    #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByGenre"]["failed"]));
                #}
            #} else {
                #$reactions.answer(selectRandomArg($RadioAnswers["TurnOnRadioByGenre"]["failed"]));
            #}

    state: SwitchRadio
        q: * ((next|change|switch|another|something else):1|(prev*|back|return*):2) [radio*|station] *
        q!: * {[$turnOn] * ((next|change|switch):1|(prev*|back|return*):2) * (radio*|station)} *
        a: Sorry, radio is currently unavailable.
        #if: ($session.radioList)
            #script:
                #$temp.radioStream = getRadioStream($session.radioList, $parseTree._Root == 1 ? "next" : "prev", selectRandomArg($RadioAnswers["unknown radio"]));
            #if: ($temp.radioStream != '')
                #script:
                    #$response.action = "radioOn";         
                    #$response.stream = $temp.radioStream;
                #a: {{ selectRandomArg($RadioAnswers["SwitchRadio"]["play next"]) }}
            #else:
                #if: $parseTree._Root == 1
                    #a: {{ selectRandomArg($RadioAnswers["SwitchRadio"]["no next"]) }}
                #else:
                    #a: {{ selectRandomArg($RadioAnswers["SwitchRadio"]["no prev"]) }}
        #else:
            #a: {{ selectRandomArg($RadioAnswers["SwitchRadio"]["no radio list"]) }} || question = true

    state: RadioName
        q: * {(now|just [now]|sound*|play*) * (what*|which) [is] (radio*|station)} * 
        q: * (what*|what [the]|which) * [the] (radio*|station) [is it]
        q: * {what is * [now] (sound*|play*)} *
        if: ($session.currentRadio)
            a: {{ selectRandomArg($RadioAnswers["RadioName"]["know"]) }}
        else:
            a: {{ selectRandomArg($RadioAnswers["RadioName"]["dont know"]) }}

    state: CheckRadio
        q: * {({it is}|it's|its|now|just [now]|sound*|play*) [current*/at the moment/now] * [what*] [radio*|station] $RadioStation} *
        q!: * {({it is}|it's|its|now|just [now]|sound*|playing) [current*/at the moment/now] * [what*] (radio*|station) ($RadioStation/$Text)} *
        q: * {(what/which)* radio* * [current*/at the moment/now] * [is] * (up/on)}
        script:
            if ($session.currentRadio) {
                if ($session.currentRadio == selectRandomArg($RadioAnswers["unknown radio"])) {
                    $reactions.answer(selectRandomArg($RadioAnswers["CheckRadio"]["dont know"]));
                } else if ($parseTree.Text) {
                    $reactions.answer(selectRandomArg($RadioAnswers["CheckRadio"]["the radio is"]));
                } else if ($parseTree._RadioStation.title == $session.currentRadio) {
                    $reactions.answer(selectRandomArg($RadioAnswers["CheckRadio"]["user named station correct"]));
                } else {

                    $reactions.answer(selectRandomArg($RadioAnswers["CheckRadio"]["user named station wrong"]));
                }

            } else {
                $reactions.answer(selectRandomArg($RadioAnswers["CheckRadio"]["dont know"]));
            }


    state: ListentoNews
        q!: * {($turnOn/switch/listen) * news} *
        q!: what 's on tap * today
        q!: news today
        a: Sorry, radio is currently unavailable.
        #script:
            #var res = Radio.dirbleSearch("news");

            #if (res !== []) {
                #$session.radioList = res;
                #$temp.stream = getRadioStream(res, "first", selectRandomArg($RadioAnswers["unknown radio"]));
                #if ($temp.stream === "") {
                    #$reactions.answer(selectRandomArg($RadioAnswers["ListentoNews"]["failed"]));
                #} else {
                    #$response.stream = $temp.stream;
                    #$response.action = "radioOn";
                    #$reactions.answer(selectRandomArg($RadioAnswers["ListentoNews"]["succeed"]));
                #}
            #} else {
                #$reactions.answer(selectRandomArg($RadioAnswers["ListentoNews"]["failed"]));
            #}

    state: ListentoAnyRadio
        q: {[$turnOn]* (any/whatever you want) * [radio]}
        q!: * $turnOn * (any/whatever you want) {[station] radio} *
        q: * {[some [thing]|just|any] music} * || fromState = ../TurnOnRadioWithoutStation, onlyThisState = true
        a: Sorry, radio is currently unavailable.  
        #script:
            #var res = Radio.dirbleSearch("Glee Radio");

            #if (res !== []) {
                #$session.radioList = res;
                #$temp.stream = getRadioStream($session.radioList, "first", selectRandomArg($RadioAnswers["unknown radio"]));
                #if ($temp.stream === "") {
                    #$reactions.answer(selectRandomArg($RadioAnswers["ListentoAnyRadio"]["failed"]));
                #} else {
                    #$response.stream = $temp.stream;
                    #$response.action = "radioOn";
                    #$reactions.answer(selectRandomArg($RadioAnswers["ListentoAnyRadio"]["succeed"]));
                #}
            #} else {
                #$reactions.answer(selectRandomArg($RadioAnswers["ListentoAnyRadio"]["failed"])); 
            #}
