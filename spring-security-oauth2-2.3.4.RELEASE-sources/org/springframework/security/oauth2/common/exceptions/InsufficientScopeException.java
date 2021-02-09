package org.springframework.security.oauth2.common.exceptions;

import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;

/**
 * Exception representing insufficient scope in a token when a request is handled by a Resource Server. It is akin to an
 * {@link AccessDeniedException} and should result in a 403 (FORBIDDEN) HTTP status.
 * 异常表示资源服务器处理请求时令牌中的作用域不足。 它类似于AccessDeniedException，并且应导致403（FORBIDDEN）HTTP状态。
 * @author Dave Syer
 */
@SuppressWarnings("serial")
public class InsufficientScopeException extends OAuth2Exception {

	public InsufficientScopeException(String msg, Set<String> validScope) {
		this(msg);
		addAdditionalInformation("scope", OAuth2Utils.formatParameterList(validScope));
	}

	public InsufficientScopeException(String msg) {
		super(msg);
	}

	@Override
	public int getHttpErrorCode() {
		return 403;
	}

	@Override
	public String getOAuth2ErrorCode() {
		// Not defined in the spec, so not really an OAuth2Exception 在规范中未定义，因此实际上不是OAuth2Exception
		return "insufficient_scope";
	}

}
