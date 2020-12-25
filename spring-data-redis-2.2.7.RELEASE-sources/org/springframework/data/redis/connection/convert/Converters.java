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
package org.springframework.data.redis.connection.convert;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisClusterNode.Flag;
import org.springframework.data.redis.connection.RedisClusterNode.LinkState;
import org.springframework.data.redis.connection.RedisClusterNode.RedisClusterNodeBuilder;
import org.springframework.data.redis.connection.RedisClusterNode.SlotRange;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisNode.NodeType;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Common type converters
 *
 * @author Jennifer Hickey
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author Christoph Strobl
 */
abstract public class Converters {

	private static final byte[] ONE = new byte[] { '1' };
	private static final byte[] ZERO = new byte[] { '0' };
	private static final String CLUSTER_NODES_LINE_SEPARATOR = "\n";
	private static final Converter<String, Properties> STRING_TO_PROPS = new StringToPropertiesConverter();
	private static final Converter<Long, Boolean> LONG_TO_BOOLEAN = new LongToBooleanConverter();
	private static final Converter<String, DataType> STRING_TO_DATA_TYPE = new StringToDataTypeConverter();
	private static final Converter<Map<?, ?>, Properties> MAP_TO_PROPERTIES = MapToPropertiesConverter.INSTANCE;
	private static final Converter<String, RedisClusterNode> STRING_TO_CLUSTER_NODE_CONVERTER;
	private static final Converter<List<String>, Properties> STRING_LIST_TO_PROPERTIES_CONVERTER;
	private static final Map<String, Flag> flagLookupMap;

	static {

		flagLookupMap = new LinkedHashMap<>(Flag.values().length, 1);
		for (Flag flag : Flag.values()) {
			flagLookupMap.put(flag.getRaw(), flag);
		}

		STRING_TO_CLUSTER_NODE_CONVERTER = new Converter<String, RedisClusterNode>() {

			static final int ID_INDEX = 0;
			static final int HOST_PORT_INDEX = 1;
			static final int FLAGS_INDEX = 2;
			static final int MASTER_ID_INDEX = 3;
			static final int LINK_STATE_INDEX = 7;
			static final int SLOTS_INDEX = 8;

			@Override
			public RedisClusterNode convert(String source) {

				String[] args = source.split(" ");
				String[] hostAndPort = StringUtils.split(args[HOST_PORT_INDEX], ":");

				Assert.notNull(hostAndPort, "CusterNode information does not define host and port!");

				SlotRange range = parseSlotRange(args);
				Set<Flag> flags = parseFlags(args);

				String portPart = hostAndPort[1];
				if (portPart.contains("@")) {
					portPart = portPart.substring(0, portPart.indexOf('@'));
				}

				RedisClusterNodeBuilder nodeBuilder = RedisClusterNode.newRedisClusterNode()
						.listeningAt(hostAndPort[0], Integer.valueOf(portPart)) //
						.withId(args[ID_INDEX]) //
						.promotedAs(flags.contains(Flag.MASTER) ? NodeType.MASTER : NodeType.SLAVE) //
						.serving(range) //
						.withFlags(flags) //
						.linkState(parseLinkState(args));

				if (!args[MASTER_ID_INDEX].isEmpty() && !args[MASTER_ID_INDEX].startsWith("-")) {
					nodeBuilder.slaveOf(args[MASTER_ID_INDEX]);
				}

				return nodeBuilder.build();
			}

			private Set<Flag> parseFlags(String[] args) {

				String raw = args[FLAGS_INDEX];

				Set<Flag> flags = new LinkedHashSet<>(8, 1);
				if (StringUtils.hasText(raw)) {
					for (String flag : raw.split(",")) {
						flags.add(flagLookupMap.get(flag));
					}
				}
				return flags;
			}

			private LinkState parseLinkState(String[] args) {

				String raw = args[LINK_STATE_INDEX];

				if (StringUtils.hasText(raw)) {
					return LinkState.valueOf(raw.toUpperCase());
				}
				return LinkState.DISCONNECTED;
			}

			private SlotRange parseSlotRange(String[] args) {

				Set<Integer> slots = new LinkedHashSet<>();

				for (int i = SLOTS_INDEX; i < args.length; i++) {

					String raw = args[i];

					if (raw.startsWith("[")) {
						continue;
					}

					if (raw.contains("-")) {
						String[] slotRange = StringUtils.split(raw, "-");

						if (slotRange != null) {
							int from = Integer.valueOf(slotRange[0]);
							int to = Integer.valueOf(slotRange[1]);
							for (int slot = from; slot <= to; slot++) {
								slots.add(slot);
							}
						}
					} else {
						slots.add(Integer.valueOf(raw));
					}
				}

				return new SlotRange(slots);
			}

		};

		STRING_LIST_TO_PROPERTIES_CONVERTER = input -> {

			Assert.notNull(input, "Input list must not be null!");
			Assert.isTrue(input.size() % 2 == 0, "Input list must contain an even number of entries!");

			Properties properties = new Properties();

			for (int i = 0; i < input.size(); i += 2) {

				properties.setProperty(input.get(i), input.get(i + 1));
			}

			return properties;
		};
	}

