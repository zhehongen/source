/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.security.oauth2.client;

import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerTokenServicesConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.lang.annotation.*;

/**
 * Enable OAuth2 Single Sign On (SSO). If there is an existing
 * {@link WebSecurityConfigurerAdapter} provided by the user and annotated with
 * {@code @EnableOAuth2Sso}, it is enhanced by adding an authentication filter and an
 * authentication entry point. If the user only has {@code @EnableOAuth2Sso} but not on a
 * WebSecurityConfigurerAdapter then one is added with all paths secured.
 * <p>
 * 启用OAuth2单一登录（SSO）。 如果用户提供了一个现有的WebSecurityConfigurerAdapter
 * 并用@ EnableOAuth2Sso进行了注释，则可以通过添加身份验证筛选器和身份验证入口点来对其进行增强。
 * 如果用户仅具有@ EnableOAuth2Sso而不在WebSecurityConfigurerAdapter上具有@ EnableOAuth2Sso，
 * 则将添加一个具有所有安全路径的用户。
 *
 * @author Dave Syer
 * @since 1.3.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableOAuth2Client
@EnableConfigurationProperties(OAuth2SsoProperties.class)
@Import({OAuth2SsoDefaultConfiguration.class, OAuth2SsoCustomConfiguration.class,
        ResourceServerTokenServicesConfiguration.class})
public @interface EnableOAuth2Sso {
//这玩意干啥呢
}
