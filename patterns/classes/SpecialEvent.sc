SpecialEvent : Event {
    *setup {
        // set default probability to 1
        this.parentEvents.default.prob = 1.0;

        // set default swing to nothing
        this.parentEvents.default.swingAmount = 0;
        this.parentEvents.default.swingBase = 1/4;

        // overide the default play method
        this.parentEvents.default[\play] = #{
            var tempo, server;

            ~type.postln;
            // only fiddle with note types
            if(~type == \note or: { ~type == \set }) {
                // handle probability
                if(~prob.isSequenceableCollection.not) {
                    if(~prob.coin.not) { ~isRest = true };
                };

                // swing this event
                SpecialEvent.swingerFunc(currentEnvironment);

                // handle any chords that have been specified as symbols
                if(~degree.isKindOf(Symbol)) { ~degree = ~degree.asDegrees(~scale) };
                if(~note.isKindOf(Symbol)) { ~note = ~note.asNotes };
                if(~midinote.isKindOf(Symbol)) { ~midinote = ~midinote.asNotes };

                // if instrument is array flop this event in to multiple events
                // allows multiple instruments to be defined per event
                if(~instrument.isSequenceableCollection 
                    or: { ~strum != 0 } 
                    or: { ~prob.isSequenceableCollection }
                ) { 
                    currentEnvironment.getPairs.flop.do { |a i|
                        var e = Event.newFrom(a);

                        // handle strum for each note
                        e[\timingOffset] = (e[\timingOffset] ?? 0) + (~strum * i);
                        e[\dur] = ~strum;
                        e[\strum] = 0;
                        e.play;
                    };
                    currentEnvironment[\isRest] = true;
                }; 
            };

            // origanal default events play method from here on
            ~finish.value;

            server = ~server ?? { Server.default };

            tempo = ~tempo;
            if (tempo.notNil) {
                thisThread.clock.tempo = tempo;
            };

            // ~isRest may be nil - force Boolean behavior
            // and check that this event hasn't been split in to multiple events
            if(~isRest != true) { 
                ~eventTypes[~type].value(server) 
            };
        };
    }

    *swingerFunc { |ev|
        var nextTime, thisShouldSwing, nextShouldSwing = false, adjust, clock;
        clock = clock ?? TempoClock.default;

        // calculate what time the next event will trigger
        nextTime = ev[\timingOffset] ?? 0 + clock.beats + ev.delta;

        // work out wether or not it should swing
        thisShouldSwing = ((nextTime absdif: nextTime.round(ev[\swingBase])) 
            <= (ev[\swingThreshold] ? 0)) and: {
                (nextTime / ev[\swingBase]).round.asInteger.odd
            };

        // calculate how far to shift notes
        adjust = ev[\swingBase] * ev[\swingAmount];

        // an odd number here means we're on an off-beat
        if(thisShouldSwing) {
            ev[\timingOffset] = (ev[\timingOffset] ? 0) + adjust;
            // if next note will not swing, this note needs to be shortened
            if(nextShouldSwing.not) {
                ev[\sustain] = ev.use { ~sustain.value } - adjust;
            };
        } {
            // if next note will swing, this note needs to be lengthened
            if(nextShouldSwing) {
                ev[\sustain] = ev.use { ~sustain.value } + adjust;
            };
        };

        // return the event
        ev;
    }
}
