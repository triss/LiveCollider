Pprog : Pstep {
    *new { |progression repeats|
        var durs, list;
        repeats = repeats ?? inf;
        progression = progression.progression;
        
        // all values must be paired with a duration
        progression.do { |v|
            v.isArray.not { "All Pprog items must have a duration".warn; ^this; }
        };

        #list, durs = progression.flop;

        ^super.new(Pseq(list), Pseq(durs), repeats);
    }
}
