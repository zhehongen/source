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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import org.opensaml.core.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.annotation.ParameterName;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Simple implementation of {@link XMLObjectLoadSaveManager} which uses an in-memory map.
 *
 * @param <T> the specific base XML object type being managed
 */
@NotThreadSafe
public class MapLoadSaveManager<T extends XMLObject> extends AbstractConditionalLoadXMLObjectLoadSaveManager<T> {
    
    /** Logger. */
    private Logger log = LoggerFactory.getLogger(MapLoadSaveManager.class);
    
    /** The backing map. */
    private Map<String,T> backingMap;
    
    /** Storage to track last modified time of data. */
    private Map<String,Long> dataLastModified;
    
    /** Constructor. */
    public MapLoadSaveManager() {
        this(new HashMap<String,T>(), new HashMap<String,Long>(), false);
    }

    /** 
     * Constructor.
     * 
     * @param conditionalLoad whether {@link #load(String)} should behave 
     *      as defined in {@link ConditionalLoadXMLObjectLoadSaveManager}
     * */
    public MapLoadSaveManager(@ParameterName(name="conditionalLoad") final boolean conditionalLoad) {
        this(new HashMap<String,T>(), new HashMap<String,Long>(), conditionalLoad);
    }

    /**
     * Constructor.
     * 
     * <p>
     * Note: conditional load is not supported with this option, because of the need to track
     * modify times of items stored in the backing map.
     * Use instead {@link MapLoadSaveManager#MapLoadSaveManager(boolean)}.
     * </p>
     *
     * @param map the backing map 
     */
    public MapLoadSaveManager(@ParameterName(name="map") @Nonnull final Map<String, T> map) {
        this(map, new HashMap<String,Long>(), false);
    }
    
    /**
     * Constructor.
     * 
     * @param map the backing map 
     * @param lastModifiedMap the storage for data last modified times
     * @param conditionalLoad whether {@link #load(String)} should behave 
     *      as defined in {@link ConditionalLoadXMLObjectLoadSaveManager}
     */
    protected MapLoadSaveManager(
            @ParameterName(name="map") @Nonnull final Map<String, T> map,
            @ParameterName(name="dataLastModified") @Nonnull final Map<String,Long> lastModifiedMap,
            @ParameterName(name="conditionalLoad") final boolean conditionalLoad) {
        super(conditionalLoad);
        backingMap = Constraint.isNotNull(map, "Backing map was null");
        dataLastModified = Constraint.isNotNull(lastModifiedMap, "Data last modified map was null");
    }

    /** {@inheritDoc} */
    public Set<String> listKeys() throws IOException {
        return backingMap.keySet();
    }

    /** {@inheritDoc} */
    public Iterable<Pair<String, T>> listAll() throws IOException {
        final ArrayList<Pair<String,T>> list = new ArrayList<>();
        for (final String key : listKeys()) {
            list.add(new Pair<>(key, load(key)));
        }
        return list;
    }

    /** {@inheritDoc} */
    public boolean exists(final String key) throws IOException {
        return backingMap.containsKey(key);
    }

    /** {@inheritDoc} */
    public T load(final String key) throws IOException {
        if (!exists(key)) {
            log.debug("Target data with key '{}' does not exist", key);
            clearLoadLastModified(key);
            return null;
        }
        if (isLoadConditionally() && isUnmodifiedSinceLastLoad(key)) {
            log.debug("Target data with key '{}' has not been modified since the last request, returning null", key);
            return null;
        }
        updateLoadLastModified(key, dataLastModified.get(key));
        return backingMap.get(key);
    }

    /** {@inheritDoc} */
    public void save(final String key, final T xmlObject) throws IOException {
        save(key, xmlObject, false);
    }

    /** {@inheritDoc} */
    public void save(final String key, final T xmlObject, final boolean overwrite) throws IOException {
        if (!overwrite && exists(key)) {
            throw new IOException(String.format("Value already exists for key '%s'", key));
        } else {
            backingMap.put(key, xmlObject);
            dataLastModified.put(key, System.currentTimeMillis());
        }
    }

    /** {@inheritDoc} */
    public boolean remove(final String key) throws IOException {
        final T removed = backingMap.remove(key);
        dataLastModified.remove(key);
        clearLoadLastModified(key);
        return removed != null;
    }

    /** {@inheritDoc} */
    public boolean updateKey(final String currentKey, final String newKey) throws IOException {
        final T value = backingMap.get(currentKey);
        if (value == null) {
            return false;
        }
        if (backingMap.containsKey(newKey)) {
            throw new IOException(String.format("Specified new key already exists: %s", newKey));
        } else {
            backingMap.put(newKey, value);
            backingMap.remove(currentKey);
            
            dataLastModified.put(newKey, dataLastModified.get(currentKey));
            dataLastModified.remove(currentKey);
            
            updateLoadLastModified(newKey, getLoadLastModified(currentKey));
            clearLoadLastModified(currentKey);
            return true;
        }
    }

    /** {@inheritDoc} */
    protected boolean isUnmodifiedSinceLastLoad(@Nonnull final String key) throws IOException {
        final Long lastModified = dataLastModified.get(key);
        log.trace("Key '{}' last modified was: {}", key, lastModified);
        return getLoadLastModified(key) != null && lastModified != null && lastModified <= getLoadLastModified(key);
    }

}
