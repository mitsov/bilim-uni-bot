require: patterns.sc
    module = zb_common

require: common.js
    module = zb_common

require: ../../main.js

require: ../../../common/common.js

require: KidsMusic.js

require: KidsMusicGenres-Albums.csv
    name = MusicGenre
    var = $MusicGenre
require: KidsMusic-artists.csv
    name = MusicArtists
    var = $MusicArtists

require: answers.yaml
    var = MusicCommonAnswers

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .MusicArtistsTagConverter = function(parseTree) {
            var id = parseTree.MusicArtists[0].value;
            return $MusicArtists[id].value;
        };
    $global.$converters
        .MusicGenresTagConverter = function(parseTree) {
            var id = parseTree.MusicGenre[0].value;
            return $MusicGenre[id].value;
        };

    $global.$MusicAnswers = (typeof MusicCustomAnswers != 'undefined') ? applyCustomAnswers(MusicCommonAnswers, MusicCustomAnswers) : MusicCommonAnswers;

patterns:
    $musicArtist = $entity<MusicArtists> || converter = $converters.MusicArtistsTagConverter
    $musicGenre = $entity<MusicGenre> || converter = $converters.MusicGenresTagConverter

theme: /KidsMusic

    state: Play
        if: $session.trackList && $request.trackId
            script:
                $session.trackId = $session.trackList.length > $request.trackId ? $request.trackId : 0;

        script:
            $session.trackId = $session.trackId || 0;
            var track = $session.trackList[$session.trackId];
            Deezer.play($session.trackList, $session.trackId, track);
        a: {{ $response.musicList[$session.trackId].speech }}
    
    state: Init
        q!: * { (запусти|поставь|$turnOn|заведи|вруб*|[хочу|давай|будем/дай] (*слушать/послушаем)) [как*] (музык*|песню|трек*|что-нибудь [из/у]|музон/~группа)}  [$Text/$musicGenre/$musicArtist] [$pls] *
        q!: * { (запусти|поставь|$turnOn|заведи|вруб*|(хочу|давай|будем/дай) [*слушать/послушаем]) [как*] (музык*|песню|трек*|что-нибудь [из/у]|музон/~группа)}  [$Text/$musicGenre/$musicArtist] [$pls] *
        q!: * (запусти|поставь|$turnOn|заведи|вруб*|[хочу|давай|будем/дай] (*слушать/послушаем)) [музык*|песню|трек*|что-нибудь [из/у]|музон/~группа] ($Text/$musicGenre/$musicArtist) [$pls] *
        q!: * {(запусти|поставь|$turnOn|заведи|вруб*|хочу|давай|будем) (~слушать/~послушать) [как*] (музык*|песню|трек*|что-нибудь|музон)} [$Text/$musicGenre/$musicArtist] *
        q!: $you (любишь/нравится) ($Text/$musicGenre/$musicArtist) музык* *
        q!: (музык*|~песня|трек*|что-нибудь [из/у]|музон/~группа) $Text *
        q!: * *нибудь [музык*|~песня|трек*] [из/у] ($musicGenre/$musicArtist) *

        q!: * {(~музыка|~песня|трек*|музон|~песенка|музычк*|что-нибудь) * $musicGenre} *
        q!: * {(запусти|поставь|$turnOn|заведи|вруб*|[хочу|давай|будем/дай] (*слушать/послушаем)) * [~музыка|~песня|трек*|музон|~песенка|музычк*] * $musicGenre} *

        q!: * (музык*|~песня|трек*|музон|~песенка|музычк*)
        q!: * {(запусти|поставь|$turnOn|заведи|вруб*|[хочу|давай|будем] *слушать) * [как*] (музык*|песн*|трек*|что-нибудь [из/у]|музон/~группа) * (популярн*/современн*/модн*)} *  
        q!: * {(музык*|песн*|трек*|что-нибудь [из/у]|музон/~группа) * (популярн*/современн*/модн*)} *
        q!: * {(запусти|поставь|$turnOn|заведи|[хочу|давай|будем/дай] (*слушать/послушаем)) [как*] (~музыка|~песня|трек*|музон|~песенка|музычк*|что-нибудь)} *
        q!: * (хочу|давай|будем/дай) * {(*слушать/послушаем) (что-нибудь)} * 

        if: $parseTree.musicGenre
            go!: ../Get trackList/Genre

        if: $parseTree.musicArtist
            go!: ../Get trackList/Artist

        if: $parseTree.Text
            go!: ../Get trackList/Something
        
        go!: ../Get trackList/Default


    state: Get trackList

        state: Default
            script:
                var albumsIds = [7463665, 13285815, 15305545, 15107971, 8188522];
                var id = selectRandomArg(albumsIds);
                var url = Deezer.getUrl('album', id);
                var response = Deezer.request(url).catch(function(err){
                    $reactions.transition("/KidsMusic/Error");
                    });
                $session.trackList = Deezer.getTrackListFromResponse(response.data);

            go!: ../Check
            
        state: Artist
            script:
                var artistId = $parseTree._musicArtist.id;
                var url = Deezer.getUrl('artist', artistId);
                var response = Deezer.request(url).catch(function(err){
                    $reactions.transition("/KidsMusic/Error");
                    });
                $session.trackList = Deezer.getTrackListFromResponse(response.data);
            go!: ../Check
        
        state: Genre
            script:
                var albumId = selectRandomArg($parseTree._musicGenre);
                var url = Deezer.getUrl('album', albumId);
                var response = Deezer.request(url).catch(function(err){
                    $reactions.transition("/KidsMusic/Error");
                    });
                $session.trackList = Deezer.getTrackListFromResponse(response.data);
                $session.trackId = 0;
                
            go!: ../Check

        state: Something
            script:
                var query = $parseTree._Text;
                query = query.split(" ").join("+");
                var url = Deezer.getUrl('search', query);
                var response = Deezer.request(url).catch(function(err){
                    $reactions.transition("/KidsMusic/Error");
                    });
                $session.trackList = Deezer.getTrackListFromResponse(response.data);
                $session.trackList = shuffleArr($session.trackList);
                $session.trackId = 0;
            
            go!: ../Check

        state: Check
            if: $session.trackList && $session.trackList.length > 0
                go!: ../../Play
            else:
                go!: ../../Error

    state: Play next or previous
        q: * (следующ*/дальше/вперед/след/~сменить/next/~другой/~поменять) * || fromState = /KidsMusic
        q: * (верни* [обратно]/$turnOn еще раз/назад/до этого был*/~предыдущий: prev) * || fromState = /KidsMusic
        if: $session.trackList && $session.trackList.length > 0
            script:
                $session.trackId = Deezer.getUpdatedTrackId($session.trackList.length, $session.trackId, $parseTree.value);
            go!: ../Play
        else:
            go!: ../Get trackList/Popular

    state: Who is playing?
        q: * (кто|какая|какой|(че/что) за) * (играет/поет/исполнитель*) *
        q: * как * (называется/зовут/звать) *
        q: * (как/где) найти *
        q: * повтори * (название/имя) *
        q: * {(это|сейчас|только что|играет/называется/название) * [как*] (~музыка|~песня|трек*|музон|~песенка|музычк*)} *
        q: * (как*|что за) (трек|дорожк*|песн*|музык*|мелод*)
        q: * {что * [сейчас] играет} *
        q: * (напомни/скажи) * [как/что/кто] * (называется/название/играет/поет) *
        if: $request.data.musicState && $request.data.musicState === "musicOn"
            if: ($request.trackId > -1 || $session.trackId > -1) && $session.trackList
                a: {{ $session.trackList[$request.trackId  > -1 ? $request.trackId : $session.trackId].speech }}
            else:
                a: {{ selectRandomArg($MusicAnswers['Music']['Who is playing?']['dont know']) }}
        else:
            a: {{ selectRandomArg($MusicAnswers['Music']['Who is playing?']['dont know']) }}


    state: Pause music
        q: * (~пауза/стоп/запаузи/*останови*/тихо/замолкн*/замолчи*) * [трек|дорожк*|песн*|музык*|мелод*] *
        if: $request.data.musicState == "musicOn"
            script:
                $response.action = "musicPause";
        else:
            a: {{ selectRandomArg($MusicAnswers['Music']['Pause music']['music is already off']) }}

            
    state: Resume music
        q: * (~продолжать/~возобновить/~воспроизводить/~играть/~включить/~врубить/включай) * || fromState= "../Pause music"

        if: $request.musicState == "musicPause"
            script:
                $response.action = "musicResume";
        elseif: !$session.trackList
            go!: ../Get trackList/Popular

        go!: /KidsMusic/Play

    state: Turn off music
        q: * ($turnOff|$shutUp|хватит|надоело|достаточно|тихо|заткни*|заткнись|заглохни*|умолкни*|стой|молчи*|*молчать|*молкни|тишин*|*молчи|закончи|заканчивай|не пой|(хватит|прекрати|перестань) [петь]) *
        script:
            $response.action = "musicOff";
        a: {{ selectRandomArg($MusicAnswers['Music']['Turn off music']) }}

    state: Error
        a: Что-то я не могу найти подходящую музыку. Может включим что-нибудь другое?