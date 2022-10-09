var Stocks = (function(){

    function getPrice(symbol){
        return $http.query("https://iss.moex.com/iss/history/engines/stock/markets/shares/securities/${symbol}.json?sort_order_desc=desc", {
            method: 'GET',
            timeout: 10000,
            query:{
                symbol: symbol
            }
        }).then(parseHttpResponse).catch(httpError);
    };

    function cantGetData(reason){
        switch(reason) {
            case "date":
                $reactions.answer(selectRandomArg($StocksAnswers["cantGetData"]["date"]));
                break;
            case "future":
                $reactions.answer(selectRandomArg($StocksAnswers["cantGetData"]["future"]));
                break;
            default:
                $reactions.answer(selectRandomArg($StocksAnswers["cantGetData"]["default"]));
                break;
        } 
    }

    function roundPrice(price){
        return Math.round(parseFloat(price)*100)/100 + " " +selectRandomArg($StocksAnswers["roundPrice"]);
    }

    function parsePrice(timeSeries){
        var $session = $jsapi.context().session;
       //если указали в запросе дату, ищем её в данных ответа; если нет - отдаёт последнюю доступную
        if ($session.stocks.dateTime) {
            for (var i = 0; i < timeSeries.length; i++) {
                if (timeSeries[i][1] == $session.stocks.dateTime.check) {
                    var price = [timeSeries[i][6],timeSeries[i][8],timeSeries[i][7],timeSeries[i][11],timeSeries[i][18]];
                    break;         
                }
            }
            //не нашли данные на требуемую дату
            if (typeof price == 'undefined') {
                Stocks.cantGetData("date");
            }
        } else {
            var price = [timeSeries[0][6],timeSeries[0][8],timeSeries[0][7],timeSeries[0][11],timeSeries[0][18]];
        }
        return price;
    }

    function saveParams(){
        var $session = $jsapi.context().session;
        var $parseTree = $jsapi.context().parseTree;

        $session.stocks = $session.stocks || {params : []};
        if ($parseTree.company) {
            if (($parseTree.text.toLowerCase()).indexOf(selectRandomArg($StocksAnswers["saveParams"]["compare"])) == -1) {
                delete $session.stocks.company2;
                $session.stocks.company = $parseTree.company[0].value;
            } else if ($session.stocks.company == undefined) {
                $session.stocks.company = $parseTree.company[0].value;
            } else {
                $session.stocks.company2 = $parseTree.company[0].value;
            }
        };
        if ($parseTree.company2) {
            $session.stocks.company2 = $parseTree.company2[0].value;
        };        
        if ($parseTree.stockPriceOption) {
            $session.stocks.stockPriceOption = $parseTree.stockPriceOption[0].value;
        };
        if ($parseTree.DateTime) {
            if(checkTag("DateWeekday")){
                $session.stocks.dateTime = {
                    "parseTree": $parseTree.DateTime[0],
                    "check" : moment.unix(toPast($parseTree.DateTime[0])).format('YYYY-MM-DD'),
                    "speak" : moment.unix(toPast($parseTree.DateTime[0])).locale('ru').format('Do MMMM')
                };
            } else {
                $session.stocks.dateTime = {
                    "parseTree": $parseTree.DateTime[0],
                    "check" : toMoment($parseTree.DateTime[0]).format('YYYY-MM-DD'),
                    "speak" : toMoment($parseTree.DateTime[0]).locale('ru').format('Do MMMM')
                };
            }   
        }
        if ($parseTree.stockParams || $parseTree.stockParamsContext) {
            $session.stocks.params = [];
            if (checkTag("stockSymbol") || checkTag("stockSymbolContext")) {
                $session.stocks.params.push("symbol");
            }
            if (checkTag("stockVolume") || checkTag("stockVolumeContext")) {
                $session.stocks.params.push("volume");
            }
            if (checkTag("stockMarketCap") || checkTag("stockMarketCapContext")) {
                $session.stocks.params.push("marketCap");
            }                       
        }        
    }

    function isTQBR(value){
        return (value[0] == 'TQBR');
    }

    return {
        getPrice : getPrice,
        cantGetData: cantGetData,
        saveParams: saveParams,
        parsePrice: parsePrice,
        roundPrice: roundPrice,
        isTQBR: isTQBR,
    };

})();

