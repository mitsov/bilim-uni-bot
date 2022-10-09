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
    $Dec = (decrease|silently|low|lower|quieter|quietly|quit|quiter|quietly)
    $Inc = (increase|[$Turn] up|loud|louder|loudly|upper)
    $Min = (min|minimum|minim|minimal|[the] quietest|[the] quitest|[the] most silent|[the] lowest)
    $Max = (max|maximum|maxim|maximal|[the] loudest)
    $Mute = ([$Turn] off|mute|[$Turn] down|silent|quiet)
    $Unmute = ([$Turn] on|unmute|[$Turn] up)
    
    $DecStrict = (silent|silently|quiet|quieter|quit|quiter|quietly|hush)
    $IncStrict = ($Turn up|louder|loud|loudly)
    $Sound = (volume|sound|speak*)
    $Turn = (turn/switch/put/get/rotat*/roate/mov*/shift*)
    $Make = (make|switch|turn|put|set)
    #$Unset = (выруби*|выключи*|отключи*)

theme: /Volume
    
    state: Volume Dec
        q!: * $Make * [$Sound] * $Dec *
        q!: * $Dec * $Sound *
        q!: * $Sound * $Dec *
        q!: * {[more] $DecStrict} *
        q: $Dec || fromState = ../
        q: (more) || fromState = ./, modal=true
        q!: [$AnyWord] {[* little [bit]] $DecStrict} [$AnyWord]
        q!: * not (so|that *) loud *
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
        q!: * $Make * [$Sound] * $Inc *
        q!: * $Inc * $Sound *
        q!: * $Sound * $Inc *
        q!: * {[i] (ca n't/do n't/can not/do not/cannot)} hear you
        q!: * can you (speak/talk)
        q!: * where is your voice
        q!: * {[more] $IncStrict} *
        q: $Inc || fromState = ../
        q: (more) || fromState = ./, modal=true
        q!: (very/why so) quiet *
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
        q!: * $Make * $Min * $Sound *
        q!: * $Make * $Sound * $Min *
        q!: * $Min * $Sound *
        q!: * $Sound * $Min *
        q: * $Min * || fromState=../
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
            $response.currentVolume = volumeControl.step;
        a: {{ selectRandomArg($VolumeAnswers["Volume Min"]) }}
        
    state: Volume Max
        q!: * $Make * $Max * $Sound *
        q!: * $Make * $Sound * $Max *
        q!: * $Max * $Sound *
        q!: * $Sound * $Max *
        q: * $Max *|| fromState=../
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
            $response.currentVolume = volumeControl.levels;
        a: {{ selectRandomArg($VolumeAnswers["Volume Max"]) }}

    state: Volume Mute
        q!: * $Mute * [$Sound] *
        q!: * [$Make] * $Sound $Mute *
        q: * $Mute *
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
            $response.currentVolume = 0;
        a: {{ selectRandomArg($VolumeAnswers["Volume Mute"]) }}
        
    state: Volume Unmute
        q!: * $Unmute * $Sound *
        q!: * [$Make] * $Sound $Unmute *
        q: * $Unmute *
        q!: voice
        q!: why [are] [you] (stop* talk* */keep* silen*)
        q!: where * [is] [your] (voice/sound)
        q: * {(get back */return*/turn on) * [$Sound]} * || fromState = "../Volume Mute", onlyThisState = true
        script:
            $temp.rep = true;
            $response.action = "changeVolume";
            $response.currentVolume = ($session.currentVolume != 0) ? $session.currentVolume : volumeControl.step;
        a: {{ selectRandomArg($VolumeAnswers["Volume Unmute"]) }}
        