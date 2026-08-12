-- Dev only module used to manually verify the module config UI renders and saves BOOLEAN arguments
local module
module = {
	id = "dev_test_bool_args",
	name = "[Dev] Boolean Argument Test",
	description = "Exercises boolean argument constraint type in the config UI",
	seeded = true,
	defaultSeedOffset = 42,
	groups = { "dev" },
	author = "PTCGR Dev Tools",
	version = "0.1",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			name = "anyBool",
			definition = {
				type = "boolean",
			},
			default = true,
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
