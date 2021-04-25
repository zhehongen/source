/* Copyright 2009 Vladimir Schäfer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.saml.websso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.SAMLStatusException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.processor.SAMLProcessor;
import org.springframework.security.saml.storage.SAMLMessageStorage;
import org.springframework.util.Assert;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Condition;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.OneTimeUse;
import org.opensaml.saml2.core.ProxyRestriction;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.validation.ValidationException;

import static org.springframework.security.saml.util.SAMLUtil.isDateTimeSkewValid;

/**在SP初始化SSO或来自IDP的未经请求的响应之后，该类能够处理从IDP返回的Response对象。 如果正确验证了响应并且未发现错误，则会创建SAMLCredential。
 * Class is able to process Response objects returned from the IDP after SP initialized SSO or unsolicited
 * response from IDP. In case the response is correctly validated and no errors are found the SAMLCredential
 * is created.
 *
 * @author Vladimir Schäfer
 */
public class WebSSOProfileConsumerImpl extends AbstractProfileBase implements WebSSOProfileConsumer {

    public WebSSOProfileConsumerImpl() {
    }

    public WebSSOProfileConsumerImpl(SAMLProcessor processor, MetadataManager manager) {
        super(processor, manager);
    }

    @Override
    public String getProfileIdentifier() {
        return SAMLConstants.SAML2_WEBSSO_PROFILE_URI;
    }

    /**
     * Maximum time between users authentication and processing of the AuthNResponse message. (in seconds)
     */
    private long maxAuthenticationAge = 7200;

    /**
     * Flag indicating whether to include attributes from all assertions, false by default.
     */
    private boolean includeAllAttributes = false;

    /**
     * Flag indicates whether to release internal DOM structures before returning SAMLCredential.
     */
    private boolean releaseDOM = true;

