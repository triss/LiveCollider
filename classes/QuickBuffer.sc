BufferPreallocator {
    classvar singleton;

	var <numBuffers;
	var <numFrames;
	var <numChannels;
	var <>server;

	var buffers;
	
    *initClass {
        StartUp.add {
            singleton = BufferPreallocator();
        }
    }

	*new { |numBuffers=8 numFrames=441000 numChannels=1 server|
		^super.newCopyArgs(numBuffers, numFrames, numChannels, server).init;
	}

	init {
		// choose the default server if none is specified
        server = server ?? Server.default;

		// initialise buffers
	    this.resetBuffers;	
    }

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

    // map class methods to singleton
    *resetBuffers { singleton.resetBuffers }
    *getBuffer    { singleton.getBuffer }
    *numBuffers   { |nb| singleton.numBuffers(nb) }
    *numFrames    { |nf| singleton.numFrames(nf) }
    *numChannels  { |nf| singleton.numChannels(nf) }
}
