require: ../../common/common.sc
require: ../../common/volumeControl/VolumeControl.js

require: ../main.js

require: params.yaml
   var = VolumeControlParams

require: answers.yaml
    var = VolumeCommonAnswers

init:
    $global.$VolumeAnswers = (typeof VolumeCustomAnswers != 'undefined') ? applyCustomAnswers(VolumeCommonAnswers, VolumeCustomAnswers) : VolumeCommonAnswers;

patterns:
    $Dec = (*тише|*меньше|ниже*|сниз*|понизь*|понизить|*убав*|убери*|приглуши*|уменьши*)
    $Inc = (*громче|*выше|*больше|повысь*|прибавь*|приват|прибавить|увелич*|добав*|голомша|грамши)
    $Min = ([на] (минимум*/минимальн*/маленьк*/тих*)|(cам*|совсем|оч|очень|как можно) (тих*/маленьк*))
    $Max = ([на] (максимум*/максимальн*/больш*/полную)|(cам*|совсем|оч|очень|как можно) (громк*/больш*))
    $Mute = (без звука|беззвучн*)
    $Unmute = (включи*)
    $NotSoMuch = (слишком/не настолько/не так/очень)
    $Why = ([а] (почему/с чего/отчего/чего) так)

    $Sound = (громкост*|звук|говори*)
    
    $Set = (*став*|установи*|сдела*|переключи*|включи*|говори|пой|ключи)
    $Unset = (выруби*|выключи*|отключи*)

theme: /Volume
    
    state: Volume Dec
        q!: * { $Set * [$Sound] * $Dec } *
        q!: {($NotSoMuch|$Why) * громк* * [$Sound] }
        q!: * (чшш*|чщщ*) *
        q!: * { $Dec * $Sound } *
        q: * [еще|ещё] $Dec * || fromState = ../
        q: * (еще|ещё) * || fromState = ./, modal=true
        q!: [$AnyWord] {[чуть] (потише|тише|тиша)} [$AnyWord]
        q!: * (говори/пой) (*тише|тихо|не (слишк*|так|настольк*) громк*) *
        q!: * убери громкость *
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
        if: ($session.currentVolume > volumeControl.step)
            script:
                $response.currentVolume = $session.currentVolume - volumeControl.step;
            a: {{ selectRandomArg($VolumeAnswers["Volume Dec"]["decreased"]) }}
        else:
            script:
                $response.currentVolume = 0;
            a: {{ selectRandomArg($VolumeAnswers["Volume Dec"]["muted"]) }}

    state: Volume Inc
        q!: * { $Set * [$Sound] * ($Inc/громко) } *
        q!: {($NotSoMuch|$Why) * тих* * [$Sound] }
        q!: * { $Sound * $Inc } *
        q: * [еще|ещё] $Inc * || fromState = ../
        q: * (еще|ещё) *  || fromState = ./, modal=true
        q!: {[говори/только/пой] (громче/погромче/громко/голомша/грамши)  }
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
        if: ($session.currentVolume < (volumeControl.levels - volumeControl.step))
            script:
                $response.currentVolume = $session.currentVolume + volumeControl.step;
            a: {{ selectRandomArg($VolumeAnswers["Volume Inc"]["increased"]) }}
        else:
            script:
                $response.currentVolume = volumeControl.levels;
            a: {{ selectRandomArg($VolumeAnswers["Volume Inc"]["maxLvl"]) }}

        state: Thanx, $noContext = true
            q: (хорошо/спасибо) || fromState = .., onlyThisState = true
            a: {{ selectRandomArg($VolumeAnswers["Volume Inc"]["Thanx"]) }}            

    state: Volume Min
        q!: * { [$Set] * $Min * $Sound } *
        q!: * { [$Set] * (максимал*/очень/~самый) * (~тихий/тихо) } *
        q!: * [$Set] * $Dec но не $Unset *
        q: * $Min * || fromState=../
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
            $response.currentVolume = volumeControl.step;
        a: {{ selectRandomArg($VolumeAnswers["Volume Min"]) }}
        
    state: Volume Max
        q!: * { [$Set] * $Max * $Sound } *
        q: * $Max * || fromState=../
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
            $response.currentVolume = volumeControl.levels;
        a: {{ selectRandomArg($VolumeAnswers["Volume Max"]) }}

    state: Volume Mute
        q!: * { $Unset * $Sound }*
        q!: * { [$Set] * $Mute } *
        q: * { $Unset * [$Sound] } *
        q!: * [$Set] (бесшумный/беззвучный) режим
        q!: * [поставь] * беззвучк* *
        q!: * убери звук *
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
            $response.currentVolume = 0;
        a: {{ selectRandomArg($VolumeAnswers["Volume Mute"]) }}
        
    state: Volume Unmute
        q!: * { $Set * $Sound }*
        q!: * { $Unset * $Mute } *
        q!: голос
        q!: * [почему] [ты] (перестал говорить [вслух]/молчишь) *
        q!: (где/куда пропал) [твой] голос
        q!: включай звук
        q!: нет звука
#        q: * {(верни*/вернуть/восстанови*/включ*/пропал) * [$Sound]} * || fromState = "../Volume Mute", onlyThisState = true
        q!: * {(верни*/вернуть/восстанови*/включ*/пропал) * $Sound} *
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
            $response.currentVolume = ($session.currentVolume != 0) ? $session.currentVolume : volumeControl.step;
        a: {{ selectRandomArg($VolumeAnswers["Volume Unmute"]) }}
        