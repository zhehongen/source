/*
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
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

package org.springframework.security.core;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Represents the token for an authentication request or for an authenticated principal
 * once the request has been processed by the
 * {@link AuthenticationManager#authenticate(Authentication)} method.
 * <p>
 * Once the request has been authenticated, the <tt>Authentication</tt> will usually be
 * stored in a thread-local <tt>SecurityContext</tt> managed by the
 * {@link SecurityContextHolder} by the authentication mechanism which is being used. An
 * explicit authentication can be achieved, without using one of Spring Security's
 * authentication mechanisms, by creating an <tt>Authentication</tt> instance and using
 * the code:
 *
 * <pre>
 * SecurityContextHolder.getContext().setAuthentication(anAuthentication);
 * </pre>
 *
 * Note that unless the <tt>Authentication</tt> has the <tt>authenticated</tt> property
 * set to <tt>true</tt>, it will still be authenticated by any security interceptor (for
 * method or web invocations) which encounters it.
 * <p>
 * In most cases, the framework transparently takes care of managing the security context
 * and authentication objects for you.
 *
 * @author Ben Alex
 * 在AuthenticationManager.authenticate（Authentication）方法处理请求后，代表认证请求或已认证主体的令牌。
 * 一旦对请求进行身份验证，身份验证通常将通过使用的身份验证机制存储在由SecurityContextHolder
 *管理的线程本地SecurityContext中。通过创建Authentication实例并使用以下代码，
 * 无需使用Spring Security的身份验证机制之一即可实现显式身份验证：
 *    SecurityContextHolder.getContext（）.setAuthentication（anAuthentication）;
 *
 * 请注意，除非Authentication的authenticated属性设置为true，
 * 否则它将由遇到它的任何安全拦截器（用于方法或Web调用）进行身份验证。
 * 在大多数情况下，框架透明地负责为您管理安全上下文和身份验证对象。
 */
public interface Authentication extends Principal, Serializable {
	// ~ Methods
	// ========================================================================================================

	/**
	 * Set by an <code>AuthenticationManager</code> to indicate the authorities that the
	 * principal has been granted. Note that classes should not rely on this value as
	 * being valid unless it has been set by a trusted <code>AuthenticationManager</code>.
	 * <p>
	 * Implementations should ensure that modifications to the returned collection array
	 * do not affect the state of the Authentication object, or use an unmodifiable
	 * instance.
	 * </p>
	 *
	 * @return the authorities granted to the principal, or an empty collection if the
	 * token has not been authenticated. Never null.
	 * 由AuthenticationManager设置以指示已授予主体的权限。
	 * 请注意，除非受信任的AuthenticationManager设置了该类，否则类不应将其视为有效。
	 * 实现应确保对返回的集合数组的修改不会影响Authentication对象的状态，或使用不可修改的实例。
	 *
	 * 返回值：
	 * 授予委托人的权限，如果令牌尚未通过身份验证，则为空集合。 永远不会为空。
	 */
	Collection<? extends GrantedAuthority> getAuthorities();

	/**
	 * The credentials that prove the principal is correct. This is usually a password,
	 * but could be anything relevant to the <code>AuthenticationManager</code>. Callers
	 * are expected to populate the credentials.
	 *
	 * @return the credentials that prove the identity of the <code>Principal</code>
	 * 证明主体正确的凭据。 这通常是密码，但可以是与AuthenticationManager相关的任何内容。 呼叫者应填充凭据。
	 *
	 * 返回值：
	 * 证明委托人身份的凭证
	 */
	Object getCredentials();

	/**
	 * Stores additional details about the authentication request. These might be an IP
	 * address, certificate serial number etc.
	 *
	 * @return additional details about the authentication request, or <code>null</code>
	 * if not used
	 * 存储有关身份验证请求的其他详细信息。 这些可能是IP地址，证书序列号等。
	 *
	 * 返回值：
	 * 有关身份验证请求的其他详细信息；如果未使用，则为null
	 */
	Object getDetails();

