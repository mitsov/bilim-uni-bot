var Radio = (function() {
    // Shuffle in order that the API key workload be distributed more or less equally
    var apiKeys = _.shuffle(RadioParams.apiKey.split(";"));

    function dirbleSearch(title) {
        var token;
        var response;

        for (var i = 0; i < apiKeys.length; i++) {
            token = apiKeys[i];
            response = $http.get("http://api.dirble.com/v2/search/${title}?token=${token}", {
                timeout: 10000,
                query: {
                    title: title,
                    token: token
                }
            });

            if (response.isOk) {
                return response.data;
            }
        }

        log("All Dirble API keys seem to have expired.");
        return [];
    }

    function dirbleSearchById(id) {
        var token;
        var response;

        for (var i = 0; i < apiKeys.length; i++) {
            token = apiKeys[i];
            response = $http.get("http://api.dirble.com/v2/station/${id}?token=${token}", {
                timeout: 10000,
                query: {
                    id: id,
                    token: token
                }
            });

            if (response.isOk) {
                return response.data;
            }
        }

        log("All Dirble API keys seem to have expired.");
        return [];
    }

    return {
        dirbleSearch: dirbleSearch,
        dirbleSearchById: dirbleSearchById
    };
})();

function findNum(stream, radioList) {
    for (var i = 0; i < radioList.length; i++) {
        for (var j = 0; j < radioList[i].streams.length; j++) {
            if (radioList[i].streams[j].stream === stream) {
                return i;
            }
        }
    }

}

function httpCheck(sList) { 
    var stream = $http.check("HEAD", sList);
    return stream;
}

function getRadioStream(radioList, mode, errorMessage) {
    var $session = $jsapi.context().session;
    var i = 0;
    var sList = [];
    var stream = '';
    switch(mode) {
        case "first":
            if (radioList) {
                for (var i = 0; i < radioList.length; i++) {
                    if(radioList[i].streams){
                        for (var j = 0; j < radioList[i].streams.length; j++) {
                            radioList[i].streams[j].stream = radioList[i].streams[j].stream.replace('\r', '').replace('\n', '');
                            sList.push(radioList[i].streams[j].stream);
                        }
                    }
                }
                try {
                    stream = httpCheck(sList);
                    if (stream === null) {
                        stream = '';
                        break;
                    }                    
                } catch (ex) {
                    stream = '';
                    break;
                }
                i = findNum(stream, radioList);
                $session.currentRadio = getRadioName(radioList[i].id, errorMessage);
                if ($session.currentRadio == errorMessage) {
                    $session.currentRadio = radioList[i].name;
                }
                $session.currentRadioOrder = i;
                break;
            }
            break;
        case "next":
            if ($session.currentRadioOrder < radioList.length) {
                if (radioList) {
                    for(var i = parseInt($session.currentRadioOrder) + 1; i < radioList.length; i++) {
                        for (var j = 0; j < radioList[i].streams.length; j++) {
                            sList.push(radioList[i].streams[j].stream);
                        }
                    }
                    try {
                        stream = httpCheck(sList);
                        if(stream === null) {
                            stream = '';
                            break;
                        }                    
                    } catch (ex) {
                        stream = '';
                        break;
                    }
                    i = findNum(stream, radioList);
                    $session.currentRadio = getRadioName(radioList[i].id, errorMessage);
                    if ($session.currentRadio == errorMessage) {
                        $session.currentRadio = radioList[i].name;
                    }
                    $session.currentRadioOrder = i;
                    break;
                }
                break; 
            }
            break;
        case "prev":
            if ($session.currentRadioOrder) {
                if (radioList) {
                    for(var i = parseInt($session.currentRadioOrder) - 1; i >= 0; i--){
                        for (var j = 0; j < radioList[i].streams.length; j++) {
                            sList.push(radioList[i].streams[j].stream);
                        }
                    }
                    try {
                        stream = httpCheck(sList);
                        if (stream === null) {
                            stream = '';
                            break;
                        }                    
                    } catch (ex) {
                        stream = '';
                        break;
                    }
                    i = findNum(stream, radioList);
                    $session.currentRadio = getRadioName(radioList[i].id, errorMessage);
                    if ($session.currentRadio == errorMessage) {
                        $session.currentRadio = radioList[i].name;
                    }
                    $session.currentRadioOrder = i;
                    break;
                }
                break;
            }
            break;
    }
    return stream;
}


function getRadioName(radioId, errorMessage) {
    var radioStation;
    for (var j = 1; j <= Object.keys($RadioStations).length; j++) {
        radioStation = $RadioStations[j].value;
        if (radioStation.id == radioId) {
            return radioStation.title;
        }
    }
    return errorMessage;
}