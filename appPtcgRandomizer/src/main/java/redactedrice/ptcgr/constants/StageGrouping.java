package redactedrice.ptcgr.constants;

/**
 * How source values are partitioned into randomization pools. ALL_TOGETHER uses one shared pool,
 * BY_STAGE splits by evolution stage, and BY_STAGE_AND_MAX_STAGE splits by stage plus the evolution
 * line's max stage.
 */
public enum StageGrouping {
    ALL_TOGETHER, BY_STAGE, BY_STAGE_AND_MAX_STAGE
}
