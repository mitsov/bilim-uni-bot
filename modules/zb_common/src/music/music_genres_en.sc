require: genres.csv
  name = Gen
  var = $Gen

init:
    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .genresConverter = function(parseTree) {
            var id = parseTree.Gen[0].value;
            return $Gen[id].value.en.name;
        };

patterns:
    $Genres = $entity<Gen> || converter = $converters.genresConverter;
