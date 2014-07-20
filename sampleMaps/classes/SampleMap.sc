SampleMap {
    *createBufferCollection { |root paths|
        var buffers;

        // Iterate over paths and load samples 
        buffers = paths.collect { |p| 
            SoundFile.collectIntoBuffers(root ++ p, Server.default);
        };

        // add command to free all buffers at once
        buffers.freeAll = { |e| e.do { |bs| bs.do { |b| b.free } } };

        ^buffers;
    }
}
