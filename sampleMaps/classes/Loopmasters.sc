RandomNuTech {
    *new { |path="/home/tris/samples/LOOPMASTERS/NU Tech/Nu Tech DRUM oneshots"|
        var drumMap, paths = (
            boom: "Nu Tech Grooves BDs/Nu Tech Grooves booms",
            kick: "Nu Tech Grooves BDs/Nu Tech Grooves room bds",
            clap: "Nu Tech Grooves CLPs",
            tom: "Nu Tech Grooves CONGAs - TOMs",
            hat: "Nu Tech Grooves HATs",
            perc: "Nu Tech Grooves PERCs",
            snare: "Nu Tech Grooves SDs"
        );

        drumMap = paths.collect { |p| 
            Buffer.read(
                Server.default, (path +/+ p +/+ "*.wav").pathMatch.choose
            ) 
        };

        drumMap.freeAll = { |e| e.do { |b| b.free } };

        ^drumMap;
    }
}

LoopmastersSamples {
    *loadMulti { |path| 
        var multi = ();

        // iterate over all the sample paths in path
        path.pathMatch.do { |p|
            var noteNumber;
        
            // Loopmasters write note names for sample just before .wav and
            // after the last _
            // - This line grabs the file name with out directory with .baseName
            // - splits off the extension and throws it away with .splitext.first
            // - splits the sample name by underscore and takes the last chunk
            //   with .split($\_).last
            // - and switches # for s (NoteSymbol uses s to denote sharp)
            // - uses NoteSymbol to look up which midi note this sample should
            //   be assigned to
            noteNumber = NoteSymbol(p.basename.splitext.first.split($\_).last.tr($\#, $s));

            // load the sample in to the appropiate midi sample slot
            multi[noteNumber] = Buffer.read(Server.default, p);
        };

        ^multi;
    }
}
