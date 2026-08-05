-- Configure the change detector for PTCG Randomizer
local randomizer = require("randomizer")

local script
script = {
	id = "PtcgrChangeDetectorSetup",
	name = "Ptcgr Change Detector Setup",
	description = "Setup change detection on cards being randomized",
	when = "randomize",
	author = "Redacted Rice",
	version = "0.8",
	requires = {
		PtcgRandomizer = "0.2.0",
	},

	-- MonsterCard.MAX_NUM_MOVES on the java side
	maxMoves = 2,

	execute = function(context, args)
		return script.setupChangeDetection(context, args)
	end,
}

--- Build tracked fields for one move slot (Move 1 Name, Move 1 Damage, etc.)
-- getMove() hands back a fresh copy every call, so name/description use toString() here
-- rather than comparing raw objects that never share identity
-- @param moveIndex number 0-based move slot index passed to obj:getMove()
-- @return table array of field specs for that move slot
function script.buildMoveFields(moveIndex)
	local moveNumber = moveIndex + 1
	return {
		{
			field = "move" .. moveNumber .. "_name",
			header = "Move " .. moveNumber .. " Name",
			getter = function(obj)
				local move = obj:getMove(moveIndex)
				return move and move.name:toString() or nil
			end,
		},
		{
			field = "move" .. moveNumber .. "_category",
			header = "Move " .. moveNumber .. " Category",
			getter = function(obj)
				local move = obj:getMove(moveIndex)
				return move and move.category or nil
			end,
		},
		{
			field = "move" .. moveNumber .. "_damage",
			header = "Move " .. moveNumber .. " Damage",
			align = "right",
			getter = function(obj)
				local move = obj:getMove(moveIndex)
				return move and move.damage or nil
			end,
		},
		{
			field = "move" .. moveNumber .. "_description",
			header = "Move " .. moveNumber .. " Effect",
			getter = function(obj)
				local move = obj:getMove(moveIndex)
				return move and move.description:toString() or nil
			end,
		},
		{
			field = "move" .. moveNumber .. "_energyCost",
			header = "Move " .. moveNumber .. " Cost",
			getter = function(obj)
				local move = obj:getMove(moveIndex)
				return move and move:getEnergyCostString(true, "/") or nil
			end,
		},
	}
end

--- All tracked monster card fields: Card base fields, MonsterCard fields, evo metadata, move slots
-- @return table array of field specs for changedetector.monitor
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

    -- Just set it up anyways in case later someone wants to enable it for
    -- some things but not others or we want to use it for specific modules for debug
    local monsterCards = context.modified:getRandomizableMonsterCards()

    -- Table layout is configured here so formatting stays simple in detectChanges()
    changedetector.monitor("Monster Cards", monsterCards, {
        title = "Monster Cards",
        headerEvery = 30, -- repeat column headers every 30 data rows
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
