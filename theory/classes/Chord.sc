Chord {
    classvar chords;

    *initClass {
        chords = (
            major:          #[0, 4, 7],
            minor:          #[0, 3, 7],
            major7:         #[0, 4, 7, 11],
            dom7:           #[0, 4, 7, 10],
            minor7:         #[0, 3, 7, 10],
            aug:            #[0, 4, 8],
            dim:            #[0, 3, 6],
            dim7:           #[0, 3, 6, 9],
            '1':            #[0],
            '5':            #[0, 7],
            plus:           #[0, 4, 8],
            sharp5:         #[0, 4, 8],
            msharp5:        #[0, 3, 8],
            sus2:           #[0, 2, 7],
            sus4:           #[0, 5, 7],
            '6':            #[0, 4, 7, 9],
            m6:             #[0, 3, 7, 9],
            '7sus2':        #[0, 2, 7, 10],
            '7sus4':        #[0, 5, 7, 10],
            '7flat5':       #[0, 4, 6, 10],
            m7flat5:        #[0, 3, 6, 10],
            '7sharp5':      #[0, 4, 8, 10],
            m7sharp5:       #[0, 3, 8, 10],
            '9':            #[0, 4, 7, 10, 14],
            m9:             #[0, 3, 7, 10, 14],
            m7sharp9:       #[0, 3, 7, 10, 14],
            maj9:           #[0, 4, 7, 11, 14],
            '9sus4':        #[0, 5, 7, 10, 14],
            '6by9':         #[0, 4, 7, 9, 14],
            m6by9:          #[0, 3, 9, 7, 14],
            '7flat9':       #[0, 4, 7, 10, 13],
            m7flat9:        #[0, 3, 7, 10, 13],
            '7flat10':      #[0, 4, 7, 10, 15],
            '9sharp5':      #[0, 1, 13],
            m9sharp5:       #[0, 1, 14],
            '7sharp5flat9': #[0, 4, 8, 10, 13],
            m7sharp5flat9:  #[0, 3, 8, 10, 13],
            '11':           #[0, 4, 7, 10, 14, 17],
            m11:            #[0, 3, 7, 10, 14, 17],
            maj11:          #[0, 4, 7, 11, 14, 17],
            '11sharp':      #[0, 4, 7, 10, 14, 18],
            m11sharp:       #[0, 3, 7, 10, 14, 18],
            '13':           #[0, 4, 7, 10, 14, 17, 21],
            m13:            #[0, 3, 7, 10, 14, 17, 21]
        );

        chords.m = chords.minor;
        chords[\M] = chords.major;
        chords[\7] = chords.dom7;
        chords[\M7] = chords.major7;
        chords[\m7] = chords.minor7;
        chords.augmented = chords.aug;
        chords.diminished = chords.dim;
        chords.diminished7 = chords.dim7
    }

    *invert { |chord|
        var c = chord.copy;
        c[c.maxIndex] = -12 + c.maxItem; 
        ^c.rotate(1);
    }

    *progression { |array| 
        ^array.collect { |c| this.fromName(c) };
    }

    *fromName { |c|
        ^if(c.isKindOf(Symbol)) {
            var over, chord;

            c = c.asString;

            // lop off the inversion if specified
            if(c.contains("_")) {
                #c, over = c.split($\_);
                over = Note(over);
            };

            // if we know the chord name return it
            chord = if(chords.includesKey(c)) {
                chords[c];
            } {
                var shape, root, noteNameLength = 1;

                // parse chord name out of string
                shape = chords[c.drop(1).asSymbol] 
                    ?? { noteNameLength = 2; chords[c.drop(2).asSymbol] }
                    ?? { noteNameLength = 3; chords[c.drop(3).asSymbol] }
                    ?? { chords.major };

                // use the remainder of the string as the root note
                root = Note(c.keep(noteNameLength));

                // if an inversion was specified
                if(over.notNil) {
                    var octaveShift = 0;
                    if(over < root) { octaveShift = 12 };

                    shape = shape.collect { |note| 
                        if(note < (over - root + octaveShift)) {
                            note + 12
                        } {
                            note
                        }
                    };

                    shape = shape - octaveShift;
                };

                root + shape;
            };

            chord.sort; 
        } {
            c
        }
    }

    *new { |c|
        ^this.fromName(c);
    }
}

Note {
    classvar notes;

    *initClass {
        // define note names
        notes = (c: 0, d: 2, e: 4, f: 5, g: 7, a: 9, b: 11);

        // bung in all the sharps and flats
        notes.keysValuesDo { |name val| 
            notes[(name ++ \s).asSymbol] = val + 1;
            notes[(name ++ \b).asSymbol] = val - 1;
        };
    }

    *new { |name|
        var octaveShift = 0;
        name = name.asString.toLower;

        // if octave specified chop it off and use it
        if(name.last.isDecDigit) {
            octaveShift = name.last.digit * 12 + 12; 
            name = name.drop(-1);
        };

        ^notes[name.asSymbol] + octaveShift;
    }

    *noteName { |n| ^notes.findKeyForValue(n % 12) }
}
