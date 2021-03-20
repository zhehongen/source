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

package org.opensaml.core.criterion;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import net.shibboleth.utilities.java.support.resolver.Criterion;

/**
 * A criterion which allows to specify at runtime whether candidates being evaluated
 * must satisfy all other specified criteria, or may satisfy any criteria.
 */
public class SatisfyAnyCriterion implements Criterion {
    
    /** The satisfyAny criterion value. */
    private Boolean satisfyAny;
    
    /**
     * Constructor.
     * 
     * <p></p>
     *
     */
    public SatisfyAnyCriterion() {
        satisfyAny = Boolean.TRUE;
    }
    
    /**
     * Constructor.
     *
     * @param value the satisfyAny flag value
     */
    public SatisfyAnyCriterion(final boolean value) {
        satisfyAny = value;
    }

    /**
     * Get the satisfyAny value.
     * 
     * @return true or false
     */
    public boolean isSatisfyAny() {
        return satisfyAny;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return satisfyAny.hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        
        if (other instanceof SatisfyAnyCriterion) {
            return Objects.equals(satisfyAny, ((SatisfyAnyCriterion)other).satisfyAny);
        }
        
        return false;
    }

    /** {@inheritDoc} */
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(satisfyAny).toString();
    }

}
