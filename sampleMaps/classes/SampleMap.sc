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

    *parseSfz { |path|
        var data, sampleMap, region, group, currentNodeType;

        // at it's toplevel an SFZ contains an arbitray number of groups 
        sampleMap = IdentityDictionary(know: true);

        // read in the file
        File.use(path, "r", { |file| data = file.readAllString });

        // create sampleMap by iterating over data from SFZ file
        data.split(Char.nl).do { |line| 
            line.split(Char.space).do { |entry|
                switch(entry,
                    "<group>", {
                        // forget any unfinished regions for now
                        region = nil;

                        // start a new group
                        group = IdentityDictionary(know: true);
                        group.regions = IdentityDictionary(know: true);
                        currentNodeType = \group;
                    }, 
                    "<region>", { 
                        // add a new region
                        region = IdentityDictionary(know: true);
                        currentNodeType = \region;
                    },
                    {   // if entry not region or group
                        var key, value;

                        // split lines in to key and value
                        # key, value = entry.split($\=);

                        // if note is specified store the region being built
                        // under the current group
                        if(key == "key") {  
                            group.regions.put(value.asInteger, region);
                        };

                        if(key == "lovel") {
                            sampleMap.put(value.asInteger, group);  
                        };

                        switch(key, 
                            "sample", { region.sample = value },
                            { 
                                if(currentNodeType == \region) {
                                    value !? { region[key.asSymbol] = value.asFloat };
                                } {
                                    value !? { group[key.asSymbol] = value.asFloat }
                                }; 
                            }
                        );
                    }
                )
            }
        }; 

        // return the sample map
        ^sampleMap;
    }

    *cueSfz { |sfz root server|
        server = server ?? { Server.default };
        
        sfz.keysValuesDo { |lovel group| 
            group.regions.keysValuesDo { |key region|
                // look up number of channels with SoundFile
                SoundFile.use(root +/+ region.sample, { |sf|
                    // cue the buffer
                    region.buffer = Buffer.cueSoundFile(
                        server, sf.path, region.offset, sf.numChannels,
                        completionMessage: { "success".postln }
                    );
                });
            };
        };
    }
}
