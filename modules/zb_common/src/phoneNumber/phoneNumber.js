$global.$converters = $global.$converters || {};
var cnv = $global.$converters;

cnv.mobilePhoneNumberInWordsConverter = function(pt) {
    var collectedValues = $converters.collectParseTreeValues(pt);
    var roughNumber = collectedValues.join();
    var phoneNumber = "";
    for (var i = 0; i < roughNumber.length; ++i) {
        if (roughNumber[i] != ",") {
            phoneNumber += roughNumber[i];
        }
    }

    if (phoneNumber.length === 10) {
        phoneNumber = "+7" + phoneNumber;
    } else if (phoneNumber.length === 11 && phoneNumber.charAt(0) === "7") {
        phoneNumber = "+" + phoneNumber;
    } else if (phoneNumber.length === 11) {
        phoneNumber = "+7" + phoneNumber.substr(1);
    }

    return phoneNumber 
}
cnv.mobilePhoneNumberInDigitsConverter = function(pt) {
    var roughNumber = pt.text;
    var phoneNumber = "";
    for (var i = 0; i < roughNumber.length; ++i) {
        if (!isNaN(parseInt(roughNumber.charAt(i)))) {
            phoneNumber += roughNumber.charAt(i);
        }
    }
    if (phoneNumber.length === 10) {
        phoneNumber = "+7" + phoneNumber;
    } else if (phoneNumber.length === 11 && phoneNumber.charAt(0) === "7") {
        phoneNumber = "+" + phoneNumber;
    } else if (phoneNumber.length === 11) {
        phoneNumber = "+7" + phoneNumber.substr(1);
    }
    return phoneNumber
}
cnv.PhoneNumberSameDigits = function(pt) {
    var times = parseInt(pt.value);
    var number = pt.oneToNineGent ? pt.oneToNineGent[0].value : 0;
    var output = "";

    for (var i = 0; i < times; ++i) {
        output += number;
    }
    return output
}