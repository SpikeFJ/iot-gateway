package com.jfeng.gateway.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
public class RedisUtils {
	public static final String LOCK_PREFIX = "redis_lock:";

	@Autowired
	private RedisTemplate<String, String> redisTemplate;


	public void expire(String key, long l, TimeUnit timeUnit) {
		redisTemplate.expire(key, l, timeUnit);
	}

	public Long getExpire(String key) {
		return redisTemplate.getExpire(key);
	}

	public Long getExpire(String key, TimeUnit timeUnit) {
		return redisTemplate.getExpire(key, timeUnit);
	}

	public Boolean delete(String key) {
		return redisTemplate.delete(key);
	}

	public DataType type(String key) {
		return redisTemplate.type(key);
	}

	public Long deletes(Collection<String> keys) {
		return redisTemplate.delete(keys);
	}

	public Boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	public Set<String> keys(String pattern) {
		return redisTemplate.keys(pattern);
	}

	public <T> T execute(SessionCallback<T> session) {
		return redisTemplate.execute(session);
	}

	public <T> T execute(RedisCallback<T> action) {
		return redisTemplate.execute(action);
	}

	public synchronized boolean setNx(String key, long l, TimeUnit timeUnit) {
		String lockKey = LOCK_PREFIX + key;
		String currentThreadId = String.valueOf(Thread.currentThread().getId());
		Boolean flag = redisTemplate.opsForValue().setIfAbsent(lockKey, currentThreadId, l, timeUnit);
		return Boolean.TRUE.equals(flag);
	}

	public synchronized boolean delNx(String key) {
		String lockKey = LOCK_PREFIX + key;
		String currentThreadId = redisTemplate.opsForValue().get(lockKey);
		if (currentThreadId != null && String.valueOf(Thread.currentThread().getId()).equals(currentThreadId.trim())) {
			redisTemplate.delete(key);
			return true;
		}
		return false;
	}

