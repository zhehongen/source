/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.security.oauth2;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OAuth2 Client.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @since 1.3.0
 */
@ConfigurationProperties(prefix = "security.oauth2.client")
public class OAuth2ClientProperties {

	/**
	 * OAuth2 client id.
	 */
	private String clientId;

	/**
	 * OAuth2 client secret. A random secret is generated by default.
	 * OAuth2客户端机密。 默认情况下会生成一个随机秘密。
	 */
	private String clientSecret = UUID.randomUUID().toString();

	private boolean defaultSecret = true;

	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return this.clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
		this.defaultSecret = false;
	}

	public boolean isDefaultSecret() {
		return this.defaultSecret;
	}

}
