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
                    // standard bits and bobs
                    arg out=0, amp=1, pan=0, gate=0, tempo=1,
                    
                    // buffer, playback rate and start positioon
                    buffer=0, rate=1, start=0, glide=0,
                    
                    // filter stuff
                    filterType=0, cutoff=1000, rez=0.5,

                    // envelope shape and application
                    a=0, d=0, s=1, r=0.01, envPitchAmt=0, envPanAmt=0, envCutoffAmt=0,

                    // lfo stuff
                    lfoRate=0, lfoShape=0, lfoPhase=0, lfoSmooth=0,
                    lfoPitchAmt=0, lfoAmpAmt=0, lfoPanAmt=0, lfoCutoffAmt=0;

                    var sig, env, lfo;

                    // apply glide to pitch
                    rate = rate.lag(glide);

                    // create the envelope
                    env = EnvGen.ar(Env.adsr(a, d, s, r), gate, doneAction: 2);

                    // apply envelope to synth params
                    amp = env * amp;
                    cutoff = env * envCutoffAmt * cutoff + cutoff;
                    rate = env * envPitchAmt * rate + rate;
                    pan = env * envPanAmt + pan;

                    // tempo sync the lfo
                    lfoRate = lfoRate * tempo;

                    // create the lfo
                    lfo = SelectX.kr(lfoShape, [
                        SinOsc.kr(lfoRate, lfoPhase),
                        LFSaw.kr(lfoRate, lfoPhase),
                        LFPulse.kr(lfoRate, lfoPhase),
                        LFNoise0.kr(lfoRate),
                        LFNoise1.kr(lfoRate)
                    ]).lag(lfoSmooth);

                    // apply lfo to synth params
                    amp = lfoAmpAmt * lfo + amp;
                    rate = lfoPitchAmt * lfo * rate + rate;
                    pan = lfoPanAmt * lfo + pan;
                    cutoff = lfoCutoffAmt * lfo * cutoff + cutoff;

                    // clip stuff that nees to be in a certain range
                    pan = pan.clip(-1, 1);
                    cutoff = cutoff.clip(30, 16000);

                    // playback the buffer
                    sig = PlayBuf.ar(
                        spec.numChannels, buffer, 
                        rate: BufRateScale.kr(buffer) * rate, 
                        trigger: gate,
                        startPos: start * BufFrames.kr(buffer),
                        doneAction: 2
                    );

                    // apply the amp envelope
                    sig = sig * amp;
                    
                    // filter the signal
                    sig = SelectX.ar(filterType, [
                        sig,
                        RLPF.ar(sig, cutoff, rez),
                        BPF.ar(sig, cutoff, rez),
                        RHPF.ar(sig, cutoff, rez)
                    ]);

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
                    amp: [0, 1],
                    pan: \pan,
                    rate: [-8, 8, \exp, 0, 1].asSpec,
                    start: \unipolar,
                    a: [0, 10], d: [0, 10], s: \unipolar, r: [0, 10]
                ))).add;
            };
        };

        Event.addEventType(
            \tsampler,  { |server|
                // choose instrument based on number of channels in buffer
                ~instrument = ~buffer.asArray.collect { |b| 
                    switch(b.numChannels,
                        1, { \tsampler },
                        2, { \tsampler2 },
                        6, { \tsampler6 }, // I have the odd six channel sound files!
                        { ("The tsampler eventType does not how to play buffers with" 
                        + b.numChannels + "channels.").error; }
                    );
                };

                // evaluate all the functions that are lurking in this event
                currentEnvironment.keysValuesChange { |k v| v.value };

                // use normal note type to trigger event
                ~type = \note;

                // play a seperate event for each note in chord since SC 
                // doesn't support multiple instruments in one event
                currentEnvironment.getPairs.flop.do { |a i|
                    var e = Event.newFrom(a);
                    e[\timingOffset] = (e[\timingOffset] ?? 0) + (~strum.value * i);
                    e.play;
                };
            }
        );
    }

    *new { |bufferPattern...args|
        ^Pbind(\type, \tsampler, \buffer, bufferPattern, *args);
    }
}
