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

package org.springframework.security.oauth2.provider;

import java.util.Map;

/**
 * Strategy for managing OAuth2 requests: {@link AuthorizationRequest}, {@link TokenRequest}, {@link OAuth2Request}.
 * 
 * @author Dave Syer
 * @author Amanda Anganes
 * 
 */
public interface OAuth2RequestFactory {

	/**
	 * Create a new {@link AuthorizationRequest} extracting all the needed information from the incoming parameter map,
	 * and initializing all individual fields on the {@link AuthorizationRequest} to reasonable values. When a class
	 * uses the factory to create an {@link AuthorizationRequest}, it should not need to access the parameter map
	 * directly afterwards.
	 * 
	 * Typical implementations would initialize the individual fields on the {@link AuthorizationRequest} with the
	 * values requested in the original parameter map. It may also load the client details from the client id provided
	 * and validate the grant type and scopes, populating any fields in the request that are known only to the
	 * authorization server.
	 * 
	 * @param authorizationParameters the parameters in the request
	 * @return a new AuthorizationRequest
	 * 创建一个新的AuthorizationRequest，从传入的参数图中提取所有需要的信息，
	 * 并将AuthorizationRequest上的所有单个字段初始化为合理的值。
	 * 当类使用工厂创建AuthorizationRequest时，此后不需要直接访问参数映射。
	 * 典型的实现将使用原始参数映射中请求的值初始化AuthorizationRequest上的各个字段。
	 * 它还可以从提供的客户端ID加载客户端详细信息，并验证授权类型和范围，填充请求中仅授权服务器知道的任何字段。
	 *
	 * 参数：
	 * authorizationParameters –请求中的参数
	 * 返回值：
	 * 一个新的AuthorizationRequest
	 */
	AuthorizationRequest createAuthorizationRequest(Map<String, String> authorizationParameters);

	/**
	 * Create a new {@link OAuth2Request} by extracting the needed information from the current
	 * {@link AuthorizationRequest} object.
	 * 
	 * @param request the request to be converted
	 * @return an immutable object for storage
	 * 通过从当前的AuthorizationRequest对象中提取所需的信息来创建新的OAuth2Request。
	 *
	 * 参数：
	 * request-要转换的请求
	 * 返回值：
	 * 一个不可变的对象用于存储
	 */
	OAuth2Request createOAuth2Request(AuthorizationRequest request);

	/**
	 * Create a new {@link OAuth2Request} by extracting the needed information from the current {@link TokenRequest}
	 * object.
	 * @param client TODO
	 * @param tokenRequest the request to be converted
	 * 
	 * @return am immutable object for storage
	 * 通过从当前TokenRequest对象中提取所需的信息来创建新的OAuth2Request。
	 *
	 * 参数：
	 * 客户– TODO
	 * tokenRequest –要转换的请求
	 * 返回值：
	 * 一个不可变的对象，用于存储
	 */
	OAuth2Request createOAuth2Request(ClientDetails client, TokenRequest tokenRequest);

	/**
	 * Create a new {@link TokenRequest} by extracting the needed information from the incoming request parameter map.
	 * 
	 * @param requestParameters the parameters in the request
	 * @param authenticatedClient the client that authenticated during the token request
	 * @return a new TokenRequest
	 * 通过从传入的请求参数映射中提取所需的信息来创建新的TokenRequest。
	 *
	 * 参数：
	 * requestParameters –请求中的参数
	 * authenticatedClient –在令牌请求期间进行身份验证的客户端
	 * 返回值：
	 * 一个新的TokenRequest
	 */
	TokenRequest createTokenRequest(Map<String, String> requestParameters, ClientDetails authenticatedClient);

	/**
	 * Create a new {@link TokenRequest} from an {@link AuthorizationRequest}. Principally used by the
	 * AuthorizationEndpoint during the implicit flow.
	 * 
	 * @param authorizationRequest the incoming request
	 * @param grantType the grant type for the token request
	 * @return a new token request
	 * 从AuthorizationRequest创建一个新的TokenRequest。 主要由隐式流中的AuthorizationEndpoint使用。
	 *
	 * 参数：
	 * 授权请求–传入请求
	 * grantType –令牌请求的授权类型
	 * 返回值：
	 * 一个新的令牌请求
	 */
	TokenRequest createTokenRequest(AuthorizationRequest authorizationRequest, String grantType);

}
