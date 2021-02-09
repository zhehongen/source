/*
 * Copyright 2002-2016 the original author or authors.
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
package org.springframework.security.web.util;

/**
 * Interface for handlers extracting the cause out of a specific {@link Throwable} type.
 * 处理程序从特定Throwable类型提取原因的接口。
 * @author Andreas Senft
 * @since 2.0
 *
 * @see ThrowableAnalyzer
 */
public interface ThrowableCauseExtractor {

	/**
	 * Extracts the cause from the provided <code>Throwable</code>.
	 *
	 * @param throwable the <code>Throwable</code>
	 * @return the extracted cause (maybe <code>null</code>)
	 *从提供的Throwable中提取原因。
	 * @throws IllegalArgumentException if <code>throwable</code> is <code>null</code> or
	 * otherwise considered invalid for the implementation
	 */
	Throwable extractCause(Throwable throwable);
}
