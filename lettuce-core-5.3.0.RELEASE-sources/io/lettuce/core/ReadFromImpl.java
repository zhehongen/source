/*
 * Copyright 2011-2020 the original author or authors.
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
package io.lettuce.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import io.lettuce.core.internal.LettuceLists;
import io.lettuce.core.models.role.RedisInstance;
import io.lettuce.core.models.role.RedisNodeDescription;

/**
 * Collection of common read setting implementations.
 *
 * @author Mark Paluch
 * @since 4.0
 */
class ReadFromImpl {

    private static final Predicate<RedisNodeDescription> IS_MASTER = node -> node.getRole() == RedisInstance.Role.MASTER;

    private static final Predicate<RedisNodeDescription> IS_REPLICA = node -> node.getRole() == RedisInstance.Role.SLAVE;

    /**
     * Read from master only.
     */
    static final class ReadFromMaster extends ReadFrom {

        @Override
        public List<RedisNodeDescription> select(Nodes nodes) {

            for (RedisNodeDescription node : nodes) {
                if (node.getRole() == RedisInstance.Role.MASTER) {
                    return LettuceLists.newList(node);
                }
            }

            return Collections.emptyList();
        }
    }

    /**
     * Read from master and replicas. Prefer master reads and fall back to replicas if the master is not available.
     */
    static final class ReadFromMasterPreferred extends OrderedPredicateReadFromAdapter {

        ReadFromMasterPreferred() {
            super(IS_MASTER, IS_REPLICA);
        }
    }

    /**
     * Read from replica only.
     */
    static final class ReadFromReplica extends OrderedPredicateReadFromAdapter {

        ReadFromReplica() {
            super(IS_REPLICA);
        }
    }

    /**
     * Read from master and replicas. Prefer replica reads and fall back to master if the no replica is not available.
     */
    static final class ReadFromReplicaPreferred extends OrderedPredicateReadFromAdapter {

        ReadFromReplicaPreferred() {
            super(IS_REPLICA, IS_MASTER);
        }
    }

    /**
     * Read from nearest node.
     */
    static final class ReadFromNearest extends ReadFrom {

        @Override
        public List<RedisNodeDescription> select(Nodes nodes) {
            return nodes.getNodes();
        }

        @Override
        protected boolean isOrderSensitive() {
            return true;
        }
    }

    /**
     * Read from any node.
     */
    static final class ReadFromAnyNode extends UnorderedPredicateReadFromAdapter {

        public ReadFromAnyNode() {
            super(x -> true);
        }
    }

    /**
     * {@link Predicate}-based {@link ReadFrom} implementation.
     *
     * @since 5.2
     */
    static class OrderedPredicateReadFromAdapter extends ReadFrom {

        private final Predicate<RedisNodeDescription> predicates[];

        @SafeVarargs
        OrderedPredicateReadFromAdapter(Predicate<RedisNodeDescription>... predicates) {
            this.predicates = predicates;
        }

        @Override
        public List<RedisNodeDescription> select(Nodes nodes) {

            List<RedisNodeDescription> result = new ArrayList<>(nodes.getNodes().size());

            for (Predicate<RedisNodeDescription> predicate : predicates) {

                for (RedisNodeDescription node : nodes) {
                    if (predicate.test(node)) {
                        result.add(node);
                    }
                }
            }

            return result;
        }

        @Override
        protected boolean isOrderSensitive() {
            return true;
        }
    }

    /**
     * Unordered {@link Predicate}-based {@link ReadFrom} implementation.
     *
     * @since 5.2
     */
    static class UnorderedPredicateReadFromAdapter extends OrderedPredicateReadFromAdapter {

        @SafeVarargs
        UnorderedPredicateReadFromAdapter(Predicate<RedisNodeDescription>... predicates) {
            super(predicates);
        }

        @Override
        protected boolean isOrderSensitive() {
            return false;
        }
    }
}