	public static Boolean stringToBoolean(String s) {
		return stringToBooleanConverter().convert(s);
	}

	public static Converter<String, Boolean> stringToBooleanConverter() {
		return (source) -> ObjectUtils.nullSafeEquals("OK", source);
	}

	public static Converter<String, Properties> stringToProps() {
		return STRING_TO_PROPS;
	}

	public static Converter<Long, Boolean> longToBoolean() {
		return LONG_TO_BOOLEAN;
	}

	public static Converter<String, DataType> stringToDataType() {
		return STRING_TO_DATA_TYPE;
	}

	public static Properties toProperties(String source) {
		return STRING_TO_PROPS.convert(source);
	}

	public static Properties toProperties(Map<?, ?> source) {

		Properties properties = MAP_TO_PROPERTIES.convert(source);
		return properties != null ? properties : new Properties();
	}

	public static Boolean toBoolean(Long source) {
		return LONG_TO_BOOLEAN.convert(source);
	}

	public static DataType toDataType(String source) {
		return STRING_TO_DATA_TYPE.convert(source);
	}

	public static byte[] toBit(Boolean source) {
		return (source ? ONE : ZERO);
	}

	/**
	 * Converts the result of a single line of {@code CLUSTER NODES} into a {@link RedisClusterNode}.
	 *
	 * @param clusterNodesLine
	 * @return
	 * @since 1.7
	 */
	protected static RedisClusterNode toClusterNode(String clusterNodesLine) {
		return STRING_TO_CLUSTER_NODE_CONVERTER.convert(clusterNodesLine);
	}

	/**
	 * Converts lines from the result of {@code CLUSTER NODES} into {@link RedisClusterNode}s.
	 *
	 * @param lines
	 * @return
	 * @since 1.7
	 */
	public static Set<RedisClusterNode> toSetOfRedisClusterNodes(Collection<String> lines) {

		if (CollectionUtils.isEmpty(lines)) {
			return Collections.emptySet();
		}

		Set<RedisClusterNode> nodes = new LinkedHashSet<>(lines.size());

		for (String line : lines) {
			nodes.add(toClusterNode(line));
		}

		return nodes;
	}

	/**
	 * Converts the result of {@code CLUSTER NODES} into {@link RedisClusterNode}s.
	 *
	 * @param clusterNodes
	 * @return
	 * @since 1.7
	 */
	public static Set<RedisClusterNode> toSetOfRedisClusterNodes(String clusterNodes) {

		if (StringUtils.isEmpty(clusterNodes)) {
			return Collections.emptySet();
		}

		String[] lines = clusterNodes.split(CLUSTER_NODES_LINE_SEPARATOR);
		return toSetOfRedisClusterNodes(Arrays.asList(lines));
	}

	public static List<Object> toObjects(Set<Tuple> tuples) {
		List<Object> tupleArgs = new ArrayList<>(tuples.size() * 2);
		for (Tuple tuple : tuples) {
			tupleArgs.add(tuple.getScore());
			tupleArgs.add(tuple.getValue());
		}
		return tupleArgs;
	}

	/**
	 * Returns the timestamp constructed from the given {@code seconds} and {@code microseconds}.
	 *
	 * @param seconds server time in seconds
	 * @param microseconds elapsed microseconds in current second
	 * @return
	 */
	public static Long toTimeMillis(String seconds, String microseconds) {
		return NumberUtils.parseNumber(seconds, Long.class) * 1000L
				+ NumberUtils.parseNumber(microseconds, Long.class) / 1000L;
	}

	/**
	 * Converts {@code seconds} to the given {@link TimeUnit}.
	 *
	 * @param seconds
	 * @param targetUnit must not be {@literal null}.
	 * @return
	 * @since 1.8
	 */
	public static long secondsToTimeUnit(long seconds, TimeUnit targetUnit) {

		Assert.notNull(targetUnit, "TimeUnit must not be null!");

		if (seconds > 0) {
			return targetUnit.convert(seconds, TimeUnit.SECONDS);
		}

		return seconds;
	}

	/**
	 * Creates a new {@link Converter} to convert from seconds to the given {@link TimeUnit}.
	 *
	 * @param timeUnit muist not be {@literal null}.
	 * @return
	 * @since 1.8
	 */
	public static Converter<Long, Long> secondsToTimeUnit(final TimeUnit timeUnit) {

		return seconds -> secondsToTimeUnit(seconds, timeUnit);
	}

	/**
	 * Converts {@code milliseconds} to the given {@link TimeUnit}.
	 *
	 * @param milliseconds
	 * @param targetUnit must not be {@literal null}.
	 * @return
	 * @since 1.8
	 */
	public static long millisecondsToTimeUnit(long milliseconds, TimeUnit targetUnit) {

		Assert.notNull(targetUnit, "TimeUnit must not be null!");

		if (milliseconds > 0) {
			return targetUnit.convert(milliseconds, TimeUnit.MILLISECONDS);
		}

		return milliseconds;
	}

