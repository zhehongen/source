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

import org.springframework.security.access.AccessDecisionManager;

/**
 * Represents an authority granted to an {@link Authentication} object.
 *
 * <p>
 * A <code>GrantedAuthority</code> must either represent itself as a <code>String</code>
 * or be specifically supported by an {@link AccessDecisionManager}.
 *
 * @author Ben Alex
 * 表示授予身份验证对象的权限。
 * GrantedAuthority必须将其自身表示为String或由AccessDecisionManager专门支持。
 */
public interface GrantedAuthority extends Serializable {
	// ~ Methods
	// ========================================================================================================

	/**
	 * If the <code>GrantedAuthority</code> can be represented as a <code>String</code>
	 * and that <code>String</code> is sufficient in precision to be relied upon for an
	 * access control decision by an {@link AccessDecisionManager} (or delegate), this
	 * method should return such a <code>String</code>.
	 * <p>
	 * If the <code>GrantedAuthority</code> cannot be expressed with sufficient precision
	 * as a <code>String</code>, <code>null</code> should be returned. Returning
	 * <code>null</code> will require an <code>AccessDecisionManager</code> (or delegate)
	 * to specifically support the <code>GrantedAuthority</code> implementation, so
	 * returning <code>null</code> should be avoided unless actually required.
	 *
	 * @return a representation of the granted authority (or <code>null</code> if the
	 * granted authority cannot be expressed as a <code>String</code> with sufficient
	 * precision).
	 * 如果GrantedAuthority可以表示为String，并且该String的精度足以由AccessDecisionManager
	 * （或委托）进行访问控制决策，则此方法应返回这样的String。
	 * 如果GrantedAuthority无法足够精确地表示为String，则应返回null。
	 * 返回null将需要一个AccessDecisionManager（或委托）来专门支持GrantedAuthority实现
	 * ，因此，除非实际需要，否则应避免返回null。
	 *
	 * 返回值：
	 * 授予权限的表示形式（如果授予权限不能足够精确地表示为String，则为null）。
	 */
	String getAuthority();
}
