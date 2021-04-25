/* Copyright 2009 Vladimir Schafer
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
package org.springframework.security.saml.parser;

import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.security.saml.util.SAMLUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;

/**
 * SAMLObject is a wrapper around XMLObject instances of OpenSAML library As some XMLObjects are stored
 * inside the HttpSession (which could be potentially sent to another cluster members), we need
 * mechanism to enable serialization of these instances.
 * SAMLObject是OpenSAML库的XMLObject实例的包装器。由于一些XMLObjects存储在HttpSession内部（可能会发送给其他集群成员），因此我们需要一种机制来启用这些实例的序列化。
 * @param <T> type of XMLObject
 * @author Vladimir Schafer
 */
public class SAMLObject<T extends XMLObject> extends SAMLBase<T, T> {

    /**
     * Default constructor.
     *
     * @param object object to wrap with serialization logic
     */
    public SAMLObject(T object) {
        super(object);
    }

    @Override
    public T getObject() {
        if (object == null) { // Lazy parse
            parse();
        }
        return super.getObject();
    }

    /**
     * Custom serialization logic which transform XMLObject into String.
     * 俩吊毛
     * @param out output stream
     * @throws java.io.IOException error performing XMLObject serialization
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        try {
            if (serializedObject == null) {
                serializedObject = XMLHelper.nodeToString(SAMLUtil.marshallMessage(getObject()));
            }
            out.writeObject(serializedObject);
        } catch (MessageEncodingException e) {
            log.error("Error serializing SAML object", e);
            throw new IOException("Error serializing SAML object: " + e.getMessage());
        }
    }

    /**从流中反序列化XMLObject。 内容的解析是在访问对象时延迟进行的。 其原因是这样的事实，即解析器池可能在系统启动期间未初始化，并且该对象可能存储在序列化会话中。
     * Deserializes XMLObject from the stream. Parsing of the content is done lazily upon access
     * to the object. The reason for this is the fact that parser pool may not be initialized during system startup
     * and the object may be stored in a serialized session.
     *
     * @param in input stream contaiing XMLObject as String 将XMLObject包含为String的输入流
     * @throws IOException            error deserializing String to XMLObject
     * @throws ClassNotFoundException class not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        serializedObject = (String)in.readObject();
    }

    /**
     * Lazily parsers serialized data.
     */
    private void parse() {
        try {
            if (serializedObject != null) {
                object = unmarshallMessage(new StringReader((String) serializedObject));
            }
        } catch (MessageDecodingException e) {
            log.error("Error de-serializing SAML object", e);
            throw new RuntimeException("Error de-serializing SAML object: " + e.getMessage());
        }
    }

}
