/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * A base {@link ReactiveAuthenticationManager} that allows subclasses to override and work with
 * {@link UserDetails} objects.
 *
 * <p>
 * Upon successful validation, a <code>UsernamePasswordAuthenticationToken</code> will be
 * created and returned to the caller. The token will include as its principal either a
 * <code>String</code> representation of the username, or the {@link UserDetails} that was
 * returned from the authentication repository.
 *
 * @author Eddú Meléndez
 * @since 5.2
 */
public abstract class AbstractUserDetailsReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	protected final Log logger = LogFactory.getLog(getClass());

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

	private ReactiveUserDetailsPasswordService userDetailsPasswordService;

	private Scheduler scheduler = Schedulers.newParallel("password-encoder", Schedulers.DEFAULT_POOL_SIZE, true);

	private UserDetailsChecker preAuthenticationChecks = user -> {
		if (!user.isAccountNonLocked()) {
			logger.debug("User account is locked");

			throw new LockedException(this.messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.locked",
					"User account is locked"));
		}

		if (!user.isEnabled()) {
			logger.debug("User account is disabled");

			throw new DisabledException(this.messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.disabled",
					"User is disabled"));
		}

		if (!user.isAccountNonExpired()) {
			logger.debug("User account is expired");

			throw new AccountExpiredException(this.messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.expired",
					"User account has expired"));
		}
	};

	private UserDetailsChecker postAuthenticationChecks = user -> {
		if (!user.isCredentialsNonExpired()) {
			logger.debug("User account credentials have expired");

			throw new CredentialsExpiredException(this.messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.credentialsExpired",
					"User credentials have expired"));
		}
	};

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		final String username = authentication.getName();
		final String presentedPassword = (String) authentication.getCredentials();
		return retrieveUser(username)
				.doOnNext(this.preAuthenticationChecks::check)
				.publishOn(this.scheduler)
				.filter(u -> this.passwordEncoder.matches(presentedPassword, u.getPassword()))
				.switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid Credentials"))))
				.flatMap(u -> {
					boolean upgradeEncoding = this.userDetailsPasswordService != null
							&& this.passwordEncoder.upgradeEncoding(u.getPassword());
					if (upgradeEncoding) {
						String newPassword = this.passwordEncoder.encode(presentedPassword);
						return this.userDetailsPasswordService.updatePassword(u, newPassword);
					}
					return Mono.just(u);
				})
				.doOnNext(this.postAuthenticationChecks::check)
				.map(u -> new UsernamePasswordAuthenticationToken(u, u.getPassword(), u.getAuthorities()) );
	}

	/**
	 * The {@link PasswordEncoder} that is used for validating the password. The default is
	 * {@link PasswordEncoderFactories#createDelegatingPasswordEncoder()}
	 * @param passwordEncoder the {@link PasswordEncoder} to use. Cannot be null
	 */
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Sets the {@link Scheduler} used by the {@link UserDetailsRepositoryReactiveAuthenticationManager}.
	 * The default is {@code Schedulers.newParallel(String)} because modern password encoding is
	 * a CPU intensive task that is non blocking. This means validation is bounded by the
	 * number of CPUs. Some applications may want to customize the {@link Scheduler}. For
	 * example, if users are stuck using the insecure {@link org.springframework.security.crypto.password.NoOpPasswordEncoder}
	 * they might want to leverage {@code Schedulers.immediate()}.
	 *
	 * @param scheduler the {@link Scheduler} to use. Cannot be null.
	 * @since 5.0.6
	 */
	public void setScheduler(Scheduler scheduler) {
		Assert.notNull(scheduler, "scheduler cannot be null");
		this.scheduler = scheduler;
	}

	/**
	 * Sets the service to use for upgrading passwords on successful authentication.
	 * @param userDetailsPasswordService the service to use
	 */
	public void setUserDetailsPasswordService(
			ReactiveUserDetailsPasswordService userDetailsPasswordService) {
		this.userDetailsPasswordService = userDetailsPasswordService;
	}

	/**
	 * Sets the strategy which will be used to validate the loaded <tt>UserDetails</tt>
	 * object after authentication occurs.
	 *
	 * @param postAuthenticationChecks The {@link UserDetailsChecker}
	 * @since 5.2
	 */
	public void setPostAuthenticationChecks(UserDetailsChecker postAuthenticationChecks) {
		Assert.notNull(this.postAuthenticationChecks, "postAuthenticationChecks cannot be null");
		this.postAuthenticationChecks = postAuthenticationChecks;
	}

	/**
	 * Allows subclasses to retrieve the <code>UserDetails</code>
	 * from an implementation-specific location.
	 *
	 * @param username The username to retrieve
	 * @return the user information. If authentication fails, a Mono error is returned.
	 */
	protected abstract Mono<UserDetails> retrieveUser(String username);

}
