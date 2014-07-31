H3500Harp : SampleMap {
    *loadSamples {
        arg root = "/home/tris/samples/goldbaby/The_FatH3500/H3500_Harp/";

        var buffers, noteNumbers;
       
        // the notes the samples in this set fall upon
        noteNumbers = [
            37, 39, 42, 44, 46, 49, 51, 
            54, 56, 58, 61, 63, 66, 68, 
            70, 73, 75, 78, 80, 82, 85
        ];
        
        // sample files are numbered 1 to 84
        buffers = (1..84).collect { |n| 
            Buffer.read(Server.default,
                root ++ 'H3500_Harp_' ++ n.asString.padLeft(3, "0") ++ '.wav'
            );
        };

        // round robin 1 of 4 samples for each note
        ^IdentityDictionary.newFrom(
            [noteNumbers, buffers.clump(4)].flop.flatten
        )
    }
}
