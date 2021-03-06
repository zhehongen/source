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

package org.opensaml.xml.security.criteria;

import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.util.DatatypeHelper;

/**Criteria的实现，它指定标识特定实体的条件。 请注意，当用作凭据解析程序的凭据标准时，实体ID是拥有已解析凭据的实体。 根据使用情况，例如，该实体ID可以代表本地实体（自身）或远程实体。 在解析签名验证凭证时，所有者实体ID将是远程对等方； 在解析解密凭证时，所有者实体ID将是本地实体ID。 另请参见PeerEntityIDCriteria。
 * An implementation of {@link Criteria} which specifies criteria identifying a
 * particular entity.
 * 
 * Note that when used as a credential criteria for a credential resolver, the entity ID is the entity which owns 
 * the resolved credential. This entity ID may represent either a local entity (self) or remote entity, depending
 * on the use case, e.g. in resolution of signature verification credentials, the owner entity ID
 * would be a remote peer; in resolution of decryption credentials, the owner entity ID would be
 * a local entity ID.
 * 
 * See also {@link PeerEntityIDCriteria}.
 */
public final class EntityIDCriteria implements Criteria {
    
    /** Entity ID criteria. */
    private String entityID;
    
    /**
    * Constructor.
     *
     * @param entity the entity ID represented by the criteria
     */
    public EntityIDCriteria(String entity) {
        setEntityID(entity);
    }

    /**
     * Get the entity ID represented by the criteria.
     * 
     * @return the primary entity ID.
     */
    public String getEntityID() {
        return entityID;
    }

    /**
     * Set the entity ID represented by the criteria.
     * 
     * @param entity The entityID to set.
     */
    public void setEntityID(String entity) {
        String trimmed = DatatypeHelper.safeTrimOrNullString(entity);
        if (trimmed == null) {
            throw new IllegalArgumentException("Entity ID criteria must be supplied");
        }
        entityID = trimmed;
    }

}
