local status = redis.call('HGET', KEYS[1], 'status')

if status == '"PENDING"' then
    redis.call('HSET', KEYS[1], 'status', '"CANCELLED"')
    redis.call('ZREM', KEYS[2], ARGV[1])
    redis.call('DEL', KEYS[3])
    redis.call('EXPIRE', KEYS[1], ARGV[2])
    return 1
end

return 0