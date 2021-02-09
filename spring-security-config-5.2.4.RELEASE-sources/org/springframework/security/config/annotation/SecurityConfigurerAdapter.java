/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.security.config.annotation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * A base class for {@link SecurityConfigurer} that allows subclasses to only implement
 * the methods they are interested in. It also provides a mechanism for using the
 * {@link SecurityConfigurer} and when done gaining access to the {@link SecurityBuilder}
 * that is being configured.
 * SecurityConfigurer的基类，它允许子类仅实现它们感兴趣的方法。它还提供了一种使用SecurityConfigurer的机制，并在完成后获得对正在配置的SecurityBuilder的访问权。
 * @author Rob Winch
 * @author Wallace Wadge
 *
 * @param <O> The Object being built by B
 * @param <B> The Builder that is building O and is configured by
 * {@link SecurityConfigurerAdapter}
 */
public abstract class SecurityConfigurerAdapter<O, B extends SecurityBuilder<O>>
		implements SecurityConfigurer<O, B> {
	private B securityBuilder;

	private CompositeObjectPostProcessor objectPostProcessor = new CompositeObjectPostProcessor();

	public void init(B builder) throws Exception {
	}

	public void configure(B builder) throws Exception {
	}

	/**
	 * Return the {@link SecurityBuilder} when done using the {@link SecurityConfigurer}.
	 * This is useful for method chaining.
	 * 使用SecurityConfigurer完成后，返回SecurityBuilder。 这对于方法链接很有用。
	 * @return the {@link SecurityBuilder} for further customizations
	 */
	public B and() {
		return getBuilder();
	}

	/**
	 * Gets the {@link SecurityBuilder}. Cannot be null.
	 *
	 * @return the {@link SecurityBuilder}
	 * @throws IllegalStateException if {@link SecurityBuilder} is null
	 */
	protected final B getBuilder() {
		if (securityBuilder == null) {
			throw new IllegalStateException("securityBuilder cannot be null");
		}
		return securityBuilder;
	}

	/**
	 * Performs post processing of an object. The default is to delegate to the
	 * {@link ObjectPostProcessor}.
	 *
	 * @param object the Object to post process
	 * @return the possibly modified Object to use
	 */
	@SuppressWarnings("unchecked")
	protected <T> T postProcess(T object) {
		return (T) this.objectPostProcessor.postProcess(object);
	}

	/**
	 * Adds an {@link ObjectPostProcessor} to be used for this
	 * {@link SecurityConfigurerAdapter}. The default implementation does nothing to the
	 * object.
	 * 批
	 * @param objectPostProcessor the {@link ObjectPostProcessor} to use
	 */
	public void addObjectPostProcessor(ObjectPostProcessor<?> objectPostProcessor) {
		this.objectPostProcessor.addObjectPostProcessor(objectPostProcessor);
	}

	/**
	 * Sets the {@link SecurityBuilder} to be used. This is automatically set when using
	 * {@link AbstractConfiguredSecurityBuilder#apply(SecurityConfigurerAdapter)}
	 * 设置要使用的SecurityBuilder。 使用AbstractConfiguredSecurityBuilder.apply（SecurityConfigurerAdapter）时会自动设置
	 * @param builder the {@link SecurityBuilder} to set
	 */
	public void setBuilder(B builder) {
		this.securityBuilder = builder;
	}

	/**
	 * An {@link ObjectPostProcessor} that delegates work to numerous
	 * {@link ObjectPostProcessor} implementations.
	 * 一个将工作委托给许多ObjectPostProcessor实现的ObjectPostProcessor。
	 * @author Rob Winch
	 */
	private static final class CompositeObjectPostProcessor implements
			ObjectPostProcessor<Object> {
		private List<ObjectPostProcessor<?>> postProcessors = new ArrayList<>();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Object postProcess(Object object) {//如果传进来的对象是后处理器的泛型参数的子类，则执行后处理方法
			for (ObjectPostProcessor opp : postProcessors) {
				Class<?> oppClass = opp.getClass();//后处理器的class，肯定是ObjectPostProcessor<T>的实现类吧
				Class<?> oppType = GenericTypeResolver.resolveTypeArgument(oppClass,
						ObjectPostProcessor.class);//解析出后处理器的泛型参数，即T.class
				if (oppType == null || oppType.isAssignableFrom(object.getClass())) {//oppType == null无法理解。子类非泛型？
					object = opp.postProcess(object);
				}
			}
			return object;
		}

		/**
		 * Adds an {@link ObjectPostProcessor} to use
		 * @param objectPostProcessor the {@link ObjectPostProcessor} to add
		 * @return true if the {@link ObjectPostProcessor} was added, else false
		 */
		private boolean addObjectPostProcessor(
				ObjectPostProcessor<?> objectPostProcessor) {
			boolean result = this.postProcessors.add(objectPostProcessor);
			postProcessors.sort(AnnotationAwareOrderComparator.INSTANCE);
			return result;
		}
	}
}
