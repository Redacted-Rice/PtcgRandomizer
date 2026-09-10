local common_field_defs = require("modules.util.common_field_defs")
local custom_pool_utils = require("modules.util.custom_pool_utils")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "retreat_cost_custom_stage_max_stage",
	name = "Randomize Retreat Cost (Custom, By Stage + Max Stage)",
	description = "Randomizes retreat cost using custom values keyed by evo line max stage then card stage",
	groups = { "Monsters", "Retreat Cost" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	needs = {
		{ name = "evoLineMaxStage", type = "EvolutionStage" },
	},
	arguments = {
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		{
			name = "retreatPools",
			displayName = "Retreat Cost Pools by Max Stage then Stage",
			description = "Weighted retreat cost values keyed by the evolution line's max stage then card's evolution stage. When randomizing it will pick the pool that matches the current cards max stage and stage to pull a value from.",
			definition = {
				type = "table",
				keyDefinition = common_field_defs.KEY_DEF_EVO_LINE_STAGES,
				valueDefinition = {
					type = "table",
					keyDefinition = common_field_defs.KEY_DEF_EVO_STAGE,
					valueDefinition = {
						type = "list",
						elementDefinition = common_field_defs.ELEMENT_DEF_RETREAT_COST_LIST,
					},
				},
			},
			default = {
				BASIC = {
					BASIC = { 0, 1, 1, 2, 2, 2, 2, 3, 3 },
				},
				STAGE_1 = {
					BASIC = { 0, 0, 1, 1, 1, 1, 2 },
					STAGE_1 = { 0, 1, 1, 1, 1, 2, 2, 3 },
				},
				STAGE_2 = {
					BASIC = { 0, 0, 1, 1, 1, 1},
					STAGE_1 = { 0, 1, 1, 1, 1, 2, 2 },
					STAGE_2 = { 1, 2, 2, 3, 3, 3, 3 },
				},
			},
		},
	},
	execute = function(context, args)
		return module.randomizeRetreatCost(context, args)
	end,
}

function module.randomizeRetreatCost(context, args)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)
	custom_pool_utils.retreat.buildStageMaxStagePoolGroup(context, args.retreatPools, targets):
			useToRandomize(targets, pool_utils.stageAndMaxStageKey, "retreatCost", options)
end

return module
