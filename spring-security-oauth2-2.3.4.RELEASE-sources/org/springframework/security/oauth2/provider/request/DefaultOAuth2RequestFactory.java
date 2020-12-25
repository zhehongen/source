/*
 * Copyright 2006-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.security.oauth2.provider.request;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link OAuth2RequestFactory} which initializes fields from the parameters map, validates
 * grant types and scopes, and fills in scopes with the default values from the client if they are missing.
 *
 * @author Dave Syer
 * @author Amanda Anganes
 * OAuth2RequestFactory的默认实现可初始化参数映射中的字段，验证授权类型和范围，并使用缺少客户端的默认值填充范围。
 */
public class DefaultOAuth2RequestFactory implements OAuth2RequestFactory {

    private final ClientDetailsService clientDetailsService;

    private SecurityContextAccessor securityContextAccessor = new DefaultSecurityContextAccessor();

    private boolean checkUserScopes = false;

    public DefaultOAuth2RequestFactory(ClientDetailsService clientDetailsService) {
        this.clientDetailsService = clientDetailsService;
    }

    /**
     * @param securityContextAccessor the security context accessor to set
     */
    public void setSecurityContextAccessor(SecurityContextAccessor securityContextAccessor) {
        this.securityContextAccessor = securityContextAccessor;
    }

    /**
     * Flag to indicate that scopes should be interpreted as valid authorities. No scopes will be granted to a user
     * unless they are permitted as a granted authority to that user.
     *
     * @param checkUserScopes the checkUserScopes to set (default false)
     *                        表示作用域应解释为有效权限的标志。 除非将范围授予该用户授权的权限，否则不会将其授予该用户。
     *                        <p>
     *                        参数：
     *                        checkUserScopes –要设置的checkUserScopes（默认为false）
     */
    public void setCheckUserScopes(boolean checkUserScopes) {
        this.checkUserScopes = checkUserScopes;
    }

    public AuthorizationRequest createAuthorizationRequest(Map<String, String> authorizationParameters) {

        String clientId = authorizationParameters.get(OAuth2Utils.CLIENT_ID);
        String state = authorizationParameters.get(OAuth2Utils.STATE);
        String redirectUri = authorizationParameters.get(OAuth2Utils.REDIRECT_URI);
        Set<String> responseTypes = OAuth2Utils.parseParameterList(authorizationParameters
                .get(OAuth2Utils.RESPONSE_TYPE));

        Set<String> scopes = extractScopes(authorizationParameters, clientId);

        AuthorizationRequest request = new AuthorizationRequest(authorizationParameters,
                Collections.<String, String>emptyMap(), clientId, scopes, null, null, false, state, redirectUri,
                responseTypes);

        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
        request.setResourceIdsAndAuthoritiesFromClientDetails(clientDetails);

        return request;

    }

    public OAuth2Request createOAuth2Request(AuthorizationRequest request) {
        return request.createOAuth2Request();
    }

    public TokenRequest createTokenRequest(Map<String, String> requestParameters, ClientDetails authenticatedClient) {

        String clientId = requestParameters.get(OAuth2Utils.CLIENT_ID);
        if (clientId == null) {
            // if the clientId wasn't passed in in the map, we add pull it from the authenticated client object
            //如果未在地图中传递clientId，则将其从经过身份验证的客户端对象中添加
            clientId = authenticatedClient.getClientId();
        } else {
            // otherwise, make sure that they match
            if (!clientId.equals(authenticatedClient.getClientId())) {
                throw new InvalidClientException("Given client ID does not match authenticated client");
            }
        }
        String grantType = requestParameters.get(OAuth2Utils.GRANT_TYPE);

        Set<String> scopes = extractScopes(requestParameters, clientId);
        TokenRequest tokenRequest = new TokenRequest(requestParameters, clientId, scopes, grantType);

        return tokenRequest;
    }

    public TokenRequest createTokenRequest(AuthorizationRequest authorizationRequest, String grantType) {
        TokenRequest tokenRequest = new TokenRequest(authorizationRequest.getRequestParameters(),
                authorizationRequest.getClientId(), authorizationRequest.getScope(), grantType);
        return tokenRequest;
    }

    public OAuth2Request createOAuth2Request(ClientDetails client, TokenRequest tokenRequest) {
        return tokenRequest.createOAuth2Request(client);
    }

    private Set<String> extractScopes(Map<String, String> requestParameters, String clientId) {
        Set<String> scopes = OAuth2Utils.parseParameterList(requestParameters.get(OAuth2Utils.SCOPE));
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);

        if ((scopes == null || scopes.isEmpty())) {
            // If no scopes are specified in the incoming data, use the default values registered with the client
            // (the spec allows us to choose between this option and rejecting the request completely, so we'll take the
            // least obnoxious choice as a default).
            //如果传入数据中未指定任何范围，请使用在客户端注册的默认值
            //（规范允许我们在此选项和完全拒绝请求之间进行选择，因此我们将最讨厌的选择作为默认值）。
            scopes = clientDetails.getScope();
        }

        if (checkUserScopes) {
            scopes = checkUserScopes(scopes, clientDetails);
        }
        return scopes;
    }

    private Set<String> checkUserScopes(Set<String> scopes, ClientDetails clientDetails) {
        if (!securityContextAccessor.isUser()) {//不是用户
            return scopes;
        }
        Set<String> result = new LinkedHashSet<String>();
        Set<String> authorities = AuthorityUtils.authorityListToSet(securityContextAccessor.getAuthorities());
        for (String scope : scopes) {
            if (authorities.contains(scope) || authorities.contains(scope.toUpperCase())
                    || authorities.contains("ROLE_" + scope.toUpperCase())) {
                result.add(scope);
            }
        }
        return result;
    }

}
