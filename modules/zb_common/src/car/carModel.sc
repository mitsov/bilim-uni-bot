require: car-model-ru.csv
    name = CarModels
    var = $CarModels

require: car-model-en.csv
    name = CarModelsEn
    var = $CarModelsEn

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .carModelConverter = function(parseTree) {
            var id = parseTree.CarModels
                 ? parseTree.CarModels[0].value
                 : parseTree.CarModelsEn[0].value;
            return $CarModels[id].value;
        };


patterns:
    $CarModel = ($entity<CarModels>/$entity<CarModelsEn>) || converter = $converters.carModelConverter
