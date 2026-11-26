local randomizer = require("randomizer")
local common_field_defs = require("modules.util.common_field_defs")
local enums_def = require("modules.util.enums_def")
local pool_utils = require("modules.util.pool_utils")
local move_utils = require("modules.util.move_utils")

local module
module = {
	id = "moves_together_and_stage",
	name = "Randomize Existing Moves",
	description = "Randomizes existing attacks and/or powers, either from one shared pool or grouped by evolution"
					.. " stage. Keeps the same number of moves per card",
	groups = { "Monsters", "Moves", "Attacks", "Powers" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		common_field_defs.ARG_DEF_SOURCE,
		common_field_defs.ARG_DEF_DUPLICATES,
		common_field_defs.ARG_DEF_RANDOMIZATION_APPROACH,
		common_field_defs.ARG_DEF_MOVE_KIND,
		common_field_defs.ARG_DEF_WITHIN_TYPE,
		common_field_defs.ARG_DEF_STAGE_GROUPING_ALL_TOGETHER,
		common_field_defs.ARG_DEF_NO_DUPLICATE_MOVE_NAMES,
	},
	execute = function(args)
		return module.randomizeMoves(args)
	end,
}

function module.randomizeMoves(args)
	if args.moveKind == enums_def.MOVE_KIND_POWERS then
		randomizer.changedetector.pushMoveChangeDisplay({ "name" })
	else
		randomizer.changedetector.pushMoveChangeDisplay({ "name", "energyCost", "damage" })
	end

	local moveTargets = move_utils.targets(args)
	move_utils.clearTempMoveRandTracking(moveTargets)
	local options = pool_utils.poolOptionsForCardSource(args)
	options.constraintFn = move_utils.constraintFnForArgs(args)

	if pool_utils.stageGroupingSplitsIntoPools(args.grouping)
			or pool_utils.typeGroupingSplitsIntoPools(args.withinType) then
		local key = move_utils.groupKey(args)
		move_utils.buildGroupedPool(args, key):useToRandomize(moveTargets, key,
			move_utils.assignChosenMove, options)
	else
		move_utils.buildPool(args):useToRandomize(moveTargets, move_utils.assignChosenMove,
			options)
	end
end

return module
