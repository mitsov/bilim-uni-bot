require: names-En.csv
  name = NamesEn
  var = $NamesEn

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }
    $global.$converters
        .NamesEnConverter = function(parseTree) {
            var id = parseTree.NamesEn[0].value;
            return $NamesEn[id].value;
        };

patterns:
    $Name = $entity<NamesEn> || converter = $converters.NamesEnConverter