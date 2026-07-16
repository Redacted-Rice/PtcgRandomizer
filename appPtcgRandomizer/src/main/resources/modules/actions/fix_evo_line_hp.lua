-- Enforces non-decreasing HP up each evolution line (by stage).
-- Run after HP randomization and set_evo_line_metadata.
local randomizer = require("randomizer")

local module
module = {
	id = "fix_evo_line_hp",
	name = "Fix Evo Line HP",
	description = "Raises HP within each evolution line so it is non decreasing by stage",
	groups = { "cards" },
	modifies = { "hp" },
	author = "Redacted Rice",
	version = "0.5",
	requires = {
		PtcgRandomizer = "0.2.0",
		set_evo_line_metadata = "0.9",
	},
	seeded = false,
	execute = function(context, args)
		return module.fixEvoLineHp(context, args)
	end,
}

function module.fixEvoLineHp(context)
	local monsterCards = context.modified:getMonsterCards()

	randomizer.groupBy(monsterCards, "evoLineId"):each(function(_, line)
		-- Sort in place so setHp mutates the real wrappers (List:sort deep-copies)
		table.sort(line.items, function(a, b)
			return a.stage:getValue() < b.stage:getValue()
		end)

		local prevHp = 0
		line:each(function(mc)
			if mc:getHp() < prevHp then
				mc:setHp(prevHp)
			end
			prevHp = mc:getHp()
		end)
	end)
end

return module
