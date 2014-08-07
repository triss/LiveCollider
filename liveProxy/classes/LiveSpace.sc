LiveSpace : ProxySpace {
    *new { |server name clock|
        server = server ?? Server.default;
        clock = clock ?? TempoClock.default;

        ^super.new(server, name, clock).initLiveSpace;
    }

    initLiveSpace {
        // set LiveSpaces easily accessible tempo keys
        this.tempo_(clock.tempo);
        
        // set default events probability
        Event.parentEvents.default.prob = 1.0;

        // reimpliment play to take in to account probaility and inpack any 
        // chord names left in the event
        Event.parentEvents.default.play = #{
            var tempo, server;

            // impliments event probability prob key
            if(~prob.coin.not) { ~isRest = true };

            // handle any chords that have been specified as symbols
            if(~degree.notNil) { ~degree = Chord.toDegrees(~degree, ~root, ~scale) };
            if(~note.notNil) { ~note = Chord.toNotes(~note) };
            if(~midinote.notNil) { ~midinote = Chord.toNotes(~midinote) };

            // origanal default events play method from here on
            // ------------------------------------------------
            ~finish.value;

            server = ~server ?? { Server.default };

            tempo = ~tempo;
            if (tempo.notNil) {
                thisThread.clock.tempo = tempo;
            };

            // ~isRest may be nil - force Boolean behavior
            if(~isRest != true) { ~eventTypes[~type].value(server) };
        };
    }

    put { |key obj| 
        // not wraping NodeProxy's in NodeProxy's allows me to access classses
        // derived from NodeProxy's methods via ~proxyName.methodName
        // wrapping an exisiting NodeProxy with another NodeProxy is wasteful 
        // anyhow
        if(obj.isKindOf(NodeProxy)) {
            envir[key] = obj;
        } {
            super.put(key, obj);
        }
    }

    tempo_ { |tempo|
        clock.tempo = tempo;
        this.put(\tempo, tempo);
        this.put(\beatDur, clock.beatDur);
    }

    tempo { ^this.clock.tempo }
}
