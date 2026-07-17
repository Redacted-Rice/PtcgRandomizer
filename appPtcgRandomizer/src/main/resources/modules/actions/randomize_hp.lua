local randomizer = require("randomizer")

local module
module = {
	id = "shuffle_hp",
	name = "Shuffle HP",
	description = "Randomizes the HP of the cards",
	groups = { "cards" },
	modifies = { "hp" },
	author = "Redacted Rice",
	version = "0.9",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	execute = function(context, args)
		return module.shuffleHp(context, args)
	end,
}

-- TODO later: Add args/options and make it more distinct from hp by stage
function module.shuffleHp(context)
	-- Get all monster cards from the original and modified data
	local monsterOrig = context.original:getRandomizableMonsterCards()
	local monsterMod = context.modified:getRandomizableMonsterCards()

	-- Get hp by stage - groupFromField expects an iterable/list
	-- Use getter function since hp is private
	local healthGroups = randomizer.groupFromField(monsterOrig, "stage", "getHp")

	-- Randomize modified entities' health using the consumable pool
	-- Use setter function since hp is private
	healthGroups:useToRandomize(monsterMod, "stage", "setHp", {
		consumable = true,
	})
end

return module
