var currencyConverter = (function(){

    function convertCurrency(base, symbols){
        return $http.get('http://api.fixer.io/latest?base=${base}&symbols=${symbols}', {
            timeout: 7000,
            query: {
                base: base,
                symbols: symbols
            }
        }).then(parseHttpResponse).catch(httpError);
    }

    function roundNumber(number) {
        for (var i = 2; i <= 6; i++) {
            if (number.toFixed(i) != 0) {
                return number.toFixed(i);
            }
        }

        return "меньше одной миллионной";
    }

    function convertionProcess(){
        var $session = $jsapi.context().session;
        var $parseTree = $jsapi.context().parseTree;
        var $temp = $jsapi.context().temp;

        var currencyToConvert = $parseTree.CurrencyToConvert || $parseTree.Country || $parseTree.CurrencyArea;

        if (currencyToConvert && $parseTree.CurrencyBase) {
            if ($parseTree.CurrencyToConvert && $parseTree.CurrencyToConvert[0]) {
                $session.currencyToConvert = $parseTree.CurrencyToConvert[0].value;
            } else if ($parseTree.Country) {
                $session.currencyToConvert = Currencies[$parseTree.Country[0].value.currency].value;
            } else {
                $session.currencyToConvert = $parseTree.CurrencyArea[0].value;
            }
            $session.currencyBase = $parseTree.CurrencyBase[0].value;
        } else if ($temp.convertCurrencyBase) {
            if (!$session.currencyToConvert) {
                $session.currencyToConvert = Currencies['EUR'].value;
            }
            $session.currencyBase = $parseTree.CurrencyBase[0].value;
        } else {
            if (!$session.currencyBase) {
                $session.currencyBase = Currencies['EUR'].value;
            }
            if ($parseTree.CurrencyToConvert) {
                $session.currencyToConvert = $parseTree.CurrencyToConvert[0].value;
            } else if ($parseTree.CurrencyBase){
                $session.currencyBase = $parseTree.CurrencyBase[0].value;
            } else {
                $reactions.answer(currencyConverterPhrases.error);
            }
        }

        $session.currencyAmount = ($parseTree.Number) ? $parseTree.Number[0].value : (currencyToConvert && $parseTree.CurrencyBase) ? 1 : ($session.currencyAmount) ? $session.currencyAmount : 1;
        if ($session.currencyToConvert.title === 'N/A' || $session.currencyBase.title === 'N/A') {
            $reactions.answer(currencyConverterPhrases.unavailableCurrency);
        } else {
            var currencyValue = Currencies[$session.currencyBase.title].value;
            var currencyName = $session.currencyAmount % 10 === 1 && $session.currencyAmount % 100 !== 11 ? currencyValue.name : ($session.currencyAmount % 10 >= 2 && $session.currencyAmount % 10 <= 4 && ($session.currencyAmount % 100 < 10 || $session.currencyAmount % 100 >= 20) ? currencyValue.genName : currencyValue.plName);
            if ($session.currencyToConvert.title === $session.currencyBase.title) {
                $reactions.answer($session.currencyAmount + ' ' + currencyName);
            } else {
                convertCurrency($session.currencyBase.title, $session.currencyToConvert.title).then(function (res) {
                    if (res && res.rates && res.rates[$session.currencyToConvert.title]) {
                        var multipliedRate = roundNumber($session.currencyAmount * res.rates[$session.currencyToConvert.title])
                        $reactions.answer($session.currencyAmount + ' ' + currencyName + currencyConverterPhrases.is + multipliedRate + ' ' + ($session.currencyToConvert.genName || ($session.currencyToConvert.plName && multipliedRate > 1 ? $session.currencyToConvert.plName : false) || $session.currencyToConvert.name) + '.');
                    } else {
                        $reactions.answer(currencyConverterPhrases.error);
                    }
                }).catch(function (err) {
                    $reactions.answer(currencyConverterPhrases.error);
                });
            }
        }
    }


    function exchangeRate(currency) {
        var $temp = $jsapi.context().temp;
        if ($temp.homeCurrency && currency.title === null) {
            $reactions.answer(currencyConverterPhrases.tellHomeCurrency);
        } else {
            convertCurrency(currency.title, 'USD,EUR,GBP').then(function (res) {
                if (res && res.rates) {
                    $temp.usdXRate = res.rates.USD;
                    $temp.eurXRate = res.rates.EUR;
                    $temp.gbpXRate = res.rates.GBP;

                    var invertedUSDXRate = roundNumber(1 / $temp.usdXRate);
                    var invertedEURXRate = roundNumber(1 / $temp.eurXRate);
                    var invertedGBPXRate = roundNumber(1 / $temp.gbpXRate);

                    var answer = ($temp.usdXRate) ? currencyConverterPhrases.usd + invertedUSDXRate + '. ' : '';
                    answer = ($temp.eurXRate) ? answer + currencyConverterPhrases.eur + invertedEURXRate + '. ' : answer + '';
                    answer = ($temp.gbpXRate) ? answer + currencyConverterPhrases.gbp + invertedGBPXRate + '. ' : answer + '';
                    if (answer != '') {
                        $reactions.answer(currencyConverterPhrases.exchangeRate + (currency.genName || currency.name) + ': ' + answer);
                    } else {
                        $reactions.answer(currencyConverterPhrases.error);
                    }
                } else {
                    $reactions.answer(currencyConverterPhrases.error);
                }
            }).catch(function (err) {
                $reactions.answer(currencyConverterPhrases.error);
            });
        }
    }

    function exchangeRateDefiniteCurrency(currency){
        var $client = $jsapi.context().client;

        convertCurrency(currency.title, $client.homeCurrency.title).then(function (res) {
            if (res && res.rates && res.rates[$client.homeCurrency.title]) {
                $reactions.answer('1 ' + currency.name + currencyConverterPhrases.is + roundNumber(res.rates[$client.homeCurrency.title]) + ' ' + ($client.homeCurrency.genName || $client.homeCurrency.name) + '.');
            } else {
                $reactions.answer(currencyConverterPhrases.error);
            }
        }).catch(function (err) {
            $reactions.answer(currencyConverterPhrases.error);
        });
    }

    return {
        convertCurrency: convertCurrency,
        convertionProcess: convertionProcess,
        exchangeRate: exchangeRate,
        exchangeRateDefiniteCurrency:exchangeRateDefiniteCurrency
    };
})();
