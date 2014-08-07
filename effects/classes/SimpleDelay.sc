SimpleDelay { 
    *ar { |input delayTime=0.5 feedback=0.5 dryWet=0.5 glide=0.1 tempo mul=1 add=0|
        var wet;
        
        // look up tempo if not provided
        tempo = tempo ?? { TempoSyncUtility.searchForTempo };
        
        // comb filter emulates simple delay here
        wet = CombC.ar(
            input, 4, 
            (delayTime / tempo).lag(glide),
            feedback / tempo
        );

        // crossfade dry/wet
        ^XFade2.ar(input, wet, dryWet);
    }
}
