require: ../../common/common.sc
require: ../../common/radio/Radio.js

require: radio-genres.csv
  name = RadioGenres
  var = $RadioGenres

require: radio-stations.csv
  name = RadioStations
  var = $RadioStations

require: ../main.js

require: patterns.sc
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
        q!: * {$turnOff * (радио*|приемник*|волну|фм|$RadioStation)} *
        q: * ($turnOff|$shutUp|тихо|заткни*|заткнись|заглохни*|умолкни*|молчи*|*молчать|*молкни|тишин*|пауз*|запаузи*|*молчи) *
        script:
            $response.intent = "radio_off";
            $response.action = "musicOff";            
        a: {{ selectRandomArg($RadioAnswers["TurnOffRadio"]) }}

    state: TurnOnRadioWithoutStation
        q!: * {($turnOn|переключ*|поставь|давай|хочу) * ((радио*|приемник*|волну|фм) [$Text]) }
        q!: радио
        q: * (включи*/верни) * || fromState = /Radio/TurnOffRadio
        a: Прости, радио сейчас недоступно. Давай пока поделаем что-нибудь другое.
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
        q!: * {($turnOn|переключ*|поставь|хочу|давай) * [радио*|приемник*|волну|фм] * $RadioStation} *
        q!: {радио* $RadioStation}
        q: * $RadioStation
        q: * {[что-нибудь|какую-нибудь|какой-нибудь|просто] (музон*|музычк*|~музыка)} * || fromState = ../TurnOnRadioWithoutStation, onlyThisState = true
        a: Прости, радио сейчас недоступно. Давай пока поделаем что-нибудь другое.       
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
        q!: * {($turnOn|переключ*|поставь|воспроизведи) * [радио*|приемник*|волну|фм] * $RadioGenre} *
        q!: * {($turnOn|переключ*|поставь/найди/спой/споешь/включай) * [что-нибудь [из]] * $RadioGenre} *
        q!: * {(как*|где) [радио|радиостанц*|фм] * (играют|играет|звучит|есть) $RadioGenre} *
        q!: * (хоч*|давай) (*слушать/услышать) [радио|фм] $RadioGenre
        q!: {радио $RadioGenre}
        q: * $RadioGenre *
        a: Прости, радио сейчас недоступно. Давай пока поделаем что-нибудь другое.
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
        q: * ((следу*|дальше*|вперед):next|(предыду*|назад|верни*):prev) [станц*] *
        q!: * {[$turnOn] * ((след*|дальше*|вперед|друг*):next|(пред*|назад|верни*):prev) * (радио*|станци*|фм)} *
        a: Прости, радио сейчас недоступно. Давай пока поделаем что-нибудь другое.
        #if: $session.prevState.indexOf('TurnOnRadioByStation') === -1 && $session.radioList
            #script:
                #$temp.direction = $parseTree._Root; // Either `next` or `prev`
                #$temp.radioStream = getRadioStream($session.radioList, $temp.direction, selectRandomArg($RadioAnswers["unknown radio"]));
            #if: $temp.radioStream
                #script:
                    #$response.action = "radioOn";
                    #$response.stream = $temp.radioStream;
                #a: {{ selectRandomArg($RadioAnswers["SwitchRadio"]["play next"]) }}
            #else:
                #a: {{ selectRandomArg($RadioAnswers["SwitchRadio"]["no " + $temp.direction]) }}
        #else:
            #a: {{ selectRandomArg($RadioAnswers["SwitchRadio"]["no radio list"]) }} || question = true

    state: RadioName
        q: * {(это|сейчас|только что|играет) * как* (радио*|станци*|фм)} * 
        q: * (как*|что [это] за) (радио*|станци*|фм)
        q: * {что * [сейчас] играет} *
        q: * (какая/что) * {(музыка/песня) * (играет/это)} * 
        if: ($session.currentRadio)
            a: {{ selectRandomArg($RadioAnswers["RadioName"]["know"]) }}
        else:
            a: {{ selectRandomArg($RadioAnswers["RadioName"]["dont know"]) }}
        
    state: CheckRadio
        q: * {(это|сейчас|только что|играло|играет) * [как*] [радио*|станци*|фм] $RadioStation} *
        q!: * {(это|сейчас|только что|играло|играет) * [как*] (радио*|станци*|фм) ($RadioStation/$Text)} *
        q: * {как* радио* * (включ*)}
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
        q!: * (включи/расскажи/хочу [узнать/послушать]) [последн*] новости *
        q!: какие сегодня новости
        q!: новости за сегодня
        a: Прости, радио сейчас недоступно. Давай пока поделаем что-нибудь другое.
        #script:
            #var res = Radio.dirbleSearch("Вести FM");

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
        q: {[$turnOn] (любое/любую/любой/(какое/какую) хочешь) [радио]}
        q!: * $turnOn * (любое/любую/(какое/какую) хочешь) {[станц*] радио*} *
        q: * {[что-нибудь|какую-нибудь|какой-нибудь|просто] (музон*|музычк*|~музыка)} * || fromState = ../TurnOnRadioWithoutStation, onlyThisState = true 
        a: Прости, радио сейчас недоступно. Давай пока поделаем что-нибудь другое.
        #script:
            #var res = Radio.dirbleSearch("Europa Plus");

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