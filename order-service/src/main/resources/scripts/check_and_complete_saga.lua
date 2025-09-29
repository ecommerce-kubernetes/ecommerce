local status = redis.call('HGET', KEYS[1], 'status')

if status ~= 'PENDING' then
    return -1
end

redis.call('HSET', KEYS[1], ARGV[1], ARGV[2])
redis.call('SREM', KEYS[2], ARGV[1])

local remainingSteps = redis.call('SCARD', KEYS[2])

return remainingSteps