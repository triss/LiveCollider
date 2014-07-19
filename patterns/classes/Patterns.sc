Pnfindur {
    *new { |n pattern repeats|
        repeats = repeats ?? inf;
        ^Pn(Pfindur(n, pattern), repeats);
    }
}

Pnfinval {
   *new { |n pattern repeats|
        ^Pn(Pfinval(n, pattern), repeats);
    }
}

// Pnfd is just shorthand for Pnfindur
Pnfd {
    *new { |n pattern repeats|
        ^Pnfindur(n, pattern, repeats);
    }
}

// Pnfv is just shorthand for Pnfinval
Pnfv {
    *new { |n pattern repeats|
        ^Pnfinval(n, pattern, repeats);
    }
}

Psampler {
    initClass {
        StartUp.add {
            // differing 
            var synthDefSpecs = (
                tsampler: (
                    numChannels: 1, 
                    panner: { |sig pan| Pan2.ar(sig, pan) }
                ),
                tsampler2: (
                    numChannels: 2, 
                    panner: { |sig pan| Pan2.ar(sig, pan) }
                ),
                tsampler6: (
                    numChannels: 6, 
                    panner: { |sig pan| Splay.ar(sig, center: pan) }
                )
            );
            
            synthDefSpecs.keysValuesDo { |name spec|         
                // set up sampler synth for use with Psamp
                SynthDef(name, {
                    arg out = 0, buffer = 0, amp = 1, pan = 0,  rate = 1, start = 0,
                    attack = 0, sustain = 1, release = 0.001;

                    var sig, env;

                    // attack and sustain are proportion of overall sustain
                    attack = attack * sustain;
                    release = release * sustain;

                    // recalculate sustain with attack/decay portions of envelope
                    sustain = sustain - attack - release;

                    // create the amp envelope
                    env = EnvGen.ar(
                        Env.linen(attack, sustain, release), 
                        doneAction: 2
                    );

                    // playback the buffer
                    sig = PlayBuf.ar(
                        spec.numChannels, buffer, 
                        rate: BufRateScale.kr(buffer) * rate, 
                        startPos: start * BufFrames.kr(buffer),
                    );

                    sig = sig * env * amp;

                    // pan and amplify the signal
                    sig = spec.panner(sig, pan);

                    // output the audio
                    OffsetOut.ar(out, sig)
                },

                // all controls at initialisation rate since these samplers are
                // taylored to short samplers 
                rates: \ir ! 9,

                metadata: (specs: (
                    buffer: [0, 1024, \lin, 1].asSpec,
                    out: \audiobus, 
                    amp: \amp,
                    pan: \pan,
                    rate: [-8, 8, \exp, 0, 1].asSpec,
                    start: \unipolar,
                    attack: \unipolar,
                    sustain: [0, 60],
                    release: \unipolar
                ))).add;
            } 
        };

        Event.addEventType(
            \tsampler,  { |server|
                // choose instrument based on number of channels in buffer
                ~instrument = switch(~buffer.numChannels,
                    1, { \tsampler },
                    2, { \tsampler2 },
                    6, { \tsampler6 }, // I have the odd six channel sound files!
                    { ("The tsampler eventType does not how to play buffers with" 
                    + ~buffer.numChannels + "channels.").error; }
                );

                // if a note has been specified? does degree stuff work?
                if(~note.notNil) {
                    ~rate = ~rate ?? 1 * ~note.midiratio
                };

                // use normal note type to trigger event
                ~type = \note;

                // trigger the event
                currentEnvironment.play;
            }
        );
    }

    *new { |bufferPattern...args|
        ^Pbind(\instrument, \tsampler, \buffer, bufferPattern, *args);
    }
}
