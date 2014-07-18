SimpleDelay { 
    *ar { |input delayTime=0.5 feedback=0.5 glide=0.1 tempo mul=1 add=0|
        tempo = tempo ?? { TempoSyncUtility.searchForTempo };

        ^CombC.ar(input, 4, (delayTime / tempo).lag(glide), feedback / tempo);
    }
}
