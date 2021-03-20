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

package org.opensaml.security.messaging;

import javax.annotation.Nullable;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.security.httpclient.HttpClientSecurityParameters;

import com.google.common.base.Function;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

/**
 * A context implementation holding parameters related to {@link org.apache.http.client.HttpClient} security features.
 */
public class HttpClientSecurityContext extends BaseContext {
    
    /** The HttpClient security parameters instance. */
    private HttpClientSecurityParameters securityParameters;
    
    /** TLS criteria strategy function. */
    private Function<MessageContext<?>, CriteriaSet> tlsCriteriaSetStrategy;
    
    /**
     * Get the {@link HttpClientSecurityParameters} instance.
     * 
     * @return the parameters instance, or null
     */
    @Nullable public HttpClientSecurityParameters getSecurityParameters() {
        return securityParameters;
    }
    
    /**
     * Set the {@link HttpClientSecurityParameters} instance.
     * 
     * @param parameters the parameters instance, or null
     */
    public void setSecurityParameters(@Nullable final HttpClientSecurityParameters parameters) {
        securityParameters = parameters;
    }

    /**
     * Get the TLS criteria strategy function.
     * 
     * @return the strategy function, or null
     */
    @Nullable public Function<MessageContext<?>, CriteriaSet> getTLSCriteriaSetStrategy() {
        return tlsCriteriaSetStrategy;
    }
    
    /**
     * Set the TLS criteria strategy function.
     * 
     * @param strategy the strategy function instance, or null
     */
    public void setTLSCriteriaSetStrategy(@Nullable final Function<MessageContext<?>, CriteriaSet> strategy) {
        tlsCriteriaSetStrategy = strategy;
    }
}
