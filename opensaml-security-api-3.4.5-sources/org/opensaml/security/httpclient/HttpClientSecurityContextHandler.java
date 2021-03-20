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

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;

import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.httpclient.HttpClientContextHandler;

/**
 * An {@link HttpClientContextHandler} that supports this package's security capabilities.
 * 
 * @since 3.4.0
 */
public class HttpClientSecurityContextHandler extends AbstractInitializableComponent
        implements HttpClientContextHandler {

    /** HTTP client security parameters. */
    @Nullable private HttpClientSecurityParameters httpClientSecurityParameters;

    /**
     * Get the optional client security parameters.
     * 
     * @return the client security parameters
     */
    @Nullable public HttpClientSecurityParameters getHttpClientSecurityParameters() {
        return httpClientSecurityParameters;
    }
    
    /**
     * Set the optional client security parameters.
     * 
     * @param params the new client security parameters
     */
    public void setHttpClientSecurityParameters(@Nullable final HttpClientSecurityParameters params) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        httpClientSecurityParameters = params;
    }
    
    /** {@inheritDoc} */
    public void invokeBefore(final HttpClientContext context, final HttpUriRequest request) throws IOException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        HttpClientSecuritySupport.marshalSecurityParameters(context, httpClientSecurityParameters, false);
        HttpClientSecuritySupport.addDefaultTLSTrustEngineCriteria(context, request);
    }

    /** {@inheritDoc} */
    public void invokeAfter(final HttpClientContext context, final HttpUriRequest request) throws IOException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        
        HttpClientSecuritySupport.checkTLSCredentialEvaluated(context, request.getURI().getScheme());
    }

}