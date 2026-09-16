-- Signed byte helpers for ROM fields stored as Java bytes.
local conversion_utils = {}

function conversion_utils.byteToUnsigned(value)
	if value < 0 then
		return value + 256
	end
	return value
end

function conversion_utils.byteToSigned(unsigned)
	if unsigned >= 256 then
		error("byte out of range: " .. tostring(unsigned))
	end
	if unsigned >= 128 then
		return unsigned - 256
	end
	return unsigned
end

return conversion_utils
