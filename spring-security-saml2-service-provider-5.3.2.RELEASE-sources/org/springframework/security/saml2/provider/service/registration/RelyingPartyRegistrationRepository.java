/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.security.saml2.provider.service.registration;

/**根据唯一的registrationId解析RelyingPartyRegistration，已配置的服务提供者和远程身份提供者对。
 * Resolves a {@link RelyingPartyRegistration}, a configured service provider and remote identity provider pair
 * based on a unique registrationId.
 * @since 5.2 Repository（存储库，这个名字是关键）。怎么把这些孙子都注册上来是个难点啊
 */
public interface RelyingPartyRegistrationRepository {

	/**通过registrationId解析RelyingPartyRegistration，如果未提供registrationId，则返回默认提供程序
	 * Resolves an {@link RelyingPartyRegistration} by registrationId, or returns the default provider
	 * if no registrationId is provided
	 *（如果找到），如果提供了registrationId且未找到注册，则为null。 如果未提供registrationId，则返回默认的，特定于实现的RelyingPartyRegistration
	 * @param registrationId - a provided registrationId, may be be null or empty 提供的registrationId，可以为null或为空
	 * @return {@link RelyingPartyRegistration} if found, {@code null} if an registrationId is provided and
	 * no registration is found. Returns a default, implementation specific,
	 * {@link RelyingPartyRegistration} if no registrationId is provided
	 */
	RelyingPartyRegistration findByRegistrationId(String registrationId);

}
