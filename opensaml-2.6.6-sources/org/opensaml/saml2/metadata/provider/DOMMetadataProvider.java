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

import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**一个MetadataProvider实现，它从用户提供的DOM元素中检索元数据。 如果此提供程序的任何属性发生更改，则调用者有责任通过initialize（）重新初始化。
 * A <code>MetadataProvider</code> implementation that retrieves metadata from a DOM <code>Element</code> as
 * supplied by the user.
 *有可能从数据库读取
 * It is the responsibility of the caller to re-initialize, via {@link #initialize()}, if any properties of this
 * provider are changed.
 */
public class DOMMetadataProvider extends AbstractObservableMetadataProvider implements MetadataProvider {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DOMMetadataProvider.class);

    /** Root metadata element exposed by this provider. */
    private Element metadataElement;

    /** Unmarshalled metadata. */
    private XMLObject metadata;

    /**
     * Constructor.
     *
     * @param mdElement the metadata element
     */
    public DOMMetadataProvider(Element mdElement) {
        super();
        metadataElement = mdElement;
    }

    /** {@inheritDoc} */
    public synchronized void destroy() {
        metadata = null;
        metadataElement = null;

        super.destroy();
    }

    /** {@inheritDoc} */
    protected XMLObject doGetMetadata() throws MetadataProviderException {
        return metadata;
    }

    /** {@inheritDoc} 子类应重写此方法以执行任何必要的初始化逻辑。 默认实现是无操作。*/
    protected void doInitialization() throws MetadataProviderException {
        try {
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(metadataElement);
            XMLObject metadataTemp = unmarshaller.unmarshall(metadataElement);
            filterMetadata(metadataTemp);
            releaseMetadataDOM(metadataTemp);
            metadata = metadataTemp;
            emitChangeEvent();
        } catch (UnmarshallingException e) {
            String errorMsg = "Unable to unmarshall metadata element";
            log.error(errorMsg, e);
            throw new MetadataProviderException(errorMsg, e);
        } catch (FilterException e) {
            String errorMsg = "Unable to filter metadata";
            log.error(errorMsg, e);
            throw new MetadataProviderException(errorMsg, e);
        }
    }
}
