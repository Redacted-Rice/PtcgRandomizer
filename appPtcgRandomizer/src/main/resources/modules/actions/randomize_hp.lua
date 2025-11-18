local randomizer = require("randomizer")

return {
	name = "shuffle_hp",
	description = "Randomizes the HP of the cards",
	group = "pokemon cards",
	modifies = { "hp" },
	author = "Redacted Rice",
	version = "0.1",
	requires = {
		UniversalRandomizerJava = "0.5.0",
		PtcgRandomizer = "0.2.0",
	},

	execute = function(context)

		-- Get all monster cards from the original and modified data
		local monsterOrig = context.original:getMonsterCards()
		local monsterMod = context.modified:getMonsterCards()

		-- Get hp by stage - groupFromField expects an iterable/list
		-- Use getter function since hp is private
		local healthGroups = randomizer.groupFromField(monsterOrig, "stage", "getHp")

		-- Randomize modified entities' health using the consumable pool
		-- Use setter function since hp is private
		healthGroups:useToRandomize(monsterMod, "stage", "setHp", {
			consumable = true,
		})
	end,
}
