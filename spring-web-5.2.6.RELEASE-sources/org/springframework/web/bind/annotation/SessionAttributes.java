/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Annotation that indicates the session attributes that a specific handler uses.
 *
 * <p>This will typically list the names of model attributes which should be
 * transparently stored in the session or some conversational storage,
 * serving as form-backing beans. <b>Declared at the type level</b>, applying
 * to the model attributes that the annotated handler class operates on.
 *
 * <p><b>NOTE:</b> Session attributes as indicated using this annotation
 * correspond to a specific handler's model attributes, getting transparently
 * stored in a conversational session. Those attributes will be removed once
 * the handler indicates completion of its conversational session. Therefore,
 * use this facility for such conversational attributes which are supposed
 * to be stored in the session <i>temporarily</i> during the course of a
 * specific handler's conversation.
 *
 * <p>For permanent session attributes, e.g. a user authentication object,
 * use the traditional {@code session.setAttribute} method instead.
 * Alternatively, consider using the attribute management capabilities of the
 * generic {@link org.springframework.web.context.request.WebRequest} interface.
 *
 * <p><b>NOTE:</b> When using controller interfaces (e.g. for AOP proxying),
 * make sure to consistently put <i>all</i> your mapping annotations &mdash;
 * such as {@code @RequestMapping} and {@code @SessionAttributes} &mdash; on
 * the controller <i>interface</i> rather than on the implementation class.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 * 指示特定处理程序使用的会话属性的注释。
 * 这通常会列出模型属性的名称，这些名称应透明地存储在会话或某些会话存储中，用作表单支持bean。在类型级别上声明，应用于带注释的处理程序类所基于的模型属性。
 * 注意：使用此批注指示的会话属性对应于特定处理程序的模型属性，并透明地存储在会话会话中。一旦处理程序指示其会话会话完成，这些属性将被删除。因此，将此功能用于这样的会话属性，这些属性应该在特定处理程序的会话过程中临时存储在会话中。
 * 对于永久会话属性，例如用户身份验证对象，请改用传统的session.setAttribute方法。或者，考虑使用通用的org.springframework.web.context.request.WebRequest接口的属性管理功能。
 * 注意：使用控制器接口时（例如，用于AOP代理），请确保将所有映射注释（例如@RequestMapping和@SessionAttributes）一致地放在控制器接口上，而不是在实现类上。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SessionAttributes {

	/**
	 * Alias for {@link #names}.
	 */
	@AliasFor("names")
	String[] value() default {};

	/**
	 * The names of session attributes in the model that should be stored in the
	 * session or some conversational storage.
	 * <p><strong>Note</strong>: This indicates the <em>model attribute names</em>.
	 * The <em>session attribute names</em> may or may not match the model attribute
	 * names. Applications should therefore not rely on the session attribute
	 * names but rather operate on the model only.
	 * @since 4.2
	 * 模型中应存储在会话或某些会话存储中的会话属性的名称。
	 * 注意：这表示模型属性名称。 会话属性名称可能与模型属性名称匹配，也可能不匹配。 因此，应用程序不应依赖于会话属性名称，而应仅对模型进行操作。
	 */
	@AliasFor("value")
	String[] names() default {};

	/**
	 * The types of session attributes in the model that should be stored in the
	 * session or some conversational storage.
	 * <p>All model attributes of these types will be stored in the session,
	 * regardless of attribute name.
	 * 模型中应存储在会话或某些会话存储中的会话属性的类型。
	 * 不管属性名称如何，这些类型的所有模型属性都将存储在会话中。
	 */
	Class<?>[] types() default {};

}
