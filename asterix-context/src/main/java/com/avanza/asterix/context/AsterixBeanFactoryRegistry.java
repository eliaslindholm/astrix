/*
 * Copyright 2014-2015 Avanza Bank AB
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
package com.avanza.asterix.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsterixBeanFactoryRegistry {
	
	private final ConcurrentMap<Class<?>, AsterixFactoryBean<?>> factoryByBeanType = new ConcurrentHashMap<>();
	private final Logger log = LoggerFactory.getLogger(getClass());

	@SuppressWarnings("unchecked")
	public <T> AsterixFactoryBean<T> getFactoryBean(Class<T> beanType) {
		AsterixFactoryBean<T> factoryBean = (AsterixFactoryBean<T>) this.factoryByBeanType.get(beanType);
		if (factoryBean == null) {
			throw new IllegalStateException(String.format("No providers found for beanType=%s",
				 		  beanType.getName())); 
		}
		return factoryBean;
	}
	
	public <T> void registerFactory(AsterixFactoryBean<T> factory) {
		Class<T> providedApi = factory.getBeanType();
		AsterixFactoryBean<?> duplicateFactory = factoryByBeanType.putIfAbsent(providedApi, factory);
		log.debug("Registering provider: api={} provider={}", providedApi.getName(), factory.getApiDescriptor().getName());
		if (duplicateFactory != null) {
			throw new IllegalStateException(String.format("Multiple providers discovered for api=%s. %s and %s",
												 		  providedApi.getName(), 
												 		  factory.getApiDescriptor().getName(), 
												 		  duplicateFactory.getApiDescriptor().getName()));
		}
	}

	public boolean hasBeanFactoryFor(Class<?> beanType) {
		return this.factoryByBeanType.containsKey(beanType);
	}

}