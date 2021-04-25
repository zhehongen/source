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

/**
 * A list of SAML known 2 error codes used during SAML authentication.
 *
 * @since 5.2
 */
public interface Saml2ErrorCodes {
	/**SAML数据不表示SAML 2响应对象。接收到有效的XML对象，但该对象不是每个规范的ResponseType类型的SAML2响应对象
	 * SAML Data does not represent a SAML 2 Response object.
	 * A valid XML object was received, but that object was not a
	 * SAML 2 Response object of type {@code ResponseType} per specification
	 * https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf#page=46
	 */
	String UNKNOWN_RESPONSE_CLASS = "unknown_response_class";
	/**响应数据格式不正确或不完整。 接收到无效的XML对象，并且XML解组失败。
	 * The response data is malformed or incomplete.
	 * An invalid XML object was received, and XML unmarshalling failed.
	 */
	String MALFORMED_RESPONSE_DATA = "malformed_response_data";
	/**响应目标与请求URL不匹配。 在与响应对象的{code Destination}属性中存储的URL不匹配的URL处接收到SAML 2响应对象。
	 * Response destination does not match the request URL.
	 * A SAML 2 response object was received at a URL that
	 * did not match the URL stored in the {code Destination} attribute
	 * in the Response object.
	 * https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf#page=38
	 */
	String INVALID_DESTINATION = "invalid_destination";
	/**该断言无效。 用于身份验证的断言验证失败。 有关错误的详细信息将出现在错误说明中。
	 * The assertion was not valid.
	 * The assertion used for authentication failed validation.
	 * Details around the failure will be present in the error description.
	 */
	String INVALID_ASSERTION = "invalid_assertion";
	/**响应或声明的签名无效。 响应或断言缺少签名，或者无法使用系统配置的凭据来验证签名。 最常见的是IDP的X509证书。
	 * The signature of response or assertion was invalid.
	 * Either the response or the assertion was missing a signature
	 * or the signature could not be verified using the system's
	 * configured credentials. Most commonly the IDP's
	 * X509 certificate.
	 */
	String INVALID_SIGNATURE = "invalid_signature";
	/**
	 * The assertion did not contain a subject element.
	 * The subject element, type SubjectType, contains
	 * a {@code NameID} or an {@code EncryptedID} that is used
	 * to assign the authenticated principal an identifier,
	 * typically a username.
	 *该断言不包含主题元素。 subject元素（类型为SubjectType）包含一个NameID或EncryptedID，用于为已验证的委托人分配一个标识符，通常是一个用户名。
	 * https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf#page=18
	 */
	String SUBJECT_NOT_FOUND = "subject_not_found";
	/**
	 * The subject did not contain a user identifier
	 * The assertion contained a subject element, but the subject
	 * element did not have a {@code NameID} or {@code EncryptedID}
	 * element
	 *主题不包含用户标识符。断言包含一个subject元素，但是subject元素没有NameID或EncryptedID元素
	 * https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf#page=18
	 */
	String USERNAME_NOT_FOUND = "username_not_found";
	/**系统无法解密断言或名称标识符。 如果EncryptedAssertion或EncryptedID的解密失败，将引发此错误代码。
	 * The system failed to decrypt an assertion or a name identifier.
	 * This error code will be thrown if the decryption of either a
	 * {@code EncryptedAssertion} or {@code EncryptedID} fails.
	 * https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf#page=17
	 */
	String DECRYPTION_ERROR = "decryption_error";
	/**
	 * An Issuer element contained a value that didn't
	 * https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf#page=15
	 */
	String INVALID_ISSUER = "invalid_issuer";
	/**验证期间发生错误。 在身份验证过程中发现内部非机密错误时使用。
	 * An error happened during validation.
	 * Used when internal, non classified, errors are caught during the
	 * authentication process.
	 */
	String INTERNAL_VALIDATION_ERROR = "internal_validation_error";
	/**找不到依赖方注册。 该注册ID与任何依赖方注册都不对应。
	 * The relying party registration was not found.
	 * The registration ID did not correspond to any relying party registration.
	 */
	String RELYING_PARTY_REGISTRATION_NOT_FOUND = "relying_party_registration_not_found";
}
