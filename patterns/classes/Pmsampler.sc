Pmsampler {
    *initClass {
        Class.initClassTree(Event);

        Event.addEventType(
            \tmsampler,  { |server|
                if(currentEnvironment.isRest.not) {
                    var nearestNotes;

                    // if no \multi key in event throw an error
                    if(~multi.isNil) {
                        ("The tmsampler event type must be provided with a" +
                        "multisample collection under the 'multi' key").error;
                    };

                    // look up the nearest notes with a buffer assigned to them
                    nearestNotes = ~detunedFreq.value.asArray.cpsmidi.collect { |mn|
                        mn.nearestInList(~multi.keys.asArray.sort);
                    };

                    // load up the buffers located there
                    ~buffer = nearestNotes.collect { |nn| 
                        ~multi[nn].asArray.choose 
                    };

                    // calculate how much it needs to be repitched whilst still 
                    // multiplying by previously specified rate if any
                    ~rate = ~rate ?? 1 * (~detunedFreq.value.cpsmidi - nearestNotes).midiratio;

                    // use tsampler event type for everything else see Psampler
                    ~type = \tsimpler;

                    // trigger the event
                    currentEnvironment.play;
                }
            }
        );

        Event.addEventType(
            \sfvplay, { |server|
                var nearestNotes, velocities, velocityLayers, velNotes, multiKeys;

                // if no \multi key in event throw an error
                if(~multi.isNil) {
                    ("The tmsampler event type must be provided with a" +
                    "multisample collection under the 'multi' key").error;
                };
                
                velocities = ~amp.value.asArray.collect { |amp|
                    \midivelocity.asSpec.map(amp.value);
                };

                // work out which velocity layer we should use
                multiKeys = ~multi.keys.asArray.sort;
                velocityLayers = velocities.collect { |v|
                    ~multi[multiKeys[multiKeys.indexInBetween(v).floor]];
                };

                velNotes = [velocityLayers, ~detunedFreq.value.asArray.cpsmidi].flop;

                nearestNotes = velNotes.collect { |velNote|
                    var velocityLayer, note;
                    # velocityLayer, note = velNote;

                    note.nearestInList(velocityLayer[\regions].keys.asArray.sort);
                };

                // load up the buffers located there
                ~buffer = List();

                nearestNotes.do { |nn i| 
                    var b, id, region;
                    
                    region = velNotes[i][0][\regions][nn];

                    b = region.buffer;

                    // add to list of buffers to be played
                    ~buffer.add(b); 

                    // re-cue the buffer for next time it gets played
                    region.buffer = Buffer.cueSoundFile(b.server, b.path, region.offset);
                   
                    // fork task to clean up buffer when we're done with it
                    { 
                        (
                            ~timingOffset.value.asArray.wrapAt(i) +
                            ~sustain.value.asArray.wrapAt(i) + 
                            (~r ?? 0.1).asArray.wrapAt(i) 
                        ).wait;

                        b.free; 
                    }.fork;
                };

                ~rate = ~rate ?? 1 * (velNotes.flop[1] - nearestNotes).midiratio;

                // we're going to assume all buffers have same number of channels
                // which is more than likely with large multisample instruments
                ~instrument = \tsimplerbig ++ ~buffer[0].numChannels;
                
                ~type = \note;

                currentEnvironment.play;
            }
        );
    }

    *new { |multi...args|
        ^Pbind(\type, \tmsampler, \multi, multi, *args) 
    }
}
