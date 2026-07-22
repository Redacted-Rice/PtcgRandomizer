-- Dev only module used to manually verify the module config UI renders and saves every
-- INTEGER argument constraint variety (ANY, RANGE, DISCRETE_RANGE, ENUM) correctly
local module
module = {
	id = "dev_test_int_args",
	name = "[Dev] Integer Argument Test",
	description = "Exercises every integer argument constraint type in the config UI",
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
			-- ANY: plain text box - UI will enforce int input within INT_MIN and INT_MAX
			name = "anyInt",
			definition = {
				type = "integer",
			},
			default = 0,
		},
		{
			-- RANGE: single box validated against [min, max]
			name = "rangeInt",
			definition = {
				type = "integer",
				constraint = { type = "range", min = 0, max = 10 },
			},
			default = 5,
		},
		{
			-- DISCRETE_RANGE: dropdown prepopulated with min, min+step, ..., max
			name = "discreteInt",
			definition = {
				type = "integer",
				constraint = { type = "discrete_range", min = 0, max = 100, step = 5 },
			},
			default = 25,
		},
		{
			-- ENUM: dropdown prepopulated with the explicit allowed values
			name = "enumInt",
			definition = {
				type = "integer",
				constraint = { type = "enum", values = { 1, 2, 3, 5, 8, 13 } },
			},
			default = 3,
		},
	},
	execute = function(context, args)
		return module.logArgs(context, args)
	end,
}

-- Log the args to show they are being passed correctly to lua
function module.logArgs(context, args)
	logger.info(string.format(
		"dev_test_int_args received anyInt=%d rangeInt=%d discreteInt=%d enumInt=%d",
		args.anyInt, args.rangeInt, args.discreteInt, args.enumInt))
end

return module
