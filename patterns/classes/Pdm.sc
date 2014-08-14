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
    var <accentsProxy, accentAmtProxy, <accentValueProxy, accentValue;
    var swingAmtProxy, swingBaseProxy; 

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
        accentAmtProxy = PatternProxy(0.5);

        // no quantisation for accent value 
        accentValueProxy = PatternProxy(1).quant_(nil);

        // start up with a little bit of swing
        swingAmtProxy = PatternProxy(1/3);
        swingBaseProxy = PatternProxy(1/4);

        // TODO: look up server from NodeProxy
        dmtSpace = ProxySpace(this.server);
        dmtSpace.quant = 4;
        dmtOrdering = IdentitySet();

        this.updateAccentPattern([1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0]);
        dmtSpace[\accentsProxy].play;
    }

    updateAccentPattern { |accents|
        dmtSpace[\accentsProxy] = Pnfd(4, Pchain(
            Pfunc({ |ev| accentValue = (ev.accent ?? 1); ev }),
            Pbind(
                \accent, Pseq(accents.rotate(-5), inf) * accentAmtProxy + 1 - accentAmtProxy,
                \dur, 0.25, \note, \,
                \lag, -0.01,
                \swingAmt, swingAmtProxy,
                \swingBase, swingBaseProxy
            )
        ));
    }

    reconstructPatterns {
        patternCache.keysValuesDo { |k p| this.setTrack(k, p) };
    }

    setTrack { |trackName pattern|
        // cache the pattern in case it needs to be rebuilt (when things like
        // bar length get updated)
        patternCache[trackName] = pattern;

        // if the pattern is given as an event type thing
        if(pattern.respondsTo(\keysValuesDo)) {
            var pbindArgs, trackNumber;

            pbindArgs = pattern;

            // wrap any arrays as Pseq's as convienace since chords are rarely 
            // fed to a drum machine
            pbindArgs.keysValuesChange { |k obj| Pseq(obj.asArray, inf) };

            pbindArgs.make {
                // ensure the following keys are set for the pattern

                // use the drum machines default drum map
                ~drums = ~drums ?? drumMapProxy;

                // look up the buffer name based on the selector
                ~buffer = ~buffer ?? trackName;

                // multiply amp by accents
                ~amp = (~amp ?? 0.5) * Pfunc({ accentValue });

                // set up swing
                ~swingAmt = ~swingAmt ?? swingAmtProxy;
                ~swingBase = ~swingBase ?? swingBaseProxy;

                ~length = ~length ?? length;

                // use the pdmtsampler event type
                ~type = \pdmtsampler;
            };

            if(pattern.isKindOf(String)) {
                
            };

            // create the accented and swung drum track
            dmtSpace[trackName] = 
                Pnfd(pbindArgs[\length], 
                    PbindProxy(*pbindArgs.getPairs)
                );

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
    accents_ { |acc| this.updateAccentPattern(acc) }
    accentAmt_ { |amt| accentAmtProxy.source = amt }     
    swing_ { |amt| swingAmtProxy.source = amt }
    swingBase_ { |base| swingBaseProxy.source = base }
    drumMap_ { |dm| drumMapProxy.source = dm }

    // length doesn't just use PatternProxy's since Pfindur can't have dur 
    // updated
    length_ { |l| length = l; this.reconstructPatterns }

    // and getters
    accents { ^accentsProxy.source }
    accentAmt { ^accentAmtProxy.source }     
    swing { ^swingAmtProxy.source }
    swingBase { ^swingBaseProxy.source }
    drumMap { ^drumMapProxy.source }
}

Pdmt {
    classvar <silence;

    *initClass {
        StartUp.add({
            Server.default.onBootAdd({
                silence = Buffer.alloc(Server.default, 1024);
            });
        });

        Event.addEventType(
            \pdmtsampler,  {
                // map buffer names to buffers
                ~buffer = ~buffer.asArray.collect { |name|
                    if(name != \) {
                        // if drum map contains round robin this handles it
                        ~drums[name].asArray.choose;
                    } {
                        // return silence if rest specified
                        Pdmt.silence;
                    }
                };

                // set event type to \tsampler for further processing
                ~type = \tsimpler;

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
