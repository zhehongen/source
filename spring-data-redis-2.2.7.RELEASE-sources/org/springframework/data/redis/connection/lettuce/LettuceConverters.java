/*
 * Copyright 2013-2020 the original author or authors.
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

import io.lettuce.core.*;
import io.lettuce.core.cluster.models.partitions.Partitions;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode.NodeFlag;
import io.lettuce.core.protocol.LettuceCharsets;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldGet;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldIncrBy;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldIncrBy.Overflow;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldSet;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldSubCommand;
import org.springframework.data.redis.connection.DefaultTuple;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisClusterNode.Flag;
import org.springframework.data.redis.connection.RedisClusterNode.LinkState;
import org.springframework.data.redis.connection.RedisClusterNode.SlotRange;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisNode.NodeType;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.RedisZSetCommands.Range.Boundary;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.SortParameters.Order;
import org.springframework.data.redis.connection.convert.Converters;
import org.springframework.data.redis.connection.convert.ListConverter;
import org.springframework.data.redis.connection.convert.LongToBooleanConverter;
import org.springframework.data.redis.connection.convert.StringToRedisClientInfoConverter;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.core.types.RedisClientInfo;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Lettuce type converters
 *
 * @author Jennifer Hickey
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author Ninad Divadkar
 */
abstract public class LettuceConverters extends Converters {

	private static final Converter<Date, Long> DATE_TO_LONG;
	private static final Converter<List<byte[]>, Set<byte[]>> BYTES_LIST_TO_BYTES_SET;
	private static final Converter<byte[], String> BYTES_TO_STRING;
	private static final Converter<String, byte[]> STRING_TO_BYTES;
	private static final Converter<Set<byte[]>, List<byte[]>> BYTES_SET_TO_BYTES_LIST;
	private static final Converter<Collection<byte[]>, List<byte[]>> BYTES_COLLECTION_TO_BYTES_LIST;
	private static final Converter<KeyValue<byte[], byte[]>, List<byte[]>> KEY_VALUE_TO_BYTES_LIST;
	private static final Converter<List<ScoredValue<byte[]>>, Set<Tuple>> SCORED_VALUES_TO_TUPLE_SET;
	private static final Converter<List<ScoredValue<byte[]>>, List<Tuple>> SCORED_VALUES_TO_TUPLE_LIST;
	private static final Converter<ScoredValue<byte[]>, Tuple> SCORED_VALUE_TO_TUPLE;
	private static final Converter<Exception, DataAccessException> EXCEPTION_CONVERTER = new LettuceExceptionConverter();
	private static final Converter<Long, Boolean> LONG_TO_BOOLEAN = new LongToBooleanConverter();
	private static final Converter<List<byte[]>, Map<byte[], byte[]>> BYTES_LIST_TO_MAP;
	private static final Converter<List<byte[]>, List<Tuple>> BYTES_LIST_TO_TUPLE_LIST_CONVERTER;
	private static final Converter<String[], List<RedisClientInfo>> STRING_TO_LIST_OF_CLIENT_INFO = new StringToRedisClientInfoConverter();
	private static final Converter<Partitions, List<RedisClusterNode>> PARTITIONS_TO_CLUSTER_NODES;
	private static Converter<io.lettuce.core.cluster.models.partitions.RedisClusterNode, RedisClusterNode> CLUSTER_NODE_TO_CLUSTER_NODE_CONVERTER;
	private static final Converter<List<byte[]>, Long> BYTES_LIST_TO_TIME_CONVERTER;
	private static final Converter<GeoCoordinates, Point> GEO_COORDINATE_TO_POINT_CONVERTER;
	private static final ListConverter<GeoCoordinates, Point> GEO_COORDINATE_LIST_TO_POINT_LIST_CONVERTER;
	private static final Converter<KeyValue<Object, Object>, Object> KEY_VALUE_UNWRAPPER;
	private static final ListConverter<KeyValue<Object, Object>, Object> KEY_VALUE_LIST_UNWRAPPER;
	private static final Converter<TransactionResult, List<Object>> TRANSACTION_RESULT_UNWRAPPER;

	public static final byte[] PLUS_BYTES;
	public static final byte[] MINUS_BYTES;
	public static final byte[] POSITIVE_INFINITY_BYTES;
	public static final byte[] NEGATIVE_INFINITY_BYTES;

	private static final long INDEXED_RANGE_START = 0;
	private static final long INDEXED_RANGE_END = -1;

