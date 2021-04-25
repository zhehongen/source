/*
 * Copyright 2009-2010 Vladimir Schaefer
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
package org.springframework.security.saml.util;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLRuntimeException;
import org.opensaml.common.binding.decoding.URIComparator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.BasicSecurityConfiguration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataManager;
import org.w3c.dom.Element;

import javax.net.ssl.HostnameVerifier;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for SAML entities
 *
 * @author Vladimir Schaefer
 */
public class SAMLUtil {

    private static final Logger log = LoggerFactory.getLogger(SAMLUtil.class);

    /** The URIComparator implementation to use. */
    private static final URIComparator DEFAULT_URI_COMPARATOR = new DefaultURLComparator();

    /**
     * Method determines binding supported by the given endpoint. Usually the biding is encoded in the binding attribute
     * of the endpoint, but in some cases more processing is needed (e.g. for HoK profile).
     *
     * @param endpoint endpoint
     * @return binding supported by the endpoint
     */
    public static String getBindingForEndpoint(Endpoint endpoint) {

        String bindingName = endpoint.getBinding();

        // For HoK profile the used binding is determined in a different way
        if (org.springframework.security.saml.SAMLConstants.SAML2_HOK_WEBSSO_PROFILE_URI.equals(bindingName)) {
            QName attributeName = org.springframework.security.saml.SAMLConstants.WEBSSO_HOK_METADATA_ATT_NAME;
            String endpointLocation = endpoint.getUnknownAttributes().get(attributeName);
            if (endpointLocation != null) {
                bindingName = endpointLocation;
            } else {
                throw new SAMLRuntimeException("Holder of Key profile endpoint doesn't contain attribute hoksso:ProtocolBinding");
            }
        }

        return bindingName;

    }

    /**
     * Returns Single logout service for given binding of the IDP.
     *
     * @param descriptor IDP to search for service in
     * @param binding    binding supported by the service
     * @return SSO service capable of handling the given binding
     * @throws MetadataProviderException if the service can't be determined
     */
    public static SingleLogoutService getLogoutServiceForBinding(SSODescriptor descriptor, String binding) throws MetadataProviderException {
        List<SingleLogoutService> services = descriptor.getSingleLogoutServices();
        for (SingleLogoutService service : services) {
            if (binding.equals(service.getBinding())) {
                return service;
            }
        }
        log.debug("No binding found for IDP with binding " + binding);
        throw new MetadataProviderException("Binding " + binding + " is not supported for this IDP");
    }

    public static String getLogoutBinding(IDPSSODescriptor idp, SPSSODescriptor sp) throws MetadataProviderException {

        List<SingleLogoutService> logoutServices = idp.getSingleLogoutServices();
        if (logoutServices.size() == 0) {
            throw new MetadataProviderException("IDP doesn't contain any SingleLogout endpoints");
        }

        String binding = null;

        // Let's find first binding supported by both IDP and SP
        idp:
        for (SingleLogoutService idpService : logoutServices) {
            for (SingleLogoutService spService : sp.getSingleLogoutServices()) {
                if (idpService.getBinding().equals(spService.getBinding())) {
                    binding = idpService.getBinding();
                    break idp;
                }
            }
        }

        // In case there's no common endpoint let's use first available
        if (binding == null) {
            binding = idp.getSingleLogoutServices().iterator().next().getBinding();
        }

        return binding;
    }

    public static IDPSSODescriptor getIDPSSODescriptor(EntityDescriptor idpEntityDescriptor) throws MessageDecodingException {

        IDPSSODescriptor idpSSODescriptor = idpEntityDescriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
        if (idpSSODescriptor == null) {
            log.error("Could not find an IDPSSODescriptor in metadata.");
            throw new MessageDecodingException("Could not find an IDPSSODescriptor in metadata.");
        }

        return idpSSODescriptor;

    }

