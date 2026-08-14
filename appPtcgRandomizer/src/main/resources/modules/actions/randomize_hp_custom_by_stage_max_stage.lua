local hp_custom_utils = require("modules.util.hp_custom_utils")
local pool_utils = require("modules.util.pool_utils")

local module
module = {
	id = "randomize_hp_custom_by_stage_max_stage",
	name = "Randomize HP with Custom Values (By Stage and Max Stage)",
	description = "Randomizes HP using custom pools keyed by evo-line max stage then card stage",
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
		hp_custom_utils.approachArg(),
		{
			-- Outer key = evo line max stage, inner key = card stage, value = weighted HP list
			name = "hpPools",
			displayName = "HP Pools by Max Stage then Stage",
			description = "Weighted HP values keyed by the evolution line's max stage then card's evolution stage. When randomizing it will pick the pool that matches the current cards max stage and stage to pull a value from.",
			definition = {
				type = "table",
				keyDefinition = hp_custom_utils.evoLineStagesKeyDef(),
				valueDefinition = {
					type = "table",
					keyDefinition = hp_custom_utils.evoStageKeyDef(),
					valueDefinition = {
						type = "list",
						elementDefinition = hp_custom_utils.HP_LIST_ELEMENT,
					},
				},
			},
			default = {
				BASIC = {
					BASIC = { 50, 50, 60, 60, 60, 70, 70, 70, 80, 90, 100, 120 },
				},
				STAGE_1 = {
					BASIC = { 30, 40, 40, 50, 50, 60, 70, 80 },
					STAGE_1 = { 60, 70, 70, 80, 80, 90, 100 },
				},
				STAGE_2 = {
					BASIC = { 30, 30, 40, 40, 50, 50, 60 },
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
	hp_custom_utils.buildStageMaxStagePoolGroup(context, args.hpPools):useToRandomize(targets,
		pool_utils.stageAndMaxStageKey, "setHp", options)
end

return module
