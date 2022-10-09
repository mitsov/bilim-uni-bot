require: colour-ru.csv
    name = Colours
    var = $Colours

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .colourConverter = function(parseTree) {
            var id = parseTree.Colours[0].value;
            return $Colours[id].value;
        };

patterns:
    $Colour = $entity<Colours> || converter = $converters.colourConverter