    /**
     * Loads the assertionConsumerIndex designated by the index. In case an index is specified the consumer
     * is located and returned, otherwise default consumer is used.
     *
     * @param ssoDescriptor descriptor
     * @param index         to load, can be null
     * @return consumer service
     * @throws org.opensaml.common.SAMLRuntimeException
     *          in case assertionConsumerService with given index isn't found
     */
    public static AssertionConsumerService getConsumerService(SPSSODescriptor ssoDescriptor, Integer index) {
        if (index != null) {//看过了
            for (AssertionConsumerService service : ssoDescriptor.getAssertionConsumerServices()) {
                if (index.equals(service.getIndex())) {
                    log.debug("Found assertionConsumerService with index {} and binding {}", index, service.getBinding());
                    return service;
                }
            }
            throw new SAMLRuntimeException("AssertionConsumerService with index " + index + " wasn't found for ServiceProvider " + ssoDescriptor.getID() + ", please check your metadata");
        }
        log.debug("Index for AssertionConsumerService not specified, returning default");
        return ssoDescriptor.getDefaultAssertionConsumerService();
    }

    public static ArtifactResolutionService getArtifactResolutionService(IDPSSODescriptor idpssoDescriptor, int endpointIndex) throws MessageDecodingException {

        List<ArtifactResolutionService> artifactResolutionServices = idpssoDescriptor.getArtifactResolutionServices();
        if (artifactResolutionServices == null || artifactResolutionServices.size() == 0) {
            log.error("Could not find any artifact resolution services in metadata.");
            throw new MessageDecodingException("Could not find any artifact resolution services in metadata.");
        }

        ArtifactResolutionService artifactResolutionService = null;
        for (ArtifactResolutionService ars : artifactResolutionServices) {
            if (ars.getIndex() == endpointIndex) {
                artifactResolutionService = ars;
                break;
            }
        }

        if (artifactResolutionService == null) {
            throw new MessageDecodingException("Could not find artifact resolution service with index " + endpointIndex + " in IDP data.");
        }

        return artifactResolutionService;

    }

    /**确定是否应为当前请求调用具有给定名称的过滤器。 当requestURI包含filterName时使用过滤器。
     * Determines whether filter with the given name should be invoked for the current request. Filter is used
     * when requestURI contains the filterName.
     *
     * @param filterName name of the filter to search URI for
     * @param request    request
     * @return true if filter should be processed for this request
     */
    public static boolean processFilter(String filterName, HttpServletRequest request) {
        return (request.getRequestURI().contains(filterName));
    }

    /**
     * Helper method compares whether SHA-1 hash of the entityId equals the hashID.
     *
     * @param hashID   hash id to compare
     * @param entityId entity id to hash and verify
     * @return true if values match
     * @throws MetadataProviderException in case SHA-1 hash can't be initialized
     */
    public static boolean compare(byte[] hashID, String entityId) throws MetadataProviderException {
        //看过了
        try {

            MessageDigest sha1Digester = MessageDigest.getInstance("SHA-1");
            byte[] hashedEntityId = sha1Digester.digest(entityId.getBytes());

            for (int i = 0; i < hashedEntityId.length; i++) {
                if (hashedEntityId[i] != hashID[i]) {
                    return false;
                }
            }

            return true;

        } catch (NoSuchAlgorithmException e) {
            throw new MetadataProviderException("SHA-1 message digest not available", e);
        }

    }

    /**验证别名是否有效。 别名是非空字符串，仅包含ASCII字符。
     * Verifies that the alias is valid. Alias mus be non-empty string which only include ASCII characters.
     *
     * @param alias alias to verify
     * @param entityId id of the entity
     * @throws MetadataProviderException in case any validation problem is found
     */
    public static void verifyAlias(String alias, String entityId) throws MetadataProviderException {

        if (alias == null) {
            throw new MetadataProviderException("Alias for entity " + entityId + " is null");
        } else if (alias.length() == 0) {
            throw new MetadataProviderException("Alias for entity " + entityId + " is empty");
        } else if (!alias.matches("\\p{ASCII}*")) {
            throw new MetadataProviderException("Only ASCII characters can be used in the alias " + alias + " for entity " + entityId);
        }

    }

