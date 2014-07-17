TempoSyncUtility {
    *searchForTempo {
        ^if(currentEnvironment[\tempo].source.notNil) {
            currentEnvironment[\tempo].kr; 
        } {
            TempoClock.default.tempo;
        };
    }
}

ProxySpaceTS : ProxySpace {
    tempo_ { |tempo|
        this.clock.tempo = tempo;
        this.put(\tempo, tempo);
    }

    tempo { ^this.clock.tempo }
}

ImpulseTS {
    *kr { |freq=1 phase=0 tempo mul=1 add=0|
        tempo = tempo ?? { TempoSyncUtility.searchForTempo };

        ^Impulse.kr(freq * tempo, phase, mul, add);
    }

    *ar { |freq=1 phase=0 tempo mul=1 add=0|
        tempo = tempo ?? { TempoSyncUtility.searchForTempo };

        ^Impulse.ar(freq * tempo, phase, mul, add);
    }
}

PhaseLfoTS {
    *kr { |rate=1 phase=0 offset=0 shape=0 tempo mul=1 add=0|
        var rateHz, lfo;

        // if no tempo provided then look up tempo in ProxySpace or on default 
        // clock
        tempo = tempo ?? { TempoSyncUtility.searchForTempo };

        // calculate lfo rate in Hz
        rateHz = rate * tempo;

        // add phasing to lfo channels
        phase = (offset + [phase, 0 - phase]).mod(2pi);

        // lfo shape is used to choose between the following lfo shapes
        lfo = SelectX.kr(shape, 
            [SinOsc, LFTri, LFPulse, LFSaw, LFNoise1].collect { |ugen| 
                ugen.kr(rateHz, phase)
            };
        );
       
        // amplify and offset the lfo
        ^lfo.madd(mul, add)
    }
}
