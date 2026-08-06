-- Configure the change detector for PTCG Randomizer
local randomizer = require("randomizer")

local script
script = {
	id = "PtcgrChangeDetectorSetup",
	name = "Ptcgr Change Detector Setup",
	description = "Setup change detection on cards being randomized",
	when = "randomize",
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},

	monsterCardsEntry = "Monster Cards",
	maxMoves = 2,
	-- Every move field suffix tracked in buildMoveFields. Anything not listed as detail
	-- when pushMoveChangeDisplay runs gets rolled into that slot's Additional column.
	moveFieldSuffixes = {
		"name",
		"damage",
		"energyCost",
		"category",
		"description",
		"effect",
		"effect1",
		"effect2",
		"effect3",
		"animation",
		"unknownByte",
	},

	execute = function(context, args)
		return script.setupChangeDetection(context, args)
	end,
}

--- Read one field from a move slot on a card
-- getMove() hands back a fresh copy every call
function script.moveGetter(moveIndex, readMove)
	return function(obj)
		local move = obj:getMove(moveIndex)
		if not move then
			return nil
		end
		return readMove(move)
	end
end

--- Build tracked fields for one move slot (all detail by default)
function script.buildMoveFields(moveIndex)
	local moveNumber = moveIndex + 1
	local prefix = "move" .. moveNumber

	return {
		{
			field = prefix .. "_name",
			header = "Move " .. moveNumber .. " Name",
			getter = script.moveGetter(moveIndex, function(move)
				return move.name:toString()
			end),
		},
		{
			field = prefix .. "_damage",
			header = "Move " .. moveNumber .. " Dmg",
			align = "right",
			getter = script.moveGetter(moveIndex, function(move)
				return move.damage
			end),
		},
		{
			field = prefix .. "_energyCost",
			header = "Move " .. moveNumber .. " Cost",
			getter = script.moveGetter(moveIndex, function(move)
				return move:getEnergyCostString(true, "/")
			end),
		},
		{
			field = prefix .. "_category",
			header = "Move " .. moveNumber .. " Cat",
			getter = script.moveGetter(moveIndex, function(move)
				return move.category
			end),
		},
		{
			field = prefix .. "_description",
			header = "Move " .. moveNumber .. " Desc",
			getter = script.moveGetter(moveIndex, function(move)
				return move.description:toString()
			end),
		},
		{
			field = prefix .. "_effect",
			header = "Move " .. moveNumber .. " Effect Ptr",
			getter = script.moveGetter(moveIndex, function(move)
				return move:getEffectSortKey()
			end),
		},
		{
			field = prefix .. "_effect1",
			header = "Move " .. moveNumber .. " Effect 1",
			align = "right",
			getter = script.moveGetter(moveIndex, function(move)
				return move:getEffect1Byte()
			end),
		},
		{
			field = prefix .. "_effect2",
			header = "Move " .. moveNumber .. " Effect 2",
			align = "right",
			getter = script.moveGetter(moveIndex, function(move)
				return move:getEffect2Byte()
			end),
		},
		{
			field = prefix .. "_effect3",
			header = "Move " .. moveNumber .. " Effect 3",
			align = "right",
			getter = script.moveGetter(moveIndex, function(move)
				return move:getEffect3Byte()
			end),
		},
		{
			field = prefix .. "_animation",
			header = "Move " .. moveNumber .. " Anim",
			align = "right",
			getter = script.moveGetter(moveIndex, function(move)
				return move:getAnimation()
			end),
		},
		{
			field = prefix .. "_unknownByte",
			header = "Move " .. moveNumber .. " Unknown",
			align = "right",
			getter = script.moveGetter(moveIndex, function(move)
				return move:getUnknownByte()
			end),
		},
	}
end

--- Build runtime display overrides for move slots
-- @param slotConfigs array of { slot = number, detail = suffix list for full From/To columns }
function script.buildMoveSlotSummarySettings(slotConfigs)
	local overrides = {
		detail = {},
		summary = {},
		summaryGroups = {},
	}
	local detailKeys = {}
	local suffixSet = {}
	local warnedSuffixes = {}

	for _, suffix in ipairs(script.moveFieldSuffixes) do
		suffixSet[suffix] = true
	end

	for _, slotConfig in ipairs(slotConfigs or {}) do
		local prefix = "move" .. slotConfig.slot
		local detailSuffixes = slotConfig.detail or {}

		for _, suffix in ipairs(detailSuffixes) do
			if not suffixSet[suffix] then
				if not warnedSuffixes[suffix] then
					warnedSuffixes[suffix] = true
					logger.warn(
						"Change detector: unknown move field suffix '"
							.. tostring(suffix)
							.. "' (expected one of: "
							.. table.concat(script.moveFieldSuffixes, ", ")
							.. ")"
					)
				end
			else
				local fieldKey = prefix .. "_" .. suffix
				table.insert(overrides.detail, fieldKey)
				detailKeys[fieldKey] = true
			end
		end

		for _, suffix in ipairs(script.moveFieldSuffixes) do
			local fieldKey = prefix .. "_" .. suffix
			if not detailKeys[fieldKey] then
				table.insert(overrides.summary, {
					field = fieldKey,
					label = suffix,
					group = prefix,
				})
			end
		end

		table.insert(overrides.summaryGroups, {
			field = prefix .. "_additional",
			header = "Move " .. slotConfig.slot .. " Additional",
			group = prefix,
		})
	end

	return overrides
