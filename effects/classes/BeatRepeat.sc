BeatRepeat {
    *ar { 
        arg input, 
        interval=1, offset=0, grid=1, chance=1, gate=1, 
        rate=1, filter=1, cutoff=16000, rez=1, amp=1, decay=1, 
        mixInsertGate=0, tempo;

        var buffer, repeatsGate, repeatsGateTrig, repeatTrig, repeatStart, 
        startPos, recordHead, env, repeats;

        tempo = tempo ?? { TempoSyncUtility.searchForTempo };

        // get buffer to act as tape for recording
        buffer = BufferPreallocator.getBuffer;

        // set up the recored head
        recordHead = Phasor.ar(1, 1, 0, BufFrames.kr(buffer));

        // write input to buffer
        BufWr.ar(input, buffer, recordHead);

        // a gate signal with interval of interval, and chance chance of being
        // triggered
        repeatsGateTrig = CoinGate.ar(
            chance, ImpulseTS.ar(interval, offset * 2pi, tempo)
        );

        repeatsGate = Trig1.ar(repeatsGateTrig, gate / tempo);

        // grab playhead pos everytime repeatsGate triggers
        startPos = Latch.ar(recordHead, repeatsGateTrig);

        // repeating trigger
        repeatTrig = repeatsGate 
        - Trig1.ar(repeatsGateTrig, 0.01) 
        * ImpulseTS.ar(grid, 0, tempo);

        // envelope applied to each slice to stop popping
        env = EnvGen.ar(
            Env.linen(0.001, grid / tempo * rate - 0.002, 0.001), 
            repeatTrig
        );

        // play back repeated section from button
        repeats = PlayBuf.ar(2, buffer, rate, repeatTrig, startPos, 1);
        
        // apply envelope and amplitude
        repeats = repeats * env * amp;

        // apply filter
        repeats = SelectX.ar(filter, [
            repeats, BPF.ar(repeats, cutoff, rez)
        ]);

        // select betweem mix, insert and gate mode
        ^SelectX.ar(mixInsertGate, [
            // mix
            repeats + input,

            // insert
            (-1 * repeatsGate).lag(0.003) * input + repeats,

            // just repeats
            repeats
        ]);
    }
}