	static {

		DATE_TO_LONG = source -> source != null ? source.getTime() : null;

		BYTES_LIST_TO_BYTES_SET = results -> results != null ? new LinkedHashSet<>(results) : null;

		BYTES_TO_STRING = source -> {
			if (source == null || Arrays.equals(source, new byte[0])) {
				return null;
			}
			return new String(source);
		};

		STRING_TO_BYTES = source -> {
			if (source == null) {
				return null;
			}
			return source.getBytes();
		};

		BYTES_SET_TO_BYTES_LIST = results -> results != null ? new ArrayList<>(results) : null;

		BYTES_COLLECTION_TO_BYTES_LIST = results -> {

			if (results instanceof List) {
				return (List<byte[]>) results;
			}
			return results != null ? new ArrayList<>(results) : null;
		};

		KEY_VALUE_TO_BYTES_LIST = source -> {

			if (source == null) {
				return null;
			}
			List<byte[]> list = new ArrayList<>(2);
			list.add(source.getKey());
			list.add(source.getValue());

			return list;
		};
		BYTES_LIST_TO_MAP = source -> {

			if (CollectionUtils.isEmpty(source)) {
				return Collections.emptyMap();
			}

			Map<byte[], byte[]> target = new LinkedHashMap<>();

			Iterator<byte[]> kv = source.iterator();
			while (kv.hasNext()) {
				target.put(kv.next(), kv.hasNext() ? kv.next() : null);
			}

			return target;
		};

		SCORED_VALUES_TO_TUPLE_SET = source -> {

			if (source == null) {
				return null;
			}
			Set<Tuple> tuples = new LinkedHashSet<>(source.size());
			for (ScoredValue<byte[]> value : source) {
				tuples.add(LettuceConverters.toTuple(value));
			}
			return tuples;
		};

		SCORED_VALUES_TO_TUPLE_LIST = source -> {

			if (source == null) {
				return null;
			}
			List<Tuple> tuples = new ArrayList<>(source.size());
			for (ScoredValue<byte[]> value : source) {
				tuples.add(LettuceConverters.toTuple(value));
			}
			return tuples;
		};

		SCORED_VALUE_TO_TUPLE = source -> source != null
				? new DefaultTuple(source.getValue(), Double.valueOf(source.getScore()))
				: null;

		BYTES_LIST_TO_TUPLE_LIST_CONVERTER = source -> {

			if (CollectionUtils.isEmpty(source)) {
				return Collections.emptyList();
			}

			List<Tuple> tuples = new ArrayList<>();
			Iterator<byte[]> it = source.iterator();
			while (it.hasNext()) {
				tuples.add(new DefaultTuple(it.next(), it.hasNext() ? Double.valueOf(toString(it.next())) : null));
			}
			return tuples;
		};

		PARTITIONS_TO_CLUSTER_NODES = new Converter<Partitions, List<RedisClusterNode>>() {

			@Override
			public List<RedisClusterNode> convert(Partitions source) {

				if (source == null) {
					return Collections.emptyList();
				}
				List<RedisClusterNode> nodes = new ArrayList<>();
				for (io.lettuce.core.cluster.models.partitions.RedisClusterNode node : source) {
					nodes.add(CLUSTER_NODE_TO_CLUSTER_NODE_CONVERTER.convert(node));
				}

				return nodes;
			};

		};

		CLUSTER_NODE_TO_CLUSTER_NODE_CONVERTER = new Converter<io.lettuce.core.cluster.models.partitions.RedisClusterNode, RedisClusterNode>() {

			@Override
			public RedisClusterNode convert(io.lettuce.core.cluster.models.partitions.RedisClusterNode source) {

				Set<Flag> flags = parseFlags(source.getFlags());

				return RedisClusterNode.newRedisClusterNode().listeningAt(source.getUri().getHost(), source.getUri().getPort())
						.withId(source.getNodeId()).promotedAs(flags.contains(Flag.MASTER) ? NodeType.MASTER : NodeType.SLAVE)
						.serving(new SlotRange(source.getSlots())).withFlags(flags)
						.linkState(source.isConnected() ? LinkState.CONNECTED : LinkState.DISCONNECTED).slaveOf(source.getSlaveOf())
						.build();
			}

			private Set<Flag> parseFlags(Set<NodeFlag> source) {

				Set<Flag> flags = new LinkedHashSet<>(source != null ? source.size() : 8, 1);
				for (NodeFlag flag : source) {
					switch (flag) {
						case NOFLAGS:
							flags.add(Flag.NOFLAGS);
							break;
						case EVENTUAL_FAIL:
							flags.add(Flag.PFAIL);
							break;
						case FAIL:
							flags.add(Flag.FAIL);
							break;
						case HANDSHAKE:
							flags.add(Flag.HANDSHAKE);
							break;
						case MASTER:
							flags.add(Flag.MASTER);
							break;
						case MYSELF:
							flags.add(Flag.MYSELF);
							break;
						case NOADDR:
							flags.add(Flag.NOADDR);
							break;
						case SLAVE:
							flags.add(Flag.SLAVE);
							break;
					}
				}
				return flags;
			}

		};

		PLUS_BYTES = toBytes("+");
		MINUS_BYTES = toBytes("-");
		POSITIVE_INFINITY_BYTES = toBytes("+inf");
		NEGATIVE_INFINITY_BYTES = toBytes("-inf");

		BYTES_LIST_TO_TIME_CONVERTER = source -> {

			Assert.notEmpty(source, "Received invalid result from server. Expected 2 items in collection.");
			Assert.isTrue(source.size() == 2,
					"Received invalid nr of arguments from redis server. Expected 2 received " + source.size());

			return toTimeMillis(toString(source.get(0)), toString(source.get(1)));
		};

		GEO_COORDINATE_TO_POINT_CONVERTER = geoCoordinate -> geoCoordinate != null
				? new Point(geoCoordinate.getX().doubleValue(), geoCoordinate.getY().doubleValue())
				: null;
		GEO_COORDINATE_LIST_TO_POINT_LIST_CONVERTER = new ListConverter<>(GEO_COORDINATE_TO_POINT_CONVERTER);

		KEY_VALUE_UNWRAPPER = source -> source.getValueOrElse(null);

		KEY_VALUE_LIST_UNWRAPPER = new ListConverter<>(KEY_VALUE_UNWRAPPER);

		TRANSACTION_RESULT_UNWRAPPER = transactionResult -> transactionResult.stream().collect(Collectors.toList());
	}