    /**输入上下文对象必须已设置与返回的Response相关的属性，该属性已经过验证，如果未发现错误，则返回SAMLCredential。
     * The input context object must have set the properties related to the returned Response, which is validated
     * and in case no errors are found the SAMLCredential is returned.
     *
     *
     * @param context context including response object
     * @return SAMLCredential with information about user
     * @throws SAMLException       in case the response is invalid
     * @throws org.opensaml.xml.security.SecurityException
     *                             in the signature on response can't be verified
     * @throws ValidationException in case the response structure is not conforming to the standard
     */
    public SAMLCredential processAuthenticationResponse(SAMLMessageContext context) throws SAMLException, org.opensaml.xml.security.SecurityException, ValidationException, DecryptionException {

        AuthnRequest request = null;
        SAMLObject message = context.getInboundSAMLMessage();

        // Verify type
        if (!(message instanceof Response)) {
            throw new SAMLException("Message is not of a Response object type");
        }
        Response response = (Response) message;

        // Verify status
		StatusCode statusCode = response.getStatus().getStatusCode();
		if (!StatusCode.SUCCESS_URI.equals(statusCode.getValue())) {
            StatusMessage statusMessage = response.getStatus().getStatusMessage();
            String statusMessageText = null;
            if (statusMessage != null) {
                statusMessageText = statusMessage.getMessage();
            }
			// The final status code will be the most internal one最终的状态码将是最内部的
			String finalStatusCode = statusCode.getValue();
			while (statusCode.getStatusCode() != null){
				finalStatusCode = statusCode.getStatusCode().getValue();
				statusCode = statusCode.getStatusCode();
			}
			throw new SAMLStatusException(
				finalStatusCode,
				"Response has invalid status code " + finalStatusCode + ", status message is " + statusMessageText
			);
        }
		//验证响应的签名（如果存在），除非已在绑定中进行验证
        // Verify signature of the response if present, unless already verified in binding
        if (response.getSignature() != null && !context.isInboundSAMLMessageAuthenticated()) {
            log.debug("Verifying Response signature");
            verifySignature(response.getSignature(), context.getPeerEntityId(), context.getLocalTrustEngine());
            context.setInboundSAMLMessageAuthenticated(true);
        }

        // Verify issue time
        DateTime time = response.getIssueInstant();
        if (!isDateTimeSkewValid(getResponseSkew(), time)) {
            throw new SAMLException("Response issue time is either too old or with date in the future, skew " + getResponseSkew() + ", time " + time);
        }

        // Reject unsolicited messages when disabled
        if (!context.getPeerExtendedMetadata().isSupportUnsolicitedResponse() && response.getInResponseTo() == null) {
            throw new SAMLException("Reception of Unsolicited Response messages (without InResponseToField) is disabled");
        }

        // Verify response to field if present, set request if correct验证对字段的响应（如果存在），如果正确则设置请求
        SAMLMessageStorage messageStorage = context.getMessageStorage();
        if (messageStorage != null && response.getInResponseTo() != null) {
            XMLObject xmlObject = messageStorage.retrieveMessage(response.getInResponseTo());
            if (xmlObject == null) {
                throw new SAMLException("InResponseToField of the Response doesn't correspond to sent message " + response.getInResponseTo());
            } else if (xmlObject instanceof AuthnRequest) {
                request = (AuthnRequest) xmlObject;
            } else {
                throw new SAMLException("Sent request was of different type than the expected AuthnRequest " + response.getInResponseTo());
            }
        }

        // Verify that message was received at the expected endpoint
        verifyEndpoint(context.getLocalEntityEndpoint(), response.getDestination());

        // Verify endpoint requested in the original request 验证原始请求中请求的端点
        if (request != null) {
            AssertionConsumerService assertionConsumerService = (AssertionConsumerService) context.getLocalEntityEndpoint();
            if (request.getAssertionConsumerServiceIndex() != null) {
                if (!request.getAssertionConsumerServiceIndex().equals(assertionConsumerService.getIndex())) {
                    log.info("Response was received at a different endpoint index than was requested");
                }
            } else {
                String requestedResponseURL = request.getAssertionConsumerServiceURL();
                String requestedBinding = request.getProtocolBinding();
                if (requestedResponseURL != null) {
                    String responseLocation;
                    if (assertionConsumerService.getResponseLocation() != null) {
                        responseLocation = assertionConsumerService.getResponseLocation();
                    } else {
                        responseLocation = assertionConsumerService.getLocation();
                    }
                    if (!requestedResponseURL.equals(responseLocation)) {
                        log.info("Response was received at a different endpoint URL {} than was requested {}", responseLocation, requestedResponseURL);
                    }
                }
                if (requestedBinding != null) {
                    if (!requestedBinding.equals(context.getInboundSAMLBinding())) {
                        log.info("Response was received using a different binding {} than was requested {}", context.getInboundSAMLBinding(), requestedBinding);
                    }
                }
            }
        }

        // Verify issuer
        if (response.getIssuer() != null) {
            log.debug("Verifying issuer of the Response");
            Issuer issuer = response.getIssuer();
            verifyIssuer(issuer, context);
        }

        Assertion subjectAssertion = null;
        List<Attribute> attributes = new ArrayList<Attribute>();
        List<Assertion> assertionList = response.getAssertions();

        // Decrypt assertions
        if (response.getEncryptedAssertions().size() > 0) {
            assertionList = new ArrayList<Assertion>(response.getAssertions().size() + response.getEncryptedAssertions().size());
            assertionList.addAll(response.getAssertions());
            List<EncryptedAssertion> encryptedAssertionList = response.getEncryptedAssertions();
            for (EncryptedAssertion ea : encryptedAssertionList) {
                try {
                    Assert.notNull(context.getLocalDecrypter(), "Can't decrypt Assertion, no decrypter is set in the context");
                    log.debug("Decrypting assertion");
                    Assertion decryptedAssertion = context.getLocalDecrypter().decrypt(ea);
                    assertionList.add(decryptedAssertion);
                } catch (DecryptionException e) {
                    log.debug("Decryption of received assertion failed, assertion will be skipped", e);
                }
            }
        }

        Exception lastError = null;

        // Find the assertion to be used for session creation and verify 找到用于会话创建的断言并验证
        for (Assertion assertion : assertionList) {
            if (assertion.getAuthnStatements().size() > 0) {
                try {
                    // Verify that the assertion is valid
                    verifyAssertion(assertion, request, context);
                    subjectAssertion = assertion;
                    log.debug("Validation of authentication statement in assertion {} was successful", assertion.getID());
                    break;
                } catch (Exception e) {
                    log.debug("Validation of authentication statement in assertion failed, skipping", e);
                    lastError = e;
                }
            } else {
                log.debug("Assertion {} did not contain any authentication statements, skipping", assertion.getID());
            }
        }
        //确保至少一个断言包含身份验证语句和带有承载者确认的主题
        // Make sure that at least one assertion contains authentication statement and subject with bearer confirmation
        if (subjectAssertion == null) {
            throw new SAMLException("Response doesn't have any valid assertion which would pass subject validation", lastError);
        }

        // Process attributes from assertions 根据断言处理属性
        for (Assertion assertion : assertionList) {
            if (assertion == subjectAssertion || isIncludeAllAttributes()) {
                for (AttributeStatement attStatement : assertion.getAttributeStatements()) {
                    for (Attribute att : attStatement.getAttributes()) {
                        log.debug("Including attribute {} from assertion {}", att.getName(), assertion.getID());
                        attributes.add(att);
                    }
                    for (EncryptedAttribute att : attStatement.getEncryptedAttributes()) {
                        Assert.notNull(context.getLocalDecrypter(), "Can't decrypt Attribute, no decrypter is set in the context");
                        Attribute decryptedAttribute = context.getLocalDecrypter().decrypt(att);
                        log.debug("Including decrypted attribute {} from assertion {}", decryptedAttribute.getName(), assertion.getID());
                        attributes.add(decryptedAttribute);
                    }
                }
            }
        }

        NameID nameId = (NameID) context.getSubjectNameIdentifier();
        if (nameId == null) {
            throw new SAMLException("NameID element must be present as part of the Subject in the Response message, please enable it in the IDP configuration");
        }

        // Populate custom data, if any
        Serializable additionalData = processAdditionalData(context);

        // Release extra DOM data which might get otherwise stored in session释放可能会另外存储在会话中的额外DOM数据
        if (isReleaseDOM()) {
            subjectAssertion.releaseDOM();
            subjectAssertion.releaseChildrenDOM(true);
        }

        // Create the credential
        return new SAMLCredential(nameId, subjectAssertion, context.getPeerEntityMetadata().getEntityID(), context.getRelayState(), attributes, context.getLocalEntityId(), additionalData);

    }