	/**
	 * ---------------------------------字符串---------------------------------
	 */
	public void set(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	public void set(String key, String value, long l, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(key, value, l, timeUnit);
	}

	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public Boolean setIfAbsent(String key, String value) {
		return redisTemplate.opsForValue().setIfAbsent(key, value);
	}

	public Boolean setIfAbsent(String key, String value, long l, TimeUnit timeUnit) {
		return redisTemplate.opsForValue().setIfAbsent(key, value, l, timeUnit);
	}

	public Boolean setIfPresent(String key, String value) {
		return redisTemplate.opsForValue().setIfPresent(key, value);
	}

	public Boolean setIfPresent(String key, String value, long l, TimeUnit timeUnit) {
		return redisTemplate.opsForValue().setIfPresent(key, value, l, timeUnit);
	}

	public void multiSet(Map<String, String> map) {
		redisTemplate.opsForValue().multiSet(map);
	}

	public Boolean multiSetIfAbsent(Map<String, String> map) {
		return redisTemplate.opsForValue().multiSetIfAbsent(map);
	}

	public String getAndSet(String key, String value) {
		return redisTemplate.opsForValue().getAndSet(key, value);
	}

	public List<String> multiGet(Collection<String> keys) {
		return redisTemplate.opsForValue().multiGet(keys);
	}

	public Long increment(String key) {
		return redisTemplate.opsForValue().increment(key);
	}

	public Long increment(String key, long number) {
		return redisTemplate.opsForValue().increment(key, number);
	}

	public Double increment(String key, double number) {
		return redisTemplate.opsForValue().increment(key, number);
	}

	public Long decrement(String key) {
		return redisTemplate.opsForValue().decrement(key);
	}

	public Long decrement(String key, long number) {
		return redisTemplate.opsForValue().decrement(key, number);
	}

	public Integer append(String key, String value) {
		return redisTemplate.opsForValue().append(key, value);
	}

	public Boolean setBit(String key, long offset, boolean value) {
		return redisTemplate.opsForValue().setBit(key, offset, value);
	}

	public Boolean getBit(String key, long offset) {
		return redisTemplate.opsForValue().getBit(key, offset);
	}

	/**
	 * ---------------------------------list---------------------------------
	 */
	public List<String> range(String key, long start, long end) {
		return redisTemplate.opsForList().range(key, start, end);
	}

	public Long size(String key) {
		return redisTemplate.opsForList().size(key);
	}

	public Long leftPush(String key, String value) {
		return redisTemplate.opsForList().leftPush(key, value);
	}

	public Long leftPushAll(String key, String... values) {
		return redisTemplate.opsForList().leftPushAll(key, values);
	}

	public Long leftPushAll(String key, Collection<String> values) {
		return redisTemplate.opsForList().leftPushAll(key, values);
	}

	public Long leftPushIfPresent(String key, String value) {
		return redisTemplate.opsForList().leftPushIfPresent(key, value);
	}

	public Long leftPush(String key, String pivot, String value) {
		return redisTemplate.opsForList().leftPush(key, pivot, value);
	}

	public Long rightPush(String key, String value) {
		return redisTemplate.opsForList().rightPush(key, value);
	}

	public Long rightPushAll(String key, String... values) {
		return redisTemplate.opsForList().rightPushAll(key, values);
	}

	public Long rightPushAll(String key, Collection<String> values) {
		return redisTemplate.opsForList().rightPushAll(key, values);
	}

	public Long rightPushIfPresent(String key, String value) {
		return redisTemplate.opsForList().rightPushIfPresent(key, value);
	}

	public Long rightPush(String key, String pivot, String value) {
		return redisTemplate.opsForList().rightPush(key, pivot, value);
	}

	public void set(String key, long index, String value) {
		redisTemplate.opsForList().set(key, index, value);
	}

	public Long remove(String key, long count, String value) {
		return redisTemplate.opsForList().remove(key, count, value);
	}

	public String index(String key, long index) {
		return redisTemplate.opsForList().index(key, index);
	}

	public Long indexOf(String key, String value) {
		return redisTemplate.opsForList().indexOf(key, value);
	}

	public Long lastIndexOf(String key, String value) {
		return redisTemplate.opsForList().lastIndexOf(key, value);
	}

	public String leftPop(String key) {
		return redisTemplate.opsForList().leftPop(key);
	}

	public String leftPop(String key, long timeout, TimeUnit unit) {
		return redisTemplate.opsForList().leftPop(key, timeout, unit);
	}

	public String rightPop(String key) {
		return redisTemplate.opsForList().rightPop(key);
	}

	public String rightPop(String key, long timeout, TimeUnit unit) {
		return redisTemplate.opsForList().rightPop(key, timeout, unit);
	}

	public String rightPopAndLeftPush(String sourceKey, String destinationKey) {
		return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
	}

	public String rightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
		return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
	}

	/**
	 * ---------------------------------set---------------------------------
	 */
	public Long add(String key, String... values) {
		return redisTemplate.opsForSet().add(key, values);
	}

	public Long sRemove(String key, Object... values) {
		return redisTemplate.opsForSet().remove(key, values);
	}

	public String pop(String key) {
		return redisTemplate.opsForSet().pop(key);
	}

	public List<String> pop(String key, long count) {
		return redisTemplate.opsForSet().pop(key, count);
	}

	public Boolean move(String key, String value, String destKey) {
		return redisTemplate.opsForSet().move(key, value, destKey);
	}

	public Long sSize(String key) {
		return redisTemplate.opsForSet().size(key);
	}

	public Boolean isMember(String key, String o) {
		return redisTemplate.opsForSet().isMember(key, o);
	}

	public Set<String> intersect(String key, String otherKey) {
		return redisTemplate.opsForSet().intersect(key, otherKey);
	}

	public Set<String> intersect(String key, Collection<String> otherKeys) {
		return redisTemplate.opsForSet().intersect(key, otherKeys);
	}

	public Set<String> intersect(Collection<String> keys) {
		return redisTemplate.opsForSet().intersect(keys);
	}

