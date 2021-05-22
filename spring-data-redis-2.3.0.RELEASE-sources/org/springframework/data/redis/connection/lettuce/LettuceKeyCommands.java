/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.data.redis.connection.lettuce;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RestoreArgs;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.SortArgs;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.ValueEncoding;
import org.springframework.data.redis.connection.ValueEncoding.RedisValueEncoding;
import org.springframework.data.redis.connection.convert.Converters;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.0
 */
@RequiredArgsConstructor
class LettuceKeyCommands implements RedisKeyCommands {

	private final @NonNull LettuceConnection connection;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#exists(byte[])
	 */
	@Override
	public Boolean exists(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().exists(new byte[][] { key }),
						LettuceConverters.longToBooleanConverter()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().exists(new byte[][] { key }),
						LettuceConverters.longToBooleanConverter()));
				return null;
			}
			return LettuceConverters.longToBooleanConverter().convert(getConnection().exists(new byte[][] { key }));
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#exists(byte[][])
	 */
	@Nullable
	@Override
	public Long exists(byte[]... keys) {

		Assert.notNull(keys, "Keys must not be null!");
		Assert.noNullElements(keys, "Keys must not contain null elements!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().exists(keys)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().exists(keys)));
				return null;
			}
			return getConnection().exists(keys);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#del(byte[][])
	 */
	@Override
	public Long del(byte[]... keys) {

		Assert.notNull(keys, "Keys must not be null!");
		Assert.noNullElements(keys, "Keys must not contain null elements!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().del(keys)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().del(keys)));
				return null;
			}
			return getConnection().del(keys);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#unlink(byte[][])
	 */
	@Override
	public Long unlink(byte[]... keys) {

		Assert.notNull(keys, "Keys must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().unlink(keys)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().unlink(keys)));
				return null;
			}
			return getConnection().unlink(keys);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#type(byte[])
	 */
	@Override
	public DataType type(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().type(key), LettuceConverters.stringToDataType()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().type(key), LettuceConverters.stringToDataType()));
				return null;
			}
			return LettuceConverters.toDataType(getConnection().type(key));
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#touch(byte[][])
	 */
	@Override
	public Long touch(byte[]... keys) {

		Assert.notNull(keys, "Keys must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().touch(keys)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().touch(keys)));
				return null;
			}
			return getConnection().touch(keys);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#keys(byte[])
	 */
	@Override
	public Set<byte[]> keys(byte[] pattern) {

		Assert.notNull(pattern, "Pattern must not be null!");

		try {
			if (isPipelined()) {
				pipeline(
						connection.newLettuceResult(getAsyncConnection().keys(pattern), LettuceConverters.bytesListToBytesSet()));
				return null;
			}
			if (isQueueing()) {
				transaction(
						connection.newLettuceResult(getAsyncConnection().keys(pattern), LettuceConverters.bytesListToBytesSet()));
				return null;
			}
			return LettuceConverters.toBytesSet(getConnection().keys(pattern));
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/**
	 * @since 1.4
	 * @return
	 */
	public Cursor<byte[]> scan() {
		return scan(ScanOptions.NONE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#scan(org.springframework.data.redis.core.ScanOptions)
	 */
	@Override
	public Cursor<byte[]> scan(ScanOptions options) {
		return doScan(options != null ? options : ScanOptions.NONE);
	}

	/**
	 * @since 1.4
	 * @param options
	 * @return
	 */
	private Cursor<byte[]> doScan(ScanOptions options) {

		return new LettuceScanCursor<byte[]>(options) {

			@Override
			protected LettuceScanIteration<byte[]> doScan(ScanCursor cursor, ScanOptions options) {

				if (isQueueing() || isPipelined()) {
					throw new UnsupportedOperationException("'SCAN' cannot be called in pipeline / transaction mode.");
				}

				ScanArgs scanArgs = LettuceConverters.toScanArgs(options);

				KeyScanCursor<byte[]> keyScanCursor = getConnection().scan(cursor, scanArgs);
				List<byte[]> keys = keyScanCursor.getKeys();

				return new LettuceScanIteration<>(keyScanCursor, keys);
			}

			@Override
			protected void doClose() {
				LettuceKeyCommands.this.connection.close();
			}

		}.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#randomKey()
	 */
	@Override
	public byte[] randomKey() {

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().randomkey()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().randomkey()));
				return null;
			}
			return getConnection().randomkey();
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#rename(byte[], byte[])
	 */
	@Override
	public void rename(byte[] sourceKey, byte[] targetKey) {

		Assert.notNull(sourceKey, "Source key must not be null!");
		Assert.notNull(targetKey, "Target key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceStatusResult(getAsyncConnection().rename(sourceKey, targetKey)));
				return;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceStatusResult(getAsyncConnection().rename(sourceKey, targetKey)));
				return;
			}
			getConnection().rename(sourceKey, targetKey);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#renameNX(byte[], byte[])
	 */
	@Override
	public Boolean renameNX(byte[] sourceKey, byte[] targetKey) {

		Assert.notNull(sourceKey, "Source key must not be null!");
		Assert.notNull(targetKey, "Target key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().renamenx(sourceKey, targetKey)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().renamenx(sourceKey, targetKey)));
				return null;
			}
			return (getConnection().renamenx(sourceKey, targetKey));
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#expire(byte[], long)
	 */
	@Override
	public Boolean expire(byte[] key, long seconds) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().expire(key, seconds)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().expire(key, seconds)));
				return null;
			}
			return getConnection().expire(key, seconds);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#pExpire(byte[], long)
	 */
	@Override
	public Boolean pExpire(byte[] key, long millis) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().pexpire(key, millis)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().pexpire(key, millis)));
				return null;
			}
			return getConnection().pexpire(key, millis);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#expireAt(byte[], long)
	 */
	@Override
	public Boolean expireAt(byte[] key, long unixTime) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().expireat(key, unixTime)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().expireat(key, unixTime)));
				return null;
			}
			return getConnection().expireat(key, unixTime);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#pExpireAt(byte[], long)
	 */
	@Override
	public Boolean pExpireAt(byte[] key, long unixTimeInMillis) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().pexpireat(key, unixTimeInMillis)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().pexpireat(key, unixTimeInMillis)));
				return null;
			}
			return getConnection().pexpireat(key, unixTimeInMillis);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#persist(byte[])
	 */
	@Override
	public Boolean persist(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().persist(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().persist(key)));
				return null;
			}
			return getConnection().persist(key);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#move(byte[], int)
	 */
	@Override
	public Boolean move(byte[] key, int dbIndex) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().move(key, dbIndex)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().move(key, dbIndex)));
				return null;
			}
			return getConnection().move(key, dbIndex);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#ttl(byte[])
	 */
	@Override
	public Long ttl(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().ttl(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().ttl(key)));
				return null;
			}

			return getConnection().ttl(key);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#ttl(byte[], java.util.concurrent.TimeUnit)
	 */
	@Override
	public Long ttl(byte[] key, TimeUnit timeUnit) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().ttl(key), Converters.secondsToTimeUnit(timeUnit)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().ttl(key), Converters.secondsToTimeUnit(timeUnit)));
				return null;
			}

			return Converters.secondsToTimeUnit(getConnection().ttl(key), timeUnit);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#pTtl(byte[])
	 */
	@Override
	public Long pTtl(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().pttl(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().pttl(key)));
				return null;
			}

			return getConnection().pttl(key);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#pTtl(byte[], java.util.concurrent.TimeUnit)
	 */
	@Override
	public Long pTtl(byte[] key, TimeUnit timeUnit) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(
						connection.newLettuceResult(getAsyncConnection().pttl(key), Converters.millisecondsToTimeUnit(timeUnit)));
				return null;
			}
			if (isQueueing()) {
				transaction(
						connection.newLettuceResult(getAsyncConnection().pttl(key), Converters.millisecondsToTimeUnit(timeUnit)));
				return null;
			}

			return Converters.millisecondsToTimeUnit(getConnection().pttl(key), timeUnit);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#sort(byte[], org.springframework.data.redis.connection.SortParameters)
	 */
	@Override
	public List<byte[]> sort(byte[] key, SortParameters params) {

		Assert.notNull(key, "Key must not be null!");

		SortArgs args = LettuceConverters.toSortArgs(params);

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().sort(key, args)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().sort(key, args)));
				return null;
			}
			return getConnection().sort(key, args);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#sort(byte[], org.springframework.data.redis.connection.SortParameters, byte[])
	 */
	@Override
	public Long sort(byte[] key, SortParameters params, byte[] sortKey) {

		Assert.notNull(key, "Key must not be null!");

		SortArgs args = LettuceConverters.toSortArgs(params);

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().sortStore(key, args, sortKey)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().sortStore(key, args, sortKey)));
				return null;
			}
			return getConnection().sortStore(key, args, sortKey);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#dump(byte[])
	 */
	@Override
	public byte[] dump(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().dump(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().dump(key)));
				return null;
			}
			return getConnection().dump(key);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#restore(byte[], long, byte[], boolean)
	 */
	@Override
	public void restore(byte[] key, long ttlInMillis, byte[] serializedValue, boolean replace) {

		Assert.notNull(key, "Key must not be null!");
		Assert.notNull(serializedValue, "Serialized value must not be null!");

		try {

			RestoreArgs restoreArgs = RestoreArgs.Builder.ttl(ttlInMillis).replace(replace);

			if (isPipelined()) {
				pipeline(connection.newLettuceStatusResult(getAsyncConnection().restore(key, serializedValue, restoreArgs)));
				return;
			}

			if (isQueueing()) {
				transaction(connection.newLettuceStatusResult(getAsyncConnection().restore(key, serializedValue, restoreArgs)));
				return;
			}

			getConnection().restore(key, serializedValue, restoreArgs);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#encoding(byte[])
	 */
	@Nullable
	@Override
	public ValueEncoding encodingOf(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().objectEncoding(key), ValueEncoding::of,
						() -> RedisValueEncoding.VACANT));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().objectEncoding(key), ValueEncoding::of,
						() -> RedisValueEncoding.VACANT));
				return null;
			}

			return ValueEncoding.of(getConnection().objectEncoding(key));
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#idletime(byte[])
	 */
	@Nullable
	@Override
	public Duration idletime(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().objectIdletime(key), Converters::secondsToDuration));
				return null;
			}
			if (isQueueing()) {
				transaction(
						connection.newLettuceResult(getAsyncConnection().objectIdletime(key), Converters::secondsToDuration));
				return null;
			}

			return Converters.secondsToDuration(getConnection().objectIdletime(key));
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#refcount(byte[])
	 */
	@Nullable
	@Override
	public Long refcount(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newLettuceResult(getAsyncConnection().objectRefcount(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newLettuceResult(getAsyncConnection().objectRefcount(key)));
				return null;
			}

			return getConnection().objectRefcount(key);
		} catch (Exception ex) {
			throw convertLettuceAccessException(ex);
		}
	}

	private boolean isPipelined() {
		return connection.isPipelined();
	}

	private boolean isQueueing() {
		return connection.isQueueing();
	}

	private void pipeline(LettuceResult result) {
		connection.pipeline(result);
	}

	private void transaction(LettuceResult result) {
		connection.transaction(result);
	}

	private RedisClusterAsyncCommands<byte[], byte[]> getAsyncConnection() {
		return connection.getAsyncConnection();
	}

	public RedisClusterCommands<byte[], byte[]> getConnection() {
		return connection.getConnection();
	}

	private DataAccessException convertLettuceAccessException(Exception ex) {
		return connection.convertLettuceAccessException(ex);
	}

}
