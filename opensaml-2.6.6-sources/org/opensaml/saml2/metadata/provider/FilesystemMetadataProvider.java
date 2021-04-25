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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**从本地文件系统上的文件中提取元数据的元数据提供程序。该元数据提供程序定期检查以查看读取的元数据文件是否已更改。每个刷新间隔之间的延迟计算如下。如果不存在任何validUntil或cacheDuration，则使用getMaxRefreshDelay（）值。否则，通过查找所有validUntil属性和cacheDuration属性中最早的一个来检查元数据文件的最早刷新间隔。如果该刷新间隔大于最大刷新延迟，则使用getMaxRefreshDelay（）。如果该数字小于最小刷新延迟，则使用getMinRefreshDelay（）。否则，将使用计算得出的刷新延迟乘以getRefreshDelayFactor（）。通过使用此因素，提供程序将尝试在缓存实际过期之前进行刷新，从而为错误和恢复留出了一定的空间。假设该因子不是非常接近1.0，并且最小刷新延迟不是太大，则此刷新可能会在缓存过期之前发生几次。
 * A metadata provider that pulls metadata from a file on the local filesystem.
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
 *
 */
public class FilesystemMetadataProvider extends AbstractReloadingMetadataProvider {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(FilesystemMetadataProvider.class);

    /** The metadata file. */
    private File metadataFile;

    /**
     * Constructor.
     *
     * @param metadata the metadata file
     *
     * @throws MetadataProviderException  this exception is no longer thrown
     */
    public FilesystemMetadataProvider(File metadata) throws MetadataProviderException {
        super();
        setMetadataFile(metadata);
    }

    /**
     * Constructor.
     *
     * @param metadata the metadata file
     * @param backgroundTaskTimer timer used to refresh metadata in the background
     *
     * @throws MetadataProviderException  this exception is no longer thrown
     */
    public FilesystemMetadataProvider(Timer backgroundTaskTimer, File metadata) throws MetadataProviderException {
        super(backgroundTaskTimer);
        setMetadataFile(metadata);
    }

    /**
     * Sets the file from which metadata is read.
     *
     * @param file path to the metadata file
     *
     * @throws MetadataProviderException this exception is no longer thrown
     */
    protected void setMetadataFile(File file) throws MetadataProviderException {
        metadataFile = file;
    }

    /**
     * Gets whether cached metadata should be discarded if it expires and can not be refreshed.
     *
     * @return whether cached metadata should be discarded if it expires and can not be refreshed.
     *
     * @deprecated use {@link #requireValidMetadata()} instead
     */
    public boolean maintainExpiredMetadata() {
        return !requireValidMetadata();
    }

    /**
     * Sets whether cached metadata should be discarded if it expires and can not be refreshed.
     *
     * @param maintain whether cached metadata should be discarded if it expires and can not be refreshed.
     *
     * @deprecated use {@link #setRequireValidMetadata(boolean)} instead
     */
    public void setMaintainExpiredMetadata(boolean maintain) {
        setRequireValidMetadata(!maintain);
    }

    /** {@inheritDoc} */
    public synchronized void destroy() {
        metadataFile = null;

        super.destroy();
    }

    /** {@inheritDoc} */
    protected String getMetadataIdentifier() {
        return metadataFile.getAbsolutePath();
    }

    /** {@inheritDoc} */
    protected byte[] fetchMetadata() throws MetadataProviderException {
        try {
            validateMetadataFile(metadataFile);
            DateTime metadataUpdateTime = new DateTime(metadataFile.lastModified(), ISOChronology.getInstanceUTC());
            if (getLastRefresh() == null || getLastUpdate() == null || metadataUpdateTime.isAfter(getLastRefresh())) {
                return inputstreamToByteArray(new FileInputStream(metadataFile));
            }

            return null;
        } catch (IOException e) {
            String errMsg = "Unable to read metadata file " + metadataFile.getAbsolutePath();
            log.error(errMsg, e);
            throw new MetadataProviderException(errMsg, e);
        }
    }

    /**验证指定的元数据文件的基本属性，例如它的存在； 它是一个文件； 并且它是可读的。
     * Validate the basic properties of the specified metadata file, for example that it exists;
     * that it is a file; and that it is readable.
     *
     * @param file the file to evaluate
     * @throws MetadataProviderException if file does not pass basic properties required of a metadata file
     */
    protected void validateMetadataFile(File file) throws MetadataProviderException {
        if (!file.exists()) {
            throw new MetadataProviderException("Metadata file '" + file.getAbsolutePath() + "' does not exist");
        }

        if (!file.isFile()) {
            throw new MetadataProviderException("Metadata file '" + file.getAbsolutePath() + "' is not a file");
        }

        if (!file.canRead()) {
            throw new MetadataProviderException("Metadata file '" + file.getAbsolutePath() + "' is not readable");
        }
    }

}
