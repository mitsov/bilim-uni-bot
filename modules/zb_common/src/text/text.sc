init:
    if (!$global.$converters) {
        $global.$converters = {};
    }
    $global.$converters.textConverter = function ($pt) {
        return $pt.text;
    }

patterns:
    $Text = * $nonEmptyGarbage * || converter = $converters.textConverter

#   short form:
#    $Text = * $nonEmptyGarbage * || converter = function ($pt) { return $pt.text; }
