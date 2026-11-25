-- Shared pieces for existing-move randomizers: kind/type args, filters, group keys.
-- Require as modules.util.move_utils
local randomizer = require("randomizer")
local common_field_defs = require("modules.util.common_field_defs")
local pool_utils = require("modules.util.pool_utils")

local move_utils = {}

-- one representative move per name. same name can appear on many cards
-- TODO later: Check if they are all the same or if they can have the same
-- name but be different (I think the latter is the case)
function move_utils.uniqueMoves(moveList)
	return randomizer.groupBy(moveList, function(move)
		return move.name:toString()
	end):mapToList(function(_, movesOfName)
		return movesOfName:get(1)
	end)
end

-- narrow a move list to attacks, powers, or leave both
function move_utils.filterByKind(moveList, moveKind)
	if moveKind == common_field_defs.MOVE_KIND_ATTACKS then
		return moveList:filter("isAttack")
	elseif moveKind == common_field_defs.MOVE_KIND_POWERS then
		return moveList:filter("isPokePower")
	end
	return moveList
end

-- Combines the type/subtype and stage keys as appropriate for the args
function move_utils.groupKey(args)
	return function(move)
		local card = move:getSourceCard()
		if args.grouping == common_field_defs.STAGE_GROUPING_BY_STAGE then
			return pool_utils.combineKeys(pool_utils.getTypeGroupingKey(card, args.withinType),
					card.stage)
		end
		return pool_utils.getTypeGroupingKey(card, args.withinType)
	end
end

-- like groupKey but keyed by pool_utils.stageAndMaxStageKey, optionally prefixed with type/subtype
function move_utils.stageAndMaxStageGroupKey(args)
	return function(move)
		local card = move:getSourceCard()
		return pool_utils.combineKeys(pool_utils.getTypeGroupingKey(card, args.withinType),
				pool_utils.stageAndMaxStageKey(card))
	end
end

-- source moves filtered by moveKind before pooling or grouping
function move_utils.filteredMoves(args)
	local cardSource = context.original.cards
	if args.source == common_field_defs.DATA_SOURCE_CURRENT then
		cardSource = context.modified.cards
	end
	return move_utils.filterByKind(randomizer.list(
		cardSource:getRandomizableMoves(true, false)), args.moveKind)
end

-- flat move pool from source, with optional unique-by-name dedup
function move_utils.buildPool(args)
	local movePool = move_utils.filteredMoves(args)
	if args.duplicates == common_field_defs.DUPLICATE_HANDLING_REMOVE then
		return move_utils.uniqueMoves(movePool)
	end
	return movePool
end

-- group first, then uniquify inside each bucket
function move_utils.buildGroupedPool(args, groupKey)
	local grouped = move_utils.filteredMoves(args):groupBy(groupKey)
	if args.duplicates == common_field_defs.DUPLICATE_HANDLING_KEEP then
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
function move_utils.targets(args)
	return move_utils.filterByKind(
		randomizer.list(context.modified.cards:getRandomizableMoves(false, false)), args.moveKind)
end

function move_utils.clearTempMoveRandTracking(moveTargets)
	-- We just always set this up for simplicity so we don't have to track
	-- the args and if we are using it or not
	moveTargets:each(function(moveTarget)
		local card = moveTarget:getSourceCard()
		card._tempMoveRandTracking = {}
	end)
end

function move_utils.assignChosenMove(moveTarget, chosenMove)
	if chosenMove == nil then
		return
	end
	local card = moveTarget:getSourceCard()
	local moveIndex = moveTarget:getSourceMoveIndex()
	card:setMove(chosenMove, moveIndex)
	-- We setup the _randomizerMoves values before starting so
	-- they already won't be nil and be tables
	card._tempMoveRandTracking[moveIndex] = true
end

-- whether this slot is in scope for the current moveKind filter
function move_utils.slotRandomizedThisRun(move, moveKind)
	if moveKind == common_field_defs.MOVE_KIND_ATTACKS then
		return move:isAttack()
	end
	if moveKind == common_field_defs.MOVE_KIND_POWERS then
		return move:isPokePower()
	end
	return true
end

-- tracked slots always compared. untracked slots of a different kind are compared too
function move_utils.shouldCompareMoveSlot(card, slotIndex, moveKind)
	if card._tempMoveRandTracking[slotIndex] then
		return true
	end
	local otherMove = card:getMove(slotIndex)
	if otherMove == nil or otherMove:isEmpty() then
		return false
	end
	return not move_utils.slotRandomizedThisRun(otherMove, moveKind)
end

function move_utils.constraintUniqueMoveOnCard(moveTarget, candidateMove, moveKind)
	local card = moveTarget:getSourceCard()
	local moveIndex = moveTarget:getSourceMoveIndex()
	local candidateName = candidateMove.name:toString()
	local maxSlots = context.MonsterCard.MAX_NUM_MOVES
	-- Check each slot that we have already assigned or that will not be assigned this run
	-- to see if we already have that name
	for slotIndex = 0, maxSlots - 1 do
		if slotIndex ~= moveIndex and move_utils.shouldCompareMoveSlot(card, slotIndex, moveKind) then
			local otherMove = card:getMove(slotIndex)
			if otherMove ~= nil and not otherMove:isEmpty()
					and otherMove.name:toString() == candidateName then
				return false
			end
		end
	end
	return true
end

function move_utils.constraintFnForArgs(args)
	if args.noDuplicateMoveNames == false then
		return nil
	end
	return function(moveTarget, candidateMove)
		return move_utils.constraintUniqueMoveOnCard(moveTarget, candidateMove, args.moveKind)
	end
end

return move_utils
