require: patterns.sc
    module = zb_common

require: common.js
    module = zb_common

require: ../../main.js

require: ../../../common/common.js

require: Music.js

require: genres-TrackLists.csv
    name = MusicGenre
    var = $MusicGenre
require: music-artists.csv
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

theme: /Music

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
        q!: * { (запусти|поставь|$turnOn|заведи|вруб*|[хочу|давай|будем/дай] (*слушать/послушаем)) [как*] (музык*|песню|трек*|что-нибудь [из/у]|музон/~группа)}  [$Text/$musicGenre/$musicArtist [$Text::probablySongName]] [$pls] *
        q!: * { (запусти|поставь|$turnOn|заведи|вруб*|(хочу|давай|будем/дай) [*слушать/послушаем]) [как*] (музык*|песню|трек*|что-нибудь [из/у]|музон/~группа)}  [$Text/$musicGenre/$musicArtist [$Text::probablySongName]] [$pls] *
        q!: * (запусти|поставь|$turnOn|заведи|вруб*|[хочу|давай|будем/дай] (*слушать/послушаем)) [тогда/лучше] [музык*|песню|трек*|что-нибудь [из/у]|музон/~группа] ($Text/$musicGenre/$musicArtist [$Text::probablySongName]) [$pls] *
        q!: * {(запусти|поставь|$turnOn|заведи|вруб*|хочу|давай|будем) (~слушать/~послушать) [как*] (музык*|песню|трек*|что-нибудь|музон)} [$Text/$musicGenre/$musicArtist [$Text::probablySongName]] *
        q!: $you (любишь/нравится) ($Text/$musicGenre/$musicArtist [$Text::probablySongName]) музык* *
        q!: (музык*|~песня|трек*|что-нибудь [из/у]|музон/~группа) $Text *
        q!: * *нибудь [музык*|~песня|трек*] [из/у] ($musicGenre/$musicArtist [$Text::probablySongName]) *
        q!: * (музык*|~песня|трек*|музон)
        q!: * {(запусти|поставь|$turnOn|заведи|вруб*|[хочу|давай|будем] *слушать) * [как*] (музык*|песн*|трек*|что-нибудь [из/у]|музон/~группа) * (популярн*/современн*/модн*)} *  
        q!: * {(музык*|песн*|трек*|что-нибудь [из/у]|музон/~группа) * (популярн*/современн*/модн*)} *
        q: * (да/давай/включи*/поставь/*игра*) * || fromState = /Music/Error
        if: $parseTree.musicGenre
            go!: ../Get trackList/Genre

        if: $parseTree.musicArtist
            go!: ../Get trackList/Artist

        if: $parseTree.Text
            go!: ../Get trackList/Something
        
        go!: ../Get trackList/Popular


    state: Get trackList

        state: Popular
            script:
                var url = Deezer.getUrl('chart');
                var response = Deezer.request(url).catch(function(err){
                    $reactions.transition("/Music/Error");
                    });
                $session.trackList = Deezer.getTrackListFromResponse(response.data);
                $session.trackList = shuffleArr($session.trackList);

            go!: ../Check
            
        state: Artist
            if: $parseTree.probablySongName
                script:
                    $temp.artistPlusSong = $parseTree._musicArtist.title + "+" + $parseTree._probablySongName;
                go!: ../Something
            script:
                var artistId = $parseTree._musicArtist.id;
                var url = Deezer.getUrl('artist', artistId);
                var response = Deezer.request(url).catch(function(err){
                    $reactions.transition("/Music/Error");
                    });
                $session.trackList = Deezer.getTrackListFromResponse(response.data);
            go!: ../Check
        
        state: Genre
            script:
                var trackListId = selectRandomArg($parseTree._musicGenre);
                var url = Deezer.getUrl('playlist', trackListId);
                var response = Deezer.request(url).catch(function(err){
                    $reactions.transition("/Music/Error");
                    });
                $session.trackList = Deezer.getTrackListFromResponse(response.data);
                $session.trackId = 0;
                
            go!: ../Check

        state: Something
            script:
                var query = $temp.artistPlusSong || $parseTree._Text;
                query = query.split(" ").join("+");
                var url = Deezer.getUrl('search', query);
                var response = Deezer.request(url).catch(function(err){
                    $reactions.transition("/Music/Error");
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
        q: * (следующ*/дальше/далее/вперед/след/~сменить/next) * || fromState = /Music
        q: * (верни* [обратно]/$turnOn еще раз/назад/до этого был*/~предыдущий: prev) * || fromState = /Music

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
        # Временно убираем проверку на данные из реквеста ("musicOn"/"musicOff") тк в zenbox.ai их нам не присылают
        #if: $request.data.musicState && $request.data.musicState === "musicOn"
        if: ($request.trackId > -1 || $session.trackId > -1) && $session.trackList
            a: {{ $session.trackList[$request.trackId  > -1 ? $request.trackId : $session.trackId].speech }}
        else:
            a: {{ selectRandomArg($MusicAnswers['Music']['Who is playing?']['dont know']) }}
        #else:
        #    a: {{ selectRandomArg($MusicAnswers['Music']['Who is playing?']['dont know']) }}

    state: Is it the artist?
        q: * это [сейчас */игра*/поет/поют] ($musicArtist| $Text) *
        q: * сейчас (*/игра*/поет/поют) ($musicArtist| $Text) *
        # Временно убираем проверку на данные из реквеста ("musicOn"/"musicOff") тк в zenbox.ai их нам не присылают
        #if: $request.data.musicState == "musicOn"
        script:
            try {
                var yn;
                if ($parseTree._musicArtist){
                    yn = $session.trackList[$session.trackId].artist.toLowerCase() === $parseTree._musicArtist.title.toLowerCase();
                } else {
                    var query = $parseTree._Text;
                    query = query.split(" ").join("+");
                    var url = Deezer.getUrl('search', query, 1);
                    var response = Deezer.request(url).catch(function(err){
                        $reactions.transition("/Music/Error");
                        });
                    yn = response.data.data[0].artist.name.toLowerCase() === $session.trackList[$session.trackId].artist.toLowerCase();
                }
                yn = (yn ? $MusicAnswers['Music']['Is it the artist?']['yes, it is'] : $MusicAnswers['Music']['Is it the artist?']['no, it is']);
                $reactions.answer(yn + $session.trackList[$session.trackId].artist);
            } catch (e) {
                $reactions.answer(selectRandomArg($MusicAnswers['Music']['Is it the artist?']['dont know']));
            }
        #else:
        #   a: {{ selectRandomArg($MusicAnswers['Music']['Is it the artist?']['music is not playing']) }}
    state: Pause music
        q: * (~пауза/стоп/запаузи/*останови*/тихо/замолкн*/замолчи*) * [трек|дорожк*|песн*|музык*|мелод*] *
        script:
            $response.action = "musicPause";

        # Временно убираем проверку на данные из реквеста ("musicOn"/"musicOff") тк в zenbox.ai их нам не присылают
        #if: $request.data.musicState == "musicOn"
        #    script:
        #        $response.action = "musicPause";
        #else:
        #    a: {{ selectRandomArg($MusicAnswers['Music']['Pause music']['music is already off']) }}

            
    state: Resume music
        q: * (~продолжать/~возобновить/~воспроизводить/~играть/~включить/~врубить/включай) * || fromState= "../Pause music"

        if: $request.musicState == "musicPause"
            script:
                $response.action = "musicResume";
        elseif: !$session.trackList
            go!: ../Get trackList/Popular

        go!: /Music/Play

    state: Turn off music
        q: * ($turnOff|$shutUp|хватит|надоело|достаточно|тихо|заткни*|заткнись|заглохни*|умолкни*|стой|молчи*|*молчать|*молкни|тишин*|*молчи|закончи|заканчивай|не пой|(хватит|прекрати|перестань) [петь]) *
        script:
            $response.action = "musicOff";
        a: {{ selectRandomArg($MusicAnswers['Music']['Turn off music']) }}

    state: Error
        a: {{ selectRandomArg($MusicAnswers['Music']['Error']) }}
