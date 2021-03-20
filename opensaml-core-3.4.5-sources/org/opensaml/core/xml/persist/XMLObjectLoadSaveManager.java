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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.core.xml.XMLObject;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.Pair;

/**
 * Interface for a component which is capable of loading and saving instances of {@link XMLObject},
 * based on a string key.
 * 
 * <p>
 * The index key strategy used is determined by the caller.
 * </p>
 * 
 * @param <T> the base type of XML objects being managed
 */
public interface XMLObjectLoadSaveManager<T extends XMLObject> {
    
    /**
     * Return a set of the index keys of all objects under management.
     * 
     * @return a set of all indexed keys
     * 
     * @throws IOException if there is a fatal error obtaining the keys
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable
    public Set<String> listKeys() throws IOException;

    /**
     * Return an iterable of all objects under management, along with their associated index key.
     * 
     * @return an iterable of all managed objects
     * 
     * @throws IOException if there is a fatal error loading the managed objects
     */
    @Nonnull @NonnullElements
    public Iterable<Pair<String,T>> listAll() throws IOException;

    /**
     * Evaluate whether an object already exists indexed by the supplied key.
     * 
     * @param key the key of the desired object
     * 
     * @return true if object exists, false otherwise
     * 
     * @throws IOException if there is a fatal error evaluating object existence
     */
    public boolean exists(@Nonnull @NotEmpty final String key) throws IOException;
    
    /**
     * Load a particular object based on the supplied key.
     * 
     * @param key the key of the desired object
     * 
     * @return the object saved under the specified key, or null if there is no such object
     * 
     * @throws IOException if there is a fatal error loading the object
     */
    @Nullable
    public T load(@Nonnull @NotEmpty final String key) throws IOException;
    
    /**
     * Save a particular object, indexed by the supplied key.
     * 
     * <p>
     * An existing object indexed by the supplied key will not be overwritten. 
     * Instead an {@link IOException} will be thrown.
     * For saving with the overwrite option, see {@link #save(String, XMLObject, boolean)}.
     * </p>
     * 
     * @param key the key under which to index the object
     * @param xmlObject the object to save
     * 
     * @throws IOException if there is a fatal error saving the object, or if an object already exists
     *          indexed by the supplied key
     */
    public void save(@Nonnull @NotEmpty final String key, @Nonnull final T xmlObject) throws IOException;
    
    /**
     * Save a particular object, indexed by the supplied key.
     * 
     * @param key the key under which to index the object
     * @param xmlObject the object to save
     * @param overwrite whether or not to overwrite any existing object indexed by the supplied key
     * 
     * @throws IOException if there is a fatal error saving the object, or if overwrite=false,
     *          if an object already exists indexed by the supplied key
     */
    public void save(@Nonnull @NotEmpty final String key, @Nonnull final T xmlObject, boolean overwrite)
            throws IOException;
    
    /**
     * Remove the object indexed by the specified key.
     * 
     * @param key the key of the object to remove
     * 
     * @return true if the object was found and successfully removed, false if no such object was found
     * 
     * @throws IOException if there was a fatal error removing the object
     */
    public boolean remove(@Nonnull @NotEmpty final String key) throws IOException;
    
    /**
     * Update the key under which a particular object is stored.
     * 
     * @param currentKey the current key under which the object is stored
     * @param newKey the new key under which the object should be stored
     * 
     * @return true if the object was found under the current key and the key successfully updated, 
     *          false if no such object was found
     *          
     * @throws IOException if there was a fatal error updating the key
     */
    public boolean updateKey(@Nonnull @NotEmpty final String currentKey, @Nonnull @NotEmpty final String newKey)
            throws IOException;
    
}
