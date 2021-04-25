/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.saml2.metadata.provider;

import java.util.List;

/**一个元数据提供程序能够提供事件通知给观察者。 例如，这可以用于表示元数据内部缓存的更新，从而允许其他子系统基于此执行某些操作。
 * A metadata provider that provides event notification to observers. This may be used, for example, to signal an update
 * of an internal cache of metadata allowing other subsystems to perform some action based on that.
 *
 */
public interface ObservableMetadataProvider extends MetadataProvider {

    /**获取提供程序的观察者列表。 新的观察者可以添加到列表中，也可以将旧的观察者删除。
     * Gets the list of observers for the provider. New observers may be added to the list or old ones removed.
     *
     * @return the list of observers
     */
    public List<Observer> getObservers();

    /**元数据提供者发生了变化的观察者。 注意：已更改的元数据提供程序将传递给onEvent（MetadataProvider）方法。 观察者不应保留对此提供程序的引用，因为这可能会阻止正确的垃圾收集。
     * An observer of metadata provider changes.
     *
     * <strong>NOTE:</strong> The metadata provider that has changed is passed in to the
     * {@link #onEvent(MetadataProvider)} method. Observers should <strong>NOT</strong> keep a reference to this
     * provider as this may prevent proper garbage collection.
     */
    public interface Observer {

        /**当提供者发出事件信号时调用。
         * Called when a provider signals an event has occured.
         *
         * @param provider the provider being observed
         */
        public void onEvent(MetadataProvider provider);
    }
}