	public static List<Tuple> toTuple(List<byte[]> list) {
		return BYTES_LIST_TO_TUPLE_LIST_CONVERTER.convert(list);
	}

	public static Converter<List<byte[]>, List<Tuple>> bytesListToTupleListConverter() {
		return BYTES_LIST_TO_TUPLE_LIST_CONVERTER;
	}

	public static Point geoCoordinatesToPoint(GeoCoordinates geoCoordinates) {
		return GEO_COORDINATE_TO_POINT_CONVERTER.convert(geoCoordinates);
	}

	public static Converter<String, List<RedisClientInfo>> stringToRedisClientListConverter() {

		return source -> {

			if (!StringUtils.hasText(source)) {
				return Collections.emptyList();
			}

			return STRING_TO_LIST_OF_CLIENT_INFO.convert(source.split("\\r?\\n"));
		};
	}

	public static Converter<Date, Long> dateToLong() {
		return DATE_TO_LONG;
	}

	public static Converter<List<byte[]>, Set<byte[]>> bytesListToBytesSet() {
		return BYTES_LIST_TO_BYTES_SET;
	}

	public static Converter<byte[], String> bytesToString() {
		return BYTES_TO_STRING;
	}

	public static Converter<KeyValue<byte[], byte[]>, List<byte[]>> keyValueToBytesList() {
		return KEY_VALUE_TO_BYTES_LIST;
	}

	public static Converter<Collection<byte[]>, List<byte[]>> bytesSetToBytesList() {
		return BYTES_COLLECTION_TO_BYTES_LIST;
	}

	public static Converter<Collection<byte[]>, List<byte[]>> bytesCollectionToBytesList() {
		return BYTES_COLLECTION_TO_BYTES_LIST;
	}

	public static Converter<List<ScoredValue<byte[]>>, Set<Tuple>> scoredValuesToTupleSet() {
		return SCORED_VALUES_TO_TUPLE_SET;
	}

	public static Converter<List<ScoredValue<byte[]>>, List<Tuple>> scoredValuesToTupleList() {
		return SCORED_VALUES_TO_TUPLE_LIST;
	}

	public static Converter<ScoredValue<byte[]>, Tuple> scoredValueToTuple() {
		return SCORED_VALUE_TO_TUPLE;
	}

	public static Converter<Exception, DataAccessException> exceptionConverter() {
		return EXCEPTION_CONVERTER;
	}

	/**
	 * @return
	 * @sice 1.3
	 */
	public static Converter<Long, Boolean> longToBooleanConverter() {
		return LONG_TO_BOOLEAN;
	}

	public static Long toLong(Date source) {
		return DATE_TO_LONG.convert(source);
	}

	public static Set<byte[]> toBytesSet(List<byte[]> source) {
		return BYTES_LIST_TO_BYTES_SET.convert(source);
	}

	public static List<byte[]> toBytesList(KeyValue<byte[], byte[]> source) {
		return KEY_VALUE_TO_BYTES_LIST.convert(source);
	}

	public static List<byte[]> toBytesList(Collection<byte[]> source) {
		return BYTES_COLLECTION_TO_BYTES_LIST.convert(source);
	}

	public static Set<Tuple> toTupleSet(List<ScoredValue<byte[]>> source) {
		return SCORED_VALUES_TO_TUPLE_SET.convert(source);
	}

