/* Copyright 2011 Vladimir Schäfer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.saml.metadata;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;

/**
 * Provider capable of supplying metadata extensions including information about requirements of the given entity.
 * 提供者能够提供元数据扩展，包括有关给定实体需求的信息。
 * @author Vladimir Schäfer
 */
public interface ExtendedMetadataProvider {

    /**实现应尝试为给定实体本地化其他元数据。
     * Implementation should try to localize additional metadata for the given entity.
     *
     * @param entityID entity to load metadata for，用于加载它的元数据的实体
     * @return null if not found, metadata otherwise
     * @throws MetadataProviderException in case an error occurs
     */
    ExtendedMetadata getExtendedMetadata(String entityID) throws MetadataProviderException;

}