    /**这是一个挂钩方法，使子类可以处理来自SAML交换的其他数据，例如具有不同确认或其他属性的断言。 返回的对象存储在SAMLCredential中。 实现负责确保符合SAML规范。 一旦所有其他处理完成并且传入消息被视为有效，则调用该方法。
     * This is a hook method enabling subclasses to process additional data from the SAML exchange, like assertions with different confirmations
     * or additional attributes. The returned object is stored inside the SAMLCredential. Implementation is responsible for ensuring compliance
     * with the SAML specification. The method is called once all the other processing was finished and incoming message is deemed as valid.
     *
     * @param context context containing incoming message
     * @return object to store in the credential, null by default
     * @throws SAMLException in case processing fails
     */
    protected Serializable processAdditionalData(SAMLMessageContext context) throws SAMLException {
        return null;
    }

    protected void verifyAssertion(Assertion assertion, AuthnRequest request, SAMLMessageContext context) throws AuthenticationException, SAMLException, org.opensaml.xml.security.SecurityException, ValidationException, DecryptionException {

        // Verify storage time skew
        if (!isDateTimeSkewValid(getResponseSkew(), getMaxAssertionTime(), assertion.getIssueInstant())) {
            throw new SAMLException("Assertion is too old to be used, value can be customized by setting maxAssertionTime value " + assertion.getIssueInstant());
        }

        // Verify validity of storage
        // Advice is ignored, core 574
        verifyIssuer(assertion.getIssuer(), context);
        verifyAssertionSignature(assertion.getSignature(), context);

        // Check subject
        if (assertion.getSubject() != null) {
            verifySubject(assertion.getSubject(), request, context);
        } else {
            throw new SAMLException("Assertion does not contain subject and is discarded");
        }
        //身份验证声明的断言必须包含受众限制
        // Assertion with authentication statement must contain audience restriction
        if (assertion.getAuthnStatements().size() > 0) {
            verifyAssertionConditions(assertion.getConditions(), context, true);
            for (AuthnStatement statement : assertion.getAuthnStatements()) {
                if (request != null) {
                    verifyAuthenticationStatement(statement, request.getRequestedAuthnContext(), context);
                } else {
                    verifyAuthenticationStatement(statement, null, context);
                }
            }
        } else {
            verifyAssertionConditions(assertion.getConditions(), context, false);
        }

    }