    /**
     * Parses list of all Base64 encoded certificates found inside the KeyInfo element. All present X509Data
     * elements are processed.
     *
     * @param keyInfo key info to parse
     * @return found base64 encoded certificates
     */
    public static List<String> getBase64EncodeCertificates(KeyInfo keyInfo) {

        List<String> certList = new LinkedList<String>();

        if (keyInfo == null) {
            return certList;
        }

        List<X509Data> x509Datas = keyInfo.getX509Datas();
        for (X509Data x509Data : x509Datas) {
            if (x509Data != null) {
                certList.addAll(getBase64EncodedCertificates(x509Data));
            }
        }

        return certList;

    }

    /**
     * Parses list of Base64 encoded certificates present in the X509Data element.
     *
     * @param x509Data data to parse
     * @return list with 0..n certificates
     */
    public static List<String> getBase64EncodedCertificates(X509Data x509Data) {

        List<String> certList = new LinkedList<String>();

        if (x509Data == null) {
            return certList;
        }

        for (org.opensaml.xml.signature.X509Certificate xmlCert : x509Data.getX509Certificates()) {
            if (xmlCert != null && xmlCert.getValue() != null) {
                certList.add(xmlCert.getValue());
            }
        }

        return certList;

    }

    /**分析请求头以确定它是否来自启用ECP的客户机，并基于此决定是否使用ECP概要文件。子类可以重写方法来控制何时调用ECP。
     * Analyzes the request headers in order to determine if it comes from an ECP-enabled
     * client and based on this decides whether ECP profile will be used. Subclasses can override
     * the method to control when is the ECP invoked.
     *
     * @param request request to analyze
     * @return whether the request comes from an ECP-enabled client or not
     */
    public static boolean isECPRequest(HttpServletRequest request) {

        String acceptHeader = request.getHeader("Accept");
        String paosHeader = request.getHeader(org.springframework.security.saml.SAMLConstants.PAOS_HTTP_HEADER);//说明："PAOS"
        return acceptHeader != null && paosHeader != null
                && acceptHeader.contains(org.springframework.security.saml.SAMLConstants.PAOS_HTTP_ACCEPT_HEADER)//说明："application/vnd.paos+xml"
                && paosHeader.contains(org.opensaml.common.xml.SAMLConstants.PAOS_NS)//说明："urn:liberty:paos:2003-08"
                && paosHeader.contains(org.opensaml.common.xml.SAMLConstants.SAML20ECP_NS);//说明：urn:oasis:names:tc:SAML:2.0:profiles:SSO:ecp

    }

    /**
     * Method helps to identify which endpoint is used to process the current message. It expects a list of potential
     * endpoints based on the current profile and selects the one which uses the specified binding and matches
     * the URL of incoming message.
     *
     * @param endpoints      endpoints to check
     * @param messageBinding binding
     * @param inTransport      transport which received the current message
     * @param <T>            type of the endpoint
     * @return first endpoint satisfying the requestURL and binding conditions
     * @throws SAMLException in case endpoint can't be found
     */
    public static <T extends Endpoint> T getEndpoint(List<T> endpoints, String messageBinding, InTransport inTransport) throws SAMLException {
        return getEndpoint(endpoints, messageBinding, inTransport, DEFAULT_URI_COMPARATOR);
    }

