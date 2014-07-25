/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.studio.plugins.reporting.task.metrics.performance;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.kernel.lang.Assertion;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Classe de bouchon pour mettre des données fictives dans les requêtes.
 *
 * @author tchassagnette
 */
public final class TaskPopulator {
	private final TaskDefinition taskDefinition;
	private final TaskBuilder taskBuilder;

	/**
	 * Constructeur.
	 * @param taskDefinition Definition de la tache
	 */
	public TaskPopulator(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---------------------------------------------------------------------
		this.taskDefinition = taskDefinition;
		taskBuilder = new TaskBuilder(taskDefinition);
	}

	/**
	 * Rempli la task avec les données fictives.
	 * @return Tache préparé à l'exécution
	 */
	public Task populateTask() {
		for (final TaskAttribute attribute : taskDefinition.getAttributes()) {
			populateTaskAttribute(attribute);
		}
		return taskBuilder.build();
	}

	private void populateTaskAttribute(final TaskAttribute attribute) {
		if (attribute.isIn()) {
			final String attributeName = attribute.getName();
			Object value = null;
			switch (attribute.getDomain().getDataType()) {
				case Boolean:
					value = Boolean.TRUE;
					break;
				case String:
					value = "Test";
					break;
				case Date:
					value = new Date();
					break;
				case Double:
					value = Double.valueOf(1);
					break;
				case Integer:
					value = Integer.valueOf(1);
					break;
				case BigDecimal:
					value = BigDecimal.valueOf(1);
					break;
				case Long:
					value = Long.valueOf(1);
					break;
				case DtObject:
					value = DtObjectUtil.createDtObject(attribute.getDomain().getDtDefinition());
					break;
				case DtList:
					value = new DtList(attribute.getDomain().getDtDefinition());
					break;
				case DataStream:
				default:
					//we do nothing
					break;

			}
			taskBuilder.withValue(attributeName, value);

		}
	}
}
