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
package se.avanzabank.asterix.integration.tests.domain2.pu;

import se.avanzabank.asterix.integration.tests.domain2.apiruntime.LunchGradeApiDescriptor;
import se.avanzabank.asterix.provider.component.AsterixServiceComponentNames;
import se.avanzabank.asterix.provider.core.AsterixService;


@AsterixService(
	apiDescriptors = {
		LunchGradeApiDescriptor.class
	},
	subsystem = "lunch-service",
	component = AsterixServiceComponentNames.GS_REMOTING
)
public class LunchGradeServiceDescriptor {
	
}


