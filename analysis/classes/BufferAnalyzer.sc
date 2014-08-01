BufferAnalyzer {
    *pitchCurve { |buffer|
        var c = Buffer.alloc(
            buffer.server, buffer.numFrames / buffer.server.options.blockSize
        );

        {
            RecordBuf.kr(
                Pitch.kr(
                    PlayBuf.ar(
                        1, buffer, BufRateScale.kr(buffer), doneAction:2, loop:0
                    )
                ).at(0).cpsmidi, 
            c)
        }.play; 

        ^c
    }
}
