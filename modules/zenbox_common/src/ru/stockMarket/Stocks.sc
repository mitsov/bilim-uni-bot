require: companies.csv
  name = Companies
  var = Companies

require: Stocks.js
require: ../../common/common.sc

require: common.js
  module = zb_common

require: text/text.sc
  module = zb_common

require: dateTime/dateTime.sc
  module = zb_common

require: answers.yaml
    var = StocksCommonAnswers

init:
    $global.$StocksAnswers = (typeof StocksCustomAnswers != 'undefined') ? applyCustomAnswers(StocksCommonAnswers, StocksCustomAnswers) : StocksCommonAnswers;

patterns:
    $company = $entity<Companies> || converter = function(pt) {
        var id = pt.Companies[0].value;
        return Companies[id].value;
        }

    $stockPriceOption = (открыти*:0|(максим*/наивысш*/сам* (выс*/больш*)):1|(миним*/наинизш*/сам* (низк*/маленьк*)):2|(закрыти*:3))
    $stockParams = * ($stockSymbol|$stockVolume|$stockMarketCap) *
    $stockParamsContext = * ($stockSymbolContext|$stockVolumeContext|$stockMarketCapContext) *
    $stockSymbol = {(торгов*) * (код*/символ*)}
    $stockSymbolContext = (код*/символ*)
    $stockVolume = {объем* * торг*}
    $stockVolumeContext = объем*
    $stockMarketCap = {рыночн* * капитал*}
    $stockMarketCapContext = капитал*

