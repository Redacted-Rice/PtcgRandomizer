-- Shared pieces for existing-move randomizers: kind/type args, filters, group keys.
-- Require as modules.util.move_utils
local randomizer = require("randomizer")
local pool_utils = require("modules.util.pool_utils")

local move_utils = {}

-- one representative move per name. same name can appear on many cards
-- TODO later: Check if they are all the same or if they can have the same
-- name but be different (I think the latter is the case)
function move_utils.uniqueMoves(moveList)
	return randomizer.groupBy(moveList, function(move)
		return move.name:toString()
	end):map(function(_, movesOfName)
		return movesOfName:get(1)
	end)
end

-- narrow a move list to attacks, powers, or leave both
function move_utils.filterByKind(moveList, moveKind)
	if moveKind == "ATTACKS" then
		return moveList:filter("isAttack")
	elseif moveKind == "POWERS" then
		return moveList:filter("isPokePower")
	end
	return moveList
end

-- all together uses type when withinType is on. by stage uses stage, or type:stage
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

-- like groupKey but keyed by pool_utils.stageAndMaxStageKey, optionally prefixed with type
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

-- source moves filtered by moveKind before pooling or grouping
function move_utils.filteredMoves(context, args)
	local cardSource = context.original
	if args.source == "CURRENT" then
		cardSource = context.modified
	end
	return move_utils.filterByKind(randomizer.list(
		cardSource:getRandomizableMoves(true, false)), args.moveKind)
end

-- flat move pool from source, with optional unique-by-name dedup
function move_utils.buildPool(context, args)
	local movePool = move_utils.filteredMoves(context, args)
	if args.duplicates == "REMOVE_DUPLICATES" then
		return move_utils.uniqueMoves(movePool)
	end
	return movePool
end

-- group first, then uniquify inside each bucket
function move_utils.buildGroupedPool(context, args, groupKey)
	local grouped = move_utils.filteredMoves(context, args):groupBy(groupKey)
	if args.duplicates == "KEEP_DUPLICATES" then
		return grouped
	end

	local selected = {}
	local keyOrder = {}
	grouped:each(function(key, list)
		selected[key] = move_utils.uniqueMoves(list)
		table.insert(keyOrder, key)
	end)
	return randomizer.group(selected, keyOrder)
end

-- cards being randomized. uses modified set, not the ROM source pool
function move_utils.targets(context, args)
	return move_utils.filterByKind(
		randomizer.list(context.modified:getRandomizableMoves(false, false)), args.moveKind)
end

return move_utils
