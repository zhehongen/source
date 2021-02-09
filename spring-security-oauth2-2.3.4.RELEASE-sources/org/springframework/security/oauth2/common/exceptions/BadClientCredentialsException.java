package org.springframework.security.oauth2.common.exceptions;

/**
 * Exception thrown when a client was unable to authenticate. 客户端无法进行身份验证时引发的异常。
 *
 * @author Ryan Heaton
 * @author Dave Syer
 */
@SuppressWarnings("serial")
public class BadClientCredentialsException extends ClientAuthenticationException {

	public BadClientCredentialsException() {
		super("Bad client credentials"); // Don't reveal source of error 不要透露错误源
	}

	@Override
	public int getHttpErrorCode() {
		return 401;
	}

	@Override
	public String getOAuth2ErrorCode() {
		return "invalid_client";
	}
}
