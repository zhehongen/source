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

package org.opensaml.core.xml.util;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.apache.commons.codec.binary.Hex;
import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;

import com.google.common.base.MoreObjects;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A bean class which can be optionally used to represent the original
 * source byte[] from which an {@link XMLObject} was parsed and unmarshalled.
 * 
 * <p>
 * It will typically be attached to an XML object via its
 * {@link org.opensaml.core.xml.XMLObject#getObjectMetadata()}.
 * </p>
 * 
 * <p>
 * This may be optionally used by some components to re-persist the object
 * in a more performant manner, at the expense of carrying the additional in-memory storage.
 * </p>
 * 
 * <p>
 * If present, it will be removed from the object when the object is
 * mutated.  See {@link AbstractXMLObject#releaseDOM()}, which is invoked by
 * the various overloaded <code>prepareForAssignment</code> methods there.
 * </p>
 */
public class XMLObjectSource {
    
    /** The object source byte[]. */
    @Nonnull @NotEmpty private byte[] source;
    
    /**
     * Constructor.
     *
     * @param objectSource the object source byte[]
     */
    public XMLObjectSource(@Nonnull @NotEmpty final byte[] objectSource) {
        source = Constraint.isNotNull(objectSource, "Object source byte[] may not be null");
        Constraint.isGreaterThan(0, source.length, "Object source byte[] length must be greater than zero");
    }
    
    /**
     * Get the object source byte[].
     * 
     * @return the object source byte[]
     */
    @Nonnull public byte[] getObjectSource() {
        return source;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", new String(Hex.encodeHex(source, true)))
                .toString();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Arrays.hashCode(source);
    }
    

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj instanceof XMLObjectSource) {
            return Arrays.equals(source, ((XMLObjectSource)obj).source);
        } else {
            return false;
        }
    }
    

}