	/**
	 * The identity of the principal being authenticated. In the case of an authentication
	 * request with username and password, this would be the username. Callers are
	 * expected to populate the principal for an authentication request.
	 * <p>
	 * The <tt>AuthenticationManager</tt> implementation will often return an
	 * <tt>Authentication</tt> containing richer information as the principal for use by
	 * the application. Many of the authentication providers will create a
	 * {@code UserDetails} object as the principal.
	 *
	 * @return the <code>Principal</code> being authenticated or the authenticated
	 * principal after authentication.
	 * 身份验证的主体的身份。 如果使用用户名和密码进行身份验证请求，则为用户名。 呼叫者应填充身份验证请求的主体。
	 * AuthenticationManager实现通常会返回一个包含更丰富信息的Authentication作为供应用程序使用的主体。 
	 * 许多身份验证提供程序将创建一个UserDetails对象作为主体。
	 *
	 * 返回值：
	 * 被认证的主体或认证后的已认证主体。
	 */
	Object getPrincipal();

	/**
	 * Used to indicate to {@code AbstractSecurityInterceptor} whether it should present
	 * the authentication token to the <code>AuthenticationManager</code>. Typically an
	 * <code>AuthenticationManager</code> (or, more often, one of its
	 * <code>AuthenticationProvider</code>s) will return an immutable authentication token
	 * after successful authentication, in which case that token can safely return
	 * <code>true</code> to this method. Returning <code>true</code> will improve
	 * performance, as calling the <code>AuthenticationManager</code> for every request
	 * will no longer be necessary.
	 * <p>
	 * For security reasons, implementations of this interface should be very careful
	 * about returning <code>true</code> from this method unless they are either
	 * immutable, or have some way of ensuring the properties have not been changed since
	 * original creation.
	 *
	 * @return true if the token has been authenticated and the
	 * <code>AbstractSecurityInterceptor</code> does not need to present the token to the
	 * <code>AuthenticationManager</code> again for re-authentication.
	 * 用于向AbstractSecurityInterceptor指示是否应将身份验证令牌提供给AuthenticationManager。
	 * 通常，AuthenticationManager（或更常见的情况是其AuthenticationProviders之一）
	 * 将在成功通过身份验证后返回不可变的身份验证令牌，在这种情况下，令牌可以安全地将true返回给此方法。
	 * 返回true将提高性能，因为不再需要为每个请求调用AuthenticationManager。
	 * 出于安全原因，此接口的实现在从此方法返回true时应格外小心，除非它们是不可变的，
	 * 或者具有确保自原始创建以来未更改属性的某种方式。
	 *
	 * 返回值：
	 * 如果令牌已经过身份验证，并且AbstractSecurityInterceptor不需要再次将令牌提供给
	 * AuthenticationManager进行重新身份验证，则为true。
	 */
	boolean isAuthenticated();

	/**
	 * See {@link #isAuthenticated()} for a full description.
	 * <p>
	 * Implementations should <b>always</b> allow this method to be called with a
	 * <code>false</code> parameter, as this is used by various classes to specify the
	 * authentication token should not be trusted. If an implementation wishes to reject
	 * an invocation with a <code>true</code> parameter (which would indicate the
	 * authentication token is trusted - a potential security risk) the implementation
	 * should throw an {@link IllegalArgumentException}.
	 *
	 * @param isAuthenticated <code>true</code> if the token should be trusted (which may
	 * result in an exception) or <code>false</code> if the token should not be trusted
	 *
	 * @throws IllegalArgumentException if an attempt to make the authentication token
	 * trusted (by passing <code>true</code> as the argument) is rejected due to the
	 * implementation being immutable or implementing its own alternative approach to
	 * {@link #isAuthenticated()}
	 * 实现应始终允许使用错误的参数调用此方法，因为各种类使用该参数来指定不应信任身份验证令牌。
	 * 如果实现希望拒绝带有真实参数的调用（这将表明身份验证令牌是受信任的，则有潜在的安全风险），
	 * 则实现应抛出IllegalArgumentException。
	 *
	 * 参数：
	 * isAuthenticated –如果令牌应该被信任（可能会导致异常），则为true；如果令牌不应被信任，则为false
	 * 抛出：
	 * IllegalArgumentException-如果由于实现不可变或对isAuthenticated（）
	 * 实现其自己的替代方法而拒绝使身份验证令牌受信任的尝试（通过传递true作为参数）
	 */
	void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
}
