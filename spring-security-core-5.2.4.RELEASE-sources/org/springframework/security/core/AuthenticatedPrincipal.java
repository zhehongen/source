/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.security.authentication.AuthenticationManager;

/**
 * Representation of an authenticated <code>Principal</code> once an
 * {@link Authentication} request has been successfully authenticated
 * by the {@link AuthenticationManager#authenticate(Authentication)} method.
 *
 * Implementors typically provide their own representation of a <code>Principal</code>,
 * which usually contains information describing the <code>Principal</code> entity,
 * such as, first/middle/last name, address, email, phone, id, etc.
 *
 * This interface allows implementors to expose specific attributes
 * of their custom representation of <code>Principal</code> in a generic way.
 *
 * @author Joe Grandja
 * @since 5.0
 * @see Authentication#getPrincipal()
 * @see org.springframework.security.core.userdetails.UserDetails
 * 一旦通过AuthenticationManager.authenticate（Authentication）方法成功验证了身份验证请求，
 * 就表示经过身份验证的主体。 
 * 实现者通常提供自己的委托人表示，通常包含描述委托人实体的信息，
 * 例如名字/中间名/姓氏，地址，电子邮件，电话，ID等。
 * 此接口允许实现者公开其自定义的特定属性 Principal的通用表示形式。
 */
public interface AuthenticatedPrincipal {

	/**
	 * Returns the name of the authenticated <code>Principal</code>. Never <code>null</code>.
	 *
	 * @return the name of the authenticated <code>Principal</code>
	 */
	String getName();

}
