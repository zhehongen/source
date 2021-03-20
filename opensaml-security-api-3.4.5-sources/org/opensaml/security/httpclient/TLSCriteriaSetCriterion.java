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

package org.opensaml.security.httpclient;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;


/**
 * An implementation of {@link Criterion} which specifies criteria pertaining 
 * usage of the resolved credential. 
 */
public final class TLSCriteriaSetCriterion implements Criterion {
   
    /** TLS CriteriaSet data. */
    private CriteriaSet criteriaSet;
    
    /**
    * Constructor.
     *
     * @param criteria the TLS criteria set
     */
    public TLSCriteriaSetCriterion(@Nonnull final CriteriaSet criteria) {
        criteriaSet = Constraint.isNotNull(criteria, "TLS CriteriaSet was null");
    }

    /**
     * Get the key usage criteria.
     * 
     * @return Returns the usage.
     */
    @Nonnull public CriteriaSet getCriteria() {
        return criteriaSet;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CriteriaSet=");
        builder.append(criteriaSet);
        builder.append("]");
        return builder.toString();
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return criteriaSet.hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof TLSCriteriaSetCriterion) {
            return criteriaSet.equals(((TLSCriteriaSetCriterion) obj).criteriaSet);
        }

        return false;
    }

}