package org.springframework.security.oauth2.client.token.grant.redirect;

import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;

/**授权码和隐式流需要重定向。我要手动重定向
 * @author Dave Syer
 */
public abstract class AbstractRedirectResourceDetails extends BaseOAuth2ProtectedResourceDetails {

	private String preEstablishedRedirectUri;//客户端预先配置的重定向地址

	private String userAuthorizationUri;//用户授权url

	private boolean useCurrentUri = true;//?

	/**
	 * Flag to signal that the current URI (if set) in the request should be used in preference to the pre-established
	 * redirect URI.标志，用于指示应优先使用请求中的当前URI（如果已设置），而不是预先建立的重定向URI。
	 *
	 * @param useCurrentUri the flag value to set (default true)
	 * 参数：
	 * useCurrentUri –要设置的标志值（默认为true）
	 */
	public void setUseCurrentUri(boolean useCurrentUri) {
		this.useCurrentUri = useCurrentUri;
	}

	/**
	 * Flag to signal that the current URI (if set) in the request should be used in preference to the pre-established
	 * redirect URI.
	 *
	 * @return the flag value
	 */
	public boolean isUseCurrentUri() {
		return useCurrentUri;
	}

	/**
	 * The URI to which the user is to be redirected to authorize an access token.
	 * 要将用户重定向到的URI，以授权访问令牌。
	 * @return The URI to which the user is to be redirected to authorize an access token.
	 * 要将用户重定向到的URI，以授权访问令牌。
	 */
	public String getUserAuthorizationUri() {
		return userAuthorizationUri;
	}

	/**
	 * The URI to which the user is to be redirected to authorize an access token.
	 * 要将用户重定向到的URI，以授权访问令牌。
	 * @param userAuthorizationUri The URI to which the user is to be redirected to authorize an access token.
	 */
	public void setUserAuthorizationUri(String userAuthorizationUri) {
		this.userAuthorizationUri = userAuthorizationUri;
	}

	/**
	 * The redirect URI that has been pre-established with the server. If present, the redirect URI will be omitted from
	 * the user authorization request because the server doesn't need to know it.
	 * 与服务器一起预先建立的重定向URI。 如果存在，则重定向URI将从用户授权请求中省略，因为服务器不需要知道它。
	 * @return The redirect URI that has been pre-established with the server.
	 */
	public String getPreEstablishedRedirectUri() {
		return preEstablishedRedirectUri;
	}

	/**
	 * The redirect URI that has been pre-established with the server. If present, the redirect URI will be omitted from
	 * the user authorization request because the server doesn't need to know it.
	 *与服务器一起预先建立的重定向URI。 如果存在，则重定向URI将从用户授权请求中省略，因为服务器不需要知道它。
	 *
	 * 参数：
	 * prebuiltedRedirectUri –已与服务器预先建立的重定向URI
	 * @param preEstablishedRedirectUri The redirect URI that has been pre-established with the server.
	 */
	public void setPreEstablishedRedirectUri(String preEstablishedRedirectUri) {
		this.preEstablishedRedirectUri = preEstablishedRedirectUri;
	}

	/**
	 * Extract a redirect uri from the resource and/or the current request.
	 * 从资源和/或当前请求中提取重定向uri。
	 * @param request the current {@link DefaultAccessTokenRequest}
	 * @return a redirect uri if one can be established
	 */
	public String getRedirectUri(AccessTokenRequest request) {

		String redirectUri = request.getFirst("redirect_uri");

		if (redirectUri == null && request.getCurrentUri() != null && useCurrentUri) {//
			redirectUri = request.getCurrentUri();
		}

		if (redirectUri == null && getPreEstablishedRedirectUri() != null) {
			// Override the redirect_uri if it is pre-registered
			redirectUri = getPreEstablishedRedirectUri();
		}

		return redirectUri;

	}

}
