/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.security.saml2.provider.service.authentication;

import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.util.Assert;

import java.nio.charset.Charset;

/**使用Saml2MessageBinding.POST或Saml2MessageBinding.REDIRECT绑定发送的AuthNRequest参数的数据持有者。 数据将被编码并可能缩小，但不会被转义进行传输，即URL编码
 * Data holder for {@code AuthNRequest} parameters to be sent using either the
 * {@link Saml2MessageBinding#POST} or {@link Saml2MessageBinding#REDIRECT} binding.
 * Data will be encoded and possibly deflated, but will not be escaped for transport,
 * ie URL encoded, {@link org.springframework.web.util.UriUtils#encode(String, Charset)}
 * or HTML encoded, {@link org.springframework.web.util.HtmlUtils#htmlEscape(String)}.
 * https://www.oasis-open.org/committees/download.php/35711/sstc-saml-core-errata-2.0-wd-06-diff.pdf (line 2031)
 *
 * @see Saml2AuthenticationRequestFactory#createPostAuthenticationRequest(Saml2AuthenticationRequestContext)
 * @see Saml2AuthenticationRequestFactory#createRedirectAuthenticationRequest(Saml2AuthenticationRequestContext)
 * @since 5.3
 */
abstract class AbstractSaml2AuthenticationRequest {

	private final String samlRequest;
	private final String relayState;
	private final String authenticationRequestUri;

	/**AbstractSaml2AuthenticationRequest的强制构造函数
	 * Mandatory constructor for the {@link AbstractSaml2AuthenticationRequest}
	 * @param samlRequest - the SAMLRequest XML data, SAML encoded, cannot be empty or null
	 * @param relayState - RelayState value that accompanies the request, may be null
	 * @param authenticationRequestUri - The authenticationRequestUri, a URL, where to send the XML message, cannot be empty or null
	 */
	AbstractSaml2AuthenticationRequest(
			String samlRequest,
			String relayState,
			String authenticationRequestUri) {
		Assert.hasText(samlRequest, "samlRequest cannot be null or empty");
		Assert.hasText(authenticationRequestUri, "authenticationRequestUri cannot be null or empty");
		this.authenticationRequestUri = authenticationRequestUri;
		this.samlRequest = samlRequest;
		this.relayState = relayState;
	}

	/**返回要发送的AuthNRequest XML值。 该值已被编码用于传输。 如果getBinding（）是Saml2MessageBinding.REDIRECT，则将值放气并进行SAML编码。
	 * Returns the AuthNRequest XML value to be sent. This value is already encoded for transport.
	 * If {@link #getBinding()} is {@link Saml2MessageBinding#REDIRECT} the value is deflated and SAML encoded.
	 * If {@link #getBinding()} is {@link Saml2MessageBinding#POST} the value is SAML encoded.
	 * @return the SAMLRequest parameter value 如果getBinding（）是Saml2MessageBinding.POST，则该值是SAML编码的。
	 */
	public String getSamlRequest() {
		return this.samlRequest;
	}

	/**参数中存在RelayState值
	 * Returns the RelayState value, if present in the parameters
	 * @return the RelayState value, or null if not available
	 */
	public String getRelayState() {
		return this.relayState;
	}

	/**可以确认就是idp的地址
	 * Returns the URI endpoint that this AuthNRequest should be sent to.
	 * @return the URI endpoint for this message
	 */
	public String getAuthenticationRequestUri() {
		return this.authenticationRequestUri;
	}

	/**返回此AuthNRequest将被发送和编码的绑定。 如果使用Saml2MessageBinding.REDIRECT，则将自动应用DEFLATE编码。
	 * Returns the binding this AuthNRequest will be sent and
	 * encoded with. If {@link Saml2MessageBinding#REDIRECT} is used, the DEFLATE encoding will be automatically applied.
	 * @return the binding this message will be sent with.将与该消息一起发送的绑定。
	 */
	public abstract Saml2MessageBinding getBinding();

	/**奇葩
	 * A builder for {@link AbstractSaml2AuthenticationRequest} and its subclasses.
	 */
	static class Builder<T extends Builder<T>> {
		String authenticationRequestUri;
		String samlRequest;
		String relayState;

		protected Builder() {
		}

		/**返回自身时，将return转换为泛型子类型
		 * Casting the return as the generic subtype, when returning itself
		 * @return this object
		 */
		@SuppressWarnings("unchecked")
		protected final T _this() {
			return (T) this;
		}


		/**
		 * Sets the {@code RelayState} parameter that will accompany this AuthNRequest
		 *中继状态值，未编码
		 * @param relayState the relay state value, unencoded. if null or empty, the parameter will be removed from the
		 * map.
		 * @return this object
		 */
		public T relayState(String relayState) {
			this.relayState = relayState;
			return _this();
		}

		/**
		 * Sets the {@code SAMLRequest} parameter that will accompany this AuthNRequest
		 *
		 * @param samlRequest the SAMLRequest parameter.
		 * @return this object
		 */
		public T samlRequest(String samlRequest) {
			this.samlRequest = samlRequest;
			return _this();
		}

		/**
		 * Sets the {@code authenticationRequestUri}, a URL that will receive the AuthNRequest message
		 *
		 * @param authenticationRequestUri the relay state value, unencoded.
		 * @return this object
		 */
		public T authenticationRequestUri(String authenticationRequestUri) {
			this.authenticationRequestUri = authenticationRequestUri;
			return _this();
		}
	}

}
