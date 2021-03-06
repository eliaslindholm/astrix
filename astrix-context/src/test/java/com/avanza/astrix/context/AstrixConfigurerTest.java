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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.avanza.astrix.beans.core.AstrixBeanKey;
import com.avanza.astrix.beans.core.AstrixBeanSettings;
import com.avanza.astrix.beans.core.AstrixBeanSettings.BooleanBeanSetting;
import com.avanza.astrix.beans.core.AstrixBeanSettings.IntBeanSetting;
import com.avanza.astrix.beans.core.AstrixBeanSettings.LongBeanSetting;
import com.avanza.astrix.beans.factory.BeanConfiguration;
import com.avanza.astrix.beans.factory.BeanConfigurations;
import com.avanza.astrix.beans.publish.ApiProviderClass;
import com.avanza.astrix.beans.publish.ApiProviders;
import com.avanza.astrix.provider.core.AstrixApiProvider;
import com.avanza.astrix.provider.core.DefaultBeanSettings;
import com.avanza.astrix.provider.core.Service;

public class AstrixConfigurerTest {
	
	@Test
	public void passesBeanSettingsToConfiguration() throws Exception {
		AstrixConfigurer configurer = new AstrixConfigurer();
		configurer.setAstrixApiProviders(new ApiProviders() {
			@Override
			public Collection<ApiProviderClass> getAll() {
				return Collections.emptyList();
			}
		});
		IntBeanSetting intSetting = new IntBeanSetting("intSetting", 1);
		BooleanBeanSetting aBooleanSetting = new BooleanBeanSetting("booleanSetting", true);
		LongBeanSetting longSetting = new LongBeanSetting("longSetting", 2);
		
		configurer.set(aBooleanSetting, AstrixBeanKey.create(Ping.class), false);
		configurer.set(intSetting, AstrixBeanKey.create(Ping.class), 21);
		configurer.set(longSetting, AstrixBeanKey.create(Ping.class), 19);
		
		AstrixContextImpl astrixContext = (AstrixContextImpl) configurer.configure();
		BeanConfigurations beanConfigurations = astrixContext.getInstance(BeanConfigurations.class);
		BeanConfiguration pingConfig = beanConfigurations.getBeanConfiguration(AstrixBeanKey.create(Ping.class));
		
		assertEquals(21, pingConfig.get(intSetting).get());
		assertFalse(pingConfig.get(aBooleanSetting).get());
		assertEquals(19, pingConfig.get(longSetting).get());
	}
	
	@Test
	public void customDefaultBeanSettings() throws Exception {
		AstrixConfigurer configurer = new AstrixConfigurer();
		configurer.setAstrixApiProviders(new ApiProviders() {
			@Override
			public Collection<ApiProviderClass> getAll() {
				return Arrays.asList(ApiProviderClass.create(PingApiProvider.class));
			}
		});
		
		AstrixContextImpl astrixContext = (AstrixContextImpl) configurer.configure();
		BeanConfigurations beanConfigurations = astrixContext.getInstance(BeanConfigurations.class);
		BeanConfiguration pingConfig = beanConfigurations.getBeanConfiguration(AstrixBeanKey.create(Ping.class));

		assertEquals(2000, pingConfig.get(AstrixBeanSettings.INITIAL_TIMEOUT).get());
	}
	
	@Test
	public void customDefaultBeanSettingsAppliesToAsyncProxy() throws Exception {
		AstrixConfigurer configurer = new AstrixConfigurer();
		configurer.setAstrixApiProviders(new ApiProviders() {
			@Override
			public Collection<ApiProviderClass> getAll() {
				return Arrays.asList(ApiProviderClass.create(PingApiProvider.class));
			}
		});
		
		AstrixContextImpl astrixContext = (AstrixContextImpl) configurer.configure();
		BeanConfiguration pingConfig = astrixContext.getBeanConfiguration(AstrixBeanKey.create(PingAsync.class));

		assertEquals(2000, pingConfig.get(AstrixBeanSettings.INITIAL_TIMEOUT).get());
	}
	
	@DefaultBeanSettings(
		initialTimeout = 2000
	)
	public interface Ping {
	}
	
	public interface PingAsync {
	}
	
	@AstrixApiProvider
	public interface PingApiProvider {
		@Service
		Ping ping();
	}
	
	
}
