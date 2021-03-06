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

package org.opensaml.saml2.metadata.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.saml2.common.SAML2Helper;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**缓存并定期刷新其元数据的元数据提供程序的基类。该元数据提供程序定期检查以查看读取的元数据文件是否已更改。每个刷新间隔之间的延迟计算如下。如果不存在任何validUntil或cacheDuration，则使用getMaxRefreshDelay（）值。否则，通过查找所有validUntil属性和cacheDuration属性中最早的一个来检查元数据文件的最早刷新间隔。如果该刷新间隔大于最大刷新延迟，则使用getMaxRefreshDelay（）。如果该数字小于最小刷新延迟，则使用getMinRefreshDelay（）。否则，将使用计算得出的刷新延迟乘以getRefreshDelayFactor（）。通过使用此因素，提供程序将尝试在缓存实际过期之前进行刷新，从而为错误和恢复留出了一定的空间。假设该因子不是非常接近1.0，并且最小刷新延迟不是太大，则此刷新可能会在缓存过期之前发生几次。
 * Base class for metadata providers that cache and periodically refresh their metadata.
 *
 * This metadata provider periodically checks to see if the read metadata file has changed. The delay between each
 * refresh interval is calculated as follows. If no validUntil or cacheDuration is present then the
 * {@link #getMaxRefreshDelay()} value is used. Otherwise, the earliest refresh interval of the metadata file is checked
 * by looking for the earliest of all the validUntil attributes and cacheDuration attributes. If that refresh interval
 * is larger than the max refresh delay then {@link #getMaxRefreshDelay()} is used. If that number is smaller than the
 * min refresh delay then {@link #getMinRefreshDelay()} is used. Otherwise the calculated refresh delay multiplied by
 * {@link #getRefreshDelayFactor()} is used. By using this factor, the provider will attempt to be refresh before the
 * cache actually expires, allowing a some room for error and recovery. Assuming the factor is not exceedingly close to
 * 1.0 and a min refresh delay that is not overly large, this refresh will likely occur a few times before the cache
 * expires.
 */
public abstract class AbstractReloadingMetadataProvider extends AbstractObservableMetadataProvider {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractReloadingMetadataProvider.class);

    /** Timer used to schedule background metadata update tasks.用于计划后台元数据更新任务的计时器。 */
    private Timer taskTimer;

    /** Whether we created our own task timer during object construction. 是否在对象构建期间创建了自己的任务计时器。*/
    private boolean createdOwnTaskTimer;

    /** Current task to refresh metadata. */
    private RefreshMetadataTask refresMetadataTask;

    /** Factor used to compute when the next refresh interval will occur. Default value: 0.75 */
    private float refreshDelayFactor = 0.75f;

    /**
     * Refresh interval used when metadata does not contain any validUntil or cacheDuration information. Default value:
     * 14400000ms=4h
     */
    private long maxRefreshDelay = 14400000;

    /** Floor, in milliseconds, for the refresh interval. Default value: 300000ms=5m */
    private int minRefreshDelay = 300000;

    /** Time when the currently cached metadata file expires. */
    private DateTime expirationTime;//说明：

    /** Last time the metadata was updated. */
    private DateTime lastUpdate;//说明：只有更新成功才算

    /** Last time a refresh cycle occurred. */
    private DateTime lastRefresh;//说明：上次调度时间

    /** Next time a refresh cycle will occur. */
    private DateTime nextRefresh;//说明：下次调度时间

    /** Last successfully read in metadata.存的就是这孙子 */
    private XMLObject cachedMetadata;

    /** Constructor. */
    protected AbstractReloadingMetadataProvider() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param backgroundTaskTimer time used to schedule background refresh tasks
     */
    protected AbstractReloadingMetadataProvider(Timer backgroundTaskTimer) {
        if (backgroundTaskTimer == null) {
            log.debug("Creating own background task Timer instance");
            taskTimer = new Timer(true);
            createdOwnTaskTimer = true;
        } else {
            log.debug("Using ctor arg-supplied background task Timer instance");
            taskTimer = backgroundTaskTimer;
        }
    }

    /**
     * Gets the time when the currently cached metadata expires.
     *
     * @return time when the currently cached metadata expires, or null if no metadata is cached
     */
    public DateTime getExpirationTime() {
        return expirationTime;
    }

    /**获取上次更新当前可用元数据的时间。 请注意，如果已知元数据在上一个刷新周期内未更改，则此时间可能不同于getLastRefresh（）检索的时间。这俩有啥区别很难理解
     * Gets the time that the currently available metadata was last updated. Note, this may be different than the time
     * retrieved by {@link #getLastRefresh()} if the metadata was known not to have changed during the last refresh
     * cycle.
     *
     * @return time when the currently metadata was last update, null if metadata has never successfully been read in
     */
    public DateTime getLastUpdate() {
        return lastUpdate;
    }

    /**也许只是记录一个周期
     * Gets the time the last refresh cycle occurred.
     *
     * @return time the last refresh cycle occurred
     */
    public DateTime getLastRefresh() {
        return lastRefresh;
    }

    /**
     * Gets the time when the next refresh cycle will occur.
     *
     * @return time when the next refresh cycle will occur
     */
    public DateTime getNextRefresh() {
        return nextRefresh;
    }

    /**
     * Gets the maximum amount of time, in milliseconds, between refresh intervals.
     *
     * @return maximum amount of time between refresh intervals
     */
    public long getMaxRefreshDelay() {
        return maxRefreshDelay;
    }

    /**
     * Sets the maximum amount of time, in milliseconds, between refresh intervals.
     *
     * @param delay maximum amount of time, in milliseconds, between refresh intervals
     */
    public void setMaxRefreshDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Maximum refresh delay must be greater than 0");
        }
        maxRefreshDelay = delay;
    }

    /**
     * Gets the delay factor used to compute the next refresh time.
     *
     * @return delay factor used to compute the next refresh time
     */
    public float getRefreshDelayFactor() {
        return refreshDelayFactor;
    }

    /**
     * Sets the delay factor used to compute the next refresh time. The delay must be between 0.0 and 1.0, exclusive.
     *
     * @param factor delay factor used to compute the next refresh time
     */
    public void setRefreshDelayFactor(float factor) {
        if (factor <= 0 || factor >= 1) {
            throw new IllegalArgumentException("Refresh delay factor must be a number between 0.0 and 1.0, exclusive");
        }

        refreshDelayFactor = factor;
    }

    /**
     * Gets the minimum amount of time, in milliseconds, between refreshes.
     *
     * @return minimum amount of time, in milliseconds, between refreshes
     */
    public int getMinRefreshDelay() {
        return minRefreshDelay;
    }

    /**
     * Sets the minimum amount of time, in milliseconds, between refreshes.
     *
     * @param delay minimum amount of time, in milliseconds, between refreshes
     */
    public void setMinRefreshDelay(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Minimum refresh delay must be greater than 0");
        }
        minRefreshDelay = delay;
    }

    /** {@inheritDoc} */
    public synchronized void destroy() {
        refresMetadataTask.cancel();

        if (createdOwnTaskTimer) {
            taskTimer.cancel();
        }

        expirationTime = null;
        lastRefresh = null;
        lastUpdate = null;
        nextRefresh = null;
        cachedMetadata = null;

        super.destroy();
    }

    /** {@inheritDoc} */
    protected XMLObject doGetMetadata() throws MetadataProviderException {
        return cachedMetadata;
    }

    /** {@inheritDoc} */
    protected void doInitialization() throws MetadataProviderException {
        refresh();

        if (minRefreshDelay > maxRefreshDelay) {
            throw new MetadataProviderException("Minimum refresh delay " + minRefreshDelay
                    + " is greater than maximum refresh delay " + maxRefreshDelay);
        }
    }

    /**
     * Refreshes the metadata from its source.
     *
     * @throws MetadataProviderException thrown is there is a problem retrieving and processing the metadata
     */
    public synchronized void refresh() throws MetadataProviderException {
        DateTime now = new DateTime(ISOChronology.getInstanceUTC());
        String mdId = getMetadataIdentifier();

        log.debug("Beginning refresh of metadata from '{}'", mdId);
        try {
            byte[] mdBytes = fetchMetadata();
            if (mdBytes == null) {//说明：接口规范认为获取不到，就认为没变
                log.debug("Metadata from '{}' has not changed since last refresh", mdId);
                processCachedMetadata(mdId, now);//说明：计算过期时间expirationTime和下次刷新时间nextRefresh
            } else {
                log.debug("Processing new metadata from '{}'", mdId);
                processNewMetadata(mdId, now, mdBytes);
            }
        } catch (Throwable t) {//说明：抛异常了怎么办？下次尽快调度，所以时间间隔加minRefreshDelay
            log.error("Error occurred while attempting to refresh metadata from '" + mdId + "'", t);
            nextRefresh = new DateTime(ISOChronology.getInstanceUTC()).plus(minRefreshDelay);
            if (t instanceof Exception) {
                throw new MetadataProviderException((Exception) t);
            } else {
                throw new MetadataProviderException(String.format("Saw an error of type '%s' with message '%s'",
                        t.getClass().getName(), t.getMessage()));
            }
        } finally {
            refresMetadataTask = new RefreshMetadataTask();//说明：算出下次调度间隔
            long nextRefreshDelay = nextRefresh.getMillis() - System.currentTimeMillis();//说明：这样做是因为需要考虑抛异常和没抛异常两种情况
            taskTimer.schedule(refresMetadataTask, nextRefreshDelay);//说明：每次都会重新计算下次如何调度？
            log.info("Next refresh cycle for metadata provider '{}' will occur on '{}' ('{}' local time)",
                    new Object[] {mdId, nextRefresh, nextRefresh.toDateTime(DateTimeZone.getDefault()),});
            lastRefresh = now;
        }
    }

    /**获取一个标识符，该标识符可用于区分日志记录语句中的此元数据。
     * Gets an identifier which may be used to distinguish this metadata in logging statements.
     *标识符，可用于在日志记录语句中区分此元数据
     * @return identifier which may be used to distinguish this metadata in logging statements
     */
    protected abstract String getMetadataIdentifier();

    /**
     * Fetches metadata from a source.
     *从源获取元数据文档。
     * @return the fetched metadata, or null if the metadata is known not to have changed since the last retrieval
     *
     * @throws MetadataProviderException thrown if there is a problem fetching the metadata
     */
    protected abstract byte[] fetchMetadata() throws MetadataProviderException;

    /**
     * Unmarshalls the given metadata bytes.
     *
     * @param metadataBytes raw metadata bytes
     *
     * @return the metadata
     *
     * @throws MetadataProviderException thrown if the metadata can not be unmarshalled
     */
    protected XMLObject unmarshallMetadata(byte[] metadataBytes) throws MetadataProviderException {
        try {
            return unmarshallMetadata(new ByteArrayInputStream(metadataBytes));
        } catch (UnmarshallingException e) {
            String errorMsg = "Unable to unmarshall metadata";
            log.error(errorMsg, e);
            throw new MetadataProviderException(errorMsg, e);
        }
    }

    /**处理缓存的元数据文档，以便确定和计划下一次应刷新的时间。
     * Processes a cached metadata document in order to determine, and schedule, the next time it should be refreshed.
     *
     * @param metadataIdentifier identifier of the metadata source
     * @param refreshStart when the current refresh cycle started
     *
     * @throws MetadataProviderException throw is there is a problem process the cached metadata
     */
    protected void processCachedMetadata(String metadataIdentifier, DateTime refreshStart)
            throws MetadataProviderException {
        log.debug("Computing new expiration time for cached metadata from '{}", metadataIdentifier);
        DateTime metadataExpirationTime =
                SAML2Helper
                        .getEarliestExpiration(cachedMetadata, refreshStart.plus(getMaxRefreshDelay()), refreshStart);
//说明：计算过期时间expirationTime和下次刷新时间nextRefresh
        expirationTime = metadataExpirationTime;
        long nextRefreshDelay = computeNextRefreshDelay(expirationTime);
        nextRefresh = new DateTime(ISOChronology.getInstanceUTC()).plus(nextRefreshDelay);
    }

    /**处理新的元数据文档。 处理包括解组和过滤元数据，确定下一次应刷新的时间以及安排下一个刷新周期的处理。
     * Process a new metadata document. Processing include unmarshalling and filtering metadata, determining the next
     * time is should be refreshed and scheduling the next refresh cycle.
     *
     * @param metadataIdentifier identifier of the metadata source
     * @param refreshStart when the current refresh cycle started
     * @param metadataBytes raw bytes of the new metadata document
     *
     * @throws MetadataProviderException thrown if there is a problem unmarshalling or filtering the new metadata
     */
    protected void processNewMetadata(String metadataIdentifier, DateTime refreshStart, byte[] metadataBytes)
            throws MetadataProviderException {
        log.debug("Unmarshalling metadata from '{}'", metadataIdentifier);
        XMLObject metadata = unmarshallMetadata(metadataBytes);

        if (!isValid(metadata)) {
            processPreExpiredMetadata(metadataIdentifier, refreshStart, metadataBytes, metadata);
        } else {
            processNonExpiredMetadata(metadataIdentifier, refreshStart, metadataBytes, metadata);
        }
    }

    /**在获取元数据时处理已确定为无效的元数据（通常是因为它已经过期）。 如果元数据文档的根元素在传递给isValid（XMLObject）方法时返回false，则认为该元数据文档无效。
     * Processes metadata that has been determined to be invalid (usually because it's already expired) at the time it
     * was fetched. A metadata document is considered be invalid if its root element returns false when passed to the
     * {@link #isValid(XMLObject)} method.
     *
     * @param metadataIdentifier identifier of the metadata source
     * @param refreshStart when the current refresh cycle started
     * @param metadataBytes raw bytes of the new metadata document
     * @param metadata new metadata document unmarshalled
     */
    protected void processPreExpiredMetadata(String metadataIdentifier, DateTime refreshStart, byte[] metadataBytes,
            XMLObject metadata) {
        log.warn("Entire metadata document from '{}' was expired at time of loading, existing metadata retained",
                metadataIdentifier);

        lastUpdate = refreshStart;
        nextRefresh = new DateTime(ISOChronology.getInstanceUTC()).plus(getMinRefreshDelay());
    }

    /**处理在获取元数据时已确定有效的元数据。 如果元数据文档的根元素在传递给isValid（XMLObject）方法时返回true，则认为该元数据文档有效。
     * Processes metadata that has been determined to be valid at the time it was fetched. A metadata document is
     * considered be valid if its root element returns true when passed to the {@link #isValid(XMLObject)} method.
     *
     * @param metadataIdentifier identifier of the metadata source
     * @param refreshStart when the current refresh cycle started
     * @param metadataBytes raw bytes of the new metadata document
     * @param metadata new metadata document unmarshalled
     *
     * @throws MetadataProviderException thrown if there s a problem processing the metadata
     */
    protected void processNonExpiredMetadata(String metadataIdentifier, DateTime refreshStart, byte[] metadataBytes,
            XMLObject metadata) throws MetadataProviderException {
        Document metadataDom = metadata.getDOM().getOwnerDocument();

        log.debug("Filtering metadata from '{}'", metadataIdentifier);
        try {
            filterMetadata(metadata);
        } catch (FilterException e) {
            String errMsg = "Error filtering metadata from " + metadataIdentifier;
            log.error(errMsg, e);
            throw new MetadataProviderException(errMsg, e);
        }

        log.debug("Releasing cached DOM for metadata from '{}'", metadataIdentifier);
        releaseMetadataDOM(metadata);

        log.debug("Post-processing metadata from '{}'", metadataIdentifier);
        postProcessMetadata(metadataBytes, metadataDom, metadata);

        log.debug("Computing expiration time for metadata from '{}'", metadataIdentifier);
        DateTime metadataExpirationTime =
                SAML2Helper.getEarliestExpiration(metadata, refreshStart.plus(getMaxRefreshDelay()), refreshStart);
        log.debug("Expiration of metadata from '{}' will occur at {}", metadataIdentifier,
                metadataExpirationTime.toString());

        cachedMetadata = metadata;
        lastUpdate = refreshStart;

        long nextRefreshDelay;
        if (metadataExpirationTime.isBeforeNow()) {
            expirationTime = new DateTime(ISOChronology.getInstanceUTC()).plus(getMinRefreshDelay());
            nextRefreshDelay = getMaxRefreshDelay();
        } else {
            expirationTime = metadataExpirationTime;
            nextRefreshDelay = computeNextRefreshDelay(expirationTime);
        }
        nextRefresh = new DateTime(ISOChronology.getInstanceUTC()).plus(nextRefreshDelay);

        emitChangeEvent();
        log.info("New metadata succesfully loaded for '{}'", getMetadataIdentifier());
    }

    /**
     * Post-processing hook called after new metadata has been unmarshalled, filtered, and the DOM released (from the
     * {@link XMLObject}) but before the metadata is saved off. Any exception thrown by this hook will cause the
     * retrieved metadata to be discarded.
     *
     * The default implementation of this method is a no-op
     *
     * @param metadataBytes raw metadata bytes retrieved via {@link #fetchMetadata}
     * @param metadataDom metadata after it has been parsed in to a DOM document
     * @param metadata metadata after it has been run through all registered filters and its DOM released
     *
     * @throws MetadataProviderException thrown if there is a problem with the provided data
     */
    protected void postProcessMetadata(byte[] metadataBytes, Document metadataDom, XMLObject metadata)
            throws MetadataProviderException {

    }

    /**根据当前元数据的到期时间和刷新间隔下限计算到下一个刷新时间的延迟。
     * Computes the delay until the next refresh time based on the current metadata's expiration time and the refresh
     * interval floor.
     *
     * @param expectedExpiration the time when the metadata is expected to expire and need refreshing
     *
     * @return delay, in milliseconds, until the next refresh time
     */
    protected long computeNextRefreshDelay(DateTime expectedExpiration) {
        long now = new DateTime(ISOChronology.getInstanceUTC()).getMillis();

        long expireInstant = 0;
        if (expectedExpiration != null) {
            expireInstant = expectedExpiration.toDateTime(ISOChronology.getInstanceUTC()).getMillis();
        }
        long refreshDelay = (long) ((expireInstant - now) * getRefreshDelayFactor());

        // if the expiration time was null or the calculated refresh delay was less than the floor
        // use the floor
        if (refreshDelay < getMinRefreshDelay()) {
            refreshDelay = getMinRefreshDelay();
        }
    //说明：为什么没限制最大过期时间?
        return refreshDelay;
    }

    /**
     * Converts an InputStream into a byte array.
     *
     * @param ins input stream to convert
     *
     * @return resultant byte array
     *
     * @throws MetadataProviderException thrown if there is a problem reading the resultant byte array
     */
    protected byte[] inputstreamToByteArray(InputStream ins) throws MetadataProviderException {
        try {
            // 1 MB read buffer
            byte[] buffer = new byte[1024 * 1024];
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            int n = 0;
            while (-1 != (n = ins.read(buffer))) {
                output.write(buffer, 0, n);
            }

            ins.close();
            return output.toByteArray();
        } catch (IOException e) {
            throw new MetadataProviderException(e);
        }
    }

    /** Background task that refreshes metadata. */
    private class RefreshMetadataTask extends TimerTask {

        /** {@inheritDoc} */
        public void run() {
            try {
                if (!isInitialized()) {
                    // just in case the metadata provider was destroyed before this task runs
                    return;
                }

                refresh();
            } catch (MetadataProviderException e) {
                // nothing to do, error message already logged by refreshMetadata()
                return;
            }
        }
    }
}