	public static Tuple toTuple(ScoredValue<byte[]> source) {
		return SCORED_VALUE_TO_TUPLE.convert(source);
	}

	public static String toString(byte[] source) {
		return BYTES_TO_STRING.convert(source);
	}

	public static ScriptOutputType toScriptOutputType(ReturnType returnType) {

		switch (returnType) {
			case BOOLEAN:
				return ScriptOutputType.BOOLEAN;
			case MULTI:
				return ScriptOutputType.MULTI;
			case VALUE:
				return ScriptOutputType.VALUE;
			case INTEGER:
				return ScriptOutputType.INTEGER;
			case STATUS:
				return ScriptOutputType.STATUS;
			default:
				throw new IllegalArgumentException("Return type " + returnType + " is not a supported script output type");
		}
	}

	public static boolean toBoolean(Position where) {
		Assert.notNull(where, "list positions are mandatory");
		return (Position.AFTER.equals(where) ? false : true);
	}

	public static int toInt(boolean value) {
		return (value ? 1 : 0);
	}

	public static Map<byte[], byte[]> toMap(List<byte[]> source) {
		return BYTES_LIST_TO_MAP.convert(source);
	}

	public static Converter<List<byte[]>, Map<byte[], byte[]>> bytesListToMapConverter() {
		return BYTES_LIST_TO_MAP;
	}

	public static SortArgs toSortArgs(SortParameters params) {

		SortArgs args = new SortArgs();
		if (params == null) {
			return args;
		}
		if (params.getByPattern() != null) {
			args.by(new String(params.getByPattern(), LettuceCharsets.ASCII));
		}
		if (params.getLimit() != null) {
			args.limit(params.getLimit().getStart(), params.getLimit().getCount());
		}
		if (params.getGetPattern() != null) {
			byte[][] pattern = params.getGetPattern();
			for (byte[] bs : pattern) {
				args.get(new String(bs, LettuceCharsets.ASCII));
			}
		}
		if (params.getOrder() != null) {
			if (params.getOrder() == Order.ASC) {
				args.asc();
			} else {
				args.desc();
			}
		}
		Boolean isAlpha = params.isAlphabetic();
		if (isAlpha != null && isAlpha) {
			args.alpha();
		}
		return args;
	}

	public static List<RedisClientInfo> toListOfRedisClientInformation(String clientList) {
		return stringToRedisClientListConverter().convert(clientList);
	}

	public static byte[][] subarray(byte[][] input, int index) {

		if (input.length > index) {
			byte[][] output = new byte[input.length - index][];
			System.arraycopy(input, index, output, 0, output.length);
			return output;
		}

		return null;
	}

	public static String boundaryToStringForZRange(Boundary boundary, String defaultValue) {

		if (boundary == null || boundary.getValue() == null) {
			return defaultValue;
		}

		return boundaryToString(boundary, "", "(");
	}

	private static String boundaryToString(Boundary boundary, String inclPrefix, String exclPrefix) {

		String prefix = boundary.isIncluding() ? inclPrefix : exclPrefix;
		String value = null;
		if (boundary.getValue() instanceof byte[]) {
			value = toString((byte[]) boundary.getValue());
		} else {
			value = boundary.getValue().toString();
		}

		return prefix + value;
	}

	/**
	 * Convert a {@link org.springframework.data.redis.connection.RedisZSetCommands.Limit} to a Lettuce
	 * {@link io.lettuce.core.Limit}.
	 *
	 * @param limit
	 * @return a lettuce {@link io.lettuce.core.Limit}.
	 * @since 2.0
	 */
	public static io.lettuce.core.Limit toLimit(RedisZSetCommands.Limit limit) {
		return limit.isUnlimited() ? Limit.unlimited() : Limit.create(limit.getOffset(), limit.getCount());
	}

	/**
	 * Convert a {@link org.springframework.data.redis.connection.RedisZSetCommands.Range} to a lettuce {@link Range}.
	 *
	 * @param range
	 * @return
	 * @since 2.0
	 */
	public static <T> Range<T> toRange(org.springframework.data.redis.connection.RedisZSetCommands.Range range) {
		return Range.from(lowerBoundaryOf(range, false), upperBoundaryOf(range, false));
	}

	/**
	 * Convert a {@link org.springframework.data.redis.connection.RedisZSetCommands.Range} to a lettuce {@link Range}.
	 *
	 * @param range
	 * @param convertNumberToBytes
	 * @return
	 * @since 2.2
	 */
	public static <T> Range<T> toRange(org.springframework.data.redis.connection.RedisZSetCommands.Range range,
			boolean convertNumberToBytes) {
		return Range.from(lowerBoundaryOf(range, convertNumberToBytes), upperBoundaryOf(range, convertNumberToBytes));
	}