    /**
     * Method helps to identify which endpoint is used to process the current message. It expects a list of potential
     * endpoints based on the current profile and selects the one which uses the specified binding and matches
     * the URL of incoming message.
     *
     * @param endpoints      endpoints to check
     * @param messageBinding binding
     * @param inTransport      transport which received the current message
     * @param uriComparator     URI comparator
     * @param <T>            type of the endpoint
     * @return first endpoint satisfying the requestURL and binding conditions
     * @throws SAMLException in case endpoint can't be found
     */
    public static <T extends Endpoint> T getEndpoint(List<T> endpoints, String messageBinding, InTransport inTransport, URIComparator uriComparator) throws SAMLException {
        HttpServletRequest httpRequest = ((HttpServletRequestAdapter)inTransport).getWrappedRequest();
        String requestURL = DatatypeHelper.safeTrimOrNullString(httpRequest.getRequestURL().toString());
        String queryString = DatatypeHelper.safeTrimOrNullString(httpRequest.getQueryString());
        if (queryString != null){
            requestURL += '?' + queryString;
        }
        for (T endpoint : endpoints) {
            String binding = getBindingForEndpoint(endpoint);
            // Check that destination and binding matches
            if (binding.equals(messageBinding)) {
                if (endpoint.getLocation() != null && uriComparator.compare(endpoint.getLocation(), requestURL)) {
                    log.debug("Found endpoint {} for request URL {} based on location attribute in metadata", endpoint, requestURL);
                    return endpoint;
                } else if (endpoint.getResponseLocation() != null && uriComparator.compare(endpoint.getResponseLocation(), requestURL)) {
                    log.debug("Found endpoint {} for request URL {} based on response location attribute in metadata", endpoint, requestURL);
                    return endpoint;
                }
            }
        }
        throw new SAMLException("Endpoint with message binding " + messageBinding + " and URL " + requestURL + " wasn't found in local metadata");
    }

    /**
     * Loads IDP descriptor for entity with the given entityID. Fails when it can't be found.
     * @param metadata metadata manager
     * @param idpId entity ID
     * @return descriptor
     * @throws MetadataProviderException in case descriptor can't be found
     */
    public static IDPSSODescriptor getIDPDescriptor(MetadataManager metadata, String idpId) throws MetadataProviderException {
        if (!metadata.isIDPValid(idpId)) {
            log.debug("IDP name of the authenticated user is not valid", idpId);
            throw new MetadataProviderException("IDP with name " + idpId + " wasn't found in the list of configured IDPs");
        }
        IDPSSODescriptor idpssoDescriptor = (IDPSSODescriptor) metadata.getRole(idpId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS);
        if (idpssoDescriptor == null) {
            throw new MetadataProviderException("Given IDP " + idpId + " doesn't contain any IDPSSODescriptor element");
        }
        return idpssoDescriptor;
    }

    /**
     * Helper method that marshals the given message.
     *
     * @param message message the marshall and serialize
     * @return marshaled message
     * @throws org.opensaml.ws.message.encoder.MessageEncodingException
     *          thrown if the give message can not be marshaled into its DOM representation
     */
    public static Element marshallMessage(XMLObject message) throws MessageEncodingException {//看过了
        try {
            if (message.getDOM() != null) {
                log.debug("XMLObject already had cached DOM, returning that element");
                return message.getDOM();
            }
            Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(message);
            if (marshaller == null) {
                throw new MessageEncodingException("Unable to marshall message, no marshaller registered for message object: "
                                                   + message.getElementQName());
            }
            Element messageElem = marshaller.marshall(message);
            if (log.isTraceEnabled()) {
                log.trace("Marshalled message into DOM:\n{}", XMLHelper.nodeToString(messageElem));
            }
            return messageElem;
        } catch (MarshallingException e) {
            log.error("Encountered error marshalling message to its DOM representation", e);
            throw new MessageEncodingException("Encountered error marshalling message into its DOM representation", e);
        }
    }