function stockMarketFormParamsAnswer () {
    var $temp = $jsapi.context().temp,
        $session = $jsapi.context().session;

    $temp.answer = "";
    if (_.contains($session.stocks.params, "symbol")) {
        $temp.answer = selectRandomArg($StocksAnswers["stockMarketFormParamsAnswer"]["getSymbol"]["company"]);
        
        if ($session.stocks.company2) {
            $temp.answer += selectRandomArg($StocksAnswers["stockMarketFormParamsAnswer"]["getSymbol"]["company2"]);
        }
        $temp.answer += ".";
        $reactions.answer($temp.answer);
        $temp.answer = "";
    }
    if (_.contains($session.stocks.params, "volume")) {
        Stocks.getPrice($session.stocks.company.symbol).then(function (res){
            if (res && res.history && res.history.data && res.history.data[0]){
                var price = Stocks.parsePrice(res.history.data.filter(Stocks.isTQBR));
                if (typeof price != 'undefined') {
                    $temp.volume = price[4];
                    $temp.answer += selectRandomArg($StocksAnswers["stockMarketFormParamsAnswer"]["getVolume"]["company"]);
                    if ($session.stocks.company2) {
                        Stocks.getPrice($session.stocks.company2.symbol).then(function (res){
                            if (res && res.history && res.history.data && res.history.data[0]){
                                var price2 = Stocks.parsePrice(res.history.data.filter(Stocks.isTQBR));
                                if (typeof price2 != 'undefined') {
                                    $temp.volume2 = price2[4];
                                    $temp.answer += selectRandomArg($StocksAnswers["stockMarketFormParamsAnswer"]["getVolume"]["company2"]);
                                    $reactions.answer($temp.answer);
                                    $temp.answer = "";
                                }
                            } else {
                                Stocks.cantGetData("service");
                            }
                        }).catch(function (err) {
                            Stocks.cantGetData("service");
                        });  
                    } else{
                        $temp.answer += ".";
                        $reactions.answer($temp.answer);
                        $temp.answer = "";
                    }
                }
            } else {
                Stocks.cantGetData("service");
            }
        }).catch(function (err) {
            Stocks.cantGetData("service");
        });   
    }
    if (_.contains($session.stocks.params, "marketCap")) {
        $temp.answer += selectRandomArg($StocksAnswers["stockMarketFormParamsAnswer"]["getMarketCap"]["company"]);
        if ($session.stocks.company2) {
            $temp.answer += selectRandomArg($StocksAnswers["stockMarketFormParamsAnswer"]["getMarketCap"]["company2"]);
        }
        $temp.answer += ".";
        $reactions.answer($temp.answer);
        $temp.answer = "";
    }          
}

function stockMarketFormPriceAnswer() {
    var $temp = $jsapi.context().temp,
        $session = $jsapi.context().session;

    if ($session.stocks.dateTime && $session.stocks.dateTime.check > moment(currentDate()).format('YYYY-MM-DD')){
        Stocks.cantGetData("future");
    } else {
        Stocks.getPrice($session.stocks.company.symbol).then(function (res) {
            if (res && res.history && res.history.data && res.history.data[0]) {
                var price = Stocks.parsePrice(res.history.data.filter(Stocks.isTQBR));
                if (typeof price != 'undefined') {
                    if ($session.stocks.stockPriceOption) {
                        $temp.price = Stocks.roundPrice(price[$session.stocks.stockPriceOption]);
                        switch($session.stocks.stockPriceOption) {
                            case "0":
                                $temp.option = selectRandomArg($StocksAnswers["stockMarketFormPriceAnswer"]["price"]["opening"]);
                                break;
                            case "1":
                                $temp.option = selectRandomArg($StocksAnswers["stockMarketFormPriceAnswer"]["price"]["highest"]);
                                break;
                            case "2":
                                $temp.option = selectRandomArg($StocksAnswers["stockMarketFormPriceAnswer"]["price"]["lowest"]);
                                break;   
                            default:
                                $temp.option = selectRandomArg($StocksAnswers["stockMarketFormPriceAnswer"]["price"]["closing"]);
                                break;
                        }                       
                    } else {
                        $temp.price = Stocks.roundPrice(price[3]);
                        $temp.option = selectRandomArg($StocksAnswers["stockMarketFormPriceAnswer"]["price"]["closing"]);
                    }
                    if ($session.stocks.company2) {
                        Stocks.getPrice($session.stocks.company2.symbol).then(function (res){
                            if (res && res.history && res.history.data && res.history.data[0]){
                                var price2 = Stocks.parsePrice(res.history.data.filter(Stocks.isTQBR));
                                if (typeof price2 != 'undefined') {
                                    if ($session.stocks.stockPriceOption) {
                                        $temp.price2 = Stocks.roundPrice(price2[$session.stocks.stockPriceOption]);
                                    } else {
                                        $temp.price2 = Stocks.roundPrice(price2[3]);
                                    }
                                    $reactions.answer(selectRandomArg($StocksAnswers["stockMarketFormPriceAnswer"]["twoCompanies"]["company"]));
                                    $reactions.answer(selectRandomArg($StocksAnswers["stockMarketFormPriceAnswer"]["twoCompanies"]["company2"]));
                                }
                            } else {
                                Stocks.cantGetData("service");
                            }
                        }).catch(function (err) {
                            Stocks.cantGetData("service");
                        });
                    } else {
                        $reactions.answer(selectRandomArg($StocksAnswers["stockMarketFormPriceAnswer"]["oneCompany"]));
                    }
                }   
            } else {
                Stocks.cantGetData("service");
            }
        }).catch(function (err) {
            Stocks.cantGetData("service");
        });
    }
}