	/**
	 * Convert a {@link org.springframework.data.redis.connection.RedisZSetCommands.Range} to a lettuce {@link Range} and
	 * reverse boundaries.
	 *
	 * @param range
	 * @return
	 * @since 2.0
	 */
	public static <T> Range<T> toRevRange(org.springframework.data.redis.connection.RedisZSetCommands.Range range) {
		return Range.from(upperBoundaryOf(range, false), lowerBoundaryOf(range, false));
	}

	@SuppressWarnings("unchecked")
	private static <T> Range.Boundary<T> lowerBoundaryOf(
			org.springframework.data.redis.connection.RedisZSetCommands.Range range, boolean convertNumberToBytes) {
		return (Range.Boundary<T>) rangeToBoundaryArgumentConverter(false, convertNumberToBytes).convert(range);
	}

	@SuppressWarnings("unchecked")
	private static <T> Range.Boundary<T> upperBoundaryOf(
			org.springframework.data.redis.connection.RedisZSetCommands.Range range, boolean convertNumberToBytes) {
		return (Range.Boundary<T>) rangeToBoundaryArgumentConverter(true, convertNumberToBytes).convert(range);
	}

	private static Converter<org.springframework.data.redis.connection.RedisZSetCommands.Range, Range.Boundary<?>> rangeToBoundaryArgumentConverter(
			boolean upper, boolean convertNumberToBytes) {

		return (source) -> {

			Boundary sourceBoundary = upper ? source.getMax() : source.getMin();
			if (sourceBoundary == null || sourceBoundary.getValue() == null) {
				return Range.Boundary.unbounded();
			}

			boolean inclusive = sourceBoundary.isIncluding();
			Object value = sourceBoundary.getValue();

			if (value instanceof Number) {

				if (convertNumberToBytes) {
					value = value.toString();
				} else {
					return inclusive ? Range.Boundary.including((Number) value) : Range.Boundary.excluding((Number) value);
				}
			}

			if (value instanceof String) {

				if (!StringUtils.hasText((String) value) || ObjectUtils.nullSafeEquals(value, "+")
						|| ObjectUtils.nullSafeEquals(value, "-")) {
					return Range.Boundary.unbounded();
				}
				return inclusive ? Range.Boundary.including(value.toString().getBytes(LettuceCharsets.UTF8))
						: Range.Boundary.excluding(value.toString().getBytes(LettuceCharsets.UTF8));
			}

			return inclusive ? Range.Boundary.including((byte[]) value) : Range.Boundary.excluding((byte[]) value);
		};
	}

	/**
	 * @param source List of Maps containing node details from SENTINEL SLAVES or SENTINEL MASTERS. May be empty or
	 *          {@literal null}.
	 * @return List of {@link RedisServer}'s. List is empty if List of Maps is empty.
	 * @since 1.5
	 */
	public static List<RedisServer> toListOfRedisServer(List<Map<String, String>> source) {

		if (CollectionUtils.isEmpty(source)) {
			return Collections.emptyList();
		}

		List<RedisServer> sentinels = new ArrayList<>();
		for (Map<String, String> info : source) {
			sentinels.add(RedisServer.newServerFrom(Converters.toProperties(info)));
		}
		return sentinels;
	}

	/**
	 * @param sentinelConfiguration the sentinel configuration containing one or more sentinels and a master name. Must
	 *          not be {@literal null}
	 * @return A {@link RedisURI} containing Redis Sentinel addresses of {@link RedisSentinelConfiguration}
	 * @since 1.5
	 */
	public static RedisURI sentinelConfigurationToRedisURI(RedisSentinelConfiguration sentinelConfiguration) {

		Assert.notNull(sentinelConfiguration, "RedisSentinelConfiguration is required");

		Set<RedisNode> sentinels = sentinelConfiguration.getSentinels();
		RedisPassword sentinelPassword = sentinelConfiguration.getSentinelPassword();
		RedisURI.Builder builder = RedisURI.builder();
		for (RedisNode sentinel : sentinels) {

			RedisURI.Builder sentinelBuilder = RedisURI.Builder.redis(sentinel.getHost(), sentinel.getPort());

			if (sentinelPassword.isPresent()) {
				sentinelBuilder.withPassword(sentinelPassword.get());
			}
			builder.withSentinel(sentinelBuilder.build());
		}

		RedisPassword password = sentinelConfiguration.getPassword();
		if (password.isPresent()) {
			builder.withPassword(password.get());
		}

		builder.withSentinelMasterId(sentinelConfiguration.getMaster().getName());

		return builder.build();
	}

	public static byte[] toBytes(String source) {
		return STRING_TO_BYTES.convert(source);
	}

	public static byte[] toBytes(Integer source) {
		return String.valueOf(source).getBytes();
	}

