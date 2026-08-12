-- Dev only module used to manually verify the module config UI renders and saves the ENUM
-- base type correctly, pulling its choices from a registered enum instead of an inline
-- values list (see dev_test_string_args.lua / dev_test_int_args.lua for that). Covers both
-- ways an enum can end up in the registry: a module registering its own via onLoad (color),
-- and a Java defined enum registered once at startup (cardType, see
-- RandomizerCore.setupLuaRandomizer() and CardDataConstants.CardType).
local module
module = {
	id = "dev_test_enum_args",
	name = "[Dev] Enum Argument Test",
	description = "Exercises the enum base type, with values pulled from a registered enum",
	seeded = true,
	groups = { "dev" },
	author = "PTCGR Dev Tools",
	version = "0.1",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			-- ENUM base type: dropdown prepopulated from the "DevTestColor" enum registered
			-- below in onLoad, rather than an inline values list
			name = "color",
			definition = {
				type = "enum",
				constraint = "DevTestColor",
			},
			default = "RED",
		},
		{
			-- ENUM base type backed by a Java defined enum (PTCGR's CardType) instead of one
			-- registered from Lua. Values are the enum constant names (e.g. "MONSTER_FIRE").
			name = "cardType",
			definition = {
				type = "enum",
				constraint = "CardType",
			},
			default = "MONSTER_FIRE",
		},
	},
	onLoad = function(context)
		context.registerEnum("DevTestColor", {
			"RED",
			"GREEN",
			"BLUE",
		})
	end,
	execute = function(context, args)
		return module.logArgs(context, args)
	end,
}

-- Log the args to show they are being passed correctly to lua
function module.logArgs(context, args)
	logger.info(string.format("dev_test_enum_args received color=%s cardType=%s", args.color,
		args.cardType))
end

return module
