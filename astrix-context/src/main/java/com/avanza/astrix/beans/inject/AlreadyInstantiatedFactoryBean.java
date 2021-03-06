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
package com.avanza.astrix.beans.inject;

import com.avanza.astrix.beans.core.AstrixBeanKey;
import com.avanza.astrix.beans.factory.AstrixBeans;
import com.avanza.astrix.beans.factory.StandardFactoryBean;

class AlreadyInstantiatedFactoryBean<T> implements StandardFactoryBean<T> {

	private AstrixBeanKey<T> beanKey;
	private T instance;
	
	public AlreadyInstantiatedFactoryBean(AstrixBeanKey<T> beanKey, T instance) {
		this.beanKey = beanKey;
		this.instance = instance;
	}

	@Override
	public T create(AstrixBeans beans) {
		return instance;
	}

	@Override
	public AstrixBeanKey<T> getBeanKey() {
		return beanKey;
	}
	
}