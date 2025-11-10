local randomizer = require("randomizer")

return {
	name = "shuffle_hp",
	description = "Randomizes the HP of the cards",
	group = "pokemon cards",
	modifies = { "hp" },

	execute = function(context)

		-- Get all monster cards from the original and modified data
		local monsterOrig = context.original:getMonsterCards()
		local monsterMod = context.modified:getMonsterCards()

		-- Get hp by stage - groupFromField expects an iterable/list
		local healthGroups = randomizer.groupFromField(monsterOrig, "stage", "hp")

		-- Randomize modified entities' health using the consumable pool
		healthGroups:useToRandomize(monsterMod, "stage", "hp", {
			consumable = true,
		})
	end,
}
