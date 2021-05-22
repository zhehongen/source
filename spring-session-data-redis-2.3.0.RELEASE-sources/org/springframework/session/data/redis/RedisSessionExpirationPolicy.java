/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.session.data.redis;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository.RedisSession;

/**RedisIndexedSessionRepository.RedisSession实例到期的策略。 这执行两个操作：Redis无法保证何时触发过期的会话事件。 为了确保及时处理过期的会话事件，将过期（四舍五入到最近的分钟）映射到当时所有过期的会话。 每当调用cleanExpiredSessions（）时，就会访问前一分钟的会话，以确保它们在过期时被删除。 在某些情况下，在特定时间内可能不会调用cleanExpiredSessions（）方法。 例如，重新启动服务器时可能会发生这种情况。 为了解决这个问题，还设置了Redis会话的到期时间。
 * A strategy for expiring {@link RedisSession} instances. This performs two operations:
 *
 * Redis has no guarantees of when an expired session event will be fired. In order to
 * ensure expired session events are processed in a timely fashion the expiration (rounded
 * to the nearest minute) is mapped to all the sessions that expire at that time. Whenever
 * {@link #cleanExpiredSessions()} is invoked, the sessions for the previous minute are
 * then accessed to ensure they are deleted if expired.
 *
 * In some instances the {@link #cleanExpiredSessions()} method may not be not invoked for
 * a specific time. For example, this may happen when a server is restarted. To account
 * for this, the expiration on the Redis session is also set.
 *
 * @author Rob Winch
 * @since 1.0
 */
final class RedisSessionExpirationPolicy {

	private static final Log logger = LogFactory.getLog(RedisSessionExpirationPolicy.class);

	private final RedisOperations<Object, Object> redis;

	private final Function<Long, String> lookupExpirationKey;//

	private final Function<String, String> lookupSessionKey;//

	RedisSessionExpirationPolicy(RedisOperations<Object, Object> sessionRedisOperations,
			Function<Long, String> lookupExpirationKey, Function<String, String> lookupSessionKey) {
		super();
		this.redis = sessionRedisOperations;
		this.lookupExpirationKey = lookupExpirationKey;
		this.lookupSessionKey = lookupSessionKey;
	}
//说明：只涉及expirations这个key的操作
	void onDelete(Session session) {//说明：谁来调用是个问题？定时任务？不是的。这应该都是用户主动干的
		long toExpire = roundUpToNextMinute(expiresInMillis(session));
		String expireKey = getExpirationKey(toExpire);
		this.redis.boundSetOps(expireKey).remove(session.getId());//只删除这个有何意义？
	}//说明：org.springframework.session.data.redis.RedisIndexedSessionRepository.deleteById
//说明：涉及expires:2c975c2f-1593-4a31-8658-48c0e4b75062和2c975c2f-1593-4a31-8658-48c0e4b75062和expirations三种key的操作
	void onExpirationUpdated(Long originalExpirationTimeInMilli, Session session) {
		String keyToExpire = "expires:" + session.getId();//类似这样的： expires:2c975c2f-1593-4a31-8658-48c0e4b75062
		long toExpire = roundUpToNextMinute(expiresInMillis(session));

		if (originalExpirationTimeInMilli != null) {
			long originalRoundedUp = roundUpToNextMinute(originalExpirationTimeInMilli);
			if (toExpire != originalRoundedUp) {
				String expireKey = getExpirationKey(originalRoundedUp);
				this.redis.boundSetOps(expireKey).remove(keyToExpire);//说明：从原始的expirations的集合记录中删除这条sessionid
			}
		}

		long sessionExpireInSeconds = session.getMaxInactiveInterval().getSeconds();
		String sessionKey = getSessionKey(keyToExpire);//说明：实际是：getExpiredKey即：-->sessions:expires

		if (sessionExpireInSeconds < 0) {
			this.redis.boundValueOps(sessionKey).append("");//空值
			this.redis.boundValueOps(sessionKey).persist();//移除过期时间
			this.redis.boundHashOps(getSessionKey(session.getId())).persist();//移除过期时间
			return;
		}

		String expireKey = getExpirationKey(toExpire);
		BoundSetOperations<Object, Object> expireOperations = this.redis.boundSetOps(expireKey);
		expireOperations.add(keyToExpire);

		long fiveMinutesAfterExpires = sessionExpireInSeconds + TimeUnit.MINUTES.toSeconds(5);

		expireOperations.expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);//expirations多5分钟。为啥？
		if (sessionExpireInSeconds == 0) {
			this.redis.delete(sessionKey);
		}
		else {
			this.redis.boundValueOps(sessionKey).append("");
			this.redis.boundValueOps(sessionKey).expire(sessionExpireInSeconds, TimeUnit.SECONDS);
		}
		this.redis.boundHashOps(getSessionKey(session.getId())).expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);//真实session也多5分钟
	}

	String getExpirationKey(long expires) {
		return this.lookupExpirationKey.apply(expires);
	}

	String getSessionKey(String sessionId) {
		return this.lookupSessionKey.apply(sessionId);
	}

	void cleanExpiredSessions() {//这应该是定时任务触发的
		long now = System.currentTimeMillis();
		long prevMin = roundDownMinute(now);

		if (logger.isDebugEnabled()) {
			logger.debug("Cleaning up sessions expiring at " + new Date(prevMin));
		}

		String expirationKey = getExpirationKey(prevMin);
		Set<Object> sessionsToExpire = this.redis.boundSetOps(expirationKey).members();
		this.redis.delete(expirationKey);//删除单个整个expiration
		for (Object session : sessionsToExpire) {//类似这样的。expires:2c975c2f-1593-4a31-8658-48c0e4b75062
			String sessionKey = getSessionKey((String) session);//实际是expires。。把这个集合中的session全部删除,实际删除的是expires key
			touch(sessionKey);//说明：这是关键点，只要expires被清理了，就认为session过期了。吊毛
		}//只删除这些？其他的呢
	}

	/**
	 * By trying to access the session we only trigger a deletion if it the TTL is
	 * expired. This is done to handle
	 * https://github.com/spring-projects/spring-session/issues/93
	 * @param key the key
	 */
	private void touch(String key) {
		this.redis.hasKey(key);//看过了
	}

	static long expiresInMillis(Session session) {//看过了。简单
		int maxInactiveInSeconds = (int) session.getMaxInactiveInterval().getSeconds();
		long lastAccessedTimeInMillis = session.getLastAccessedTime().toEpochMilli();
		return lastAccessedTimeInMillis + TimeUnit.SECONDS.toMillis(maxInactiveInSeconds);
	}

	static long roundUpToNextMinute(long timeInMs) {//说明：下一分钟。比session真实过期时间晚个几十秒。

		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timeInMs);//说明：可以学习一下。原本5.35.46秒过期。实际时间为5.36分过期。稍微晚一点去调用更好
		date.add(Calendar.MINUTE, 1);
		date.clear(Calendar.SECOND);
		date.clear(Calendar.MILLISECOND);
		return date.getTimeInMillis();
	}

	static long roundDownMinute(long timeInMs) {//说明：精确到分钟
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timeInMs);
		date.clear(Calendar.SECOND);
		date.clear(Calendar.MILLISECOND);
		return date.getTimeInMillis();
	}

}
