-- Dev only module used to manually verify the module config UI renders and saves every
-- BOOLEAN argument constraint variety (ANY, ENUM) correctly
local module
module = {
	id = "dev_test_bool_args",
	name = "[Dev] Boolean Argument Test",
	description = "Exercises every boolean argument constraint type in the config UI",
	seeded = true,
	groups = { "dev" },
	modifies = {},
	author = "PTCGR Dev Tools",
	version = "0.1",
	requires = {
		PtcgRandomizer = "0.2.0",
	},
	arguments = {
		{
			-- ANY: dropdown showing both true and false - booleans have no free entry ANY
			-- editor, the same way integer ANY is really just the full int range
			name = "anyBool",
			definition = {
				type = "boolean",
			},
			default = true,
		},
		{
			-- ENUM: even though this restricts the allowed values to just "false", the UI
			-- still shows both true and false - a boolean choice of only one value doesn't
			-- make sense, so it isn't treated as a distinct constraint from ANY
			name = "enumBool",
			definition = {
				type = "boolean",
				constraint = { type = "enum", values = { false } },
			},
			default = false,
		},
	},
	execute = function(context, args)
		return module.logArgs(context, args)
	end,
}

-- Log the args to show they are being passed correctly to lua
function module.logArgs(context, args)
	logger.info(string.format("dev_test_bool_args received anyBool=%s enumBool=%s",
		tostring(args.anyBool), tostring(args.enumBool)))
end

return module
