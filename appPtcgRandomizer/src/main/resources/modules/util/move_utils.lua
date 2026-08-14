-- Shared pieces for existing-move randomizers: kind/type args, filters, group keys.
-- Require as modules.util.move_utils
local randomizer = require("randomizer")
local pool_utils = require("modules.util.pool_utils")

local move_utils = {}

function move_utils.moveKindArg()
	return {
		name = "moveKind",
		displayName = "Moves To Randomize",
		description = "'All Moves' randomizes attacks and powers together. 'Attacks' and 'Powers' keep each kind on its own slots",
		definition = {
			type = "enum",
			constraint = "MoveKind",
		},
		default = "ALL_MOVES",
	}
end

function move_utils.withinTypeArg()
	return {
		name = "withinType",
		displayName = "Within Energy Type",
		description = "When enabled moves are pooled by Energy type so, for example, a fire type card will only get moves from Fire type cards",
		definition = {
			type = "boolean",
		},
		default = false,
	}
end

function move_utils.uniqueMoves(moveList)
	return randomizer.groupBy(moveList, function(move)
		return move.name:toString()
	end):map(function(_, movesOfName)
		return movesOfName:get(1)
	end)
end

function move_utils.filterByKind(moveList, moveKind)
	if moveKind == "ATTACKS" then
		return moveList:filter("isAttack")
	elseif moveKind == "POWERS" then
		return moveList:filter("isPokePower")
	end
	return moveList
end

-- All together uses type when withinType is on. By stage uses stage, or type:stage.
function move_utils.groupKey(args)
	return function(move)
		local card = move:getSourceCard()
		if args.grouping == "BY_STAGE" then
			if args.withinType then
				return tostring(card.type) .. ":" .. tostring(card.stage)
			end
			return card.stage
		end
		return card.type
	end
end

function move_utils.stageAndMaxStageGroupKey(args)
	return function(move)
		local card = move:getSourceCard()
		local stageKey = pool_utils.stageAndMaxStageKey(card)
		if args.withinType then
			return tostring(card.type) .. ":" .. tostring(stageKey)
		end
		return stageKey
	end
end

function move_utils.buildPool(context, args)
	local movePool = move_utils.filterByKind(randomizer.list(
		pool_utils.sourceData(context, args.source):getRandomizableMoves(true, false)),
		args.moveKind)
	if args.duplicates == "REMOVE_DUPLICATES" then
		return move_utils.uniqueMoves(movePool)
	end
	return movePool
end

function move_utils.targets(context, args)
	return move_utils.filterByKind(
		randomizer.list(context.modified:getRandomizableMoves(false, false)), args.moveKind)
end

return move_utils
