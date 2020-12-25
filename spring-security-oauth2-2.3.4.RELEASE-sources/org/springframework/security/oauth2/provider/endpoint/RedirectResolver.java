package org.springframework.security.oauth2.provider.endpoint;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * Basic interface for determining the redirect URI for a user agent.
 *
 * @author Ryan Heaton
 * 用于确定用户代理的重定向URI的基本接口。
 */
public interface RedirectResolver {

  /**
   * Resolve the redirect for the specified client.
   *
   * @param requestedRedirect The redirect that was requested (may not be null).
   * @param client The client for which we're resolving the redirect.
   * @return The resolved redirect URI.
   * @throws OAuth2Exception If the requested redirect is invalid for the specified client.
   * 解决指定客户端的重定向。
   *
   * 参数：
   * requestRedirect-请求的重定向（不能为null）。
   * client-我们要为其解决重定向的客户端。
   * 返回值：
   * 解析的重定向URI。
   * 抛出：
   * OAuth2Exception-如果请求的重定向对指定的客户端无效。
   */
  String resolveRedirect(String requestedRedirect, ClientDetails client) throws OAuth2Exception;

}
