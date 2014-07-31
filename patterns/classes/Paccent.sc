Paccent {
    *new { |pattern amt=0.5 rate=0.25 repeats|
        repeats = repeats ?? inf;

        ^Pstep(Pseq(pattern) * amt + 1 - amt, rate, repeats);
    }
}
