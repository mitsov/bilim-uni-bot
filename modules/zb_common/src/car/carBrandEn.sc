require: car-brand-en.csv
    name = CarBrandsEn
    var = $CarBrandsEn

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .carBrandEnConverter = function(parseTree) {
            var id = parseTree.CarBrandsEn[0].value;
            return $CarBrandsEn[id].value;
        };


patterns:
    $CarBrandEn = $entity<CarBrandsEn> || converter = $converters.carBrandEnConverter

