/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.io.stream;

/**由其名称标识的可写对象。 用于任意可序列化的对象（例如查询）； 读取它们时，它们的名称指示需要创建的特定对象。
 * A {@link Writeable} object identified by its name.
 * To be used for arbitrary serializable objects (e.g. queries); when reading them, their name tells
 * which specific object needs to be created.
 */
public interface NamedWriteable extends Writeable {

    /**
     * Returns the name of the writeable object
     */
    String getWriteableName();
}
