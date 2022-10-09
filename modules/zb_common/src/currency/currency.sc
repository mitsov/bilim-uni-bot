require: currency.csv
    name = Currencies
    var = $Currencies

require: ambiguousCurrency.csv
    name = ambiguousCurrencies
    var = $ambiguousCurrencies


init:

    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .currencyConverter = function(parseTree) {
            var id = parseTree.Currencies[0].value;
            return $Currencies[id].value;
        };

patterns:
    $Currency = $entity<Currencies> || converter = $converters.currencyConverter



init:

    if (!$global.$converters) {
        $global.$converters = {};
    }

    $global.$converters
        .ambiguousCurrencyConverter = function(parseTree) {
            var id = parseTree.ambiguousCurrencies[0].value;
            return $ambiguousCurrencies[id].value;
        };

patterns:
    $ambiguousCurrency = $entity<ambiguousCurrencies> || converter = $converters.ambiguousCurrencyConverter   
    
