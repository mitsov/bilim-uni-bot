require: car-brand-ru.csv
    name = CarBrands
    var = $CarBrands

require: car-brand-en.csv
    name = CarBrandsEn
    var = $CarBrandsEn
    
init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .carBrandConverter = function(parseTree) {
            var id = parseTree.CarBrands
                ? parseTree.CarBrands[0].value
                : parseTree.CarBrandsEn[0].value;
            return $CarBrands[id].value;
        };


patterns:
    $CarBrand = ($entity<CarBrands>/$entity<CarBrandsEn>) || converter = $converters.carBrandConverter
