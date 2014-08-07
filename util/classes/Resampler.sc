Resample {
    // records np for length beats starting at quant 
    *nodeProxy { |np length=4 quant=4|
        var frames;

        // calculate how long the recording is going to be
        frames = np.clock.beatDur * np.server.sampleRate * length;
        
        // allocate a buffer of that size
        ^Buffer.alloc(np.server, frames, np.numChannels, { |b|
            // create the synth to do recording ahead of time
            var recorder = { |run=0|
                RecordBuf.ar(np.ar, b, run: run, loop: 0, doneAction: 2); 0
            }.play(np.group, addAction: \addAfter);

            // set the recorder running at appropriate time
            { recorder.set(\run, 1) }.fork(np.clock, quant);
        });
    }
}
