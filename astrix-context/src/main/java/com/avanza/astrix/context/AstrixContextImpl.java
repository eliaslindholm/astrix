/*
 * Copyright 2014 Avanza Bank AB
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
package com.avanza.astrix.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avanza.astrix.beans.core.AstrixBeanKey;
import com.avanza.astrix.beans.factory.AstrixBeanFactory;
import com.avanza.astrix.beans.factory.AstrixFactoryBeanRegistry;
import com.avanza.astrix.beans.factory.BeanConfiguration;
import com.avanza.astrix.beans.factory.BeanConfigurations;
import com.avanza.astrix.beans.factory.SimpleAstrixFactoryBeanRegistry;
import com.avanza.astrix.beans.factory.StandardFactoryBean;
import com.avanza.astrix.beans.inject.AstrixInjector;
import com.avanza.astrix.beans.publish.ApiProviderClass;
import com.avanza.astrix.beans.publish.ApiProviderPlugins;
import com.avanza.astrix.beans.publish.ApiProviders;
import com.avanza.astrix.beans.publish.PublishedBean;
import com.avanza.astrix.beans.service.StatefulAstrixBean;
import com.avanza.astrix.config.DynamicConfig;
/**
 * An AstrixContextImpl is the runtime-environment for the astrix-framework. It is used
 * both by consuming applications as well as server applications. AstrixContextImpl providers access
 * to different Astrix-plugins at runtime and is used as a factory to create Astrix-beans.
 * 
 * @author Elias Lindholm (elilin)
 */
final class AstrixContextImpl implements Astrix, AstrixApplicationContext {
	
	private static final Logger log = LoggerFactory.getLogger(AstrixContextImpl.class);
	
	private final SimpleAstrixFactoryBeanRegistry beanFactoryRegistry;
	private final ApiProviderPlugins apiProviderPlugins;
	private final AstrixBeanFactory beanFactory;
	private final DynamicConfig dynamicConfig;
	private final AstrixInjector astrixInjector;
	private final BeanConfigurations beanConfigurations;
	
	public AstrixContextImpl(DynamicConfig dynamicConfig, AstrixInjector injector, ApiProviderPlugins apiProviderPlugins) {
		this.dynamicConfig = dynamicConfig;
		this.astrixInjector = injector;
		this.apiProviderPlugins = apiProviderPlugins;
		this.beanFactory = this.astrixInjector.getBean(AstrixBeanFactory.class); // The bean-factory used for apis managed by astrix
		this.beanConfigurations = this.astrixInjector.getBean(BeanConfigurations.class); 
		this.beanFactoryRegistry = (SimpleAstrixFactoryBeanRegistry) this.astrixInjector.getBean(AstrixFactoryBeanRegistry.class);
		for (ApiProviderClass apiProvider : this.astrixInjector.getBean(ApiProviders.class).getAll()) {
			log.debug("Creating factory beans for apiProvider={}", apiProvider.getName());
			for (PublishedBean publishedBean: this.apiProviderPlugins.getProviderPlugin(apiProvider).createFactoryBeans(apiProvider)) {
				log.debug("Registering factory for bean. beanKey={} apiProvider={} factoryType={}",  
						publishedBean.getBeanKey(), 
						apiProvider.getName(), 
						publishedBean.getFactory().getClass().getName());
				this.beanFactoryRegistry.registerFactory(publishedBean.getFactory());
				this.beanConfigurations.setDefaultBeanConfig(publishedBean.getBeanKey(), publishedBean.getDefaultBeanSettingsOverride());
			}
		}
	}
	
	
	<T> void registerBeanFactory(StandardFactoryBean<T> beanFactory) {
		this.beanFactoryRegistry.registerFactory(beanFactory);
	}
	
	@Override
	public void destroy() {
		this.astrixInjector.destroy();
	}

	@Override
	public void close() throws Exception {
		destroy();
	}
	
	public BeanConfiguration getBeanConfiguration(AstrixBeanKey<?> beanKey) {
		return this.beanConfigurations.getBeanConfiguration(beanKey);
	}
	
	@Override
	public <T> T getBean(Class<T> beanType) {
		return getBean(beanType, null);
	}
	
	@Override
	public <T> T getBean(Class<T> beanType, String qualifier) {
		return beanFactory.getBean(AstrixBeanKey.create(beanType, qualifier));
	}
	
	public <T> T getBean(AstrixBeanKey<T> beanKey) {
		return beanFactory.getBean(beanKey);
	}
	

	@Override
	public <T> T waitForBean(Class<T> beanType, long timeoutMillis) throws InterruptedException {
		return waitForBean(beanType, null, timeoutMillis);
	}
	
	@Override
	public <T> T waitForBean(Class<T> beanType, String qualifier, long timeoutMillis) throws InterruptedException {
		AstrixBeanKey<T> beanKey = AstrixBeanKey.create(beanType, qualifier);
		T bean = beanFactory.getBean(beanKey);
		for (AstrixBeanKey<?> dependencyKey : beanFactory.getDependencies(beanKey)) {
			Object dependency = beanFactory.getBean(dependencyKey);
			waitForBeanToBeBound(dependency, timeoutMillis);
		}
		waitForBeanToBeBound(bean, timeoutMillis);
		return bean;
	}
	private void waitForBeanToBeBound(Object bean, long timeoutMillis) throws InterruptedException {
		if (bean instanceof StatefulAstrixBean) {
			StatefulAstrixBean.class.cast(bean).waitUntilBound(timeoutMillis);
		}
	}

	public <T> T getPlugin(Class<T> pluginType) {
		return this.astrixInjector.getBean(pluginType);
	}
	
	/**
	 * Returns an instance of an internal framework class.
	 * @param classType
	 * @return
	 */
	public final <T> T getInstance(final Class<T> classType) {
		return this.astrixInjector.getBean(classType);
	}
	
	@Override
	public DynamicConfig getConfig() {
		return dynamicConfig;
	}
	
}
