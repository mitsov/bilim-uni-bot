require: text/text.sc
require: number/number.sc
require: floatNumber/floatNumber.sc
require: dateTime/dateTime.sc
require: address/address.sc
require: phoneNumber/phoneNumber.sc
require: music/music_genres_ru.sc

require: city/cities-small.csv
    name = Cities
    var = $Cities
#TODO: pass dictionary name through $injector
require: city/city.sc

require: language/language.sc

require: colour/colourRu.sc

require: currency/currency.sc

require: car/carBrand.sc
require: car/carBrandEn.sc
require: car/carModel.sc
require: car/carModelEn.sc

require: newSessionOnStart/newSession.sc
    injector = {
        newSessionStartState: "/NewSessionWelcome",
        newSessionTimeout: 10
        }

require: catchAll/catchAll.sc
    injector = {withOperator: true, CheckSameAnswer: true}


#require: offtopic/offtopic.sc 
require: newOfftopic/newOfftopic.sc 

theme: /
    
    state: Agree
        q: $agree
        a: Вы согласились.

    state: PhoneNumber
        q: $mobilePhoneNumber
        a: phoneNumber: {{ $parseTree._mobilePhoneNumber}}

    state: NumberPattern
        q: * $Number *
        a: number: {{$parseTree._Number}}
        
    state: FloatNumberPattern
        q: * $FloatNumber *
        a: floatNumber: {{$parseTree._FloatNumber}}

    state: DateTimePattern
        q: * $DateTime *
        a: dateTime: {{ toPrettyString( $parseTree._DateTime ) }}

    state: CityPattern
        q: * $City *
        if: $parseTree._City
            a: city: {{$parseTree._City.name}}
        else:
            a: 111

    state: EmailPattern
        q: * $email *
        a: email: {{$parseTree._email}}

    state: LanguagePattern
        q: * $Language *
        a: language: {{toPrettyString($parseTree._Language)}}

    state: ColourPattern
        q: $Colour
        a: colour: {{ toPrettyString($parseTree._Colour) }}

    state: CurrencyPattern 
        q: * ($Currency) *
        a: currency: {{ toPrettyString($parseTree._Currency.name) }}
        
    state: CarBrandPattern 
        q: * $CarBrand *
        a: марка машины: {{ toPrettyString($parseTree._CarBrand.name) }}

    state: CarModelPattern 
        q: * $CarModel *
        a: модель машины: {{ toPrettyString($parseTree._CarModel.name) }}

    state: CarBrandEnPattern 
        q: * $CarBrandEn *
        a: car brand: {{ toPrettyString($parseTree._CarBrandEn.name) }}

    state: CarModelEnPattern 
        q: * $CarModelEn *
        a: car model: {{ toPrettyString($parseTree._CarModelEn.name) }}

    state: AmbiguousCurrencyPattern 
        q: * $ambiguousCurrency *
        a: ambiguousCurrency: {{ toPrettyString($parseTree._ambiguousCurrency.name) }}

    state: AgreePattern
        q: тест паттерна agree
        a: Можно отправить данные в техподдержку. Давай так и сделаем?

        state: Agree
            q: $agree
            a: Отлично! Я все отправил.

        state: CatchAll
            q: *
            a: Тогда лучше вернемся в Главное меню.

    state: NewSessionWelcome
        q: тест на создание новой сессии || toState = /Start
        a: welcome

        state:
            q: context pattern
            a: context
            
    state: TestNumberToString
        q: test numberToString
        a: Введите число

        state: GetNumeral
            q: $Number
            script:
                try {
                    $reactions.answer(numberToString($parseTree._Number));
                }
                catch(e) {
                    $reactions.answer(e.message);
                }

                try {
                    $reactions.answer(numberToString($parseTree._Number, "ordinal"));
                }
                catch(e) {
                    $reactions.answer(e.message);
                }

    state: GenresPattern
        q: $Genres
        a: Genre: {{$parseTree._Genres}}