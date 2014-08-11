SampleMap {
    *createBufferCollection { |root paths|
        var buffers;

        // Iterate over paths and load samples 
        buffers = paths.collect { |p| 
            SoundFile.collectIntoBuffers(root ++ p, Server.default);
        };

        // add command to free all buffers at once
        buffers.freeAll = { |e| e.do { |bs| bs.do { |b| b.free } } };

        ^buffers;
    }

    *loadSonatinaSfz { |path|
        var map = IdentityDictionary(know: true);

        path = path.standardizePath;

        File.use(path, "r", { |f|
            var root, sampleNote, samplePath;

            root = PathName(path).pathOnly;

            f.readAllString.split($\n).do { |line|
                var key, value;
                
                // split lines in to key and value
                # key, value = line.split($\=);

                switch(key,
                    // if sample line store away path
                    "sample", { samplePath = root +/+ value.stripWhiteSpace.replace("\\", "/").toLower },

                    // if note is specified calculate its value
                    "pitch_keycenter", { 
                        sampleNote = Note(value.stripWhiteSpace.tr($\#, $\s)) 
                    },

                    // reset every time we see a region marker
                    "<region>", { sampleNote = nil; samplePath = nil });

                // load the buffer in to the map if we have all the data we need
                if(samplePath.notNil && sampleNote.notNil) {
                    map[sampleNote] = (map[sampleNote] ?? []) ++ Buffer.read(Server.default, samplePath);
                    sampleNote = nil; samplePath = nil;
                };
            }
        });

        // return the map
        ^map;
    }
}
