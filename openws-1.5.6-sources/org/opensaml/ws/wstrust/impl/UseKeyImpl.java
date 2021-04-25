/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.ws.wstrust.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.ws.wstrust.UseKey;
import org.opensaml.xml.XMLObject;

/**
 * UseKeyImpl.
 * 
 */
public class UseKeyImpl extends AbstractWSTrustObject implements UseKey {
    
    /** Wildcard child element. */
    private XMLObject unknownChild;

    /** wst:UseKey/@Sig attribute value. */
    private String sig;

    /**
     * Constructor.
     * 
     * @param namespaceURI The namespace of the element
     * @param elementLocalName The local name of the element
     * @param namespacePrefix The namespace prefix of the element
     */
    public UseKeyImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getSig() {
        return sig;
    }

    /** {@inheritDoc} */
    public void setSig(String newSig) {
        sig = prepareForAssignment(sig, newSig);
    }
    
    /** {@inheritDoc} */
    public XMLObject getUnknownXMLObject() {
        return unknownChild;
    }

    /** {@inheritDoc} */
    public void setUnknownXMLObject(XMLObject unknownObject) {
        unknownChild = prepareForAssignment(unknownChild, unknownObject);
    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        if (unknownChild != null) {
            children.add(unknownChild);
        }
        return Collections.unmodifiableList(children);
    }

}
