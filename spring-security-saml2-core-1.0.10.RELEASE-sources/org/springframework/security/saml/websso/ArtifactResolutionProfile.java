/*
 * Copyright 2010 Vladimir Schaefer
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

import org.opensaml.common.SAMLObject;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.springframework.security.saml.context.SAMLMessageContext;

/**实现必须使用SAML工件解析协议加载引用的工件。
 * Implementations must load referenced artifact using SAML artifact resolution protocol.
 */
public interface ArtifactResolutionProfile {

    /**实现必须使用给定的ID解析工件，找到可用于其解析的端点并加载引用的SAML消息。
     * Implementation must resolve artifact with the given ID, locate endpoint usable for it resolution
     * and load referenced SAML message.
     *具有预填充本地实体的saml上下文
     * @param context saml context with pre-populated local entity
     * @param artifactId artifact to resolve
     * @param endpointURI URI of the endpoint the message was sent to
     * @return message the artifact references
     * @throws MessageDecodingException in case message loading fails
     */
    SAMLObject resolveArtifact(SAMLMessageContext context, String artifactId, String endpointURI) throws MessageDecodingException;

}
