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
        q!: * [can you] * ($turnOn|let 's (play|listen)|(give|get) me|want|wanna|will|play|recommend) * {[music|track|song|audio*|band] ($musicGenre/$musicArtist)} [$Text]  
        q!: * [can you] * ($turnOn|let 's|(give|get) me|want|wanna|will|play|recommend|listen) [play|listen] * {(music|track|song|audio*|band) * [$musicGenre/$musicArtist]} *
        q!: * {(music|song|track|audio|band) * ($musicGenre/$musicArtist)} *
        q!: * (turn on|play) {[music|song|track|audio|something] * ($Text/$musicGenre/$musicArtist)} *
        q!: * [can you] * ($turnOn|let 's |(give|get) me|want|wanna|will|play|recommend) [play|listen] * {[music|track|song|audio*|something] (popular|modern)} *
        q!: * [can you] * ($turnOn|let 's |(give|get) me|want|wanna|will|play|recommend) [play|listen] * (music|track|song|audio*) *
        q!: * {[music|track|song|audio*|something] (popular|modern)} *

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

            go!: ../Check
            
        state: Artist
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
                var query = $parseTree._Text;
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
        q: * (next|[fast] forward|one up|change) * || fromState = /Music
        q: * ((previous*|back|rewind|again|prev) [music|track|song|audio*]):prev * || fromState = /Music
        q!: * [$turnOn] * (next|[fast] forward|change) * (music|track|song|audio*)
        q!: * ([$turnOn] * (previous|back|rewind|prev) * (music|track|song|audio*)):prev *

        if: $session.trackList && $session.trackList.length > 0
            script:
                $session.trackId = Deezer.getUpdatedTrackId($session.trackList.length, $session.trackId, $parseTree.value);
            go!: ../Play
        else:
            go!: ../Get trackList/Popular

    state: Who is playing?
        q: * (what*|which) * [music|track|song|audio*|is] * play* [now] *
        q: * what* [is] [this] (music|it) *
        if: $request.data.musicState && $request.data.musicState === "musicOn"
            if: ($request.trackId > -1 || $session.trackId > -1) && $session.trackList
                a: {{ $session.trackList[$request.trackId  > -1 ? $request.trackId : $session.trackId].speech }}
            else:
                a: {{ selectRandomArg($MusicAnswers['Music']['Who is playing?']['dont know']) }}
        else:
            a: {{ selectRandomArg($MusicAnswers['Music']['Who is playing?']['dont know']) }}
    state: Is it the artist?
        q: * is it [play*/sing*] ($musicArtist| $Text) *
        if: $request.data.musicState == "musicOn"
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
        else:
            a: {{ selectRandomArg($MusicAnswers['Music']['Is it the artist?']['music is not playing']) }}

    state: Pause music
        q: * (pause|stop) * [music|track|song|audio*] *
        if: $request.data.musicState == "musicOn"
            script:
                $response.action = "musicPause";
        else:
            a: {{ selectRandomArg($MusicAnswers['Music']['Pause music']['music is already off']) }}

            
    state: Resume music
        q: * (continue/play*/resume) * || fromState= "../Pause music"

        if: $request.musicState == "musicPause"
            script:
                $response.action = "musicResume";
        elseif: !$session.trackList
            go!: ../Get trackList/Popular

        go!: /Music/Play

    state: Turn off music
        q!: * {($turnOff|stop) * (music|track|song|audio*)} *
        q: * ($turnOff|$shutUp|hush|mute*|silen*|mute|cut|enough|stop|do n't) [sing*] *
        script:
            $response.action = "musicOff";
        a: {{ selectRandomArg($MusicAnswers['Music']['Turn off music']) }}

    state: Error
        a: {{ selectRandomArg($MusicAnswers['Music']['Error']) }}
