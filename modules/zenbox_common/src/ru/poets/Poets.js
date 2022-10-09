function getPoem(poet, poem) {
    var $session = $jsapi.context().session;
    var $response = $jsapi.context().response;
    if (typeof poet == 'undefined') {
        $session.currentSong = poem;
        $session.currentPoet = getAuthorByPoem(poem);
    } else {
        if (typeof poem != 'undefined') {
            var checkedPoem = checkPoem(poet.secondName, poem);
            if (checkedPoem.author == poet.secondName) {
                $session.currentSong = checkedPoem;
            } else {
                //достать другой стих поэта
                getPoemByAuthor(poet);
                $reactions.answer("Я {знаю/помню} другое произведение.");
            }
        } else {
            //достать любой стих поэта
            getPoemByAuthor(poet);
        }
        $session.currentPoet = poet;
    }
    $response.intent = "poems_on";
    $response.stream = $session.currentSong.stream;
}

function checkPoem(secondName, currentPoem){
    var i, poem;
    for (i = 1; i <= Object.keys($Poems).length; i++) {
        poem = $Poems[i].value;
        if (poem.author == secondName && poem.title == currentPoem.title) {
            return poem;
        }
    }
    return currentPoem;
}

function getAuthorByPoem(poem) {
    var poet;
    for (var i = 1; i <= Object.keys($Poets).length; i++) {
        poet = $Poets[i].value;
        if (poem.author == poet.secondName) {
            return poet;
        }
    }
}

function getPoemByAuthor(poet) {
    var $session = $jsapi.context().session;
    var poem, i;
    var poems = [];
    for (i = 1; i <= Object.keys($Poems).length; i++) {
        poem = $Poems[i].value;
        if (poem.author == poet.secondName) {
            poems.push(poem);
        }
    }
    if (poems.length > 1) {
        if ($session.currentSong) {
            for (i=0; i<poems.length; i++) {
                if ($session.currentSong.title == poems[i].title) {
                    poems.splice(i, 1);
                    break;
                }
            }
        }
        $session.currentSong = poems[randomInteger(0,poems.length-1)];
    } else {
        $session.currentSong = poems[0];
    }
}
