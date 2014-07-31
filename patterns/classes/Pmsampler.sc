Pmsampler {
    *initClass {
         Event.addEventType(
            \tmsampler,  { |server|
                var nearestNote;

                // if no \multi key in event throw an error
                if(~multi.isNil) {
                    ("The tmsampler event type must be provided with a" +
                    "multisample collection under the 'multi' key").error;
                };

                // look up the nearest note with a buffer assigned to it
                nearestNote = ~midinote.value.nearestInList(~multi.keys.asArray);

                // load up the buffer located there
                ~buffer = ~multi[nearestNote];
                
                // handle round robin if in sample map
                ~buffer = ~buffer.asArray.choose;

                // calculate how much it needs to be repitched whilst still 
                // multiplying by previously specified rate if any
                ~rate = ~rate ?? 1 * (~midinote - nearestNote).midiratio;

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
