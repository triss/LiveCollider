// steps through progression
Pprogstep {
    *new { |prog durs=4 repeats=1|
        ^Pstep(Pprog(prog), Pseq([durs], inf), repeats)
    }
}

// Like Pseq but converts symbols to chords
Pprog {
    *new { |pattern repeats=inf|
        // wrap arrays etc up in a Pseq
        if(pattern.isSequenceableCollection) {
            pattern = Pseq(pattern, repeats);
        };

        // translate chord names to chord values
        ^pattern.collect { |c| Chord(c) };
    }
}
