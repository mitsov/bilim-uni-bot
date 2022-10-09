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
    $battery = (~батарея/~батарейка)

theme: /Battery control
    state: Show battery
        q!: * {(покаж*/*скажи/какой/какая/уровень/запас) * (заряд* [$battery]/$battery/энергии)} *
        q!: * {(сколько/достаточно/{(есть/много) еще}) * (заряд* [$battery]/$battery/электричеств*/у тебя сил) [осталось/еще]} *
        q!: * (насколько/как) * заряжен *
        q!: (ты/твоя батарея) (разряжен*/заряжен*)
        q!: * {как твоя $battery}
        q!: {(заряд/зарядка) [энергии/батареи]}
        q!: (на зарядку становись/заряжайся)
        q!: батаре*
        q!: {  (батаре*/разрядился)}
        script: 
            var max = BatteryControlParams["maxValue"];
            $response.action = "showBatteryStatus";
            $temp.maxValue = (max == 100) ? "%" : " " + $BatteryControlAnswers["outOf"] + " " + max;
            if ($request.data.batteryStatus > max || $request.data.batteryStatus === undefined) {
                $reactions.answer($BatteryControlAnswers["cantGetData"]);
            } else {
                $reactions.answer($BatteryControlAnswers["showBattareyStatus"]);
            }
