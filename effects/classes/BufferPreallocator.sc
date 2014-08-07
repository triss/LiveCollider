BufferPreallocator {
    classvar singleton;

	var <numBuffers;
	var <numFrames;
	var <numChannels;
	var <>server;

	var buffers;
	
    *initClass {
        StartUp.add {
            Server.default.onBootAdd({
                "Preallocating buffers for Buffer Preallocator".postln;
                singleton = BufferPreallocator();
            });
        }
    }

	*new { |numBuffers=8 numFrames=441000 numChannels=2 server|
		^super.newCopyArgs(numBuffers, numFrames, numChannels, server).init;
	}

	init {
		// choose the default server if none is specified
        server = server ?? Server.default;

		// initialise buffers
	    this.resetBuffers;	
    }

    // free all buffers and reinitialise them
	resetBuffers {
        buffers.do { |b| b.free };
        
        // initialise buffers
		buffers = Buffer.allocConsecutive(
			numBuffers, server, numFrames, numChannels
		);
    }

    // returns a buffer
	getBuffer {
		// enqueue a new buffer
		buffers = buffers.add(Buffer.alloc(server, numFrames, numChannels));

		// dequeue a buffer
		^buffers.removeAt(0);
	}

    numBuffers_ { |nb=8|
        numBuffers = nb;
        this.resetBuffers;
    }

    numFrames_ { |nf=441000|
        numFrames = nf;
        this.resetBuffers;
    }

    numChannels_ { |nc=2|
        numChannels = nc;
        this.resetBuffers;
    }

    doOnServerBoot {
        
    }

    // map class methods to singleton
    *resetBuffers { ^singleton.resetBuffers }
    *getBuffer    { ^singleton.getBuffer }
    *numBuffers   { ^singleton.numBuffers }
    *numFrames    { ^singleton.numFrames }
    *numChannels  { ^singleton.numChannel }
    *numBuffers_  { |nb| ^singleton.numBuffers(nb) }
    *numFrames_   { |nf| ^singleton.numFrames(nf) }
    *numChannels_ { |nf| ^singleton.numChannels(nf) }
}
