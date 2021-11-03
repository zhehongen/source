/*
 * Copyright 2002-2018 the original author or authors.
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
package org.springframework.security.config.annotation.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.util.Assert;

/**
 * Allows registering Objects to participate with an {@link AutowireCapableBeanFactory}'s
 * post processing of {@link Aware} methods, {@link InitializingBean#afterPropertiesSet()}
 * , and {@link DisposableBean#destroy()}.
 *
 * @author Rob Winch
 * @since 3.2
 */
final class AutowireBeanFactoryObjectPostProcessor
		implements ObjectPostProcessor<Object>, DisposableBean, SmartInitializingSingleton {
	private final Log logger = LogFactory.getLog(getClass());
	private final AutowireCapableBeanFactory autowireBeanFactory;
	private final List<DisposableBean> disposableBeans = new ArrayList<>();
	private final List<SmartInitializingSingleton> smartSingletons = new ArrayList<>();
	// 使用指定的 autowireBeanFactory 构造对象,autowireBeanFactory 通常是 Spring bean 容器
	AutowireBeanFactoryObjectPostProcessor(
			AutowireCapableBeanFactory autowireBeanFactory) {
		Assert.notNull(autowireBeanFactory, "autowireBeanFactory cannot be null");
		this.autowireBeanFactory = autowireBeanFactory;
	}
	//对某个刚刚创建的对象 object 执行这里所谓的 post-process 流程 :1. 使用指定的 autowireBeanFactory 对该对象 object 执行初始化过程;
	/*2. 使用指定的 autowireBeanFactory 对该对象 object 执行依赖注入过程;
	 * (non-Javadoc)3. 如果该对象 object 是一个 DisposableBean , 则将它记录下来，在当前对象的destroy()被调用时，它们的 destroy() 方法也都会被调用;
	 *4. 如果该对象 object 是一个 SmartInitializingSingleton , 则将它记录下来，在当前对象的 afterSingletonsInstantiated () 被调用时，它们的 afterSingletonsInstantiated() 方法也都会被调用;
	 * @see
	 * org.springframework.security.config.annotation.web.Initializer#initialize(java.
	 * lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public <T> T postProcess(T object) {
		if (object == null) {
			return null;
		}
		T result = null;
		try {// 使用容器autowireBeanFactory标准初始化方法initializeBean()初始化对象 object
			result = (T) this.autowireBeanFactory.initializeBean(object,
					object.toString());//本质就是调用对象实现的InitializingBean.afterPropertiesSet的方法
		}
		catch (RuntimeException e) {
			Class<?> type = object.getClass();
			throw new RuntimeException(
					"Could not postProcess " + object + " of type " + type, e);
		}// 使用容器autowireBeanFactory标准依赖注入方法autowireBean()处理 object对象的依赖注入
		this.autowireBeanFactory.autowireBean(object);//真的是依赖注入吗。确实是的。可以注入一些东西
		if (result instanceof DisposableBean) {// 记录一个 DisposableBean 对象
			this.disposableBeans.add((DisposableBean) result);
		}
		if (result instanceof SmartInitializingSingleton) {// 记录一个 SmartInitializingSingleton 对象
			this.smartSingletons.add((SmartInitializingSingleton) result);
		}
		return result;
	}
	// SmartInitializingSingleton 接口定义的生命周期方法，在被调用时也回调用被记录的实现了SmartInitializingSingleton 接口的那些对象的方法 afterSingletonsInstantiated()
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.SmartInitializingSingleton#afterSingletonsInstantiated()
	 */
	@Override
	public void afterSingletonsInstantiated() {
		for (SmartInitializingSingleton singleton : smartSingletons) {
			singleton.afterSingletonsInstantiated();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() {
		for (DisposableBean disposable : this.disposableBeans) {
			try {
				disposable.destroy();
			}
			catch (Exception error) {
				this.logger.error(error);
			}
		}
	}

}
