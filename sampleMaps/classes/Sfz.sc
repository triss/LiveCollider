SfzReader {
    *initClass {
        Class.initClassTree(Event);

        Event.addEventType(
            \sfvplay, { |server|
                var nearestNotes, velocities, velocityLayers, velNotes, multiKeys;
                
                // if no \multi key in event throw an error
                if(~multi.isNil) {
                    ("The tmsampler event type must be provided with a" +
                    "sfv multisample collection under the 'multi' key").error;
                };
                
                if(~detunedFreq.value.isRest.not) {
                    velocities = ~amp.value.asArray.collect { |amp|
                        \midivelocity.asSpec.map(amp.value);
                    };

                    // work out which velocity layer we should use
                    multiKeys = ~multi.keys.asArray.sort;
                    velocityLayers = velocities.collect { |v|
                        ~multi[multiKeys[multiKeys.indexInBetween(v).floor]];
                    };

                    // flop velocityLayers and note numbers together
                    velNotes = [velocityLayers, ~detunedFreq.value.asArray.cpsmidi].flop;

                    nearestNotes = velNotes.collect { |velNote|
                        var velocityLayer, note;
                        # velocityLayer, note = velNote;

                        note.nearestInList(velocityLayer[\regions].keys.asArray.sort);
                    };

                    // load up the buffers located there
                    ~buffer = List();

                    // iterate over each of the notes
                    nearestNotes.do { |nn i| 
                        var b, id, region;
                        
                        // look up the region that needs to be played back
                        region = velNotes[i][0][\regions][nn];

                        // keep reference to current buffer such that it can be freed
                        b = region.buffer;

                        // add to list of buffers to be played
                        ~buffer.add(b); 

                        // re-cue the buffer for next time it gets played
                        region.buffer = Buffer.cueSoundFile(b.server, b.path, region.offset);
                    
                        // fork task to clean up buffer when we're done with it
                        { 
                            (
                                ~timingOffset.value.asArray.wrapAt(i) +
                                ~sustain.value.asArray.wrapAt(i) + 
                                (~r ?? 0.1).asArray.wrapAt(i) 
                            ).wait;

                            b.free; 
                        }.fork;

                        // work out if any of regions properties can be readily mapped
                        // to our \tsimpler synth
                        region.keysValuesDo { |key value|
                            switch(key, 
                                \volume, { ~amp = (~amp.value ?? 1) * value.dbamp },
                                \ampeg_release, { ~r = ~r ?? { value } }
                            );
                        };
                    };

                    // calculate rate of sample playback
                    ~rate = ~rate ?? 1 * (velNotes.flop[1] - nearestNotes).midiratio;

                    // we're going to assume all buffers have same number of channels
                    // which is more than likely with large multisample instruments
                    ~instrument = (\tsimplerbig ++ ~buffer[0].numChannels).asSymbol;
                    
                };
                // use normal note 
                ~type = \note;

                currentEnvironment.play;
            }
        );
    }

    *parse { |path|
        var data, sampleMap, region, group, groupProperties, currentNodeType;

        // at it's toplevel an SFZ contains an arbitray number of groups 
        sampleMap = IdentityDictionary(know: true);
        groupProperties = IdentityDictionary(know: true);

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

                        // copy the current group properties in to it
                        region.putAll(groupProperties);

                        // provide quick look up of the group you're in
                        region.group = group;

                        // set this paraser to updatethis region
                        currentNodeType = \region;
                    },
                    {   // if entry not region or group
                        var key, value;

                        // split lines in to key and value
                        # key, value = entry.split($\=);

                        // if note is specified store the region being built
                        // under the current group
                        if(key == "key" or: { key == "pitch_keycenter" }) {  
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
                                    value !? { 
                                        groupProperties[key.asSymbol] = value.asFloat;
                                        group[key.asSymbol] = value.asFloat;
                                    }
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

    *cue { |sfz root server|
        server = server ?? { Server.default };
        
        sfz.keysValuesDo { |lovel group| 
            group.regions.keysValuesDo { |key region|
                // look up number of channels with SoundFile
                SoundFile.use(root +/+ region.sample, { |sf|
                    // cue the buffer
                    region.buffer = Buffer.cueSoundFile(
                        server, sf.path, region.offset, sf.numChannels
                    );
                });
            };
        };
    }

    // convienace for parsing and cueing all samples
    *load { |path root server| 
        var sfz = SfzReader.parse(path);

        // if no root specified guess files are in same folder as .sfz file
        root = root ?? path.dirname;
        
        // enqueue the files
        SfzReader.cue(sfz, root, server);
        
        ^sfz
    }
}

// a helper class for Pbind'ing sfv files
Psfv : Pbind {
    *new { |multi...args|
        ^super.new(\type, \sfvplay, \multi, multi, *args)
    }
}