	public static byte[] toBytes(Long source) {
		return String.valueOf(source).getBytes();
	}

	/**
	 * @param source
	 * @return
	 * @since 1.6
	 */
	public static byte[] toBytes(Double source) {
		return toBytes(String.valueOf(source));
	}

	/**
	 * Converts a given {@link Boundary} to its binary representation suitable for {@literal ZRANGEBY*} commands, despite
	 * {@literal ZRANGEBYLEX}.
	 *
	 * @param boundary
	 * @param defaultValue
	 * @return
	 * @since 1.6
	 */
	public static String boundaryToBytesForZRange(Boundary boundary, byte[] defaultValue) {

		if (boundary == null || boundary.getValue() == null) {
			return toString(defaultValue);
		}

		return boundaryToBytes(boundary, new byte[] {}, toBytes("("));
	}

	/**
	 * Converts a given {@link Boundary} to its binary representation suitable for ZRANGEBYLEX command.
	 *
	 * @param boundary
	 * @return
	 * @since 1.6
	 */
	public static String boundaryToBytesForZRangeByLex(Boundary boundary, byte[] defaultValue) {

		if (boundary == null || boundary.getValue() == null) {
			return toString(defaultValue);
		}

		return boundaryToBytes(boundary, toBytes("["), toBytes("("));
	}

	private static String boundaryToBytes(Boundary boundary, byte[] inclPrefix, byte[] exclPrefix) {

		byte[] prefix = boundary.isIncluding() ? inclPrefix : exclPrefix;
		byte[] value = null;
		if (boundary.getValue() instanceof byte[]) {
			value = (byte[]) boundary.getValue();
		} else if (boundary.getValue() instanceof Double) {
			value = toBytes((Double) boundary.getValue());
		} else if (boundary.getValue() instanceof Long) {
			value = toBytes((Long) boundary.getValue());
		} else if (boundary.getValue() instanceof Integer) {
			value = toBytes((Integer) boundary.getValue());
		} else if (boundary.getValue() instanceof String) {
			value = toBytes((String) boundary.getValue());
		} else {
			throw new IllegalArgumentException(String.format("Cannot convert %s to binary format", boundary.getValue()));
		}

		ByteBuffer buffer = ByteBuffer.allocate(prefix.length + value.length);
		buffer.put(prefix);
		buffer.put(value);
		return toString(ByteUtils.getBytes(buffer));
	}

	public static List<RedisClusterNode> partitionsToClusterNodes(Partitions partitions) {
		return PARTITIONS_TO_CLUSTER_NODES.convert(partitions);
	}

	/**
	 * @param source
	 * @return
	 * @since 1.7
	 */
	public static RedisClusterNode toRedisClusterNode(io.lettuce.core.cluster.models.partitions.RedisClusterNode source) {
		return CLUSTER_NODE_TO_CLUSTER_NODE_CONVERTER.convert(source);
	}

	/**
	 * Converts a given {@link Expiration} and {@link SetOption} to the according {@link SetArgs}.<br />
	 *
	 * @param expiration can be {@literal null}.
	 * @param option can be {@literal null}.
	 * @since 1.7
	 */
	public static SetArgs toSetArgs(@Nullable Expiration expiration, @Nullable SetOption option) {

		SetArgs args = new SetArgs();
		if (expiration != null && !expiration.isPersistent()) {

			switch (expiration.getTimeUnit()) {
				case SECONDS:
					args.ex(expiration.getExpirationTime());
					break;
				default:
					args.px(expiration.getConverted(TimeUnit.MILLISECONDS));
					break;
			}
		}

		if (option != null) {

			switch (option) {
				case SET_IF_ABSENT:
					args.nx();
					break;
				case SET_IF_PRESENT:
					args.xx();
					break;
				default:
					break;
			}
		}
		return args;
	}

	static Converter<List<byte[]>, Long> toTimeConverter() {
		return BYTES_LIST_TO_TIME_CONVERTER;
	}

	/**
	 * Convert {@link Metric} into {@link GeoArgs.Unit}.
	 *
	 * @param metric
	 * @return
	 * @since 1.8
	 */
	public static GeoArgs.Unit toGeoArgsUnit(Metric metric) {

		Metric metricToUse = metric == null || ObjectUtils.nullSafeEquals(Metrics.NEUTRAL, metric) ? DistanceUnit.METERS
				: metric;
		return ObjectUtils.caseInsensitiveValueOf(GeoArgs.Unit.values(), metricToUse.getAbbreviation());
	}

