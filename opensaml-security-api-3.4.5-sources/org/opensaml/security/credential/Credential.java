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

package org.opensaml.security.credential;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

/**实体的凭证。 特定凭证可以包含非对称密钥信息（公共密钥以及可选的相应私钥），也可以包含对称（秘密）密钥，但绝不能同时包含两者。 使用基于非对称密钥的凭据，本地实体凭据通常将同时包含公钥和私钥，而对等凭据通常将仅包含公钥。
 * A credential for an entity. A particular credential may contain either asymmetric key information (a public key
 * and optionally the corresponding private key), or a symmetric (secret) key, but never both.
 * With asymmetric key-based credentials, local entity credentials will usually contain both a public
 * and private key while peer credentials will normally contain only a public key.
 */
public interface Credential {

    /**
     * The unique ID of the entity this credential is for.
     * 此凭证所针对的实体的唯一ID。
     * @return unique ID of the entity this credential is for
     */
    @Nullable public String getEntityId();

    /**
     * Gets usage type of this credential.
     * 使用类型
     * @return usage type of this credential
     */
    @Nullable public UsageType getUsageType();

    /**获取此证书的密钥名称。 这些名称可用于引用通过带外协议交换的密钥。 实现可能会或可能不会实现将这些名称解析为可通过getPublicKey（），getPrivateKey（）或getSecretKey（）方法检索的键的方法。
     * Gets key names for this credential. These names may be used to reference a key(s) exchanged
     * through an out-of-band agreement. Implementations may or may not implement means to resolve
     * these names into keys retrievable through the {@link #getPublicKey()}, {@link #getPrivateKey()}
     * or {@link #getSecretKey()} methods.
     *
     * @return key names for this credential
     */
    @Nonnull public Collection<String> getKeyNames();

    /**
     * Gets the public key for the entity.
     *
     * @return public key for the entity
     */
    @Nullable public PublicKey getPublicKey();

    /**
     * Gets the private key for the entity if there is one.
     *
     * @return the private key for the entity
     */
    @Nullable public PrivateKey getPrivateKey();

    /**
     * Gets the secret key for this entity.
     * 私有和秘密有毛区别
     * @return secret key for this entity
     */
    @Nullable public SecretKey getSecretKey();

    /**
     * Get the set of credential context information, which provides additional information
     * specific to the contexts in which the credential was resolved.
     *获取凭据上下文信息集，该凭据上下文信息提供了特定于解析凭据的上下文的附加信息。
     * @return set of resolution contexts of the credential
     */
    @Nullable public CredentialContextSet getCredentialContextSet();

    /**
     * Get the primary type of the credential instance. This will usually be the primary sub-interface
     * of {@link Credential} implemented by an implementation.
     * 获取凭据实例的主要类型。 这通常将是由实现实现的Credential的主要子接口。
     * @return the credential type
     */
    @Nonnull public Class<? extends Credential> getCredentialType();
}
