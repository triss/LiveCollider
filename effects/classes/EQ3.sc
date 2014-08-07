EQ3 {
    *ar { 
        arg input, 
        lodb = -45, middb = -45, hidb = -45,
        lofreq = 80, midfreq = 800, hifreq = 1000, 
        dryWet = 1;

        var wet;
        
        wet = BLowShelf.ar(input, lofreq, 1, lodb);
        wet = BPeakEQ.ar(wet, midfreq, 1, middb);
        wet = BHiShelf.ar(wet, hifreq, 1, hidb);
        
        ^XFade2.ar(input, wet, dryWet * 2 - 1);
    }
}
