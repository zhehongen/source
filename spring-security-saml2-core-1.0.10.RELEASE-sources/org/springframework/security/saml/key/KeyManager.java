/* Copyright 2009-2011 Vladimir Schafer
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
package org.springframework.security.saml.key;

import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;

import java.security.cert.X509Certificate;
import java.util.Set;

/**接口定义了SAML扩展实现所需的基本服务。
 * Interface defines basic service required by the SAML Extension implementation.
 *
 * @author Vladimir Schafer
 */
public interface KeyManager extends CredentialResolver {

    /**返回用于签署此实体发出的消息的凭据对象。 公钥，X509和私钥在证书中设置。
     * Returns Credential object used to sign the messages issued by this entity.
     * Public, X509 and Private keys are set in the credential.
     *要使用的密钥的名称，如果使用null默认密钥
     * @param keyName name of the key to use, in case of null default key is used
     * @return credential
     */
    public Credential getCredential(String keyName);

    /**
     * Returns Credential object used to sign the messages issued by this entity.
     * Public, X509 and Private keys are set in the credential.
     *
     * @return credential
     */
    public Credential getDefaultCredential();

    /**方法提供凭据的名称，如果未指定其他名称，则默认情况下应使用该名称。 必须有可能使用返回的名称调用getCredential以获得Credential值。
     * Method provides name of the credential which should be used by default when no other is specified. It
     * must be possible to call getCredential with the returned name in order to obtain Credential value.
     *
     * @return default credential name
     */
    public String getDefaultCredentialName();

    /**方法提供了存储中所有可用凭证的列表。
     * Method provides list of all credentials available in the storage.
     *
     * @return available credentials
     */
    public Set<String> getAvailableCredentials();

    /**从密钥库中返回具有给定别名的证书。
     * Returns certificate with the given alias from the keystore.
     *
     * @param alias alias of certificate to find
     * @return certificate with the given alias or null if not found
     */
    public X509Certificate getCertificate(String alias);


}
