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

package org.opensaml.core.xml.persist;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.core.xml.XMLObject;

import net.shibboleth.utilities.java.support.annotation.ParameterName;

/**
 * Abstract base class for {@link XMLObjectLoadSaveManager} implementations which 
 * track the modify times of requested data such that {@link #load(String)} returns
 * data only if the data associated with the key has been modified since the last
 * request.
 * 
 * @param <T> the base type of XML objects being managed
 */
public abstract class AbstractConditionalLoadXMLObjectLoadSaveManager<T extends XMLObject> 
        implements ConditionalLoadXMLObjectLoadSaveManager<T> {
    
    /** Configuration flag for whether {@link #load(String)} will check and return data only if modified 
     * since the last request for that data. */
    private boolean loadConditionally;
    
    /** Storage for last modified time of requested data. */
    private Map<String, Long> loadLastModified;
    
    /** 
     * Constructor. 
     * 
     * @param conditionalLoad whether {@link #load(String)} should behave 
     *      as defined in {@link ConditionalLoadXMLObjectLoadSaveManager}
     */
    protected AbstractConditionalLoadXMLObjectLoadSaveManager(
            @ParameterName(name="conditionalLoad") final boolean conditionalLoad) {
        loadLastModified = new HashMap<>();
        loadConditionally = conditionalLoad;
    }
    
    /** {@inheritDoc} */
    public boolean isLoadConditionally() {
        return loadConditionally;
    }
    
    /** {@inheritDoc} */
    @Nullable public synchronized Long getLoadLastModified(@Nonnull final String key) {
        return loadLastModified.get(key);
    }

    /** {@inheritDoc} */
    @Nullable public synchronized Long clearLoadLastModified(@Nonnull final String key) {
        final Long prev = loadLastModified.get(key);
        loadLastModified.remove(key);
        return prev;
    }
    
    /** {@inheritDoc} */
    public void clearAllLoadLastModified() {
        loadLastModified.clear();
    }

    /**
     * Update the cached modified time for the specified key with the current time.
     * 
     * @param key the target key
     * @return the previously cached modified time, or null if did not exist
     */
    protected synchronized Long updateLoadLastModified(@Nonnull final String key) {
        return updateLoadLastModified(key, System.currentTimeMillis());
    }
    
    /**
     * Update the cached modified time for the specified key with the specified time.
     * 
     * @param key the target key
     * @param modified the new cached modified time
     * @return the previously cached modified time, or null if did not exist
     */
    protected synchronized Long updateLoadLastModified(@Nonnull final String key, @Nullable final Long modified) {
        if (modified == null) {
            return null;
        }
        final Long prev = loadLastModified.get(key);
        loadLastModified.put(key, modified);
        return prev;
    }
    
    /**
     * Check whether the data corresponding to the specified key has been modified since the last time
     * {@link #load(String)} was called for that key.
     * 
     * @param key the data key
     * @return true if the corresponding data has been modified since the last load, false otherwise
     * @throws IOException if there is a fatal error evaluating the last modified status
     */
    protected abstract boolean isUnmodifiedSinceLastLoad(@Nonnull final String key) throws IOException;
    
}
