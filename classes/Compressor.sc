Compressor { 
    *ar { |input thresh=0.1 attack=0.028 release=0.032 makeup=1 slope=0.5 sidechain|
        // if no sidechain provided use input as control signal
        sidechain = sidechain ?? input;

        ^Compander.ar(input, sidechain,
            thresh: thresh,
            slopeBelow: 1,
            slopeAbove: slope,
            clampTime: attack,
            relaxTime: release
        ) * makeup;
    }
}
