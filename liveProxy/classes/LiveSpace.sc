LiveSpace : ProxySpace {
    *new { |server name clock|
        server = server ?? Server.default;
        clock = clock ?? TempoClock.default;

        ^super.new(server, name, clock).initLiveSpace;
    }

    initLiveSpace {
        // set LiveSpaces easily accessible tempo keys
        this.tempo_(clock.tempo);
        this.quant = 4;
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
