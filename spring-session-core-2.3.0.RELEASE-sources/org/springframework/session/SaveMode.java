/*
 * Copyright 2014-2019 the original author or authors.
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

package org.springframework.session;

/**
 * Supported modes of tracking and saving session changes to session store.
 *
 * @author Rob Winch
 * @author Vedran Pavic
 * @since 2.2.0
 */
public enum SaveMode {

	/**仅保存对会话所做的更改，例如使用Session.setAttribute（String，Object）。 在高度并发的环境中，此模式最大程度地减少了在处理并行请求期间属性被覆盖的风险。
	 * Save only changes made to session, for instance using
	 * {@link Session#setAttribute(String, Object)}. In highly concurrent environments,
	 * this mode minimizes the risk of attributes being overwritten during processing of
	 * parallel requests.
	 */
	ON_SET_ATTRIBUTE,

	/**与ON_SET_ATTRIBUTE相同，但添加了使用Session.getAttribute（String）读取的保存属性。
	 * Same as {@link #ON_SET_ATTRIBUTE} with addition of saving attributes that have been
	 * read using {@link Session#getAttribute(String)}.
	 */
	ON_GET_ATTRIBUTE,

	/**始终保存所有会话属性，无论与会话进行交互如何。 在高度并发的环境中，此模式增加了在处理并行请求期间属性被覆盖的风险。
	 * Always save all session attributes, regardless of the interaction with the session.
	 * In highly concurrent environments, this mode increases the risk of attributes being
	 * overwritten during processing of parallel requests.
	 */
	ALWAYS

}