	/**
	 * Creates a new {@link Converter} to convert from milliseconds to the given {@link TimeUnit}.
	 *
	 * @param timeUnit muist not be {@literal null}.
	 * @return
	 * @since 1.8
	 */
	public static Converter<Long, Long> millisecondsToTimeUnit(final TimeUnit timeUnit) {

		return seconds -> millisecondsToTimeUnit(seconds, timeUnit);
	}

	/**
	 * {@link Converter} capable of deserializing {@link GeoResults}.
	 *
	 * @param serializer
	 * @return
	 * @since 1.8
	 */
	public static <V> Converter<GeoResults<GeoLocation<byte[]>>, GeoResults<GeoLocation<V>>> deserializingGeoResultsConverter(
			RedisSerializer<V> serializer) {
		return new DeserializingGeoResultsConverter<>(serializer);
	}

	/**
	 * {@link Converter} capable of converting Double into {@link Distance} using given {@link Metric}.
	 *
	 * @param metric
	 * @return
	 * @since 1.8
	 */
	public static Converter<Double, Distance> distanceConverterForMetric(Metric metric) {
		return DistanceConverterFactory.INSTANCE.forMetric(metric);
	}

	/**
	 * Converts array outputs with key-value sequences (such as produced by {@code CONFIG GET}) from a {@link List} to
	 * {@link Properties}.
	 *
	 * @param input must not be {@literal null}.
	 * @return the mapped result.
	 * @since 2.0
	 */
	public static Properties toProperties(List<String> input) {
		return STRING_LIST_TO_PROPERTIES_CONVERTER.convert(input);
	}

	/**
	 * Returns a converter to convert array outputs with key-value sequences (such as produced by {@code CONFIG GET}) from
	 * a {@link List} to {@link Properties}.
	 *
	 * @return the converter.
	 * @since 2.0
	 */
	public static Converter<List<String>, Properties> listToPropertiesConverter() {
		return STRING_LIST_TO_PROPERTIES_CONVERTER;
	}

	/**
	 * Returns a converter to convert from {@link Map} to {@link Properties}.
	 *
	 * @return the converter.
	 * @since 2.0
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Converter<Map<K, V>, Properties> mapToPropertiesConverter() {
		return (Converter) MAP_TO_PROPERTIES;
	}

	/**
	 * Convert the given {@literal nullable seconds} to a {@link Duration} or {@literal null}.
	 *
	 * @param seconds can be {@literal null}.
	 * @return given {@literal seconds} as {@link Duration} or {@literal null}.
	 * @since 2.1
	 */
	@Nullable
	public static Duration secondsToDuration(@Nullable Long seconds) {
		return seconds != null ? Duration.ofSeconds(seconds) : null;

	}

	/**
	 * @author Christoph Strobl
	 * @since 1.8
	 */
	enum DistanceConverterFactory {

		INSTANCE;

		/**
		 * @param metric can be {@literal null}. Defaults to {@link DistanceUnit#METERS}.
		 * @return never {@literal null}.
		 */
		DistanceConverter forMetric(@Nullable Metric metric) {
			return new DistanceConverter(
					metric == null || ObjectUtils.nullSafeEquals(Metrics.NEUTRAL, metric) ? DistanceUnit.METERS : metric);
		}

		static class DistanceConverter implements Converter<Double, Distance> {

			private Metric metric;

			/**
			 * @param metric can be {@literal null}. Defaults to {@link DistanceUnit#METERS}.
			 * @return never {@literal null}.
			 */
			DistanceConverter(@Nullable Metric metric) {
				this.metric = metric == null || ObjectUtils.nullSafeEquals(Metrics.NEUTRAL, metric) ? DistanceUnit.METERS
						: metric;
			}

			/*
			 * (non-Javadoc)
			 * @see org.springframework.core.convert.converter.Converter#convert(Object)
			 */
			@Override
			public Distance convert(Double source) {
				return new Distance(source, metric);
			}
		}
	}

	/**
	 * @author Christoph Strobl
	 * @param <V>
	 * @since 1.8
	 */
	@RequiredArgsConstructor
	static class DeserializingGeoResultsConverter<V>
			implements Converter<GeoResults<GeoLocation<byte[]>>, GeoResults<GeoLocation<V>>> {

		final RedisSerializer<V> serializer;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.convert.converter.Converter#convert(Object)
		 */
		@Override
		public GeoResults<GeoLocation<V>> convert(GeoResults<GeoLocation<byte[]>> source) {

			List<GeoResult<GeoLocation<V>>> values = new ArrayList<>(source.getContent().size());
			for (GeoResult<GeoLocation<byte[]>> value : source.getContent()) {

				values.add(new GeoResult<>(
						new GeoLocation<>(serializer.deserialize(value.getContent().getName()), value.getContent().getPoint()),
						value.getDistance()));
			}

			return new GeoResults<>(values, source.getAverageDistance().getMetric());
		}
	}
}
