LiveSpace : ProxySpace {
    *new { |server name clock|
        server = server ?? Server.default;
        clock = clock ?? TempoClock.default;

        ^super.new(server, name, clock).tempo_(clock.tempo);
    }

    put { |key obj| 
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
