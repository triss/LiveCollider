ValveDR220e : SampleMap {
    classvar sampleNames;

    *initClass {
        sampleNames = (
            kick:   "BD_DR220e",
            clap:   "Clap_DR220e",
            cow:    "Cow_DR220e",
            cymbal: "Cym_DR220e",
            hat:    "HH_DR220e",
            ohat:   "HHo_DR220e",
            ride:   "Ride_DR220e",
            snare:  "SD_DR220e",
            toma:   "Tom1_DR220e",
            tomb:   "Tom2_DR220e",
            tomc:   "Tom3_DR220e"
        );
    }

    *loadWithSuffix { |root suffix|
        var paths;

        root = root ?? "/home/tris/samples/goldbaby/ValveDR220e_Samples/";

        paths = sampleNames.collect { |p| root ++ p ++ suffix }

        ^paths.collect { |p| Buffer.read(Server.default, p) }
    }

    *loadClean { |root|
        ^this.loadWithSuffix(root, ".wav");
    }

    *loadValve { |root|
        ^this.loadWithSuffix(root, "Valve.wav");
    }

    *loadXValve { |root|
        ^this.loadWithSuffix(root, "X_Valve.wav");
    }

    *loadX { |root|
        ^this.loadWithSuffix(root, "X.wav");
    }
}
