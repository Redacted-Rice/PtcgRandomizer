-- Dev only module used to manually verify the module config UI renders and saves every
-- STRING argument constraint variety (ANY, ENUM) correctly
local module
module = {
	id = "dev_test_string_args",
	name = "[Dev] String Argument Test",
	description = "Exercises every string argument constraint type in the config UI",
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
			-- ANY: free entry text box, no restrictions on content
			name = "anyString",
			definition = {
				type = "string",
			},
			default = "hello",
		},
		{
			-- ENUM: dropdown prepopulated with the explicit allowed values
			name = "enumString",
			definition = {
				type = "string",
				constraint = { type = "enum", values = { "red", "green", "blue" } },
			},
			default = "green",
		},
	},
	execute = function(context, args)
		return module.logArgs(context, args)
	end,
}

-- Log the args to show they are being passed correctly to lua
function module.logArgs(context, args)
	logger.info(string.format("dev_test_string_args received anyString=%s enumString=%s",
		args.anyString, args.enumString))
end

return module