    /**验证Subject元素的有效性，仅确认承载确认。
     * Verifies validity of Subject element, only bearer confirmation is validated.
     *
     * @param subject subject to validate
     * @param request request
     * @param context context
     * @throws SAMLException       error validating the object
     * @throws DecryptionException in case the NameID can't be decrypted
     */
    protected void verifySubject(Subject subject, AuthnRequest request, SAMLMessageContext context) throws SAMLException, DecryptionException {

        for (SubjectConfirmation confirmation : subject.getSubjectConfirmations()) {

            if (SubjectConfirmation.METHOD_BEARER.equals(confirmation.getMethod())) {

                log.debug("Processing Bearer subject confirmation");
                SubjectConfirmationData data = confirmation.getSubjectConfirmationData();

                // Bearer must have confirmation saml-profiles-2.0-os 554
                if (data == null) {
                    log.debug("Bearer SubjectConfirmation invalidated by missing confirmation data");
                    continue;
                }

                // Not before forbidden by saml-profiles-2.0-os 558
                if (data.getNotBefore() != null) {
                    log.debug("Bearer SubjectConfirmation invalidated by not before which is forbidden");
                    continue;
                }

                // Required by saml-profiles-2.0-os 556
                if (data.getNotOnOrAfter() == null) {
                    log.debug("Bearer SubjectConfirmation invalidated by missing notOnOrAfter");
                    continue;
                }

                // Validate not on or after
                if (data.getNotOnOrAfter().plusSeconds(getResponseSkew()).isBeforeNow()) {
                    log.debug("Bearer SubjectConfirmation invalidated by notOnOrAfter");
                    continue;
                }

                // Validate in response to
                if (request != null) {
                    if (data.getInResponseTo() == null) {//无法理解
                        log.debug("Bearer SubjectConfirmation invalidated by missing inResponseTo field");
                        continue;
                    } else {
                        if (!data.getInResponseTo().equals(request.getID())) {
                            log.debug("Bearer SubjectConfirmation invalidated by invalid in response to");
                            continue;
                        }
                    }
                }

                // Validate recipient 验证收件人
                if (data.getRecipient() == null) {
                    log.debug("Bearer SubjectConfirmation invalidated by missing recipient");
                    continue;
                } else {
                    try {
                        verifyEndpoint(context.getLocalEntityEndpoint(), data.getRecipient());
                    } catch (SAMLException e) {
                        log.debug("Bearer SubjectConfirmation invalidated by recipient assertion consumer URL, found {}", data.getRecipient());
                        continue;
                    }
                }
                //该主题被这个确认数据确认了吗？ 如果是这样，让我们将主题存储在上下文中。
                // Was the subject confirmed by this confirmation data? If so let's store the subject in the context.
                NameID nameID;
                if (subject.getEncryptedID() != null) {
                    Assert.notNull(context.getLocalDecrypter(), "Can't decrypt NameID, no decrypter is set in the context");
                    nameID = (NameID) context.getLocalDecrypter().decrypt(subject.getEncryptedID());
                } else {
                    nameID = subject.getNameID();
                }
                context.setSubjectNameIdentifier(nameID);
                return;

            }

        }

        throw new SAMLException("Assertion invalidated by subject confirmation - can't be confirmed by the bearer method");

    }

