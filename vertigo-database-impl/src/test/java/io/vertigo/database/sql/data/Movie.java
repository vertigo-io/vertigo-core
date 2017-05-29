/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.database.sql.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * Movie.
 */
public final class Movie {
	private Long id;
	private String title;
	@Deprecated
	private Date releaseDate;
	private LocalDate releaseLocalDate;
	private Double fps;
	private BigDecimal income;
	private Boolean color;

	//	private LocalDateTime releaseLocalDateTime;
	//	private Mail mail;

	public final Long getId() {
		return id;
	}

	public final void setId(final Long id) {
		this.id = id;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(final String title) {
		this.title = title;
	}

	@Deprecated
	public Date getReleaseDate() {
		return releaseDate;
	}

	@Deprecated
	public void setReleaseDate(final Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public void setReleaseLocalDate(final LocalDate releaseLocalDate) {
		this.releaseLocalDate = releaseLocalDate;
	}

	public LocalDate getReleaseLocalDate() {
		return releaseLocalDate;
	}

	public Double getFps() {
		return fps;
	}

	public void setFps(final Double fps) {
		this.fps = fps;
	}

	public BigDecimal getIncome() {
		return income;
	}

	public void setIncome(final BigDecimal budget) {
		income = budget;
	}

	public Boolean getColor() {
		return color;
	}

	public void setColor(final Boolean color) {
		this.color = color;
	}

	//	public LocalDateTime getreleaseLocalDateTime() {
	//		return releaseLocalDateTime;
	//	}
	//
	//	public void setreleaseLocalDateTime(final LocalDateTime releaseLocalDateTime) {
	//		this.releaseLocalDateTime = releaseLocalDateTime;
	//	}
	//
	//	public void setMail(final Mail mail) {
	//		this.mail = mail;
	//	}
	//
	//	public Mail getMail() {
	//		return mail;
	//	}

}