end

--- Build slot configs for both move slots using the same detail suffix list
-- @param detailSuffixes array of move field suffixes to show as full From/To columns
-- @return table slot configs for buildMoveSlotSummarySettings
function script.buildMoveSlotConfigs(detailSuffixes)
	local slotConfigs = {}
	for moveIndex = 1, script.maxMoves do
		table.insert(slotConfigs, {
			slot = moveIndex,
			detail = detailSuffixes,
		})
	end
	return slotConfigs
end

--- Push move summary layout for the next detect pass
-- Pass the move field suffixes to show as full From/To columns (e.g. { "name", "damage" }).
-- Every other tracked move field for that slot is summarized in Move N Additional.
-- @param detailSuffixes array of move field suffix strings
function script.pushMoveChangeDisplay(detailSuffixes)
	randomizer.changedetector.pushDisplaySettings(
		script.monsterCardsEntry,
		script.buildMoveSlotSummarySettings(script.buildMoveSlotConfigs(detailSuffixes))
	)
end

function script.buildMonsterCardFields()
	local fields = {
		-- Card fields
		{ field = "type", header = "Type" },
		{ field = "gfx", header = "Gfx", align = "right" },
		{ field = "rarity", header = "Rarity" },
		{ field = "set", header = "Set" },
		{ field = "pack", header = "Pack" },
		-- MonsterCard fields
		{ field = "hp", header = "HP", align = "right", getter = function(obj) return obj:getHp() end },
		{ field = "stage", header = "Stage" },
		{ field = "prevEvoName", header = "Prev Evolution" },
		{ field = "numMoves", header = "Num Moves", align = "right", getter = function(obj) return obj:getNumMoves() end },
		{ field = "retreatCost", header = "Retreat Cost", align = "right" },
		{ field = "weakness", header = "Weakness" },
		{ field = "resistance", header = "Resistance" },
		{ field = "monsterCategory", header = "Category" },
		{ field = "dexNumber", header = "Dex #", align = "right" },
		{ field = "unknownByte1", header = "Unknown 1", align = "right" },
		{ field = "level", header = "Level", align = "right" },
		{ field = "lengthFt", header = "Length Ft", align = "right" },
		{ field = "lengthIn", header = "Length In", align = "right" },
		{ field = "weight", header = "Weight", align = "right" },
		{ field = "description", header = "Description" },
		{ field = "unknownByte2", header = "Unknown 2", align = "right" },
	}

	for moveIndex = 0, script.maxMoves - 1 do
		for _, moveField in ipairs(script.buildMoveFields(moveIndex)) do
			table.insert(fields, moveField)
		end
	end

	return fields
end

function script.setupChangeDetection(context)
	local changedetector = randomizer.changedetector

    -- Respect the GUI/config toggle for whether change detection runs at all
	local isActive = context.config and context.config.changeDetectionActive or false
	changedetector.configure(isActive)

    -- Expose these on the change detector object for other scripts
	changedetector.monsterCardsEntry = script.monsterCardsEntry
	changedetector.buildMoveSlotSummarySettings = script.buildMoveSlotSummarySettings
	changedetector.buildMoveSlotConfigs = script.buildMoveSlotConfigs
	changedetector.pushMoveChangeDisplay = script.pushMoveChangeDisplay

    -- Just set it up anyways in case later someone wants to enable it for
    -- some things but not others or we want to use it for specific modules for debug
    local monsterCards = context.modified:getRandomizableMonsterCards()

    changedetector.monitor(script.monsterCardsEntry, monsterCards, {
        title = "Monster Cards",
        headerEvery = 30,
        trailingHeader = true,
        primaryKey = {
            header = "ID",
            align = "right",
            numeric = true,
            getter = function(obj)
                return obj:getIdValue()
            end,
        },
        description = {
            header = "Name",
            getter = function(obj)
                return obj.name:toString()
            end,
        },
        fields = script.buildMonsterCardFields(),
    })

    local entries = changedetector.getMonitoredEntryNames()
    if #entries > 0 then
        logger.info("Change detection configured with " .. #entries .. " monitoring entries")
        for _, entryName in ipairs(entries) do
            logger.info("  - Monitoring: " .. entryName)
        end
    end

	logger.info("Prescript changedetector_setup completed")
end

return script