    /**验证断言的签名。 如果不存在签名，并且元数据中包含SP必需的签名，则会引发异常。
     * Verifies signature of the assertion. In case signature is not present and SP required signatures in metadata
     * the exception is thrown.
     *
     * @param signature signature to verify
     * @param context   context
     * @throws SAMLException       signature missing although required
     * @throws org.opensaml.xml.security.SecurityException
     *                             signature can't be validated
     * @throws ValidationException signature is malformed
     */
    protected void verifyAssertionSignature(Signature signature, SAMLMessageContext context) throws SAMLException, org.opensaml.xml.security.SecurityException, ValidationException {
        SPSSODescriptor roleMetadata = (SPSSODescriptor) context.getLocalEntityRoleMetadata();
        boolean wantSigned = roleMetadata.getWantAssertionsSigned();
        if (signature != null) {
            verifySignature(signature, context.getPeerEntityMetadata().getEntityID(), context.getLocalTrustEngine());
        } else if (wantSigned) {
            if (!context.isInboundSAMLMessageAuthenticated()) {//Response or Assertion 其中一个合法就行？
                throw new SAMLException("Metadata includes wantAssertionSigned, but neither Response nor included Assertion is signed");
            }
        }
    }

    protected void verifyAssertionConditions(Conditions conditions, SAMLMessageContext context, boolean audienceRequired) throws SAMLException {

        // Verify that audience is present when required
        if (audienceRequired && (conditions == null || conditions.getAudienceRestrictions().size() == 0)) {
            throw new SAMLException("Assertion invalidated by missing Audience Restriction");
        }

        // If no conditions are implied, storage is deemed valid 如果没有暗示条件，则认为存储有效
        if (conditions == null) {
            return;
        }

        if (conditions.getNotBefore() != null) {
            if (conditions.getNotBefore().minusSeconds(getResponseSkew()).isAfterNow()) {
                throw new SAMLException("Assertion is not yet valid, invalidated by condition notBefore " + conditions.getNotBefore());
            }
        }
        if (conditions.getNotOnOrAfter() != null) {
            if (conditions.getNotOnOrAfter().plusSeconds(getResponseSkew()).isBeforeNow()) {
                throw new SAMLException("Assertion is no longer valid, invalidated by condition notOnOrAfter " + conditions.getNotOnOrAfter());
            }
        }

        List<Condition> notUnderstoodConditions = new LinkedList<Condition>();

        for (Condition condition : conditions.getConditions()) {

            QName conditionQName = condition.getElementQName();

            if (conditionQName.equals(AudienceRestriction.DEFAULT_ELEMENT_NAME)) {

                verifyAudience(context, conditions.getAudienceRestrictions());

            } else if (conditionQName.equals(OneTimeUse.DEFAULT_ELEMENT_NAME)) {

                throw new SAMLException("System cannot honor OneTimeUse condition of the Assertion for WebSSO");

            } else if (conditionQName.equals(ProxyRestriction.DEFAULT_ELEMENT_NAME)) {

                ProxyRestriction restriction = (ProxyRestriction) condition;
                log.debug("Honoring ProxyRestriction with count {}, system does not issue assertions to 3rd parties", restriction.getProxyCount());

            } else {

                log.debug("Condition {} is not understood", condition);
                notUnderstoodConditions.add(condition);

            }

        }

        // Check not understood conditions
        verifyConditions(context, notUnderstoodConditions);

    }

