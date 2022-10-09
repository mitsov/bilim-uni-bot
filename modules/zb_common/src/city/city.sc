require: cities-ru.csv
    name = Cities
    var = $Cities

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .cityConverter = function(parseTree) {
            var id = parseTree.Cities[0].value;
            return $Cities[id].value;
        };


patterns:
    $City = $entity<Cities> || converter = $converters.cityConverter
