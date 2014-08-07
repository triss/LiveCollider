Pnfindur {
    *new { |n pattern repeats|
        repeats = repeats ?? inf;
        ^Pn(Pfindur(n, pattern), repeats);
    }
}

Pnfinval {
    *new { |n pattern repeats|
        repeats = repeats ?? inf;
        ^Pn(Pfinval(n, pattern), repeats);
    }
}

// Pnfd is just shorthand for Pnfindur
Pnfd {
    *new { |n pattern repeats|
        ^Pnfindur(n, pattern, repeats);
    }
}

// Pnfv is just shorthand for Pnfinval
Pnfv {
    *new { |n pattern repeats|
        ^Pnfinval(n, pattern, repeats);
    }
}
