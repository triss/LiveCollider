Psampler {
    *initClass {
        StartUp.add {
            // we'll need a different synth and differing panning methods 
            // dependant on the number of channels in buffer to be played 
            var synthDefSpecs = (
                tsampler: (
                    numChannels: 1, 
                    panner: { |e sig pan| Pan2.ar(sig, pan) }
                ),
                tsampler2: (
                    numChannels: 2, 
                    panner: { |e sig pan| Pan2.ar(sig, pan) }
                ),
                tsampler6: (
                    numChannels: 6, 
                    panner: { |e sig pan| Splay.ar(sig, center: pan) }
                )
            );
           
            // generate each of the synthDefs according to spec
            synthDefSpecs.keysValuesDo { |name spec|         
                // set up sampler synths for use with Psampler etc.
                SynthDef(name, {
                    arg out = 0, buffer = 0, amp = 1, pan = 0, rate = 1, start = 0,
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
                        ~rate = ~rate.value ?? 1 * ~note.value.midiratio
                    };

                    // use normal note type to trigger event
                    ~type = \note;

                    // trigger the event
                    currentEnvironment.play;
                }
            );
        };
    }

    *new { |bufferPattern...args|
        ^Pbind(\type, \tsampler, \buffer, bufferPattern, *args);
    }
}
