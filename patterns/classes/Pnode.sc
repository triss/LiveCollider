Pbindnode : PbindProxy {
    var setupAction;

    *new { |node...args|
        ^super.new(\type, \set, *args).initPnode(node, args);
    }

    initPnode { |node args|
        // throw an error if node isn't something we can control
        if(node.respondsTo(\asNodeID).not and: { node.isKindOf(Symbol).not }) {
            "node must be a Synth, Group, Node, or NodeProxy".error
            ^this;
        };

        if(node.isKindOf(Symbol)) {
            // if a synthDef name was specified instead of a node arrange to 
            // create a synth when this pattern is played
            setupAction = {
                // get starting settings
                var initialSettings = 
                    Pbind(*args).asStream.next(()).trig_(0).gate_(0).getPairs;

                // create the synth
                node = Synth(node, initialSettings);

                // map this pattern to the synth
                this.set(\id, node.asNodeID);
            };
        } {
            // otherwise just set the id this pattern should work on
            this.set(\id, node.asNodeID);
        }
    }

    play { |clock protoEvent quant|
        // create synth if neccesary
        { setupAction.() }.fork(clock, quant);

        // play just like any other pattern
        ^super.play(clock, protoEvent, quant)
    }
}
