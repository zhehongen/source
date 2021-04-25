/* Copyright 2011 Vladimir Schaefer
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
package org.springframework.security.saml.metadata;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**元数据管理器缓存从提供程序加载的EntityDescriptor的所有结果。 每当某些提供程序发布观察到的消息时，都会清除缓存。使用超类ReentrantReadWriteLock同步该类。
 * Metadata manager caches all results of EntityDescriptors loaded from the providers. Cache is cleaned
 * whenever some of the providers published an observed message.
 * <p>
 * The class is synchronized using the superclass ReentrantReadWriteLock.
 *
 * @author Vladimir Schaefer
 */
public class CachingMetadataManager extends MetadataManager {
//说明：整个类几乎最终都还是调用的父类的方法。只是加了一层缓存
    // Cache for alias data
    private Map<String, String> aliasCache;//说明：alias,entityid

    // Cache for basic metadata
    private Map<String, EntityDescriptor> basicMetadataCache;

    // Cache for basic metadata based on SHA-1 hash of the entityID
    private Map<byte[], EntityDescriptor> hashMetadataCache;
    //说明：key都是entityid
    // Cache for extended metadata
    private Map<String, ExtendedMetadata> extendedMetadataCache;

    // Lock for the cache
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates caching metadata provider.
     *
     * @param providers providers to include
     * @throws MetadataProviderException error initializing
     */
    public CachingMetadataManager(List<MetadataProvider> providers) throws MetadataProviderException {

        super(providers);

        this.aliasCache = new HashMap<String, String>();
        this.basicMetadataCache = new HashMap<String, EntityDescriptor>();
        this.hashMetadataCache = new HashMap<byte[], EntityDescriptor>();
        this.extendedMetadataCache = new HashMap<String, ExtendedMetadata>();

    }

    /**保证在初始化过程中由超类调用。
     * Guaranteed to be called by the superclass as part of the initialization.
     */
    @Override
    public void refreshMetadata() {

        try {

            lock.writeLock().lock();
            log.debug("Clearing metadata cache");

            // Clear the caches so they get reinitialized as needed from the new data清除缓存，以便根据需要从新数据中对其进行初始化
            this.aliasCache = new HashMap<String, String>();
            this.basicMetadataCache = new HashMap<String, EntityDescriptor>();
            this.hashMetadataCache = new HashMap<byte[], EntityDescriptor>();
            this.extendedMetadataCache = new HashMap<String, ExtendedMetadata>();

            // Do whatever it takes to refresh the metadata尽一切努力刷新元数据
            super.refreshMetadata();

        } finally {

            lock.writeLock().unlock();

        }

    }

    /**
     * Locates name of the entity for the given alias.
     *
     * @param entityAlias to load entityId for
     * @return entityId or null if not found
     * @throws MetadataProviderException provider in case alias is not unique or missing
     */
    public String getEntityIdForAlias(String entityAlias) throws MetadataProviderException {
        return getFromCacheOrUpdate(aliasCache, entityAlias, aliasLoader);
    }

    /**
     * In case entity exists in the cache it is returned, otherwise mechanism from the super class is used to locate it.
     *
     * @param entityID id to load descriptor for
     * @return entity descriptor or null if not found
     * @throws MetadataProviderException provider
     */
    @Override
    public EntityDescriptor getEntityDescriptor(String entityID) throws MetadataProviderException {
        return getFromCacheOrUpdate(basicMetadataCache, entityID, entityLoader);
    }

    /**
     * Locates entity descriptor whose entityId SHA-1 hash equals the one in the parameter.
     *
     * @param hash hash of the entity descriptor
     * @return found descriptor or null
     */
    @Override
    public EntityDescriptor getEntityDescriptor(byte[] hash) throws MetadataProviderException {
        return getFromCacheOrUpdate(hashMetadataCache, hash, entityHashLoader);
    }

    /**
     * In case entity exists in the cache it is returned, otherwise mechanism from the super class is used to locate it.
     *
     * @param entityID id to load extended metadata for
     * @return entity descriptor or null if not found
     * @throws MetadataProviderException provider
     */
    @Override
    public ExtendedMetadata getExtendedMetadata(String entityID) throws MetadataProviderException {
        return getFromCacheOrUpdate(extendedMetadataCache, entityID, extendedLoader);
    }

    /**如果不存在，则尝试从缓存中加载值，然后从chainingProvider定位该值并将其添加到缓存中。
     * Attempts to load value from the cache, in case it doesn't exist locates it from the chainingProvider and adds
     * to the cache.
     *
     * @param cache       caching map
     * @param key         key to find the value
     * @param valueLoader loader to load value in case it is not present in the cache
     * @param <T>         type of cache
     * @return found value or null if not found
     * @throws MetadataProviderException error or null key
     */
    private <T, U> T getFromCacheOrUpdate(Map<U, T> cache, U key, ValueLoader<T, U> valueLoader) throws MetadataProviderException {

        if (key == null) {
            return null;
        }

        // Try to load the cached value 先验证一下缓存
        lock.readLock().lock();
        if (cache.containsKey(key)) {
            T item = cache.get(key);
            lock.readLock().unlock();
            return item;
        }

        // Upgrade lock when not found
        lock.readLock().unlock();
        lock.writeLock().lock();

        // Re-verify cache
        if (cache.containsKey(key)) {
            T item = cache.get(key);
            lock.writeLock().unlock();
            return item;
        }
     //说明：这个类就这么简单？
        try {

            // Load new value
            T value = valueLoader.getValue(key);
            cache.put(key, value);
            return value;

        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * Interface whose implementations should load value related to the given identifier.
     *
     * @param <T> found value, null if not found
     */
    private interface ValueLoader<T, U> {
        T getValue(U identifier) throws MetadataProviderException;
    }
    //说明：全部都是从父类获取，没啥意思。不明觉厉
    private final ValueLoader<String, String> aliasLoader = new ValueLoader<String, String>() {
        public String getValue(String identifier) throws MetadataProviderException {
            return CachingMetadataManager.super.getEntityIdForAlias(identifier);
        }
    };

    private final ValueLoader<EntityDescriptor, String> entityLoader = new ValueLoader<EntityDescriptor, String>() {
        public EntityDescriptor getValue(String identifier) throws MetadataProviderException {
            return CachingMetadataManager.super.getEntityDescriptor(identifier);
        }
    };

    private final ValueLoader<EntityDescriptor, byte[]> entityHashLoader = new ValueLoader<EntityDescriptor, byte[]>() {
        public EntityDescriptor getValue(byte[] identifier) throws MetadataProviderException {
            return CachingMetadataManager.super.getEntityDescriptor(identifier);
        }
    };

    private final ValueLoader<ExtendedMetadata, String> extendedLoader = new ValueLoader<ExtendedMetadata, String>() {
        public ExtendedMetadata getValue(String identifier) throws MetadataProviderException {
            return CachingMetadataManager.super.getExtendedMetadata(identifier);
        }
    };

}