    /**如果该对象是可签名的，并且提供了签名凭据，则该方法会对对象进行数字签名和封送。如果对象已经签名或未提供签名凭据，则仅整理消息。
     * Method digitally signs and marshals the object in case it is signable and the signing credential is provided.
     *
     * In case the object is already signed or the signing credential is not provided message is just marshalled.
     *
     * @param signableMessage    object to sign
     * @param signingCredential credential to sign with
     * @param signingAlgorithm  signing algorithm to use (optional). Leave null to use credential's default algorithm要使用的签名算法（可选）。保留为空以使用凭据的默认算法
     * @param signingAlgorithm  digest method algorithm to use (optional). Leave null to use credential's default algorithm
     * @param keyInfoGenerator name of generator used to create KeyInfo elements with key data用于使用密钥数据创建KeyInfo元素的生成器名称
     * @throws org.opensaml.ws.message.encoder.MessageEncodingException
     *          thrown if there is a problem marshalling or signing the message
     * @return marshalled and signed message
     */
    @SuppressWarnings("unchecked")
    public static Element marshallAndSignMessage(SignableXMLObject signableMessage, Credential signingCredential, String signingAlgorithm, String digestMethodAlgorithm, String keyInfoGenerator) throws MessageEncodingException {

        if (signingCredential != null && !signableMessage.isSigned()) {

            XMLObjectBuilder<Signature> signatureBuilder = org.opensaml.Configuration.getBuilderFactory().getBuilder(
                    Signature.DEFAULT_ELEMENT_NAME);//说明：ds:Signature
            Signature signature = signatureBuilder.buildObject(Signature.DEFAULT_ELEMENT_NAME);

            if (signingAlgorithm != null) {
                signature.setSignatureAlgorithm(signingAlgorithm);//签名算法
            }

            signature.setSigningCredential(signingCredential);//说明：签名凭据

            BasicSecurityConfiguration secConfig = null;

            if (digestMethodAlgorithm != null)
            {
                secConfig = (BasicSecurityConfiguration) Configuration.getGlobalSecurityConfiguration();

                secConfig.setSignatureReferenceDigestMethod(digestMethodAlgorithm);//说明：摘要的时候是不需要密钥的
            }

            try {
                SecurityHelper.prepareSignatureParams(signature, signingCredential, secConfig, keyInfoGenerator);
            } catch (org.opensaml.xml.security.SecurityException e) {
                throw new MessageEncodingException("Error preparing signature for signing", e);
            }

            signableMessage.setSignature(signature);//说明：就这？签名就加进去了？
            Element element = marshallMessage(signableMessage);

            try {
                Signer.signObject(signature);//奇葩，上面都获取到element了。下面才签名？
            } catch (SignatureException e) {
                log.error("Unable to sign protocol message", e);
                throw new MessageEncodingException("Unable to sign protocol message", e);
            }

            return element;

        } else {

            return marshallMessage(signableMessage);

        }

    }

    /**
     * Verifies that the current time is within skewInSec interval from the time value.
     *
     * @param skewInSec skew interval in seconds
     * @param time time the current time must fit into with the given skew
     * @return true if time matches, false otherwise
     */
    public static boolean isDateTimeSkewValid(int skewInSec, DateTime time) {
        return isDateTimeSkewValid(skewInSec, 0, time);
    }

    /**
     * Verifies that the current time fits into interval defined by time minus backwardInterval minus skew and time plus forward interval plus skew.
     *
     *
     * @param skewInSec skew interval in seconds
     * @param forwardInterval forward interval in sec
     * @param time time the current time must fit into with the given skew
     * @return true if time matches, false otherwise
     */
    public static boolean isDateTimeSkewValid(int skewInSec, long forwardInterval, DateTime time) {
        final DateTime reference = new DateTime();
        final Interval validTimeInterval = new Interval(
                reference.minusSeconds(skewInSec + (int)forwardInterval),
                reference.plusSeconds(skewInSec)
        );
        return validTimeInterval.contains(time);
    }

