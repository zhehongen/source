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
package org.springframework.security.saml.storage;

import org.opensaml.xml.XMLObject;

/**实现充当发送/接收的SAML消息的数据存储。 可能的实现方式可能是使用例如应用程序或HttpSession中所有用户共有的中央存储库。例如，可能需要存储消息，以将响应与原始请求配对。
 * Implementations serve as data stores for sent/received SAML messages. Potential implementations could
 * be using for example central repository common for all users within the application or HttpSession.
 * <p>
 * Messages may need to be stored for example to pair a response with an original request.
 *
 * @author Vladimir Schäfer
 */
public interface SAMLMessageStorage {

    /**将给定的消息存储在数据存储中。 请求必须填写ID。
     * Stores given message in the data store. Request must have the ID filled.
     *
     * @param messageId key under which will the message be stored将在其下存储消息的键
     * @param message   message to store要存储的消息
     */
    void storeMessage(String messageId, XMLObject message);

    /**检索以给定ID存储的消息。
     * Retrieves message stored under given ID.
     *
     * @param messageID message ID to look up
     * @return request or null if not found
     */
    XMLObject retrieveMessage(String messageID);

}
