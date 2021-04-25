/*
 * Copyright 2009 Vladimir Schaefer
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

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.decoding.BasicURLComparator;
import org.opensaml.common.binding.decoding.URIComparator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.processor.SAMLProcessor;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.util.Assert;

import java.util.Random;

/**实现SAML消息处理的类的基类。
 * Base superclass for classes implementing processing of SAML messages.
 *
 * @author Vladimir Schaefer
 */
public abstract class AbstractProfileBase implements InitializingBean {

    /**从响应创建到认为消息有效的最长时间。
     * Maximum time from response creation when the message is deemed valid.
     */
    private int responseSkew = 60;

    /**从响应创建到认为消息有效的最长时间。
     * Maximum time between assertion creation and current time when the assertion is usable
     */
    private int maxAssertionTime = 3000;

    /**
     * Class logger.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected MetadataManager metadata;
    protected SAMLProcessor processor;
    protected SAMLArtifactMap artifactMap;
    protected XMLObjectBuilderFactory builderFactory;
    protected URIComparator uriComparator;

    public AbstractProfileBase() {
        this.builderFactory = Configuration.getBuilderFactory();
        this.uriComparator = new BasicURLComparator();
    }

    public AbstractProfileBase(SAMLProcessor processor, MetadataManager manager) {
        this();
        this.processor = processor;
        this.metadata = manager;
    }

    /**期望实现为此类实现的配置文件提供唯一的标识符。
     * Implementation are expected to provide an unique identifier for the profile this class implements.
     *
     * @return profile name
     */
    public abstract String getProfileIdentifier();

    /**设置本地时间与断言创建时间之间的最大时差，该时差仍允许消息被处理。 基本上确定IDP和SP机器的时钟之间的最大差异。 默认为60。
     * Sets maximum difference between local time and time of the assertion creation which still allows
     * message to be processed. Basically determines maximum difference between clocks of the IDP and SP machines.
     * Defaults to 60.
     *响应偏斜时间（以秒为单位）
     * @param responseSkew response skew time (in seconds)
     */
    public void setResponseSkew(int responseSkew) {
        this.responseSkew = responseSkew;
    }

    /**
     * @return response skew time (in seconds)
     */
    public int getResponseSkew() {
        return responseSkew;
    }

    /**
     * Maximum time between assertion creation and current time when the assertion is usable in seconds.
     *
     * @return max assertion time
     */
    public int getMaxAssertionTime() {
        return maxAssertionTime;
    }

    /**
     * Customizes max assertion time between assertion creation and it's usability. Default to 3000 seconds.
     *
     * @param maxAssertionTime time in seconds
     */
    public void setMaxAssertionTime(int maxAssertionTime) {
        this.maxAssertionTime = maxAssertionTime;
    }

    /**方法调用处理器并发送上下文中包含的消息。 子类可以在消息传递之前提供其他处理。 使用上下文的对等实体中定义的绑定来发送消息。
     * Method calls the processor and sends the message contained in the context. Subclasses can provide additional
     * processing before the message delivery. Message is sent using binding defined in the peer entity of the context.
     *
     * @param context context
     * @param sign    whether the message should be signed
     * @throws MetadataProviderException metadata error
     * @throws SAMLException             SAML encoding error
     * @throws org.opensaml.ws.message.encoder.MessageEncodingException
     *                                   message encoding error
     */
    protected void sendMessage(SAMLMessageContext context, boolean sign) throws MetadataProviderException, SAMLException, MessageEncodingException {
        processor.sendMessage(context, sign);
    }

    /**方法调用处理器并发送上下文中包含的消息。 子类可以在消息传递之前提供其他处理。 使用指定的绑定发送消息。
     * Method calls the processor and sends the message contained in the context. Subclasses can provide additional
     * processing before the message delivery. Message is sent using the specified binding.
     *
     * @param context context
     * @param sign    whether the message should be signed
     * @param binding binding to use to send the message
     * @throws MetadataProviderException metadata error
     * @throws SAMLException             SAML encoding error
     * @throws org.opensaml.ws.message.encoder.MessageEncodingException
     *                                   message encoding error
     */
    protected void sendMessage(SAMLMessageContext context, boolean sign, String binding) throws MetadataProviderException, SAMLException, MessageEncodingException {
        processor.sendMessage(context, sign, binding);
    }

    protected Status getStatus(String code, String statusMessage) {
        SAMLObjectBuilder<StatusCode> codeBuilder = (SAMLObjectBuilder<StatusCode>) builderFactory.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = codeBuilder.buildObject();
        statusCode.setValue(code);

        SAMLObjectBuilder<Status> statusBuilder = (SAMLObjectBuilder<Status>) builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME);
        Status status = statusBuilder.buildObject();
        status.setStatusCode(statusCode);

        if (statusMessage != null) {
            SAMLObjectBuilder<StatusMessage> messageBuilder = (SAMLObjectBuilder<StatusMessage>) builderFactory.getBuilder(StatusMessage.DEFAULT_ELEMENT_NAME);
            StatusMessage statusMessageObject = messageBuilder.buildObject();
            statusMessageObject.setMessage(statusMessage);
            status.setStatusMessage(statusMessageObject);
        }

