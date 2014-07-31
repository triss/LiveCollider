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
    var dmtOrdering;

    var dmtSpace;

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

    doesNotUnderstand { |selector...args| 
        // if we're setting a drum track
        if(selector.isSetter) {
            // if the first argument is an event type thing
            if(args[0].respondsTo(\keysValuesDo)) {
                var pbindArgs, trackName, trackNumber;

                pbindArgs = args[0];
                trackName = selector.asGetter;

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
                    ~drums = ~drums.value ?? drumMapProxy;

                    // look up the buffer name based on the selector
                    ~buffer = ~buffer.value ?? trackName;

                    // multiply amp by accents
                    ~amp = (~amp.value ?? 0.5) * accentsProxy;

                    // set up swing
                    ~swingAmount = ~swingAmount.value ?? swingAmountProxy;
                    ~swingBase = ~swingBase.value ?? swingBaseProxy;

                    // TODO: hmmm Pfindur doesn't take patterns?
                    ~length = ~length.value ?? length.postln;

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
        } {
            ^dmtSpace[selector]
        }
    }

    // setters for shared pattern params
    accents_ { |acc| accentsProxy.source = Paccent(acc, accentAmountProxy) }
    accentAmount_ { |amt| accentAmountProxy.source = amt }     
    swingAmount_ { |amt| swingAmountProxy.source = amt }
    swingBase_ { |base| swingBaseProxy.source = base }
    length_ { |l| length= l }
    drumMap_ { |dm| drumMapProxy.source = dm }

    // and getters
    accents { ^accentsProxy.source }
    accentAmount { ^accentAmountProxy.source }     
    swingAmount { ^swingAmountProxy.source }
    swingBase { ^swingBaseProxy.source }
    drumMap { ^drumMapProxy.source }
}

Pdmt {
    *initClass {
        StartUp.add {
            Event.addEventType(
                \pdmtsampler,  {
                    // if a note has been specified? does degree stuff work?
                    if(~note.notNil) {
                        ~rate = ~rate.value ?? 1 * ~note.value.midiratio
                    };

                    // if buffer is an array of values
                    ~buffer = if(~buffer.value.isArray) {
                        // collect up all the buffers refernced in the array
                        ~buffer.value.collect { |b|
                            if(~drums.value.isArray) {
                                ~drums.value[b].choose;
                            } {
                                ~drums.value[b]; 
                            }
                        }
                    } {
                        if(~drums.value[~buffer.value].isArray) {
                            // otherwise just look up the one
                            ~drums.value[~buffer.value].choose;
                        } {
                            ~drums.value[~buffer.value];
                        }
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
            \type, \pdmtsampler, 
            \drums, drums, 
            \buffer, bufferPattern, 
            *args
        );
    }
}
