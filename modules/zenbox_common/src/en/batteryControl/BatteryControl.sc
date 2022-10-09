require: ../main.js
require: ../../common/common.sc

require: params.yaml
   var = BatteryControlParams

require: answers.yaml
    var = BatteryControlCommonAnswers

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }
    $global.$BatteryControlAnswers = (typeof BatteryControlCustomAnswers != 'undefined') ? applyCustomAnswers(BatteryControlCommonAnswers, BatteryControlCustomAnswers) : BatteryControlCommonAnswers;

patterns:
    $battery = (battery|accumulator|power|energy)

theme: /Battery control
    state: Show battery
        q!: * (how much) * $battery * (left|{is it}|have|has) *
        q!: * (show|tell) * {$battery * [status|level]} * 
        q!: * (how much|how well) * (charge*|energy|power) * 
        q!: how much battery is left
        q!: * (enough|any) * $battery * [left] *
        q!: * $battery * (status|level) *
        q!: * {$battery * (dead|low|flat|*charged|run* out)} *
        script: 
            var max = BatteryControlParams["maxValue"];
            $response.action = "showBatteryStatus";
            $temp.maxValue = (max == 100) ? "%" : " " + $BatteryControlAnswers["outOf"] + " " + max;
            if ($request.data.batteryStatus > max || $request.data.batteryStatus === undefined) {
                $reactions.answer($BatteryControlAnswers["cantGetData"]);
            } else {
                $reactions.answer($BatteryControlAnswers["showBattareyStatus"]);
            }
