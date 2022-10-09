require: car-model-en.csv
    name = CarModelsEn
    var = $CarModelsEn

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .carModelEnConverter = function(parseTree) {
            var id = parseTree.CarModelsEn[0].value;
            return $CarModelsEn[id].value;
        };


patterns:
    $CarModelEn = $entity<CarModelsEn> || converter = $converters.carModelEnConverter

