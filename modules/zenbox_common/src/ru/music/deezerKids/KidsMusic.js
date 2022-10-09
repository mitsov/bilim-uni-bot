var Deezer = (function() {
    function getUrl(type, query) {
        var urls = {
            'search': 'https://api.deezer.com/search?q=' + query,
            'chart': 'https://api.deezer.com/chart?limit=50',
            'artist': 'https://api.deezer.com/artist/' + query + '/top?limit=50',
            'track': 'https://api.deezer.com/track/' + query,
            'playlist': 'https://api.deezer.com/playlist/' + query,
            'album': 'https://api.deezer.com/album/' + query,
        }
        return urls[type]
    }

    function getTrackListFromResponse(apiResponse) {
        var data = apiResponse.tracks ? apiResponse.tracks.data : apiResponse.data;
        var trackList = data.map(function(el){
            return {
                "stream": el.preview,
                "title": el.title,
                "speech": $MusicAnswers['Music']['Play'] + " " + el.artist.name + ", '" + el.title + "'.",
            };
        });
        return trackList
    }

    function request(url) {
        return $http.query(url, {
            method: 'GET',
            timeout: 10000
        }).then(parseHttpResponse).catch(httpError);
    }

    function play(trackList, trackId, track) {
        var $response = $jsapi.context().response; 
        $response.action = "musicOn";
        $response.trackId = trackId;
        $response.stream = track.stream;
        $response.musicList = trackList;
    }
    function getUpdatedTrackId(trackListLength, currentTrackId, backwardsDirection) {
        if (backwardsDirection) {
            return currentTrackId === 0 ? trackListLength - 1 : currentTrackId - 1;
        }
        return (currentTrackId + 1 === trackListLength) ? 0 : currentTrackId + 1;
    }
    
    return {
        "getUrl": getUrl,
        "getTrackListFromResponse": getTrackListFromResponse,
        "request": request,
        "play": play,
        "getUpdatedTrackId": getUpdatedTrackId,
    }
})();