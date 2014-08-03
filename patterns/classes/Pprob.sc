// filters an event pattern and plays events with a probability embedded 
// within it
Pprob {
    *new { |pattern|
        Pchain(
            // randomly insert rests with prob from parent pattern
            Pfunc({ |e| if(e.prob.coin) { e } { e.note = \ } }),
            pattern
        )
    }
}
