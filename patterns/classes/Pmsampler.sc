Pmsampler {
    *initClass {
         Event.addEventType(
            \tmsampler,  { |server|
                var nearestNotes;

                // if no \multi key in event throw an error
                if(~multi.isNil) {
                    ("The tmsampler event type must be provided with a" +
                    "multisample collection under the 'multi' key").error;
                };

                // look up the nearest notes with a buffer assigned to them
                nearestNotes = ~midinote.value.asArray.collect { |mn|
                    mn.nearestInList(~multi.keys.asArray);
                };

                // load up the buffers located there
                ~buffer = nearestNotes.collect { |nn| 
                    ~multi[nn].asArray.choose 
                };

                // calculate how much it needs to be repitched whilst still 
                // multiplying by previously specified rate if any
                ~rate = ~rate ?? 1 * (~midinote - nearestNotes).midiratio;

                // use tsampler event type for everything else see Psampler
                ~type = \tsampler;

                // trigger the event
                currentEnvironment.play;
            }
        );
    }

    *new {
            
    }
}
