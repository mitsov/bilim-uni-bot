require: params.yaml
   var = StocksParams

require: companies.csv
  name = Companies
  var = Companies

require: Stocks.js
require: ../../common/common.sc

require: common.js
  module = zb_common

require: text/text.sc
  module = zb_common

require: dateTime/dateTimeEn.sc
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

    $stockPriceOption = (open*:0|(high*/max*):1|low*:2|((close*|closing):3))
    $stockParams = * ($stockSymbol|$stockVolume|$stockMarketCap) *
    $stockParamsContext = * ($stockSymbolContext|$stockVolumeContext|$stockMarketCapContext) *
    $stockSymbol = {(ticker|stock*) * symbol*}
    $stockSymbolContext = symbol*
    $stockVolume = {(stock*|trading*) * volume*}
    $stockVolumeContext = volume*
    $stockMarketCap = {market* * (cap|capitalization)}
    $stockMarketCapContext = cap

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
        q!: * {[$stockPriceOption] * {{[stock*|share*] [of] price*} * ([of] $company)} * [$company::company2] * [$DateTime]} *
        q!: * {[$stockPriceOption] * (stock*|share*) * price* * [$DateTime]} *

        q: * $DateTime * || fromState = ../Price
        q: * [(what|how) about] {$company * [$company::company2] * [$DateTime]} * || fromState = "../Ask company/For Price"
        q: * [(what|how) about] {$company * [$company::company2] * [$DateTime]} * || fromState = "../Price"
        q: * [(what|how) about] {$stockPriceOption * [[stock*] price*|one] * [$company] * [$DateTime]} * || fromState = ../Price
        q: * [(what|how) about] $DateTime * || fromState = ../Price

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

        q: * $DateTime * || fromState = ../Params
        q: * {$company * [$company::company2] * [$DateTime]} * || fromState = ../Params
        q: * {$company * [$company::company2] * [$DateTime]} * || fromState = "../Ask company/For Params"


        script:
            Stocks.saveParams();
        if: $session.stocks.company
            script:
                stockMarketFormParamsAnswer();
        else:
            go!: ../Ask company/For Params