	/**
	 * Convert {@link GeoRadiusCommandArgs} into {@link GeoArgs}.
	 *
	 * @param args
	 * @return
	 * @since 1.8
	 */
	public static GeoArgs toGeoArgs(GeoRadiusCommandArgs args) {

		GeoArgs geoArgs = new GeoArgs();

		if (args.hasFlags()) {
			for (GeoRadiusCommandArgs.Flag flag : args.getFlags()) {
				switch (flag) {
					case WITHCOORD:
						geoArgs.withCoordinates();
						break;
					case WITHDIST:
						geoArgs.withDistance();
						break;
				}
			}
		}

		if (args.hasSortDirection()) {
			switch (args.getSortDirection()) {
				case ASC:
					geoArgs.asc();
					break;
				case DESC:
					geoArgs.desc();
					break;
			}
		}

		if (args.hasLimit()) {
			geoArgs.withCount(args.getLimit());
		}
		return geoArgs;
	}

	/**
	 * Convert {@link BitFieldSubCommands} into {@link BitFieldArgs}.
	 *
	 * @param subCommands
	 * @return
	 * @since 2.1
	 */
	public static BitFieldArgs toBitFieldArgs(BitFieldSubCommands subCommands) {

		BitFieldArgs args = new BitFieldArgs();

		for (BitFieldSubCommand subCommand : subCommands) {

			BitFieldArgs.BitFieldType bft = subCommand.getType().isSigned()
					? BitFieldArgs.signed(subCommand.getType().getBits())
					: BitFieldArgs.unsigned(subCommand.getType().getBits());

			BitFieldArgs.Offset offset;
			if (subCommand.getOffset().isZeroBased()) {
				offset = BitFieldArgs.offset((int) subCommand.getOffset().getValue());
			} else {
				offset = BitFieldArgs.typeWidthBasedOffset((int) subCommand.getOffset().getValue());
			}

			if (subCommand instanceof BitFieldGet) {
				args = args.get(bft, offset);
			} else if (subCommand instanceof BitFieldSet) {
				args = args.set(bft, offset, ((BitFieldSet) subCommand).getValue());
			} else if (subCommand instanceof BitFieldIncrBy) {

				BitFieldIncrBy.Overflow overflow = ((BitFieldIncrBy) subCommand).getOverflow();
				if (overflow != null) {

					BitFieldArgs.OverflowType type;

					switch (overflow) {
						case SAT:
							type = BitFieldArgs.OverflowType.SAT;
							break;
						case FAIL:
							type = BitFieldArgs.OverflowType.FAIL;
							break;
						case WRAP:
							type = BitFieldArgs.OverflowType.WRAP;
							break;
						default:
							throw new IllegalArgumentException(
									String.format("Invalid OVERFLOW. Expected one the following %s but got %s.",
											Arrays.toString(Overflow.values()), overflow));
					}
					args = args.overflow(type);
				}

				args = args.incrBy(bft, (int) subCommand.getOffset().getValue(), ((BitFieldIncrBy) subCommand).getValue());
			}
		}

		return args;
	}

	/**
	 * Convert {@link ScanOptions} to {@link ScanArgs}.
	 *
	 * @param options the {@link ScanOptions} to convert, may be {@literal null}.
	 * @return the converted {@link ScanArgs}. Returns {@literal null} if {@link ScanOptions} is {@literal null}.
	 * @see 2.1
	 */
	@Nullable
	static ScanArgs toScanArgs(@Nullable ScanOptions options) {

		if (options == null) {
			return null;
		}

		ScanArgs scanArgs = new ScanArgs();

		if (options.getPattern() != null) {
			scanArgs.match(options.getPattern());
		}

		if (options.getCount() != null) {
			scanArgs.limit(options.getCount());
		}

		return scanArgs;
	}

	/**
	 * Get {@link Converter} capable of {@link Set} of {@link Byte} into {@link GeoResults}.
	 *
	 * @return
	 * @since 1.8
	 */
	public static Converter<Set<byte[]>, GeoResults<GeoLocation<byte[]>>> bytesSetToGeoResultsConverter() {

		return source -> {

			if (CollectionUtils.isEmpty(source)) {
				return new GeoResults<>(Collections.<GeoResult<GeoLocation<byte[]>>> emptyList());
			}

			List<GeoResult<GeoLocation<byte[]>>> results = new ArrayList<>(source.size());
			Iterator<byte[]> it = source.iterator();
			while (it.hasNext()) {
				results.add(new GeoResult<>(new GeoLocation<>(it.next(), null), new Distance(0D)));
			}
			return new GeoResults<>(results);
		};
	}

	/**
	 * Get {@link Converter} capable of convering {@link GeoWithin} into {@link GeoResults}.
	 *
	 * @param metric
	 * @return
	 * @since 1.8
	 */
	public static Converter<List<GeoWithin<byte[]>>, GeoResults<GeoLocation<byte[]>>> geoRadiusResponseToGeoResultsConverter(
			Metric metric) {
		return GeoResultsConverterFactory.INSTANCE.forMetric(metric);
	}