theme: /StockMarket

    state: Ask company

        state: For Price
            script:
                $reactions.answer(selectRandomArg($StocksAnswers["Ask company"]["For Price"]));

        state: For Params
            script:
                $reactions.answer(selectRandomArg($StocksAnswers["Ask company"]["For Params"]));

        state: Unknown company
            q: $Text || fromState = ..
            script:
                $reactions.answer(selectRandomArg($StocksAnswers["Ask company"]["Unknown company"]));
            go: ../../../Stocks

    state: Price
        q!: * $company * {[$stockPriceOption] * [акци*] [для/у/за/о] цен* * [$company::company2] * [$DateTime]} *
        q!: * [а] [$company] * {$stockPriceOption * [[акци*] [для/у/за/о] цен*] * [$DateTime]} * 
        q!: * {[$stockPriceOption] * (акци*) * цен* * [$DateTime] * $company} * 
        q!: * [а] $stockPriceOption * {[[акци*] [для/у/за] цен*] * [$company] * [$DateTime]} * 
        q!: * {[$DateTime] * [скажи/расскажи/покажи] [о] цен* [$stockPriceOption] * [акции]} * $company *
        q!: * {[$DateTime] * [скажи/расскажи/покажи] [о] цен* $stockPriceOption * [акции]} * [$company] *
        q!: * {[$DateTime] * (скажи/расскажи/покажи) [о] цен* [$stockPriceOption] * [акции]} * [$company] *
        q!: * [а] [$DateTime]* {$stockPriceOption * [[акци*] [для/у/за/о] цен*] * [$company]} * 
        #q!: * [$stockPriceOption] * {[акци*] [для/у/за] цен* * $company * [$company::company2] * [$DateTime]} *

        q!: * [$stockPriceOption] * [акци*] [для/у/за] цен* * $company * {[$company::company2] * [$DateTime]} *
        q!: * [$stockPriceOption] * [$DateTime] * {[акци*] [для/у/за] цен* * $company * [$company::company2]} *

        q!: * [$DateTime] * [цен*] * [акци*] * $stockPriceOption * [цен*] * [акци*] * [для/у/за/о] * [$company * [$company::company2]] * [$DateTime] * 
        q!: * [$DateTime] * {[$stockPriceOption] * [акци*] [для/у/за/о] цен* * $company * [$company::company2]} *
        q!: * {[$stockPriceOption] * (акци*) * цен* * [$DateTime]} * 


        q: * [а] [$company] * {$stockPriceOption * [[акци*] [для/у/за/о] цен*] * [$DateTime]} * || fromState = ../Price
        q: * $company * {[$stockPriceOption] * [акци*] [для/у/за/о] цен* * [$company::company2] * [$DateTime]} * || fromState = ../Price
        q: * {[$stockPriceOption] * (акци*) * цен* * [$DateTime] * $company} * || fromState = ../Price
        q: * [а] $stockPriceOption * {[[акци*] [для/у/за] цен*] * [$company] * [$DateTime]} * || fromState = ../Price
        q: * {[$DateTime] * [скажи/расскажи/покажи] [о] цен* [$stockPriceOption] * [акции]} * [$company] * || fromState = ../Price
        q: * {[$DateTime] * (скажи/расскажи/покажи) [о] цен* [$stockPriceOption] * [акции]} * [$company] * || fromState = ../Price
        q: * [а] [$DateTime]* {$stockPriceOption * [[акци*] [для/у/за/о] цен*] * [$company]} * || fromState = ../Price 
        #q: * [$stockPriceOption] * {[акци*] [для/у/за] цен* * $company * [$company::company2] * [$DateTime]} * || fromState = ../Price

        q: * [$stockPriceOption] * [акци*] [для/у/за] цен* * $company * {[$company::company2] * [$DateTime]} * || fromState = ../Price
        q: * [$stockPriceOption] * [$DateTime] * {[акци*] [для/у/за] цен* * $company * [$company::company2]} * || fromState = ../Price

        q: * [$DateTime] * [цен*] * [акци*] * $stockPriceOption * [цен*] * [акци*] * [для/у/за/о] * [$company * [$company::company2]] * [$DateTime] * || fromState = ../Price
        q: * [$DateTime] * {[$stockPriceOption] * [акци*] [для/у/за/о] цен* * $company * [$company::company2]} * || fromState = ../Price
        q: * {[$stockPriceOption] * (акци*) * цен* * [$DateTime]} * || fromState = ../Price

        q: * [а] $DateTime * || fromState = ../Price
        q: * [а] {$company * [$company::company2] * [$DateTime]} * ||  fromState = "../Ask company/For Price"
        q: * [а] {$company * [$company::company2] * [$DateTime]} * || fromState = "../Price"

        q: * {[$repeat<$stockParams>] * $company * [$company::company2] $repeat<$stockParams> * [$DateTime]} * || toState = ../Params, fromState = ../Price   
        q: * {[$repeat<$stockParams>] * $company * [$company::company2] $repeat<$stockParamsContext> * [$DateTime]} * || toState = ../Params, fromState = ../Price 
        q: * {[$repeat<$stockParams>] * [$company] $repeat<$stockParamsContext> * [$DateTime]} * || toState = ../Params, fromState = ../Price 
        script:
            Stocks.saveParams();           
        if: $session.stocks.company
            script:
                stockMarketFormPriceAnswer();
        else:
            go!: ../Ask company/For Price

    state: Params
        q!: * {[$repeat<$stockParams>] * $company * [$company::company2] $repeat<$stockParams> * [$DateTime]} *
        q!: * {[$repeat<$stockParams>] * $company * [$company::company2] $repeat<$stockParamsContext> * [$DateTime]} *
        q!: * {[$repeat<$stockParams>] * [$company] $repeat<$stockParams> * [$DateTime]} *
        
        q: * {[$repeat<$stockParams>] * $company * [$company::company2] $repeat<$stockParams> * [$DateTime]} * || fromState = ../Params   
        q: * {[$repeat<$stockParams>] * $company * [$company::company2] $repeat<$stockParamsContext> * [$DateTime]} * || fromState = ../Params 
        q: * {[$repeat<$stockParams>] * [$company] $repeat<$stockParamsContext> * [$DateTime]} * || fromState = ../Params 
        

        q: {* $stockPriceOption * [$company] * [$DateTime] *} || toState = ../Price

        q: * {$company * [$company::company2] * [$DateTime]} * || fromState = ../Params
        q: * {$company * [$company::company2] * [$DateTime]} * || fromState = "../Ask company/For Params"
        q: * $DateTime * || fromState = ../Params


        script:
            Stocks.saveParams();
        if: $session.stocks.company
            script:
                stockMarketFormParamsAnswer();
        else:
            go!: ../Ask company/For Params