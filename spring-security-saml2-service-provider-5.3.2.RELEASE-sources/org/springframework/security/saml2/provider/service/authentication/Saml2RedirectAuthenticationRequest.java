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

import static org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding.REDIRECT;

/**
 * Data holder for information required to send an {@code AuthNRequest} over a REDIRECT binding
 * from the service provider to the identity provider
 * https://www.oasis-open.org/committees/download.php/35711/sstc-saml-core-errata-2.0-wd-06-diff.pdf (line 2031)
 * 数据持有人，用于通过REDIRECT绑定从服务提供商向身份提供商发送AuthNRequest所需的信息
 * @see Saml2AuthenticationRequestFactory
 * @since 5.3重定向绑定
 */
public class Saml2RedirectAuthenticationRequest extends AbstractSaml2AuthenticationRequest {

	private final String sigAlg;
	private final String signature;

	private Saml2RedirectAuthenticationRequest(
			String samlRequest,
			String sigAlg,
			String signature,
			String relayState,
			String authenticationRequestUri) {
		super(samlRequest, relayState, authenticationRequestUri);
		this.sigAlg = sigAlg;
		this.signature = signature;
	}

	/**返回Saml2MessageBinding.REDIRECT请求的SigAlg值
	 * Returns the SigAlg value for {@link Saml2MessageBinding#REDIRECT} requests
	 * @return the SigAlg value签名算法
	 */
	public String getSigAlg() {
		return this.sigAlg;
	}

	/**
	 * Returns the Signature value for {@link Saml2MessageBinding#REDIRECT} requests
	 * @return the Signature value签名
	 */
	public String getSignature() {
		return this.signature;
	}

	/**
     * @return {@link Saml2MessageBinding#REDIRECT}
	 */
	@Override
	public Saml2MessageBinding getBinding() {
		return REDIRECT;
	}

	/**从Saml2AuthenticationRequestContext对象构造一个Saml2RedirectAuthenticationRequest.Builder。 默认情况下，getAuthenticationRequestUri（）将设置为Saml2AuthenticationRequestContext.getDestination（）值。
	 * Constructs a {@link Saml2RedirectAuthenticationRequest.Builder} from a {@link Saml2AuthenticationRequestContext} object.
	 * By default the {@link Saml2RedirectAuthenticationRequest#getAuthenticationRequestUri()} will be set to the
	 * {@link Saml2AuthenticationRequestContext#getDestination()} value.
	 * @param context input providing {@code Destination}, {@code RelayState}, and {@code Issuer} objects.
	 * @return a modifiable builder object
	 */
	public static Builder withAuthenticationRequestContext(Saml2AuthenticationRequestContext context) {
		return new Builder()
				.authenticationRequestUri(context.getDestination())
				.relayState(context.getRelayState())
				;
	}

	/**
	 * Builder class for a {@link Saml2RedirectAuthenticationRequest} object.
	 */
	public static class Builder extends AbstractSaml2AuthenticationRequest.Builder<Builder> {
		private String sigAlg;
		private String signature;

		private Builder() {
			super();
		}

		/**
		 * Sets the {@code SigAlg} parameter that will accompany this AuthNRequest
		 * @param sigAlg the SigAlg parameter value.
		 * @return this object
		 */
		public Builder sigAlg(String sigAlg) {
			this.sigAlg = sigAlg;
			return _this();
		}

		/**
		 * Sets the {@code Signature} parameter that will accompany this AuthNRequest
		 * @param signature the Signature parameter value.
		 * @return this object
		 */
		public Builder signature(String signature) {
			this.signature = signature;
			return _this();
		}

		/**
		 * Constructs an immutable {@link Saml2RedirectAuthenticationRequest} object.
		 * @return an immutable {@link Saml2RedirectAuthenticationRequest} object.
		 */
		public Saml2RedirectAuthenticationRequest build() {
			return new Saml2RedirectAuthenticationRequest(
					this.samlRequest,
					this.sigAlg,
					this.signature,
					this.relayState,
					this.authenticationRequestUri
			);
		}

	}


}
