/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.task.x;

import java.util.List;
import java.util.Optional;

import io.vertigo.commons.transaction.Transactional;
import io.vertigo.core.component.Component;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.task.data.domain.SuperHero;
import io.vertigo.dynamo.task.proxy.TaskAnnotation;
import io.vertigo.dynamo.task.proxy.TaskInput;
import io.vertigo.dynamo.task.proxy.TaskOutput;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;

@Transactional
public interface SuperHeroDao extends Component {
	@TaskAnnotation(
			name = "TK_CAR_COUNT",
			request = "select count(*) from super_hero ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(domain = "DO_INTEGER")
	int count();

	@TaskAnnotation(
			name = "TK_SUPER_HERO_LOAD_BY_NAME",
			request = "<%if (name !=null) {%>select * from super_hero where name = #name# <%} else {%>"
					+ "select * from super_hero <%}%>",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(domain = "DO_DT_SUPER_HERO_DTC")
	DtList<SuperHero> findAll(
			@TaskInput(name = "name", domain = "DO_STRING") Optional<String> nameOpt);

	@TaskAnnotation(
			name = "TK_SUPER_HERO_COUNT_BY_NAME",
			request = "select count(*) from super_hero where name=#name# ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(domain = "DO_INTEGER")
	int count(
			@TaskInput(name = "name", domain = "DO_STRING") String manufacturer);

	@TaskAnnotation(
			name = "TK_LOAD_SUPER_HERO_NAMES",
			request = "select distinct name from super_hero ",
			taskEngineClass = TaskEngineSelect.class)
	@TaskOutput(domain = "DO_STRINGS")
	List<String> names();

	@TaskAnnotation(
			name = "TK_UPDATE_SUPER_HERO_NAMES",
			request = "update  super_hero set name =#newName# where name=#oldName#",
			taskEngineClass = TaskEngineProc.class)
	void update(
			@TaskInput(name = "oldName", domain = "DO_STRING") String oldName,
			@TaskInput(name = "newName", domain = "DO_STRING") String newName);

}
