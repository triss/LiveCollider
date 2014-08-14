Pprog : Pstep {
    *new { |progression repeats|
        var durs, list;
    
        repeats = repeats ?? inf;
    
        // use ChordSymbol to decode progression
        progression = progression.progression;
        
        // all values must be paired with a duration
        progression.do { |v|
            v.isArray.not { 
                "All Pprog items must be paired with a duration - e.g. [[0, 3, 7], 4] or Cm_4".warn;
                ^this; 
            }
        };

        // separate the chords from the durations
        #chords, durs = progression.flop;

        // populate the Pstep (super) and return it
        ^super.new(Pseq(chords), Pseq(durs), repeats);
    }
}
