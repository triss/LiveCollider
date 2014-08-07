GBHapiXylo : SampleMap {
    *loadSamples { 
        arg root = "/home/tris/samples/goldbaby/GB_Hapi_vs_Xylophone/";

        var notePaths = (
            39: "01", 41: "02", 43: "03", 45: "04",
            47: "05", 49: "06", 51: "07", 54: "08",
            56: "09", 58: "10", 61: "11", 63: "12",
            66: "13", 68: "14", 70: "15", 73: "16",
            75: "17", 78: "18", 80: "19", 82: "20",
            85: "21", 87: "22", 90: "23", 92: "24",
            94: "25", 97: "26"
        );

        ^notePaths.collect { |n|
            Buffer.read(
                Server.default, 
                root ++ "Hapi_vs_Xylophone_0" ++ n ++ ".wav"
            );
        } 
    }
}
