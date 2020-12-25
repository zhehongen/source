/*
 * Copyright 2015-2020 the original author or authors.
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
package org.springframework.data.redis.core;

import org.springframework.lang.Nullable;

/**
 * {@link TimeToLiveAccessor} extracts the objects time to live used for {@code EXPIRE}.
 *
 * @author Christoph Strobl
 * @since 1.7
 */
public interface TimeToLiveAccessor {

	/**
	 * @param source must not be {@literal null}.
	 * @return {@literal null} if not configured.
	 */
	@Nullable
	Long getTimeToLive(Object source);
}
