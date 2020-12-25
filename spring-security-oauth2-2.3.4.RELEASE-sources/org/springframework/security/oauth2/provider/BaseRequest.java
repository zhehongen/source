/*
 * Copyright 2012-2013 the original author or authors.
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

package org.springframework.security.oauth2.provider;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.common.util.OAuth2Utils;

/**
 *
 * A base class for the three "*Request" classes used in processing OAuth 2
 * authorizations. This class should <strong>never</strong> be used directly,
 * and it should <strong>never</strong> be used as the type for a local or other
 * variable.
 *
 * @author Dave Syer
 * 在处理OAuth 2授权中使用的三个“ * Request”类的基类。
 * 切勿直接使用此类，也不应将其用作局部变量或其他变量的类型。
 */
@SuppressWarnings("serial")
abstract class BaseRequest implements Serializable {

	/**
	 * Resolved client ID. This may be present in the original request
	 * parameters, or in some cases may be inferred by a processing class and
	 * inserted here.
	 * 已解决的客户ID。 这可能存在于原始请求参数中，或者在某些情况下可能由处理类推断并插入此处。
	 */
	private String clientId;

	/**
	 * Resolved scope set, initialized (by the OAuth2RequestFactory) with the
	 * scopes originally requested. Further processing and user interaction may
	 * alter the set of scopes that is finally granted and stored when the
	 * request processing is complete.
	 * 解析的范围集，使用原始请求的范围初始化（由OAuth2RequestFactory初始化）。
	 * 进一步的处理和用户交互可以更改在请求处理完成时最终授予并存储的范围集。
	 */
	private Set<String> scope = new HashSet<String>();

	/**
	 * Map of parameters passed in to the Authorization Endpoint or Token
	 * Endpoint, preserved unchanged from the original request. This map should
	 * not be modified after initialization. In general, classes should not
	 * retrieve values from this map directly, and should instead use the
	 * individual members on this class.
	 *
	 * The OAuth2RequestFactory is responsible for initializing all members of
	 * this class, usually by parsing the values inside the requestParmaeters
	 * map.
	 *	传递到授权端点或令牌端点的参数映射，与原始请求保持不变。 初始化后不应修改此映射。
	 *  通常，类不应直接从此映射检索值，而应使用此类的单个成员。
	 *  OAuth2RequestFactory通常负责解析此类中的所有成员，方法是解析requestParmaeters映射中的值。
	 */
	private Map<String, String> requestParameters = Collections
			.unmodifiableMap(new HashMap<String, String>());

	public String getClientId() {
		return clientId;
	}

	public Set<String> getScope() {
		return scope;
	}

	/**
	 * Warning: most clients should use the individual properties of this class,
	 * such as {{@link #getScope()} or { {@link #getClientId()}, rather than
	 * retrieving values from this map.
	 *
	 * @return the original, unchanged set of request parameters
	 */
	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientId == null) ? 0 : clientId.hashCode());
		result = prime
				* result
				+ ((requestParameters == null) ? 0 : requestParameters
						.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseRequest other = (BaseRequest) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (requestParameters == null) {
			if (other.requestParameters != null)
				return false;
		} else if (!requestParameters.equals(other.requestParameters))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	protected void setScope(Collection<String> scope) {
		if (scope != null && scope.size() == 1) {
			String value = scope.iterator().next();
			/*
			 * This is really an error, but it can catch out unsuspecting users
			 * and it's easy to fix. It happens when an AuthorizationRequest
			 * gets bound accidentally from request parameters using
			 * @ModelAttribute.
			 * 这确实是一个错误，但是它可以找出毫无疑问的用户，并且很容易修复。 
			 * 当使用@ModelAttribute从请求参数中意外绑定AuthorizationRequest时，会发生这种情况。
			 */
			if (value.contains(" ") || value.contains(",")) {
				scope = OAuth2Utils.parseParameterList(value);
			}
		}
		this.scope = Collections
				.unmodifiableSet(scope == null ? new LinkedHashSet<String>()
						: new LinkedHashSet<String>(scope));
	}

	protected void setRequestParameters(Map<String, String> requestParameters) {
		if (requestParameters != null) {
			this.requestParameters = Collections
					.unmodifiableMap(new HashMap<String, String>(requestParameters));
		}
	}

	protected void setClientId(String clientId) {
		this.clientId = clientId;
	}

}
