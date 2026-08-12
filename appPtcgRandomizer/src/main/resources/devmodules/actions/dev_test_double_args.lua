-- Dev only module used to manually verify the module config UI renders and saves every
-- DOUBLE argument constraint variety (ANY, RANGE, DISCRETE_RANGE, ENUM) correctly
local module
module = {
	id = "dev_test_double_args",
	name = "[Dev] Double Argument Test",
	description = "Exercises every double argument constraint type in the config UI",
	seeded = true,
	groups = { "dev" },
	author = "PTCGR Dev Tools",
	version = "0.1",
	requires = {
		PtcgRandomizer = "0.9.0",
	},
	arguments = {
		{
			-- ANY: plain text box - UI will enforce double input
			name = "anyDouble",
			definition = {
				type = "double",
			},
			default = 0.0,
		},
		{
			-- RANGE: single box validated against [min, max]
			name = "rangeDouble",
			definition = {
				type = "double",
				constraint = { type = "range", min = 0.0, max = 2.0 },
			},
			default = 1.0,
		},
		{
			-- DISCRETE_RANGE: dropdown prepopulated with min, min+step, ..., max
			name = "discreteDouble",
			definition = {
				type = "double",
				constraint = { type = "discrete_range", min = 0.0, max = 5.0, step = 0.5 },
			},
			default = 2.5,
		},
		{
			-- ENUM: dropdown prepopulated with the explicit allowed values
			name = "enumDouble",
			definition = {
				type = "double",
				constraint = { type = "enum", values = { 0.1, 0.25, 0.5, 1.0, 2.0 } },
			},
			default = 1.0,
		},
	},
	execute = function(context, args)
		return module.logArgs(context, args)
	end,
}

-- Log the args to show they are being passed correctly to lua
function module.logArgs(context, args)
	logger.info(string.format(
		"dev_test_double_args received anyDouble=%.3f rangeDouble=%.3f discreteDouble=%.3f enumDouble=%.3f",
		args.anyDouble, args.rangeDouble, args.discreteDouble, args.enumDouble))
end

return module
