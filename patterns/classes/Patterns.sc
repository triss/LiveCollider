Pnfindur {
    *new { |n pattern repeats|
        repeats = repeats ?? inf;
        ^Pn(Pfindur(n, pattern), repeats);
    }
}

Pnfinval {
   *new { |n pattern repeats|
        ^Pn(Pfinval(n, pattern), repeats);
    }
}

Pnfd {
    *new { |n pattern repeats|
        ^Pnfindur(n, pattern, repeats);
    }
}

Pnfv {
    *new { |n pattern repeats|
        ^Pnfinval(n, pattern, repeats);
    }
}

Psampler {
    initClass {
        Event.addEventType(
            \simpler, 
        )
    }

    *new { |bufferPattern...args|
        ^Pbind(\instrument, \tsampler, \buffer, bufferPattern, *args);
    }
}