	public Long sIntersectAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
	}

	public Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
	}

	public Long sIntersectAndStore(Collection<String> keys, String destKey) {
		return redisTemplate.opsForSet().intersectAndStore(keys, destKey);
	}

	public Set<String> union(String key, String otherKey) {
		return redisTemplate.opsForSet().union(key, otherKey);
	}

	public Set<String> union(String key, Collection<String> otherKeys) {
		return redisTemplate.opsForSet().union(key, otherKeys);
	}

	public Set<String> union(Collection<String> keys) {
		return redisTemplate.opsForSet().union(keys);
	}

	public Long sUnionAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
	}

	public Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
	}

	public Long sUnionAndStore(Collection<String> keys, String destKey) {
		return redisTemplate.opsForSet().unionAndStore(keys, destKey);
	}

	public Set<String> difference(String key, String otherKey) {
		return redisTemplate.opsForSet().difference(key, otherKey);
	}

	public Set<String> difference(String key, Collection<String> otherKeys) {
		return redisTemplate.opsForSet().difference(key, otherKeys);
	}

	public Set<String> difference(Collection<String> keys) {
		return redisTemplate.opsForSet().difference(keys);
	}

	public Long differenceAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
	}

	public Long differenceAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
	}

	public Long differenceAndStore(Collection<String> keys, String destKey) {
		return redisTemplate.opsForSet().differenceAndStore(keys, destKey);
	}

	public Set<String> members(String key) {
		return redisTemplate.opsForSet().members(key);
	}

	public String randomMember(String key) {
		return redisTemplate.opsForSet().randomMember(key);
	}

	public Set<String> distinctRandomMembers(String key, long count) {
		return redisTemplate.opsForSet().distinctRandomMembers(key, count);
	}

	public List<String> randomMembers(String key, long count) {
		return redisTemplate.opsForSet().randomMembers(key, count);
	}

	public Cursor<String> sScan(String key, ScanOptions options) {
		return redisTemplate.opsForSet().scan(key, options);
	}

	/**
	 * ---------------------------------有序set---------------------------------
	 */
	public Boolean add(String key, String value, double score) {
		return redisTemplate.opsForZSet().add(key, value, score);
	}

	public Long add(String key, Set<ZSetOperations.TypedTuple<String>> set) {
		return redisTemplate.opsForZSet().add(key, set);
	}

	public Long remove(String key, Object... values) {
		return redisTemplate.opsForZSet().remove(key, values);
	}

	public Double incrementScore(String key, String value, double delta) {
		return redisTemplate.opsForZSet().incrementScore(key, value, delta);
	}

	public Long rank(String key, Object o) {
		return redisTemplate.opsForZSet().rank(key, o);
	}

	public Long reverseRank(String key, Object o) {
		return redisTemplate.opsForZSet().reverseRank(key, o);
	}

	public Set<String> ranges(String key, long start, long end) {
		return redisTemplate.opsForZSet().range(key, start, end);
	}

	public Set<ZSetOperations.TypedTuple<String>> rangeWithScores(String key, long start, long end) {
		return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
	}

	public Set<String> rangeByScore(String key, double min, double max) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max);
	}

	public Set<ZSetOperations.TypedTuple<String>> rangeByScoreWithScores(String key, double min, double max) {
		return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
	}

	public Set<String> rangeByScore(String key, double min, double max, long offset, long count) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
	}

	public Set<ZSetOperations.TypedTuple<String>> rangeByScoreWithScores(String key, double min, double max, long offset, long count) {
		return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<String> reverseRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().reverseRange(key, start, end);
	}

	public Set<ZSetOperations.TypedTuple<String>> reverseRangeWithScores(String key, long start, long end) {
		return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
	}

	public Set<String> reverseRangeByScore(String key, double min, double max) {
		return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
	}

	public Set<ZSetOperations.TypedTuple<String>> reverseRangeByScoreWithScores(String key, double min, double max) {
		return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
	}

	public Set<String> reverseRangeByScore(String key, double min, double max, long offset, long count) {
		return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
	}

	public Set<ZSetOperations.TypedTuple<String>> reverseRangeByScoreWithScores(String key, double min, double max, long offset, long count) {
		return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max, offset, count);
	}

	public Long count(String key, double min, double max) {
		return redisTemplate.opsForZSet().count(key, min, max);
	}

	public Long lexCount(String key, RedisZSetCommands.Range range) {
		return redisTemplate.opsForZSet().lexCount(key, range);
	}

	public Long zSize(String key) {
		return redisTemplate.opsForZSet().size(key);
	}

	public Long zCard(String key) {
		return redisTemplate.opsForZSet().zCard(key);
	}

	public Double score(String key, Object o) {
		return redisTemplate.opsForZSet().score(key, o);
	}

	public Long removeRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().removeRange(key, start, end);
	}

	public Long removeRangeByScore(String key, double min, double max) {
		return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
	}

	public Long unionAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
	}

	public Long unionAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
	}

	public Long unionAndStore(String key, Collection<String> otherKeys, String destKey, RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights) {
		return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey, aggregate, weights);
	}

	public Long intersectAndStore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
	}

	public Long intersectAndStore(String key, Collection<String> otherKeys, String destKey) {
		return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
	}

	public Long intersectAndStore(String key, Collection<String> otherKeys, String destKey, RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights) {
		return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey, aggregate, weights);
	}

	public Cursor<ZSetOperations.TypedTuple<String>> zScan(String key, ScanOptions options) {
		return redisTemplate.opsForZSet().scan(key, options);
	}

	public Set<String> rangeByLex(String key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit) {
		return redisTemplate.opsForZSet().rangeByLex(key, range, limit);
	}

	public Set<String> reverseRangeByLex(String key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit) {
		return redisTemplate.opsForZSet().reverseRangeByLex(key, range, limit);
	}

	public Long unionAndStore(String key, Collection<String> otherKeys, String destKey, RedisZSetCommands.Aggregate aggregate) {
		return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey, aggregate);
	}

	public Long intersectAndStore(String key, Collection<String> otherKeys, String destKey, RedisZSetCommands.Aggregate aggregate) {
		return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey, aggregate);
	}

	public Set<String> rangeByLex(String key, RedisZSetCommands.Range range) {
		return redisTemplate.opsForZSet().rangeByLex(key, range);
	}

	public Set<String> reverseRangeByLex(String key, RedisZSetCommands.Range range) {
		return redisTemplate.opsForZSet().reverseRangeByLex(key, range);
	}

	/**
	 * ---------------------------------hash---------------------------------
	 */
	public Long delete(String key, Object... hashKeys) {
		return redisTemplate.opsForHash().delete(key, hashKeys);
	}

	public Boolean hasKey(String key, Object hashKey) {
		return redisTemplate.opsForHash().hasKey(key, hashKey);
	}

	public Object get(String key, Object hashKey) {
		return redisTemplate.opsForHash().get(key, hashKey);
	}

	public List<String> multiGet(String key, Collection<String> hashKeys) {
		HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
		return hashOperations.multiGet(key, hashKeys);
	}

	public Long increment(String key, String hashKey, long delta) {
		return redisTemplate.opsForHash().increment(key, hashKey, delta);
	}

	public Double increment(String key, String hashKey, double delta) {
		return redisTemplate.opsForHash().increment(key, hashKey, delta);
	}

	public Set<String> hKeys(String key) {
		HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
		return hashOperations.keys(key);
	}

	public Long lengthOfValue(String key, String hashKey) {
		return redisTemplate.opsForHash().lengthOfValue(key, hashKey);
	}

	public Long hSize(String key) {
		return redisTemplate.opsForHash().size(key);
	}

	public void putAll(String key, Map<String, String> m) {
		redisTemplate.opsForHash().putAll(key, m);
	}

	public void put(String key, String hashKey, String value) {
		redisTemplate.opsForHash().put(key, hashKey, value);
	}

	public Boolean putIfAbsent(String key, String hashKey, String value) {
		return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
	}

	public List<String> values(String key) {
		HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
		return hashOperations.values(key);
	}

	public Map<String, String> entries(String key) {
		HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
		return hashOperations.entries(key);
	}

	public Cursor<Map.Entry<String, String>> scan(String key, ScanOptions options) {
		HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
		return hashOperations.scan(key, options);
	}

}
