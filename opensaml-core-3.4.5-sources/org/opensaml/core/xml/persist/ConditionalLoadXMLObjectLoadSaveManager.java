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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.core.xml.XMLObject;

/**
 * Interface for specialization of {@link XMLObjectLoadSaveManager} implementations which 
 * track the modify times of requested data such that {@link #load(String)} returns
 * data only if the data associated with the key has been modified since the last
 * request.
 * 
 * @param <T> the base type of XML objects being managed
 */
public interface ConditionalLoadXMLObjectLoadSaveManager<T extends XMLObject> extends XMLObjectLoadSaveManager<T> {
    
    /** 
     * Get whether {@link #load(String)} will check and return data only if modified 
     * since the last request for that data.
     * 
     * @return true if data modify time check is enabled, false if not
     */
    public boolean isLoadConditionally();
    
    /**
     * Retrieve the cached modified time for the last load of the specified key.
     * 
     * <p>
     * Note that this will be null if {@link #load(String)} has not been called
     * for the specified key since construction or since the last call to 
     * {@link #clearLoadLastModified(String)} or {@link #clearAllLoadLastModified()}.
     * </p> 
     * 
     * @param key the target key
     * @return the current cached modified time in milliseconds since the epoch, may be null
     */
    @Nullable public Long getLoadLastModified(@Nonnull final String key);
    
    /**
     * Clear the cached modified time for the last load of the specified key.
     * 
     * @param key the target key
     * @return the previously cached modified time in milliseconds since the epoch, or null if did not exist
     */
    @Nullable public Long clearLoadLastModified(@Nonnull final String key);

    /**
     * Clear the cached modified times for the last load for all keys.
     */
    public void clearAllLoadLastModified();

}
