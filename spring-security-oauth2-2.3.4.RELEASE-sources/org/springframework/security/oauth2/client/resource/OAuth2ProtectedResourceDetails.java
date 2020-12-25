package org.springframework.security.oauth2.client.resource;

import java.util.List;

import org.springframework.security.oauth2.common.AuthenticationScheme;

/**
 * Details for an OAuth2-protected resource.
 *
 * @author Ryan Heaton
 * @author Dave Syer
 */
public interface OAuth2ProtectedResourceDetails {

	/**
	 * Get a unique identifier for these protected resource details.
	 *
	 * @return A unique identifier for these protected resource details.
	 * 获取这些受保护资源详细信息的唯一标识符。
	 *
	 * 返回值：
	 * 这些受保护资源详细信息的唯一标识符。
	 */
	public String getId();

	/**
	 * The client identifier to use for this protected resource.
	 *
	 * @return The client identifier to use for this protected resource.
	 * 用于此受保护资源的客户端标识符。
	 *
	 * 返回值：
	 * 用于此受保护资源的客户端标识符。 日你妹 啥翻译
	 */
	public String getClientId();

	/**
	 * The URL to use to obtain an OAuth2 access token.
	 *
	 * @return The URL to use to obtain an OAuth2 access token.
	 * 用于获取OAuth2访问令牌的URL。
	 *
	 * 返回值：
	 * 用于获取OAuth2访问令牌的URL。
	 */
	String getAccessTokenUri();

	/**
	 * Whether this resource is limited to a specific scope. If false, the scope of the authentication request will be
	 * ignored.
	 *
	 * @return Whether this resource is limited to a specific scope.
	 * 此资源是否限于特定范围。 如果为false，则身份验证请求的范围将被忽略。
	 *
	 * 返回值：
	 * 此资源是否限于特定范围。
	 */
	boolean isScoped();

	/**
	 * The scope of this resource. Ignored if the {@link #isScoped() resource isn't scoped}.
	 *
	 * @return The scope of this resource.
	 * 此资源的范围。 如果资源没有作用域则忽略。
	 *
	 * 返回值：
	 * 此资源的范围。
	 */
	List<String> getScope();

	/**
	 * Whether a secret is required to obtain an access token to this resource.
	 *
	 * @return Whether a secret is required to obtain an access token to this resource.
	 * 获取此资源的访问令牌是否需要密码。
	 *
	 * 返回值：
	 * 获取此资源的访问令牌是否需要密码。
	 */
	boolean isAuthenticationRequired();

	/**
	 * The client secret. Ignored if the {@link #isAuthenticationRequired() secret isn't required}.
	 *
	 * @return The client secret.
	 * 客户机密。 如果不需要该机密，则将其忽略。
	 *
	 * 返回值：
	 * 客户机密。
	 */
	String getClientSecret();

	/**
	 * The scheme to use to authenticate the client. E.g. "header" or "query".
	 *
	 * @return The scheme used to authenticate the client.
	 * 用于验证客户端的方案。 例如。 “标题”或“查询”。
	 *
	 * 返回值：
	 * 用于认证客户端的方案。
	 */
	AuthenticationScheme getClientAuthenticationScheme();

	/**
	 * The grant type for obtaining an acces token for this resource.
	 *
	 * @return The grant type for obtaining an acces token for this resource.
	 * 用于获取此资源的访问令牌的授权类型。
	 *
	 * 返回值：
	 * 用于获取此资源的访问令牌的授权类型。
	 */
	String getGrantType();

	/**
	 * Get the bearer token method for this resource.
	 *
	 * @return The bearer token method for this resource.
	 * 获取此资源的承载令牌方法。
	 *
	 * 返回值：
	 * 此资源的承载令牌方法。
	 */
	AuthenticationScheme getAuthenticationScheme();//getClientAuthenticationScheme?啥区别

	/**
	 * The name of the bearer token. The default is "access_token", which is according to the spec, but some providers
	 * (e.g. Facebook) don't conform to the spec.)
	 *
	 * @return The name of the bearer token.
	 * 承载令牌的名称。 默认值为“ access_token”，符合规范，但某些提供商（例如Facebook）不符合规范。）
	 *
	 * 返回值：
	 * 承载令牌的名称。
	 */
	String getTokenName();

	/**
	 * A flag to indicate that this resource is only to be used with client credentials, thus allowing access tokens to
	 * be cached independent of a user's session.
	 *
	 * @return true if this resource is only used with client credentials grant
	 * 一个标志，指示该资源仅与客户端凭据一起使用，因此允许访问令牌独立于用户会话进行缓存。
	 *
	 * 返回值：
	 * 如果此资源仅与客户端凭据授予一起使用，则为true
	 */
	public boolean isClientOnly();
}
