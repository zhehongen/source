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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.parser.SAMLObject;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

/**该类实现SAML消息的存储，并使用HttpSession作为基础dataStore。 由于XMLObjects无法序列化（这可能导致故障转移期间出现问题），因此消息被转换为SAMLObject，后者在序列化期间内部将内容编组为XML。 消息被填充到Hashtable中，并存储在HttpSession中。 在第一次尝试创建或检索消息期间，哈希表被延迟了初始化。
 * Class implements storage of SAML messages and uses HttpSession as underlying dataStore. As the XMLObjects
 * can't be serialized (which could lead to problems during failover), the messages are transformed into SAMLObject
 * which internally marshalls the content into XML during serialization.
 *
 * Messages are populated to a Hashtable and stored inside HttpSession. The Hashtable is lazily initialized
 * during first attempt to create or retrieve a message.
 *
 * @author Vladimir Schäfer
 */
public class HttpSessionStorage implements SAMLMessageStorage {

    /**
     * Class logger.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Session the storage operates on.
     */
    private final HttpSession session;

    /**
     * Internal storage for messages, corresponding to the object in session.
     */
    private Hashtable<String, SAMLObject<XMLObject>> internalMessages;

    /**
     * Session key for storage of the hashtable.
     */
    private static final String SAML_STORAGE_KEY = "_springSamlStorageKey";

    /**创建存储对象。 仅当呼叫者尝试存储或检索消息时，才对会话进行操作。 如果请求还没有开始的会话，则将创建它。
     * Creates the storage object. The session is manipulated only once caller tries to store
     * or retrieve a message.
     *
     * In case request doesn't already have a started session, it will be created.
     *
     * @param request request to load/store internalMessages from请求从中加载/存储internalMessages
     */
    public HttpSessionStorage(HttpServletRequest request) {
        Assert.notNull(request, "Request must be set");
        this.session = request.getSession(true);
    }

    /**创建存储对象。 仅当呼叫者尝试存储或检索消息时，才对会话进行操作。
     * Creates the storage object. The session is manipulated only once caller tries to store
     * or retrieve a message.
     *
     * @param session session to load/store internalMessages from
     */
    public HttpSessionStorage(HttpSession session) {
        Assert.notNull(session, "Session must be set");
        this.session = session;
    }

    /**将请求消息存储到存储库中。 RequestAbstractType必须设置一个ID。 具有相同ID的所有先前消息将被覆盖。
     * Stores a request message into the repository. RequestAbstractType must have an ID
     * set. Any previous message with the same ID will be overwritten.
     *
     * @param messageID ID of message
     * @param message   message to be stored
     */
    public void storeMessage(String messageID, XMLObject message) {
        log.debug("Storing message {} to session {}", messageID, session.getId());
        Hashtable<String, SAMLObject<XMLObject>> messages = getMessages();
        messages.put(messageID, new SAMLObject<XMLObject>(message));
        updateSession(messages);
    }

    /**如果未存储任何消息，则返回先前存储的具有给定ID或null的消息。消息以String格式存储，并且必须解组为XMLObject。 因此，调用此方法可能很昂贵。成功接收消息后会自动清除消息，因为我们假定同一会话永远不会进行多个SAML交换。 这样可以节省会话使用的内存。
     * Returns previously stored message with the given ID or null, if there is no message
     * stored.
     * <p>
     * Message is stored in String format and must be unmarshalled into XMLObject. Call to this
     * method may thus be expensive.
     * <p>
     * Messages are automatically cleared upon successful reception, as we presume that there
     * are never multiple ongoing SAML exchanges for the same session. This saves memory used by
     * the session.
     *
     * @param messageID ID of message to retrieve
     * @return message found or null
     */
    public XMLObject retrieveMessage(String messageID) {
        Hashtable<String, SAMLObject<XMLObject>> messages = getMessages();
        SAMLObject o = messages.get(messageID);
        if (o == null) {
            log.debug("Message {} not found in session {}", messageID, session.getId());
            return null;
        } else {
            log.debug("Message {} found in session {}, clearing", messageID, session.getId());
            messages.clear();
            updateSession(messages);
            return o.getObject();
        }
    }

    /**
     * @return all internalMessages currently stored in the storage
     */
    public Set<String> getAllMessages() {
        Hashtable<String, SAMLObject<XMLObject>> messages = getMessages();
        return Collections.unmodifiableSet(messages.keySet());
    }

    /**
     * Provides message storage hashtable. Table is lazily initialized when user tries to store or retrieve
     * the first message.
     *
     * @return message storage
     */
    private Hashtable<String, SAMLObject<XMLObject>> getMessages() {
        if (internalMessages == null) {
            internalMessages = initializeSession();
        }
        return internalMessages;
    }

    /**对该方法的调用尝试从会话中加载internalMessages哈希表对象，如果该对象不存在，则将创建并存储该对象。方法在会话互斥量上进行同步，以防止两个线程互相覆盖彼此的哈希表。
     * Call to the method tries to load internalMessages hashtable object from the session, if the object doesn't exist
     * it will be created and stored.
     * <p>
     * Method synchronizes on session mutex to prevent two threads from overwriting each others hashtable.
     */
    @SuppressWarnings("unchecked")
    private Hashtable<String, SAMLObject<XMLObject>> initializeSession() {
        Hashtable<String, SAMLObject<XMLObject>> messages = (Hashtable<String, SAMLObject<XMLObject>>) session.getAttribute(SAML_STORAGE_KEY);
        if (messages == null) {
            final Object mutex = WebUtils.getSessionMutex(session);
            synchronized (mutex) {
                messages = (Hashtable<String, SAMLObject<XMLObject>>) session.getAttribute(SAML_STORAGE_KEY);
                if (messages == null) {
                    messages = new Hashtable<String, SAMLObject<XMLObject>>();
                    updateSession(messages);
                }
            }
        }
        return messages;
    }

    /**使用internalMessages键更新会话。 某些应用程序服务器需要更新会话值，以便跨节点复制会话或正确保留会话。
     * Updates session with the internalMessages key. Some application servers require session value to be updated
     * in order to replicate the session across nodes or persist it correctly.
     */
    private void updateSession(Hashtable<String, SAMLObject<XMLObject>> messages) {
        session.setAttribute(SAML_STORAGE_KEY, messages);
    }

}
