package redactedrice.ptcgr.constants;

/**
 * Whether duplicate values are kept when building a randomization pool. REMOVE_DUPLICATES leaves
 * one of each distinct value (equal weight), KEEP_DUPLICATES preserves source multiplicity
 * (weighted by frequency).
 */
public enum DuplicateHandling {
    REMOVE_DUPLICATES, KEEP_DUPLICATES
}
