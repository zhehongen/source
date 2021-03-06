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
package org.springframework.security.providers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Date;

/**具有在特定日期时间后禁用自身的功能的身份验证令牌。 如果没有为令牌功能指定任何过期日期，则该日期与UsernamePasswordAuthenticationToken完全相同。
 * Authentication token with capability to disable itself after specific datetime. In case no expiration date is
 * specified for the token functionality is exactly the same as of {@link UsernamePasswordAuthenticationToken}.
 *
 * @author Vladimir Schäfer
 */
public class ExpiringUsernameAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private Date tokenExpiration;

    /**
     * @param principal   principal
     * @param credentials credential
     *
     * @see UsernamePasswordAuthenticationToken#UsernamePasswordAuthenticationToken(Object, Object)
     */
    public ExpiringUsernameAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    /**
     * Should only be used by authenticationManager as specified in {@link UsernamePasswordAuthenticationToken}. In
     * case the tokenExpiration is not null the calls to the isAuthenticated method will return false after
     * the current time is beyond the tokenExpiration. No functionality is changed when tokenExpiration is null.
     *只应由UsernamePasswordAuthenticationToken中指定的authenticationManager使用。 如果tokenExpiration不为null，则在当前时间超出tokenExpiration之后，对isAuthenticated方法的调用将返回false。 当tokenExpiration为null时，不会更改任何功能
     * @param tokenExpiration null or date after which the token is not valid anymore
     * @param principal       principal
     * @param credentials     credentials
     * @param authorities     authorities
     */
    public ExpiringUsernameAuthenticationToken(Date tokenExpiration, Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.tokenExpiration = tokenExpiration;
    }

    /**
     * @return true in case the token is authenticated (determined by constructor call) and tokenExpiration
     *         is either null or the expiration time is on or after current time.
     */
    @Override
    public boolean isAuthenticated() {
        if (tokenExpiration != null && new Date().compareTo(tokenExpiration) >= 0) {
            return false;
        } else {
            return super.isAuthenticated();
        }
    }

    /**
     * @return null if no expiration is set, expiration date otherwise
     */
    public Date getTokenExpiration() {
        return tokenExpiration;
    }

    /**
     * SAML credentials can be kept without clearing. 牛逼
     */
    @Override
    public void eraseCredentials() {
    }

}
