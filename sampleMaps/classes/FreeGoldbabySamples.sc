FreeGoldbabySamples {
    *loadDrumKit {
        arg root, filter, namePos=0, chooseRandomOrRoundRobin=\choose, server;
        
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
            var name = p.basename.split($\_)[namePos].toLower.asSymbol;

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
}
