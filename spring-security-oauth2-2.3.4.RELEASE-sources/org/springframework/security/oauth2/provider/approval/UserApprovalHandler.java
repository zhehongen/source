package org.springframework.security.oauth2.provider.approval;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;

/**
 * Basic interface for determining whether a given client authentication request has been
 * approved by the current user.
 *
 * @author Ryan Heaton
 * @author Dave Syer
 * @author Amanda Anganes
 * 用于确定给定客户端身份验证请求是否已被当前用户批准的基本接口。
 */
public interface UserApprovalHandler {

	/**
	 * <p>
	 * Tests whether the specified authorization request has been approved by the current
	 * user (if there is one).
	 * </p>
	 *
	 * @param authorizationRequest the authorization request.
	 * @param userAuthentication the user authentication for the current user.
	 * @return true if the request has been approved, false otherwise
	 */
	boolean isApproved(AuthorizationRequest authorizationRequest,
			Authentication userAuthentication);

	/**
	 * <p>
	 * Provides a hook for allowing requests to be pre-approved (skipping the User
	 * Approval Page). Some implementations may allow users to store approval decisions so
	 * that they only have to approve a site once. This method is called in the
	 * AuthorizationEndpoint before sending the user to the Approval page. If this method
	 * sets oAuth2Request.approved to true, the Approval page will be skipped.
	 * </p>
	 *
	 * @param authorizationRequest the authorization request.
	 * @param userAuthentication the user authentication
	 * @return the AuthorizationRequest, modified if necessary
	 * 提供一个挂钩，以允许预先批准请求（跳过用户批准页面）。 一些实现可能允许用户存储批准决定，以便他们只需要批准一次站点。
	 * 在将用户发送到“批准”页面之前，在AuthorizationEndpoint中调用此方法。 如果此方法将oAuth2Request.approved设置为true，则将跳过“批准”页面。
	 *
	 * 参数：
	 * authorizationRequest –授权请求。
	 * userAuthentication –用户身份验证
	 * 返回值：
	 * AuthorizationRequest，必要时进行修改
	 */                  //检查之前是否批准过
	AuthorizationRequest checkForPreApproval(AuthorizationRequest authorizationRequest,
			Authentication userAuthentication);

	/**
	 * <p>
	 * Provides an opportunity to update the authorization request after the
	 * {@link AuthorizationRequest#setApprovalParameters(Map) approval parameters} are set
	 * but before it is checked for approval. Useful in cases where the incoming approval
	 * parameters contain richer information than just true/false (e.g. some scopes are
	 * approved, and others are rejected), implementations may need to be able to modify
	 * the {@link AuthorizationRequest} before a token is generated from it.
	 * </p>
	 *
	 * @param authorizationRequest the authorization request.
	 * @param userAuthentication the user authentication
	 * @return the AuthorizationRequest, modified if necessary
	 * 在设置了批准参数之后但在检查批准之前，提供了更新授权请求的机会。
	 * 在传入的批准参数包含的信息不仅仅包含true / false的信息时（例如，某些范围被批准，而其他范围被拒绝），
	 * 此方法很有用，实现可能需要能够在从令牌生成令牌之前修改AuthorizationRequest。
	 *
	 * 参数：
	 * authorizationRequest –授权请求。
	 * userAuthentication –用户身份验证
	 * 返回值：
	 * AuthorizationRequest，必要时进行修改
	 */
	AuthorizationRequest updateAfterApproval(AuthorizationRequest authorizationRequest,
			Authentication userAuthentication);

	/**
	 * Generate a request for the authorization server to ask for the user's approval.
	 * Typically this will be rendered into a view (HTML etc.) to prompt for the approval,
	 * so it needs to contain information about the grant (scopes and client id for
	 * instance).
	 *
	 * @param authorizationRequest the authorization request
	 * @param userAuthentication the user authentication
	 * @return a model map for rendering to the user to ask for approval
	 * 生成请求授权服务器以请求用户的批准。 通常，这将呈现到视图（HTML等）中以提示批准，因此它需要包含有关授予的信息（例如，作用域和客户端ID）。
	 *
	 * 参数：
	 * authorizationRequest –授权请求
	 * userAuthentication –用户身份验证
	 * 返回值：
	 * 模型图以呈现给用户以请求批准
	 */                 //也就是要在页面上展示的供用户授权的权限吧
	Map<String, Object> getUserApprovalRequest(AuthorizationRequest authorizationRequest,
			Authentication userAuthentication);

}
