AutoFilter {
    *ar { 
        arg input,
        cutoff = 16000, rez = 0.5, type = 0,
        lfoAmt = 0, lfoRate = 1, lfoPhase = 0, lfoOffset = 0, lfoShape = 0, 
        lfoStep = 0, lfoStepRate = 1,
        ampFollowAmt = 0, ampFollowAttack = 0.01, ampFollowDecay = 0.01,
        sidechain, tempo, 
        mul = 1, add = 0;

        var lfo, ampFollower, ampTrig, sig;

        // create a nice phasing tempo-sync'd lfo
        lfo = PhaseLfoTS.kr(
            lfoRate, lfoPhase, lfoOffset, lfoShape, tempo, lfoAmt
        );

        // if lfo step is engaged step the lfo
        lfo = if(lfoStep == 1) {
            Latch.kr(lfo, ImpulseTS.kr(lfoStepRate));
        } {
            lfo;
        };

        // if sidechain not provided use input
        ampTrig = sidechain ?? input;

        // track the amplitude and smooth by amp follow attack/decay
        ampFollower = Amplitude.kr(
            ampTrig, ampFollowAttack, ampFollowDecay, ampFollowAmt
        );

        // scale the cutoff
        cutoff = \safeFilter.asSpec.map(
            // add the lfo and ampFollower
            ampFollower + lfo + \safeFilter.asSpec.unmap(cutoff) 
        );

        // select between filter types
        sig = SelectX.ar(type, [
            RLPF.ar(input, cutoff, rez),
            BPF.ar(input, cutoff, rez),
            RHPF.ar(input, cutoff, rez)
        ]);

        ^sig.madd(mul, add); 
    }
}

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

//BeatRepeat { }
//
//Chorus { }
//
//Compressor {  }
//
//Corpus {  }
//
//DynamicTube {}
//
//EQ8 {}
//
//EQ3 {}
//
//Erosion {}
//
//FilterDelay {}
//
//Flanger {}
//
//FrequencyShifter {}
//
//GateFX {}
//
//GrainDelay {}
//
//Overdrive {}
//
//Phaser {}
//
//PingPongDelay {}
//
//Saturator {}

SimpleDelay { 
    *ar { |input delayTime=0.5 feedback=0.5 glide=0.1 tempo mul=1 add=0|
        tempo = tempo ?? { TempoSyncUtility.searchForTempo };

        ^CombC.ar(input, 4, (delayTime / tempo).lag(glide), feedback / tempo);
    }
}
