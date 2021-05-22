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

package org.opensaml.core.xml.schema.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSInteger;
import org.w3c.dom.Attr;

/**
 * Thread-safe unmarshaller for {@link org.opensaml.core.xml.schema.XSInteger} objects.
 */
public class XSIntegerUnmarshaller extends AbstractXMLObjectUnmarshaller {

    /** {@inheritDoc} */
    protected void processChildElement(final XMLObject parentXMLObject, final XMLObject childXMLObject)
            throws UnmarshallingException {
        // no children
    }

    /** {@inheritDoc} */
    protected void processAttribute(final XMLObject xmlObject, final Attr attribute) throws UnmarshallingException {
        // no attributes
    }

    /** {@inheritDoc} */
    protected void processElementContent(final XMLObject xmlObject, final String elementContent) {
        final XSInteger xsiInteger = (XSInteger) xmlObject;

        if (elementContent != null) {
            xsiInteger.setValue(Integer.valueOf(elementContent.trim()));
        }
    }
}