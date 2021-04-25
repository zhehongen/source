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

package org.opensaml.xml.encryption;

import java.util.List;

/**用于基于特定EncryptedData上下文解析EncryptedKey元素的接口，主要用于解密过程中。 解析的EncryptedKey元素将包含用于加密指定的EncryptedData的数据加密密钥。
 * Interface for resolving {@link EncryptedKey} elements based on a particular
 * {@link EncryptedData} context, primarily for use during the decryption process.
 *
 * The resolved EncryptedKey element(s) will contain the data encryption key used to encrypt
 * the specified EncryptedData.
 */
public interface EncryptedKeyResolver {

    /**解决包含用于加密指定的EncryptedData元素的数据加密密钥的EncryptedKey元素。
     * Resolve the EncryptedKey elements containing the data encryption key used to
     * encrypt the specified EncryptedData element.
     *
     * @param encryptedData  the EncryptedData element context in which to resolve
     * @return an iterable of EncryptedKey elements
     */
    Iterable<EncryptedKey> resolve(EncryptedData encryptedData);

    /**获取此解析程序使用的收件人条件列表，并针对该条件评估候选EncryptedKey的Recipient属性。
     * Get the list of recipient criteria used by this resolver, and against which a candidate
     * EncryptedKey's Recipient attribute is evaluated.
     *
     * @return the list of  recipient criteria
     */
    List<String> getRecipients();

}
