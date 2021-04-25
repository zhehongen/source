/* Copyright 2009 Vladimir Sch�fer
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

import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.*;

/**密钥管理器不提供对任何密钥的访问，可用于跳过Spring SAML项目中密钥库文件的包含。 空密钥存储只能在Spring SAML不对传入消息进行解密并且不需要创建数字签名的情况下使用。
 * Key manager doesn't provide access to any keys and can be used to skip inclusion of keystore files in the
 * Spring SAML projects. Empty key store can only be used in situations when Spring SAML doesn't perform decryption
 * of incoming messages and doesn't need to create digital signatures.
 *
 * @author Vladimir Schafer
 */
public class EmptyKeyManager implements KeyManager {

    private final Logger log = LoggerFactory.getLogger(EmptyKeyManager.class);

    @Override
    public Credential getCredential(String keyName) {
        return null;
    }

    @Override
    public Credential getDefaultCredential() {
        return null;
    }

    @Override
    public String getDefaultCredentialName() {
        return null;
    }

    @Override
    public Set<String> getAvailableCredentials() {
        return Collections.emptySet();
    }

    @Override
    public X509Certificate getCertificate(String alias) {
        return null;
    }

    @Override
    public Iterable<Credential> resolve(CriteriaSet criteria) throws SecurityException {
        return null;
    }

    @Override
    public Credential resolveSingle(CriteriaSet criteria) throws SecurityException {
        return null;
    }

}
