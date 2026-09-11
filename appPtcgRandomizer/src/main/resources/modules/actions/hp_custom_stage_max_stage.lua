local common_field_defs = require("modules.util.common_field_defs")
local custom_pool_utils = require("modules.util.custom_pool_utils")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "hp_custom_stage_max_stage",
	name = "Randomize HP (Custom, By Stage + Max Stage)",
	description = "Randomizes HP using custom values keyed by evo line max stage then card stage",
	groups = { "Monsters", "HP" },
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
			name = "hpPools",
			displayName = "HP Pools by Max Stage then Stage",
			description = "Weighted HP values keyed by the evolution line's max stage then card's evolution stage. When randomizing it will pick the pool that matches the current cards max stage and stage to pull a value from.",
			definition = {
				type = "table",
				keyDefinition = common_field_defs.KEY_DEF_EVO_LINE_STAGES,
				valueDefinition = {
					type = "table",
					keyDefinition = common_field_defs.KEY_DEF_EVO_STAGE,
					valueDefinition = {
						type = "list",
						elementDefinition = common_field_defs.ELEMENT_DEF_HP_LIST,
					},
				},
			},
			default = {
				BASIC = {
					BASIC = { 40, 50, 50, 60, 60, 70, 70, 80, 90, 100, 120 },
				},
				STAGE_1 = {
					BASIC = { 30, 40, 40, 50, 50, 60, 70 },
					STAGE_1 = { 60, 70, 80, 80, 90, 90, 100, 120 },
				},
				STAGE_2 = {
					BASIC = { 30, 40, 40, 50, 50, 60 },
					STAGE_1 = { 50, 60, 60, 70, 70, 80, 90 },
					STAGE_2 = { 90, 90, 100, 100, 110, 120 },
				},
			},
		},
	},
	execute = function(context, args)
		return module.randomizeHp(context, args)
	end,
}

function module.randomizeHp(context, args)
	local targets = context.modified:getRandomizableMonsterCards()
	local options = pool_utils.poolOptions(args.approach)
	custom_pool_utils.hp.buildStageMaxStagePoolGroup(context, args.hpPools, targets):useToRandomize(
			targets, pool_utils.stageAndMaxStageKey, "hp", options)
end

return module
