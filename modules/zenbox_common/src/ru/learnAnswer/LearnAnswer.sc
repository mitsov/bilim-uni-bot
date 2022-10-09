require: LearnAnswer.js
require: ../../common/common.sc
require: ../main.js
require: answers.yaml
    var = LearnAnswerCommonAnswers

init:
    $global.themes = $global.themes || [];
    $global.themes.push("LearnAnswer");

    bind("preProcess", function($context) {
        if ($context.parseTree && $context.parseTree.text && isQuestionLearned("answersLearned", $context.parseTree.text)) {
            $context.temp.targetState =  "/LearnAnswer/SayAnswer";
        }        
    });
    $global.$LearnAnswerAnswers = (typeof LearnAnswerCustomAnswers != 'undefined') ? applyCustomAnswers(LearnAnswerCommonAnswers, LearnAnswerCustomAnswers) : LearnAnswerCommonAnswers;

theme: /LearnAnswer

    state: LearnAnswer
        q!: * (когда/если) * ([тебе/тебя] (скаж*/говор*/спр*)/[ты] *слышишь [вопрос]) $Text::Question [ты] (ответь/отвечай/(скажи/говори) [в ответ]) $Text::Answer
        q!: * [ты] (ответь/отвечай/(скажи/говори) [в ответ]) $Text::Answer (когда/если) * ([тебе/тебя] (скаж*/говор*/спр*)/[ты] *слышишь [вопрос]) $Text::Question
        q!: ([тебе/тебя] (скаж*/говор*/спр*)/[ты] *слышишь [вопрос]) $Text::Question [ты] (ответь/отвечай/(скажи/говори) [в ответ]) $Text::Answer
        q!: [ты] (ответь/отвечай/(скажи/говори) [в ответ]) $Text::Answer ([тебе/тебя] (скаж*/говор*/спр*)/[ты] *слышишь [вопрос]) $Text::Question      
        script:
            learnAnswer("answersLearned", $parseTree.Question[0].text, $parseTree.Answer[0].text);
        a: {{ selectRandomArg($LearnAnswerAnswers["LearnAnswer"]["a1"]) }}
        a: {{ selectRandomArg($LearnAnswerAnswers["LearnAnswer"]["a2"]) }}

    state: SayAnswer || noContext = true
        a: {{getAnswerLearned("answersLearned", $parseTree.text)}}

    state: ForgetAnswer
        q!: * (забудь/сотри) * ответ* *
        q: * (забудь/сотри/$agree) * || fromState = ../HowToForget
        script:
            $client.answersLearned = []; 
        a: {{ selectRandomArg($LearnAnswerAnswers["ForgetAnswer"]) }}

    state: HowToForget
        q!: * как * забы* * ответ* *
        q!: * не (отвечай/говори) * [так] больше *
        q: забудь [это]
        q: * ($bad/нельзя) так говорить *    
        a: {{ selectRandomArg($LearnAnswerAnswers["HowToForget"]["a1"]) }}

        state: DontForget
            q: * ($disagree/не (заб*/стир*)) *
            a: {{ selectRandomArg($LearnAnswerAnswers["HowToForget"]["DontForget"]) }}