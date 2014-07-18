// Modified PingPong with BPF in signal chain
PingPongDelay {
    *ar { |input delayTime=1 feedback=0.7 cutoff=2000 rez=1 dryWet=1 tempo|

        var buffer, delaySamps, phase, 
        feedbackChannels, delayedSignals, rotatedDelayed, frames;
       
        var dry;

        tempo = tempo ?? { TempoSyncUtility.searchForTempo };

        // add a fade in line to dull glitches when swapped in and out.
        // - glitches occur due to LocalBuf taking a moment to initialise #9
        input = input * Line.kr(0, 1, 0.5);

        dry = input;

        // filter the input
        input = BPF.ar(input, cutoff, rez);

        // set up a local buffer for storing delayed signal
        buffer = LocalBuf(44100 * 8, 2);
        frames = BufFrames.kr(buffer);

        // compensate for control rate delay added by LocalIn
        delaySamps = max(0, delayTime * SampleRate.ir - ControlDur.ir / tempo).round;

        // read/write head "on tape" - acts as drive for read/write operations
		phase = Phasor.ar(0, 1, 0, frames);

        // get the feedback
		feedbackChannels = LocalIn.ar(2) * feedback;

        // read back the delayed signal from tape
 		delayedSignals = BufRd.ar(
            2, buffer, 

            // delay achieved by shifting and wraping the recordhead
            (phase - delaySamps).wrap(0, frames)
        );

        // write the delayed signal to feedback bus
		LocalOut.ar(delayedSignals);

        // add feedback to inputs and rotate the signal
        rotatedDelayed = (input + feedbackChannels).rotate(1) 
            <! delayedSignals.asArray.first;

        // filter the output before we write it
        rotatedDelayed = BPF.ar(rotatedDelayed, cutoff, rez);

        // write the delayed signal + feedback to the buffer
		BufWr.ar(rotatedDelayed, buffer, phase, 1);

		^XFade2.ar(dry, delayedSignals, dryWet * 2 - 1);
    }
}
