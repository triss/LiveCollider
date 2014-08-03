Pprogstep {
    *new { |prog durs repeats|
        repeats = repeats ?? inf; 
        ^Pstep(Pseq(Chord.progression(prog)), Pseq([durs]), repeats)
    }
}