    /**方法用下划线替换xsd：NCName类型中不允许的所有字符。 通过用下划线替换它，还可以确保该值不以连字符开头。
     * Method replaces all characters which are not allowed in xsd:NCName type with underscores. It also makes sure
     * that value doesn't start with a hyphen by replacing it with underscore.
     *
     * @param value value to clean
     * @return null for null input, otherwise cleaned value
     */
    public static String getNCNameString(String value) {
        if (value == null) {
            return null;
        }
        String cleanValue = value.replaceAll("[^a-zA-Z0-9-_.]", "_");
        if (cleanValue.startsWith("-")) {
            cleanValue = "_" + cleanValue.substring(1);
        }
        return cleanValue;
    }

    /**
     * Populates hostname verifier of the given type. Supported values are default, defaultAndLocalhost,
     * strict and allowAll. Unsupported values will return default verifier.
     *
     * @param hostnameVerificationType type
     * @return verifier
     */
    public static HostnameVerifier getHostnameVerifier(String hostnameVerificationType) {

        HostnameVerifier hostnameVerifier;//看过了
        if ("default".equalsIgnoreCase(hostnameVerificationType)) {
            hostnameVerifier = org.apache.commons.ssl.HostnameVerifier.DEFAULT;
        } else if ("defaultAndLocalhost".equalsIgnoreCase(hostnameVerificationType)) {
            hostnameVerifier = org.apache.commons.ssl.HostnameVerifier.DEFAULT_AND_LOCALHOST;
        } else if ("strict".equalsIgnoreCase(hostnameVerificationType)) {
            hostnameVerifier = org.apache.commons.ssl.HostnameVerifier.STRICT;
        } else if ("allowAll".equalsIgnoreCase(hostnameVerificationType)) {
            hostnameVerifier = org.apache.commons.ssl.HostnameVerifier.ALLOW_ALL;
        } else {
            hostnameVerifier = org.apache.commons.ssl.HostnameVerifier.DEFAULT;
        }

        return hostnameVerifier;

    }

    /**方法对EntityDescriptor元素进行数字签名（在配置了属性符号元数据时），并将结果序列化为字符串。
     * Method digitally signs the EntityDescriptor element (when configured with property sign metadata) and
     * serializes the result into a string.
     *
     * @param metadataManager metadata manager
     * @param keyManager key manager
     * @param descriptor descriptor to sign and serialize
     * @param extendedMetadata information about metadata signing, looked up when null
     * @return serialized and signed metadata
     * @throws MarshallingException in case serialization fails
     */
    public static String getMetadataAsString(MetadataManager metadataManager, KeyManager keyManager, EntityDescriptor descriptor, ExtendedMetadata extendedMetadata) throws MarshallingException {

        Element element;

        if (extendedMetadata == null) {
            try {
                extendedMetadata = metadataManager.getExtendedMetadata(descriptor.getEntityID());//说明：这个孙子获取到的都一样吧
            } catch (MetadataProviderException e) {
                log.error("Unable to locate extended metadata", e);
                throw new MarshallingException("Unable to locate extended metadata", e);
            }
        }

        try {
            if (extendedMetadata.isLocal() && extendedMetadata.isSignMetadata()) {
                Credential credential = keyManager.getCredential(extendedMetadata.getSigningKey());
                String signingAlgorithm = extendedMetadata.getSigningAlgorithm();
                String keyGenerator = extendedMetadata.getKeyInfoGeneratorName();
                String digestMethodAlgorithm = extendedMetadata.getDigestMethodAlgorithm();
                element = SAMLUtil.marshallAndSignMessage(descriptor, credential, signingAlgorithm, digestMethodAlgorithm, keyGenerator);
            } else {
                element = SAMLUtil.marshallMessage(descriptor);
            }
        } catch (MessageEncodingException e) {
            log.error("Unable to marshall message", e);
            throw new MarshallingException("Unable to marshall message", e);
        }

        return XMLHelper.nodeToString(element);

    }

}
