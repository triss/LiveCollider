// filters an event pattern and plays events with a probability embedded 
// within it
Peprob {
    *new { |pattern|
        ^Pchain(
            // randomly insert rests with prob from parent pattern
            Pfunc({ |e| if(e.prob.value.coin) { e } { e.note = \ } }),
            pattern
        )
    }
}