	/**
	 * @return
	 * @since 1.8
	 */
	public static ListConverter<io.lettuce.core.GeoCoordinates, Point> geoCoordinatesToPointConverter() {
		return GEO_COORDINATE_LIST_TO_POINT_LIST_CONVERTER;
	}

	/**
	 * @return
	 * @since 2.0
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> ListConverter<KeyValue<K, V>, V> keyValueListUnwrapper() {
		return (ListConverter) KEY_VALUE_LIST_UNWRAPPER;
	}

	public static Converter<TransactionResult, List<Object>> transactionResultUnwrapper() {
		return TRANSACTION_RESULT_UNWRAPPER;
	}

	/**
	 * Return {@link Optional} lower bound from {@link Range}.
	 *
	 * @param range
	 * @param <T>
	 * @return
	 * @since 2.0.9
	 */
	static <T extends Comparable<T>> Optional<T> getLowerBound(org.springframework.data.domain.Range<T> range) {
		return range.getLowerBound().getValue();
	}

	/**
	 * Return {@link Optional} upper bound from {@link Range}.
	 *
	 * @param range
	 * @param <T>
	 * @return
	 * @since 2.0.9
	 */
	static <T extends Comparable<T>> Optional<T> getUpperBound(org.springframework.data.domain.Range<T> range) {
		return range.getUpperBound().getValue();
	}

	/**
	 * Return the lower bound index from {@link Range} or {@literal 0} (zero) if the lower range is not bounded to point
	 * to the first element. To be used with index-based commands such as {@code LRANGE}, {@code GETRANGE}.
	 *
	 * @param range
	 * @return the lower index bound value or {@literal 0} for the first element if not bounded.
	 * @since 2.0.9
	 */
	static long getLowerBoundIndex(org.springframework.data.domain.Range<Long> range) {
		return getLowerBound(range).orElse(INDEXED_RANGE_START);
	}

	/**
	 * Return the upper bound index from {@link Range} or {@literal -1} (minus one) if the upper range is not bounded to
	 * point to the last element. To be used with index-based commands such as {@code LRANGE}, {@code GETRANGE}.
	 *
	 * @param range
	 * @return the upper index bound value or {@literal -1} for the last element if not bounded.
	 * @since 2.0.9
	 */
	static long getUpperBoundIndex(org.springframework.data.domain.Range<Long> range) {
		return getUpperBound(range).orElse(INDEXED_RANGE_END);
	}

	/**
	 * @author Christoph Strobl
	 * @since 1.8
	 */
	static enum GeoResultsConverterFactory {

		INSTANCE;

		Converter<List<GeoWithin<byte[]>>, GeoResults<GeoLocation<byte[]>>> forMetric(Metric metric) {
			return new GeoResultsConverter(
					metric == null || ObjectUtils.nullSafeEquals(Metrics.NEUTRAL, metric) ? DistanceUnit.METERS : metric);
		}

		private static class GeoResultsConverter
				implements Converter<List<GeoWithin<byte[]>>, GeoResults<GeoLocation<byte[]>>> {

			private Metric metric;

			public GeoResultsConverter(Metric metric) {
				this.metric = metric;
			}

			@Override
			public GeoResults<GeoLocation<byte[]>> convert(List<GeoWithin<byte[]>> source) {

				List<GeoResult<GeoLocation<byte[]>>> results = new ArrayList<>(source.size());

				Converter<GeoWithin<byte[]>, GeoResult<GeoLocation<byte[]>>> converter = GeoResultConverterFactory.INSTANCE
						.forMetric(metric);
				for (GeoWithin<byte[]> result : source) {
					results.add(converter.convert(result));
				}

				return new GeoResults<>(results, metric);
			}
		}
	}

	/**
	 * @author Christoph Strobl
	 * @since 1.8
	 */
	static enum GeoResultConverterFactory {

		INSTANCE;

		Converter<GeoWithin<byte[]>, GeoResult<GeoLocation<byte[]>>> forMetric(Metric metric) {
			return new GeoResultConverter(metric);
		}

		private static class GeoResultConverter implements Converter<GeoWithin<byte[]>, GeoResult<GeoLocation<byte[]>>> {

			private Metric metric;

			public GeoResultConverter(Metric metric) {
				this.metric = metric;
			}

			@Override
			public GeoResult<GeoLocation<byte[]>> convert(GeoWithin<byte[]> source) {

				Point point = GEO_COORDINATE_TO_POINT_CONVERTER.convert(source.getCoordinates());

				return new GeoResult<>(new GeoLocation<>(source.getMember(), point),
						new Distance(source.getDistance() != null ? source.getDistance() : 0D, metric));
			}
		}
	}
}
