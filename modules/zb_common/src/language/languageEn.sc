require: language-en.csv
    name = Languages
    var = $Languages

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .languageConverter = function(parseTree) {
            var id = parseTree.Languages[0].value;
            return $Languages[id].value;
        };

patterns:
    $Language = $entity<Languages> || converter = $converters.languageConverter
