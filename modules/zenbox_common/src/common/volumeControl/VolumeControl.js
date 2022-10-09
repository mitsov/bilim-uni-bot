var volumeControl = VolumeControlParams;

function volumePreProcess($context) {
    var $session = $context.session;
    var $request = $context.request;
    var $client = $context.client;
    $session.currentVolume = ($request.data.currentVolume) ? $request.data.currentVolume : ($client.currentVolume) ?  $client.currentVolume : 40;
}

function volumePostProcess($context) {
    var $response = $context.response;
    var $client = $context.client;    
    $client.currentVolume = $response.currentVolume;
}

bind("preProcess", volumePreProcess, "/Volume");
bind("postProcess", volumePostProcess, "/Volume");