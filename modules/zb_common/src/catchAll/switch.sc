require: answers.yaml
    var = zwitch
#require: ../dict/answers.yaml
#    var = specialSwitch

require: answer.js

theme: /

    state: Switch         || noContext = true

        state: DoYouWannaSwitch?
            script:
                $client.history = $parseTree.text;
                $reactions.answer(getAnswer('DoYouWannaSwitch?'));

            state: Yes
                q: * ($agree|переведи*|переводи*|перевести) *       || onlyThisState = true
                go!: /Switch/TransferToOperator

            state: No
                q: * ($disagree|не (переведи*|переводи*|перевести)) || onlyThisState = true
                go!: /CatchAll/AskAgain?
                
                
        state: NeedOperator
            q!: * $serviceHelperHuman *
            q!: $switchToOperator
            q: * ($two|переключ* [на оператор*]|оператор*|$yes) *  || fromState = ../NotInCompetence
            script:
                $client.history = $parseTree.text;
            go!: ../TransferToOperator

        state: TransferToOperator
            if: catchAll.withOperator
                script:
                    $reactions.answer(getAnswer('TransferToOperator'));
                    $response.replies = $response.replies || [];
                    $response.replies
                     .push({
                        type:"switch",
                        closeChatPhrases: catchAll.closeChatPhrases,
                        firstMessage: $client.history,
                        destination: catchAll.operatorGroup,
                    });
            else:
                script:
                    $reactions.answer(getAnswer('SwitchIsImpossible'));

            state: NoOperatorsOnline
                event: noLivechatOperatorsOnline
                script:
                    $reactions.answer(getAnswer('NoOperatorsOnline'));
                    
                state: GetUserInfo
                    q: * ($mobilePhoneNumber|$email) *
                    q: * {$mobilePhoneNumber * $email} *
                    script:
                        var info = $parseTree.text;
                        $response.replies = $response.replies || [];
                        $response.replies
                         .push({
                            type:"switch",
                            firstMessage: 'Данное сообщение было отправлено в нерабочее время.\n' + $client.history + '\n' + info,
                            ignoreOffline: true,
                            oneTimeMessage: true,
                            destination: catchAll.operatorGroup,
                         });
                         $reactions.answer(getAnswer('GetUserInfo'));

                state: NoInfo
                    q: (нет|не хочу|не буду) *
                    go!: /CatchAll/AskAgain?
                    

        state: LivechatReset
            event: livechatFinished
            if: catchAll.livechatFinished
                go!: {{ catchAll.livechatFinished }}
            else:
                a: Диалог закрыт


