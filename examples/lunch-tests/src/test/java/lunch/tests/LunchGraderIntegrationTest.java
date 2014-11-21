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
package lunch.tests;

import static org.junit.Assert.assertEquals;
import lunch.api.LunchRestaurant;
import lunch.api.LunchService;
import lunch.grader.api.LunchRestaurantGrader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import com.avanza.astrix.context.AstrixConfigurer;
import com.avanza.astrix.context.AstrixContext;
import com.avanza.astrix.context.AstrixSettings;
import com.avanza.astrix.gs.test.util.PuConfigurers;
import com.avanza.astrix.gs.test.util.RunningPu;
import com.avanza.astrix.service.registry.util.InMemoryServiceRegistry;

public class LunchGraderIntegrationTest {
	
	public static InMemoryServiceRegistry serviceRegistry = new InMemoryServiceRegistry() {{
		addConfig(AstrixSettings.BEAN_REBIND_ATTEMPT_INTERVAL, 200);
	}};
	
	@ClassRule
	public static RunningPu lunchGraderPu = PuConfigurers.partitionedPu("classpath:/META-INF/spring/lunch-grader-pu.xml")
			   											 .contextProperty("externalConfigUri", serviceRegistry.getExternalConfigUri())
			   											 .startAsync(false)
														 .configure();
	
	@Test
	public void testName() throws Exception {
		LunchService lunchMock = Mockito.mock(LunchService.class);
		Mockito.stub(lunchMock.getLunchRestaurant("mcdonalds")).toReturn(new LunchRestaurant("mcdonalds", "fastfood"));
		serviceRegistry.registerProvider(LunchService.class, lunchMock, "lunch-system");
		
		AstrixConfigurer configurer = new AstrixConfigurer();
		configurer.set(AstrixSettings.ASTRIX_SERVICE_REGISTRY_URI, serviceRegistry.getServiceUri());
		AstrixContext astrix = configurer.configure();
		LunchRestaurantGrader grader = astrix.waitForBean(LunchRestaurantGrader.class, 5_000);
		
		grader.grade("mcdonalds", 5);
		grader.grade("mcdonalds", 3);
		
		assertEquals(4D, grader.getAvarageGrade("mcdonalds"), 0.01D);
	}

}
