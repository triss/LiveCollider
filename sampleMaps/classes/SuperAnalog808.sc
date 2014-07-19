// Makes loading of 808 sample sets quick
SuperAnalog808 {
    *loadFatEMPEQ { 
        arg path="~/samples/goldbaby/SuperAnalog808_Samples/";

        var paths = (
            // Fat EMPEQ sounds
            lkick:   "BD/808BD_Long_FatEMPEQ_R*.wav",
            kicka:   "BD/808BD_FatEMPEQ_A_R*.wav",
            kickb:   "BD/808BD_FatEMPEQ_B_R*.wav",
            kickc:   "BD/808BD_FatEMPEQ_C_R*.wav",
            kickcx:  "BD/808BD_FatEMPEQ_Cx_R*.wav",
            kickd:   "BD/808BD_FatEMPEQ_D_R*.wav",

            snarea:  "SD/808SD_FatEMPEQ_A_R*.wav",
            snareb:  "SD/808SD_FatEMPEQ_B_R*.wav",
            snarec:  "SD/808SD_FatEMPEQ_C_R*.wav",
            
            toma:    "Toms/808Tom_FatEMPEQ_A_R*.wav",
            tomb:    "Toms/808Tom_FatEMPEQ_B_R*.wav",
            tomc:    "Toms/808Tom_FatEMPEQ_C_R*.wav",
            tomd:    "Toms/808Tom_FatEMPEQ_D_R*.wav",

            // Standard EMPEQ sounds for stuff that isn't availiable in Fat
            cymbal:  "Cym/808Cym_EMPEQ_R*.wav",
            hat:     "HH/808HH_EMPEQ_R*.wav",
            ohat:    "HH/808HHo_EMPEQ_R*.wav",
            shaker:  "Perc/808Shaker_EMPEQ_R*.wav",
            clave:   "Perc/808Clave_EMPEQ_R*.wav",
            cow:     "Perc/808Cow_EMPEQ_R*.wav",
            clap:    "Perc/808Clap_EMPEQ_R*.wav",
            rim:     "Perc/808Rim_EMPEQ_R*.wav",

            // Pitched sounds? - A-H types?
            congaa:  "Congas/808Conga_EMPEQ_A_R*.wav",
            congab:  "Congas/808Conga_EMPEQ_B_R*.wav",
            congac:  "Congas/808Conga_EMPEQ_C_R*.wav",
            congad:  "Congas/808Conga_EMPEQ_D_R*.wav",
            congae:  "Congas/808Conga_EMPEQ_E_R*.wav",
            congaf:  "Congas/808Conga_EMPEQ_F_R*.wav",
            congag:  "Congas/808Conga_EMPEQ_G_R*.wav",
            congah:  "Congas/808Conga_EMPEQ_H_R*.wav",

            // No EMPEQ recording of maracas we'll use HD ones
            maraca:  "Perc/HD808_Maracas_R*.wav",
        );

        // Iterate over paths and load samples 
        ^paths.collect { |p| 
            SoundFile.collectIntoBuffers(path ++ p, Server.default);
        };
    }
}