    /**方法验证断言的受众限制。 多个受众群体限制被视为逻辑“与”，并且所有实体中都必须存在本地实体。 一个逻辑OR的限制内有多个受众。
     * Method verifies audience restrictions of the assertion. Multiple audience restrictions are treated as
     * a logical AND and local entity must be present in all of them. Multiple audiences within one restrictions
     * for a logical OR.
     *
     * @param context context
     * @param audienceRestrictions audience restrictions to verify
     * @throws SAMLException in case local entity doesn't match the audience restrictions
     */
    protected void verifyAudience(SAMLMessageContext context, List<AudienceRestriction> audienceRestrictions) throws SAMLException {

        // Multiple AudienceRestrictions form a logical "AND" (saml-core, 922-925)
        audience:
        for (AudienceRestriction rest : audienceRestrictions) {
            if (rest.getAudiences().size() == 0) {
                throw new SAMLException("No audit audience specified for the assertion");
            }
            for (Audience aud : rest.getAudiences()) {
                // Multiple Audiences within one AudienceRestriction form a logical "OR" (saml-core, 922-925)
                if (context.getLocalEntityId().equals(aud.getAudienceURI())) {
                    continue audience;
                }
            }
            throw new SAMLException("Local entity is not the intended audience of the assertion in at least " +
                    "one AudienceRestriction");
        }

    }

    /**验证未理解的断言条件。 默认情况下，如果出现任何不可理解的情况，系统将发生故障。
     * Verifies conditions of the assertion which were are not understood. By default system fails in case any
     * non-understood condition is present.
     *
     * @param context    message context
     * @param conditions conditions which were not understood
     * @throws SAMLException in case conditions are not empty
     */
    protected void verifyConditions(SAMLMessageContext context, List<Condition> conditions) throws SAMLException {
        if (conditions != null && conditions.size() > 0) {
            throw new SAMLException("Assertion contains conditions which are not understood");
        }
    }

    /**验证身份验证语句是否有效。 检查authInstant和sessionNotOnOrAfter字段。
     * Verifies that authentication statement is valid. Checks the authInstant and sessionNotOnOrAfter fields.
     *
     * @param auth                  statement to check
     * @param requestedAuthnContext original requested context can be null for unsolicited messages or when no context was requested
     * @param context               message context
     * @throws AuthenticationException in case the statement is invalid
     */
    protected void verifyAuthenticationStatement(AuthnStatement auth, RequestedAuthnContext requestedAuthnContext, SAMLMessageContext context) throws AuthenticationException {

        // Validate that user wasn't authenticated too long time ago
        if (!isDateTimeSkewValid(getResponseSkew(), getMaxAuthenticationAge(), auth.getAuthnInstant())) {
            throw new CredentialsExpiredException("Authentication statement is too old to be used with value " + auth.getAuthnInstant());
        }

        // Validate users session is still valid
            if (auth.getSessionNotOnOrAfter() != null && auth.getSessionNotOnOrAfter().isBeforeNow()) {
            throw new CredentialsExpiredException("Authentication session is not valid on or after " + auth.getSessionNotOnOrAfter());
        }

        // Verify context
        verifyAuthnContext(requestedAuthnContext, auth.getAuthnContext(), context);

    }

