Pdm : NodeProxy {
    // drum track ordering for output of this nodeproxy
    var dmtOrdering;

    // a proxy space that holds all the seperate tracks
    var dmtSpace;

    // a cache of the incoming patterns for when everything needs rebuilding
    // or presets
    var patternCache;

    // the drum map to use
    var drumMapProxy, shorthands;

    // the patterns length in beats
    var <length;

    // pattern proxies for swing and accents
    var <accentsProxy, accentAmtProxy, <accentValue;
    var swingAmtProxy, swingBaseProxy; 

    *new { |drumMap|
        ^super.new(Server.default, \audio, 2).initPdm(drumMap);
    }

    initPdm { |drumMap|
        this.quant = 4;

        patternCache = IdentityDictionary();

        // set up the drum map
        drumMapProxy = PatternProxy(IdentityDictionary());
        shorthands = IdentityDictionary();
        this.drumMap_(drumMap);

        // repeat every four beats by default
        length = 4;

        // the variable discerns whether or not to update
        accentValue = 1;

        // accent every fourth hit by default
        accentAmtProxy = PatternProxy(0.5);

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
        patternCache.keysDo { |k| this.reconstructPattern(k) };
    }

    reconstructPattern { |name| this.setTrack(name, patternCache[name]) }

    parseEvent { |pbindArgs|
        pbindArgs = pbindArgs.asEvent;

        // wrap any arrays as Pseq's as convienace since chords are rarely 
        // fed to a drum machine
        pbindArgs.keysValuesChange { |k obj| 
            if(obj.isSequenceableCollection) {
                Pseq(obj, inf) 
            } {
                obj
            };
        };

        ^pbindArgs;
    }

    parseString { |string|
        var durs = List(), buffers = List(), amps = List(), dur = 1;

        string.do { |c i|
            var buffer = c.toLower.asSymbol;

            // if we know buffer c
            if(shorthands[buffer].notNil) { 
                // add it to the list of buffers
                buffers.add(shorthands[buffer]); 

                // if not the first or last hit add a duration
                if(i != 0) {
                    durs.add(dur / 4); 
                };

                // upper case is loud, lower case is soft
                if(c.isLower) {
                    amps.add(0.25); 
                } {
                    amps.add(0.5);
                };

                // reset note duration
                dur = 1; 
            } {
                // if it's the first hit and we don't know c
                if(i == 0) {
                    // put a rest on the first hit
                    buffers.add(\);
                    amps.add(0);
                };

                // add one to duration
                dur = dur + 1;
            };

            if(i == (string.size - 1)) {
                durs.add(dur / 4);
            };
        };

        // return an event containing the pattern
        ^(buffer: buffers, dur: durs, amp: amps).postln; 
    }

    setTrack { |trackName pattern|
        var pbindArgs, trackNumber;

        // if Pbind specify take that in and modify it's args
        if(pattern.respondsTo(\patternpairs)) {
            pbindArgs = pattern.patternpairs;
        };

        // if a string was provided parse out the pattern
        if(pattern.isKindOf(String)) {
            pattern = this.parseString(pattern);
        };

        if(pattern.respondsTo(\keysValuesDo)) {
            // turn all arrays to Pseqs
            pbindArgs = this.parseEvent(pattern);
        };

        if(pbindArgs.notNil) {
            // set some defalts if not allready set
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

                // use the pdmtsampler event type if none specified
                ~type = ~type ?? \pdmtsampler;
            };

            // create the accented and swung drum track
            dmtSpace[trackName] = Pnfd(pbindArgs[\length], 
                Pbind(*pbindArgs.getPairs)
            );

            // cache the pattern in case it needs to be rebuilt (when things like
            // bar length get updated)
            patternCache[trackName] = pbindArgs;

            // work out which slot in our outProxy this dmt consume
            dmtOrdering.add(trackName);
            trackNumber = dmtOrdering.array.indexOf(trackName);

            // add the track to the out proxy
            this[trackNumber] = dmtSpace[trackName];
        } {
            "Can not convert" + pattern.class + "to pattern for Pdm.".warn;
        }
    }

    postShorthand {
        shorthands.associationsDo { |a| a.postln };
    }

    postBuffers {
        drumMapProxy.source.keysValuesDo { |k b|
            (k -> b.path).postln;
        };
    }

    doesNotUnderstand { |selector...args| 
        // if we're setting a drum track
        if(selector.isSetter) {
            this.setTrack(selector.asGetter, args[0]);
        } {
            ^(
               ar: dmtSpace[selector],
               set: { |ev param value| 
                   patternCache[selector][param] = value; 
                   this.reconstructPatterns;
               }
            )
        }
    }

    // setters for shared pattern params
    accents_ { |acc| this.updateAccentPattern(acc) }
    accentAmt_ { |amt| accentAmtProxy.source = amt }     
    swing_ { |amt| swingAmtProxy.source = amt }
    swingBase_ { |base| swingBaseProxy.source = base }
    drumMap_ { |dm| 
        var drumMap = drumMapProxy.source;

        // add any new drum sounds and overwrite exisiting ones
        drumMap.putAll(dm);

        // recreate the shorthands
        dm.keysValuesDo { |key buffer|
            var i=0, name = key.asString, noShorthandFound = true;

            // if we already have a shorthand for this buffer name
            if(shorthands.includes(key)) {
                noShorthandFound = false;
            };

            // find a short hand by interating through characters one at a time
            while({ noShorthandFound and: { i < name.size } }, {
                var shorthand = name[i].asSymbol;
                
                // if we've not used this abreviation before store buffer under
                // it
                shorthands[shorthand] ?? {
                    shorthands[shorthand] = key;
                    noShorthandFound = false;
                    ("Buffer" + key + "mapped to" + shorthand).postln;
                };

                i = i + 1;
            });
            
            // if we still haven't found one choose one at random
            if(noShorthandFound) {
                var usedShorthand, allChars, leftOverNames;

                // get all letters in alphabet
                allChars = (97..122).collect({ |i| i.asAscii.asSymbol }).asSet;

                // and all of the used shorthand
                usedShorthand = shorthands.keys.asSet;

                // work out which chars are left over
                leftOverNames = allChars - usedShorthand;

                // randomly choose a character
                if(leftOverNames.size > 0) {
                    var shorthand = leftOverNames.choose;
                    shorthands[shorthand] = key;
                    ("Buffer" + key + "mapped to" + shorthand).postln;
                } {
                    ("Couldn't find shorthand for" + key).warn;
                };
            }
        };
    }


    // length doesn't just use PatternProxy's since Pfindur can't have dur 
    // updated
    length_ { |l| length = l; this.reconstructPatterns }

    // and getters
    accents { ^accentsProxy.source }
    accentAmt { ^accentAmtProxy.source }     
    swing { ^swingAmtProxy.source }
    swingBase { ^swingBaseProxy.source }
    drumMap { ^drumMapProxy.source }

    swap { |param source dest| 
        var patterns = patternCache.keys.asArray.scramble, t;

        if(param.isNil) { 
            "You must at least tell Pdm which paramater to swap".warn; ^this 
        };

        // if not specified choose random patterns
        source = source ?? { patterns.take(1) };
        dest = dest ?? { patterns.take(1) };

        t = patternCache[source][param];
        patternCache[source][param] = patterns[dest][param];
        patternCache[dest][param] = t;

        this.reconstructPatterns;
    }
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
