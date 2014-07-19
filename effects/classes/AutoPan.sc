AutoPan { 
    *ar { |input rate=1 phase=0 offset=0 shape=0 amt=1 tempo|
        var lfo;
    
        // create a phased lfo
        lfo = PhaseLfoTS.kr(
            rate, pi / 2 + phase, offset, shape, tempo, amt
        ) * 0.5 + 0.5;

        // subtract it from 1 and apply it to input
        ^(1 - lfo * input);
    }
}