    /**期望实现能够验证所请求的身份验证上下文与接收到的值相对应。可以从SAMLContext加载发送上下文的身份提供者。默认情况下，仅对“精确”上下文进行验证。检查接收到的上下文是否包含请求的方法之一。如果requestedAuthnContext为null，则不进行验证。
     可以在子类中重新实现方法。
     * Implementation is expected to verify that the requested authentication context corresponds with the received value.
     * Identity provider sending the context can be loaded from the SAMLContext.
     * <p>
     * By default verification is done only for "exact" context. It is checked whether received context contains one of the requested
     * method.
     * <p>
     * In case requestedAuthnContext is null no verification is done.
     * <p>
     * Method can be reimplemented in subclasses.
     *
     * @param requestedAuthnContext context requested in the original request, null for unsolicited messages or when no context was required在原始请求中请求的上下文，对于不请自来的消息或不需要上下文时为null
     * @param receivedContext       context from the response message
     * @param context               saml context
     * @throws InsufficientAuthenticationException
     *          in case expected context doesn't correspond with the received value
     */
    protected void verifyAuthnContext(RequestedAuthnContext requestedAuthnContext, AuthnContext receivedContext, SAMLMessageContext context) throws InsufficientAuthenticationException {

        log.debug("Verifying received AuthnContext {} against requested {}", receivedContext, requestedAuthnContext);

        if (requestedAuthnContext != null && AuthnContextComparisonTypeEnumeration.EXACT.equals(requestedAuthnContext.getComparison())) {

            String classRef = null, declRef = null;

            if (receivedContext.getAuthnContextClassRef() != null) {
                classRef = receivedContext.getAuthnContextClassRef().getAuthnContextClassRef();
            }

            if (requestedAuthnContext.getAuthnContextClassRefs() != null) {
                for (AuthnContextClassRef classRefRequested : requestedAuthnContext.getAuthnContextClassRefs()) {
                    if (classRefRequested.getAuthnContextClassRef().equals(classRef)) {
                        log.debug("AuthContext matched with value {}", classRef);
                        return;
                    }
                }
            }

            if (receivedContext.getAuthnContextDeclRef() != null) {
                declRef = receivedContext.getAuthnContextDeclRef().getAuthnContextDeclRef();
            }

            if (requestedAuthnContext.getAuthnContextDeclRefs() != null) {
                for (AuthnContextDeclRef declRefRequested : requestedAuthnContext.getAuthnContextDeclRefs()) {
                    if (declRefRequested.getAuthnContextDeclRef().equals(declRef)) {
                        log.debug("AuthContext matched with value {}", declRef);
                        return;
                    }
                }
            }

            throw new InsufficientAuthenticationException("Response doesn't contain any of the requested authentication context class or declaration references");

        }

    }

    /**
     * Maximum time between authentication of user and processing of an authentication statement.
     *
     * @return max authentication age, defaults to 7200 (in seconds)
     */
    public long getMaxAuthenticationAge() {
        return maxAuthenticationAge;
    }

    /**设置用户身份验证和身份验证语句处理之间的最长时间。
     * Sets maximum time between users authentication and processing of an authentication statement.
     *
     * @param maxAuthenticationAge authentication age (in seconds)
     */
    public void setMaxAuthenticationAge(long maxAuthenticationAge) {
        this.maxAuthenticationAge = maxAuthenticationAge;
    }

    /**
     * @return true to include attributes from all assertions, false to only include those from the confirmed assertion
     */
    public boolean isIncludeAllAttributes() {
        return includeAllAttributes;
    }

    /**标志指示是包括所有声明的属性（值为true），还是仅包括使用Bearer SubjectConfirmation进行身份验证的声明的属性（默认为false）。
     * Flag indicates whether to include attributes from all assertions (value true), or only from
     * the assertion which was authentication using the Bearer SubjectConfirmation (value false, by default).
     *
     * @param includeAllAttributes true to include attributes from all assertions
     */
    public void setIncludeAllAttributes(boolean includeAllAttributes) {
        this.includeAllAttributes = includeAllAttributes;
    }

    /**
     * @return release dom flag, true by default
     */
    public boolean isReleaseDOM() {
        return releaseDOM;
    }

    /**标志指示是否释放SAMLCredential中返回的断言的内部结构。 如果您想访问原始的Assertion值（包括签名），请设置为false。
     * Flag indicates whether to release internal structure of the assertion returned in SAMLCredential. Set to false
     * in case you'd like to have access to the original Assertion value (include signatures).
     *
     * @param releaseDOM release dom flag
     */
    public void setReleaseDOM(boolean releaseDOM) {
        this.releaseDOM = releaseDOM;
    }

}

