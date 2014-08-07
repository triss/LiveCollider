SP1200_CR78 : SampleMap {
    *loadSamples { |root type|
        var map = ();

        root = root ?? "/home/tris/samples/goldbaby/Goldbaby2010XmasGift/SP1200_vs_CR78/";

        (root ++ "*" ++ type ++ ".wav").pathMatch.do { |p|
            // extract drum name from path
            var name = p.split($\/).last.split($\_)[1].toLower.asSymbol;

            // load the buffer into our dictionary
            map[name] = Buffer.read(Server.default, p);
        };

        ^map;
    }
}
