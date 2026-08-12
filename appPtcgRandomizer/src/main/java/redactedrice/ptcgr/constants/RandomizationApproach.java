package redactedrice.ptcgr.constants;

/**
 * How values are drawn from a randomization pool. Maps to URC useToRandomize options: FULLY_RANDOM
 * leaves the pool intact and picks with replacement MINIMIZE_REPEATS consumes values and
 * regenerates the pool when it empties.
 */
public enum RandomizationApproach {
    FULLY_RANDOM, MINIMIZE_REPEATS
}
