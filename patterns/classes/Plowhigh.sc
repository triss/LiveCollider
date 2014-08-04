// filter off all but lowest values
Plow {
    *new { |pattern|
        ^pattern.collect { |i| i.asArray.sort.first };
    }
}

// filter off all but highest values
Phigh {
    *new { |pattern|
        ^pattern.collect { |i| i.asArray.sort.last };
    }
}
