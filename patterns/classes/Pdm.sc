Pdm {
    *initClass {
        StartUp.add {
            Event.addEventType(
                \tdmsampler,  { |server|
                    // if buffer is an array of values
                    ~buffer = if(~buffer.value.isArray) {
                        // collect up all the buffers refernced in the array
                        ~buffer.value.collect { |b|
                            ~buffer = ~drums.value[b].choose;
                        }
                    } {
                        // otherwise just look up the one
                        ~drums.value[~buffer.value].choose;
                    };

                    // set event type to \tsampler for further processing
                    ~type = \tsampler;

                    // trigger the event
                    currentEnvironment.play;
                }
            );
        };
    }

    *new { |drums bufferPattern...args|
        ^Pbind(
            \type, \tdmsampler, 
            \drums, drums, 
            \buffer, bufferPattern, 
            *args
        );
    }
}
