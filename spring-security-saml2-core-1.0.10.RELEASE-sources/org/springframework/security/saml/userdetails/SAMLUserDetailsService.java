/* Copyright 2009 Vladimir Schäfer
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
package org.springframework.security.saml.userdetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;

/**SAMLUserDetailsService接口类似于UserDetailsService，区别在于使用SAML数据来获取有关用户的信息。
 * The SAMLUserDetailsService interface is similar to UserDetailsService with difference that SAML
 * data is used in order obtain information about the user. Implementers of the interface are
 * supposed to locate user in a arbitrary dataStore based on information present in the SAMLCredential
 * and return such a date in a form of application specific UserDetails object.
 *该接口的实现者应该根据SAMLCredential中存在的信息在任意dataStore中定位用户，并以特定于应用程序的UserDetails对象的形式返回此类数据。
 * @author Vladimir Schäfer
 */
public interface SAMLUserDetailsService {

    /**该方法应该标识由SAML断言中的数据引用的用户的本地帐户，并返回描述用户的UserDetails对象。
     * The method is supposed to identify local account of user referenced by data in the SAML assertion
     * and return UserDetails object describing the user. In case the user has no local account, implementation
     * may decide to create one or just populate UserDetails object with data from assertion.
     * <p>如果用户没有本地帐户，则实现可以决定创建一个或仅使用断言中的数据填充UserDetails对象。
     * Returned object should correctly implement the getAuthorities method as it will be used to populate
     * entitlements inside the Authentication object.
     *返回的对象应正确实现getAuthorities方法，因为它将用于在Authentication对象内填充权利。
     * @param credential data populated from SAML message used to validate the user
     *从SAML消息填充的数据，用于验证用户
     * @return a fully populated user record (never <code>null</code>)
     *
     * @throws UsernameNotFoundException if the user details object can't be populated
     */
    Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException;

}
