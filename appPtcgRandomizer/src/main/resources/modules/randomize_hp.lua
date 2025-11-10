local randomizer = require("randomizer")

return {
	name = "shuffle_hp",
	description = "Randomizes the HP of the cards",
	group = "pokemon cards",
	modifies = { "hp" },

	execute = function(context)

		-- TODO: Supply this in the context
		local monsterOrig = randomizer.list(context.original.cards).filter(function(card)
			return card.cardType.isMonsterCard()
		end)
		local monsterMod = randomizer.list(context.modified.cards).filter(function(card)
			return card.cardType.isMonsterCard()
		end)

		-- Get hp by stage
		local healthGroups = randomizer.groupFromField(monsterOrig, "getEvoStage", "getHealth")

		-- Randomize modified entities' health using the consumable pool
		local setter = function(entity, value)
			entity:setHealth(value)
		end
		randomizer.randomize(monsterMod, healthGroups, "hp", {
			consumable = true,
		})
	end,
}