        return status;
    }

    /**用版本，发布即时消息和目标数据填充请求。
     * Fills the request with version, issue instants and destination data.
     *
     * @param localEntityId entityId of the local party acting as message issuer
     * @param request       request to be filled
     * @param service       service to use as destination for the request
     */
    protected void buildCommonAttributes(String localEntityId, RequestAbstractType request, Endpoint service) {

        request.setID(generateID());
        request.setIssuer(getIssuer(localEntityId));
        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(new DateTime());

        if (service != null) {
            // Service is now known when we do not know which IDP will be used现在，当我们不知道将使用哪个IDP时就可以知道该服务
            request.setDestination(service.getLocation());
        }

    }

    protected Issuer getIssuer(String localEntityId) {
        SAMLObjectBuilder<Issuer> issuerBuilder = (SAMLObjectBuilder<Issuer>) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(localEntityId);
        return issuer;
    }

    /**
     * Generates random ID to be used as Request/Response ID.
     *
     * @return random ID
     */
    protected String generateID() {
        Random r = new Random();
        return 'a' + Long.toString(Math.abs(r.nextLong()), 20) + Long.toString(Math.abs(r.nextLong()), 20);
    }

    protected void verifyIssuer(Issuer issuer, SAMLMessageContext context) throws SAMLException {
        // Validate format of issuer
        if (issuer.getFormat() != null && !issuer.getFormat().equals(NameIDType.ENTITY)) {
            throw new SAMLException("Issuer invalidated by issuer type " + issuer.getFormat());
        }
        // Validate that issuer is expected peer entity
        if (!context.getPeerEntityMetadata().getEntityID().equals(issuer.getValue())) {
            throw new SAMLException("Issuer invalidated by issuer value " + issuer.getValue());
        }
    }

    /**验证消息中预期的目标URL是否与端点地址匹配。 最终收到的URL消息不必与元数据中定义的URL消息相匹配（例如，消息的反向代理）。
     * Verifies that the destination URL intended in the message matches with the endpoint address. The URL message
     * was ultimately received doesn't need to necessarily match the one defined in the metadata (in case of e.g. reverse-proxying
     * of messages).
     *验证本地收到的
     * @param endpoint endpoint the message was received at在以下位置接收到消息的端点
     * @param destination URL of the endpoint the message was intended to be sent to by the peer or null when not included
     * @throws SAMLException in case endpoint doesn't match目标消息的端点的URL，该消息旨在由对等方发送到；如果不包括，则为null
     */
    protected void verifyEndpoint(Endpoint endpoint, String destination) throws SAMLException {
        // Verify that destination in the response matches one of the available endpoints
        if (destination != null) {
            if (uriComparator.compare(destination, endpoint.getLocation())) {
                // Expected
            } else if (uriComparator.compare(destination, endpoint.getResponseLocation())) {//什么玩意
                // Expected
            } else {
                throw new SAMLException("Intended destination " + destination + " doesn't match any of the endpoint URLs on endpoint " + endpoint.getLocation() + " for profile " + getProfileIdentifier());
            }
        }
    }

    protected void verifySignature(Signature signature, String IDPEntityID, SignatureTrustEngine trustEngine) throws org.opensaml.xml.security.SecurityException, ValidationException {

        if (trustEngine == null) {
            throw new SecurityException("Trust engine is not set, signature can't be verified");
        }

        SAMLSignatureProfileValidator validator = new SAMLSignatureProfileValidator();
        validator.validate(signature);
        CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIDCriteria(IDPEntityID));
        criteriaSet.add(new MetadataCriteria(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS));
        criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
        log.debug("Verifying signature", signature);

        if (!trustEngine.validate(signature, criteriaSet)) {
            throw new ValidationException("Signature is not trusted or invalid");
        }

    }

    /**期望方法返回用于将消息传输到此端点的绑定。 对于某些配置文件，元数据中的绑定属性包含配置文件名称，在这些情况下，方法可以正确解析实际绑定。
     * Method is expected to return binding used to transfer messages to this endpoint. For some profiles the
     * binding attribute in the metadata contains the profile name, method correctly parses the real binding
     * in these situations.
     *
     * @param endpoint endpoint
     * @return binding
     */
    protected String getEndpointBinding(Endpoint endpoint) {
        return SAMLUtil.getBindingForEndpoint(endpoint);
    }

    /**确定给定的端点是否可以与指定的绑定一起使用。
     * Determines whether given endpoint can be used together with the specified binding.
     * <p>
     * By default value of the binding in the endpoint is compared for equality with the user provided binding.
     * <p>
     * Method is automatically called for verification of user supplied binding value in the WebSSOProfileOptions.
     *默认情况下，将端点中绑定的值与用户提供的绑定进行相等性比较。在WebSSOProfileOptions中，将自动调用方法以验证用户提供的绑定值。
     * @param endpoint endpoint to check
     * @param binding  binding the endpoint must support for the method to return true
     * @return true if given endpoint can be used with the binding
     */
    protected boolean isEndpointMatching(Endpoint endpoint, String binding) {
        return binding.equals(getEndpointBinding(endpoint));
    }

    @Autowired
    public void setMetadata(MetadataManager metadata) {
        this.metadata = metadata;
    }

    @Autowired(required = false)
    public void setProcessor(SAMLProcessor processor) {
        this.processor = processor;
    }

    // TODO autowire when ready
    public void setArtifactMap(SAMLArtifactMap artifactMap) {
        this.artifactMap = artifactMap;
    }

    public void afterPropertiesSet() throws Exception {
        // TODO verify artifact map when ready
        Assert.notNull(metadata, "Metadata must be set");
        Assert.notNull(processor, "SAML Processor must be set");
    }

}
