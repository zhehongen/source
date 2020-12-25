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

package org.springframework.security.access;

import java.util.Collection;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

/**
 * Makes a final access control (authorization) decision.
 *做出最终的访问控制（授权）决定。
 * @author Ben Alex
 */
public interface AccessDecisionManager {
	// ~ Methods
	// ========================================================================================================

	/**
	 * Resolves an access control decision for the passed parameters.
	 *
	 * @param authentication the caller invoking the method (not null)
	 * @param object the secured object being called
	 * @param configAttributes the configuration attributes associated with the secured
	 * object being invoked
	 *
	 * @throws AccessDeniedException if access is denied as the authentication does not
	 * hold a required authority or ACL privilege
	 * @throws InsufficientAuthenticationException if access is denied as the
	 * authentication does not provide a sufficient level of trust
	 * 解决传递参数的访问控制决策。
	 *
	 * 参数：
	 * 身份验证–调用方调用方法（不为null）
	 * 对象–被调用的受保护对象
	 * configAttributes –与要调用的安全对象关联的配置属性
	 * 抛出：
	 * AccessDeniedException –如果访问被拒绝，因为身份验证不具有所需的权限或ACL特权
	 * InsufficientAuthenticationException-如果由于身份验证未提供足够的信任级别而拒绝访问
	 */
	void decide(Authentication authentication, Object object,
			Collection<ConfigAttribute> configAttributes) throws AccessDeniedException,
			InsufficientAuthenticationException;

	/**
	 * Indicates whether this <code>AccessDecisionManager</code> is able to process
	 * authorization requests presented with the passed <code>ConfigAttribute</code>.
	 * <p>
	 * This allows the <code>AbstractSecurityInterceptor</code> to check every
	 * configuration attribute can be consumed by the configured
	 * <code>AccessDecisionManager</code> and/or <code>RunAsManager</code> and/or
	 * <code>AfterInvocationManager</code>.
	 * </p>
	 *
	 * @param attribute a configuration attribute that has been configured against the
	 * <code>AbstractSecurityInterceptor</code>
	 *
	 * @return true if this <code>AccessDecisionManager</code> can support the passed
	 * configuration attribute
	 * 指示此AccessDecisionManager是否能够处理通过传递的ConfigAttribute提出的授权请求。
	 * 这允许AbstractSecurityInterceptor检查配置的AccessDecisionManager和/或RunAsManager和/或AfterInvocationManager可以使用的每个配置属性。
	 *
	 * 参数：
	 * 属性–已针对AbstractSecurityInterceptor配置的配置属性
	 * 返回值：
	 * 如果此AccessDecisionManager可以支持传递的配置属性，则为true
	 */
	boolean supports(ConfigAttribute attribute);

	/**
	 * Indicates whether the <code>AccessDecisionManager</code> implementation is able to
	 * provide access control decisions for the indicated secured object type.
	 *
	 * @param clazz the class that is being queried
	 *
	 * @return <code>true</code> if the implementation can process the indicated class
	 * 指示AccessDecisionManager实现是否能够为指示的安全对象类型提供访问控制决策。
	 * 返回值：
	 * 如果实现可以处理指定的类，则为true
	 */
	boolean supports(Class<?> clazz);
}
