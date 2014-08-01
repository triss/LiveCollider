Resample {
    *new { |np length=4 quant=4|
        var buffer, frames, recorder;

        frames = np.clock.beatDur * np.server.sampleRate * length;
        
        buffer = Buffer.alloc(np.server, frames, np.numChannels, { |b|
            recorder = { |run=0|
                RecordBuf.ar(np.ar, b, run: run, loop: 0, doneAction: 2);

                // don't output any sound!
                0
            }.play(np.group, addAction: \addAfter);

            { recorder.set(\run, 1) }.fork(np.clock, quant);
        });

        ^buffer;
    }
}
