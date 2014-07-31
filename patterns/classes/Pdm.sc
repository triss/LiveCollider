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
    }

    tempo { ^this.clock.tempo }
}

Pdm : NodeProxy {
    // drum track ordering for output of this nodeproxy
    var dmtOrdering;

    // a proxy space that holds all the seperate tracks
    var dmtSpace;

    // a cache of the uincoming patterns for when everything needs rebuilding
    var patternCache;

    // the drum map to use
    var drumMapProxy;

    // the patterns length in beats
    var <length;

    // pattern proxies for swing and accents
    var accentsProxy, accentAmountProxy;
    var swingAmountProxy, swingBaseProxy; 

    *new { |drumMap|
        ^super.new(Server.default, \audio, 2).initPdm(drumMap);
    }

    initPdm { |drumMap|
        this.quant = 4;

        patternCache = IdentityDictionary();

        drumMapProxy = PatternProxy(drumMap);

        // repeat every four beats by default
        length = 4;

        // accent every fourth hit by default
        accentAmountProxy = PatternProxy(0.5).quant_(4);
        accentsProxy = PatternProxy(
            Paccent(
                [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0], 
                accentAmountProxy)
            ).quant_(4);

        // start up with a little bit of swing
        swingAmountProxy = PatternProxy(1/3).quant_(4);
        swingBaseProxy = PatternProxy(1/4).quant_(4);

        // TODO: look up server from NodeProxy
        dmtSpace = ProxySpace(Server.default);
        dmtSpace.quant = 4;
        dmtOrdering = IdentitySet();
    }

    reconstructPatterns {
        patternCache.keysValuesDo { |k p| this.setTrack(k, p) };
    }

    setTrack { |trackName pattern|
        patternCache[trackName] = pattern;

        // if the pattern is given as an event type thing
        if(pattern.respondsTo(\keysValuesDo)) {
            var pbindArgs, trackNumber;

            pbindArgs = pattern;

            // wrap arrays up any arrays as Pseq's as convienace since
            // chords would rarely be fed to a drum machine
            pbindArgs.keysValuesChange { |k obj|
                if(obj.isArray) {
                    Pseq(obj, inf);
                } {
                    obj
                }
            };

            pbindArgs.make {
                // ensure the following keys are set for the pattern

                // use the drum machines default drum map
                ~drums = ~drums ?? drumMapProxy;

                // look up the buffer name based on the selector
                ~buffer = ~buffer ?? trackName;

                // multiply amp by accents
                ~amp = (~amp ?? 0.5) * accentsProxy;

                // set up swing
                ~swingAmount = ~swingAmount ?? swingAmountProxy;
                ~swingBase = ~swingBase ?? swingBaseProxy;

                // TODO: hmmm Pfindur doesn't take patterns?
                ~length = ~length ?? length.postln;

                // use the pdmtsampler event type
                ~type = \pdmtsampler;
            };

            // create the accented and swung drum track
            dmtSpace[trackName] = 
                Pnfd(max(length, pbindArgs[\length]),
                    Pnfd(pbindArgs[\length], 
                    Pswing(Pbind(*pbindArgs.getPairs)
                )));

            // work out which slot in our outProxy this dmt consume
            dmtOrdering.add(trackName);
            trackNumber = dmtOrdering.array.indexOf(trackName);

            // add the track to the out proxy
            this[trackNumber] = dmtSpace[trackName];
        }
    }

    doesNotUnderstand { |selector...args| 
        // if we're setting a drum track
        if(selector.isSetter) {
            this.setTrack(selector.asGetter, args[0]);
        } {
            ^dmtSpace[selector];
        }
    }

    // setters for shared pattern params
    accents_ { |acc| accentsProxy.source = Paccent(acc, accentAmountProxy) }
    accentAmount_ { |amt| accentAmountProxy.source = amt }     
    swingAmount_ { |amt| swingAmountProxy.source = amt }
    swingBase_ { |base| swingBaseProxy.source = base }
    drumMap_ { |dm| drumMapProxy.source = dm }

    // length doesn't just use PatternProxy's since Pfindur can't have dur 
    // updated
    length_ { |l| length = l; this.reconstructPatterns }

    // and getters
    accents { ^accentsProxy.source }
    accentAmount { ^accentAmountProxy.source }     
    swingAmount { ^swingAmountProxy.source }
    swingBase { ^swingBaseProxy.source }
    drumMap { ^drumMapProxy.source }
}

Pdmt {
    *initClass {
        Event.addEventType(
            \pdmtsampler,  {
                // if a note has been specified? does degree stuff work?
                if(~note.notNil) {
                    ~rate = ~rate ?? 1 * ~note.midiratio
                };

                // map buffer names to buffers
                ~buffer = ~buffer.asArray.collect { |name|
                    // if drum map contains round robin this handles it
                    ~drums[name].asArray.choose;
                };

                // set event type to \tsampler for further processing
                ~type = \tsampler;

                // trigger the event
                currentEnvironment.play;
            }
        );
    }
    
    *new { |drums bufferPattern...args|
        ^Pbind(
            \type, \pdmtsampler, 
            \drums, drums, 
            \buffer, bufferPattern, 
            *args
        );
    }
}
