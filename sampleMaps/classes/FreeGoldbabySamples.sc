FreeGoldbabySamples {
    classvar <kits;

    initClass { 
        kits = (
            randomCassette808: [
                "~/samples/goldbaby/Cassette808_SamplePack/Cassette808_Samples/*.wav", 1, nil, \choose, true],
            randomBlofeld: [
                       
            ]
        )
    }

    *loadDrumMap {
        arg root, namePos=0, filter, chooseRandomOrRoundRobin=\choose, 
        chopNumbersFromNames=true, server;
        
        var paths, map;
        
        map = IdentityDictionary(know: true);

        // set default server if none specified
        server = server ?? Server.default;

        // load up paths of files
        paths = root.pathMatch;
        
        // filter them if filter specified
        filter !? { paths = paths.select { |p| p.basename.contains(filter) } };

        paths.do { |p|
            // extract drum name from path
            var name = p.basename.splitext[0].split($\_)[namePos].toLower;

            // chop numbers from names if required
            if(chopNumbersFromNames) { 
                name = name.reject { |c| c.isDecDigit } 
            };

            // load name
            name = name.asSymbol;

            // add it to our map
            map[name] = map[name] ?? List();
            map[name].add(p);
        };

        // either load all samples for round robin or just choose one
        if(chooseRandomOrRoundRobin == \choose) {
            map = map.collect { |a| Buffer.read(server, a.choose.postln) };
        } {
            map = map.collect { |a| a.collect { |p| Buffer.read(server, p) } };
        };

        map.know = true;

        ^map;
    }

    *cassette808 { |filter root|
        // default path
        var path = "~/samples/goldbaby/Cassette808_SamplePack/Cassette808_Samples/*.wav";

        // if another location is provided
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 1, filter, chopNumbersFromNames: true); 
    }

    *blofeld { |filter root|
        // default path
        var path = "~/samples/goldbaby/Blofeld_Drums_Samples/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 0, filter);
    }

    *cheetahMD16 { |filter root| 
        // default path
        var path = "~/samples/goldbaby/Cheetah_MD16/MD16_samples/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 1, filter);
    }

    *dmx606 { |filter root| 
        // default path
        var path = "~/samples/goldbaby/Cheetah_MD16/MD16_samples/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 1, filter);
    }
    
    *cr68 { |filter root| 
        // default path
        var path = "~/samples/goldbaby/Free_CR68_Wav&Rex/Samples/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 1, filter);
    }

    *nov2010 { |filter root| 
        // default path
        var path = "~/samples/goldbaby/GBfree_NOV2010/GBfree_NOV2010_Samples/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 0, filter);
    }

    *gbVsFx { |filter root|
        // default path
        var path = "~/samples/goldbaby/GBvsFX_FreePack/GBvsFX_FreePack_Samples/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 0, filter);
    }

    *sp1200 { |filter root|
        // default path
        var path = "~/samples/goldbaby/Goldbaby2010XmasGift/SP1200_Samples2010Xmas/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 0, filter);
    }

    *sp1200_cr78 { |filter root| 
        // default path
        var path = "~/samples/goldbaby/Goldbaby2010XmasGift/SP1200_vs_CR78/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 1, filter, chopNumbersFromNames: true);
    }

    *mpc60 { |filter root|
        // default path
        var path = "~/samples/goldbaby/MPC60_Free/Samples/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 0, filter);
    }

    *dp50 { |filter root|
        // default path
        var path = "~/samples/goldbaby/MPC60_Free/Samples/*.wav";

        // if another location is provided use it
        root !? { path = path +/+ "*.wav" };

        ^FreeGoldbabySamples.loadDrumMap(path, 0, filter);
    }
}
