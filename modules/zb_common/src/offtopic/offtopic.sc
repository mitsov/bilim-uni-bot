require: greetings.sc
require: fallback.sc
require: other.sc
require: offtopic.yaml
    var = commonOfftopic

#require: /dict/offtopic.yaml
#    var = specialOfftopic

require: offtopic.js

init:
    $global.OfftopicAnswers = (typeof specialOfftopic != 'undefined') ? applyCustomAnswers(commonOfftopic, specialOfftopic) : commonOfftopic;