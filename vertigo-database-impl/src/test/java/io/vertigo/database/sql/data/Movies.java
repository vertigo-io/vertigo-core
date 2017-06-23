package io.vertigo.database.sql.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Assert;

import io.vertigo.lang.DataStream;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ListBuilder;

public final class Movies {
	public static final String TITLE_MOVIE_1 = "citizen kane"; //1 May 1941, ?
	public static final String TITLE_MOVIE_2 = "vertigo"; //9 May 1958
	public static final String TITLE_MOVIE_3 = "gone girl";
	public static final String TITLE_MOVIE_4 = "Jurassic Park";

	private Movies() {
		//nothing
	}

	public static Movie createMovie(
			final long id,
			final String title,
			final Double fps,
			final Double income,
			final Boolean color,
			final Date date,
			final LocalDate localDate) {
		final Movie movie = new Movie();
		movie.setId(id);
		movie.setTitle(title);
		movie.setReleaseDate(date);
		movie.setReleaseLocalDate(localDate);
		movie.setReleaseZonedDateTime(ZonedDateTime.of(localDate, LocalTime.of(16, 30), ZoneId.of("UTC")));
		movie.setFps(fps);
		movie.setIncome(new BigDecimal(income));
		movie.setColor(color);
		movie.setIcon(buildIcon());
		return movie;
	}

	public static DataStream buildIcon() {
		return new DataStream() {

			@Override
			public long getLength() {
				try {
					return new File(Movie.class.getResource("icons.png").toURI()).length();
				} catch (final URISyntaxException e) {
					throw WrappedException.wrap(e);
				}
			}

			@Override
			public InputStream createInputStream() throws IOException {
				return Movie.class.getResourceAsStream("icons.png");
			}
		};
	}

	public static void checkMovie(final Movie movie) {
		switch (movie.getId().intValue()) {
			case 1:
				Assert.assertEquals(TITLE_MOVIE_1, movie.getTitle());
				//java.util.Date
				Assert.assertEquals("01-05-1941 16:30", new SimpleDateFormat("dd-MM-YYYY HH:mm").format(movie.getReleaseDate()));
				//LocalDate
				Assert.assertEquals(1, movie.getReleaseLocalDate().getDayOfMonth());
				Assert.assertEquals(5, movie.getReleaseLocalDate().getMonthValue());
				Assert.assertEquals(1941, movie.getReleaseLocalDate().getYear());
				//ZonedDateTime
				Assert.assertEquals("UTC", movie.getReleaseZonedDateTime().getZone().getId());
				Assert.assertEquals(1, movie.getReleaseZonedDateTime().getDayOfMonth());
				Assert.assertEquals(5, movie.getReleaseZonedDateTime().getMonthValue());
				Assert.assertEquals(1941, movie.getReleaseZonedDateTime().getYear());
				Assert.assertEquals(16, movie.getReleaseZonedDateTime().getHour());
				Assert.assertEquals(30, movie.getReleaseZonedDateTime().getMinute());
				Assert.assertEquals(0, movie.getReleaseZonedDateTime().getSecond());

				break;
			case 2:
				Assert.assertEquals(TITLE_MOVIE_2, movie.getTitle());
				//java.util.Date
				Assert.assertEquals("09-05-1958 16:30", new SimpleDateFormat("dd-MM-YYYY HH:mm").format(movie.getReleaseDate()));
				//LocalDate
				Assert.assertEquals(9, movie.getReleaseLocalDate().getDayOfMonth());
				Assert.assertEquals(5, movie.getReleaseLocalDate().getMonthValue());
				Assert.assertEquals(1958, movie.getReleaseLocalDate().getYear());
				//ZonedDateTime
				Assert.assertEquals("UTC", movie.getReleaseZonedDateTime().getZone().getId());
				Assert.assertEquals(9, movie.getReleaseZonedDateTime().getDayOfMonth());
				Assert.assertEquals(5, movie.getReleaseZonedDateTime().getMonthValue());
				Assert.assertEquals(1958, movie.getReleaseZonedDateTime().getYear());
				Assert.assertEquals(16, movie.getReleaseZonedDateTime().getHour());
				Assert.assertEquals(30, movie.getReleaseZonedDateTime().getMinute());
				Assert.assertEquals(0, movie.getReleaseZonedDateTime().getSecond());

				break;
			case 3:
				Assert.assertEquals(TITLE_MOVIE_3, movie.getTitle());
				//java.util.Date
				Assert.assertEquals(null, movie.getReleaseDate());
				//LocalDate
				Assert.assertEquals(null, movie.getReleaseLocalDate());
				//ZonedDateTime
				Assert.assertEquals(null, movie.getReleaseZonedDateTime());
				break;
			default:
				Assert.fail();
		}
	}

	private static Movie createMovie(
			final long id,
			final String title,
			final Double fps,
			final Double income,
			final Boolean color,
			final Integer year,
			final Integer month,
			final Integer dayOfMonth) {

		final Calendar calendar = new GregorianCalendar();
		calendar.set(year, month, dayOfMonth);

		final LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
		return Movies.createMovie(id, title, fps, income, color, calendar.getTime(), localDate);
	}

	public static final List<Movie> bondMovies() {
		return new ListBuilder<Movie>()
				.add(createMovie(1, "Doctor No", 23.976, 59.600, true, 1962, 10, 5))
				.add(createMovie(2, "From Russia with Love", 23.976, 78.900, true, 1963, 10, 10))
				.add(createMovie(3, "Goldfinger", 23.976, 124.900, true, 1964, 9, 17))
				.add(createMovie(4, "Thunderball", 23.976, 141.200, true, 1965, 12, 17))
				.add(createMovie(5, "You only live twice", 23.976, 111.600, true, 1967, 6, 12))
				.add(createMovie(6, "On her majesty's secret service", 23.976, 87.400, true, 1969, 12, 18))
				.add(createMovie(7, "Diamonds are forever", 23.976, 116.000, true, 1971, 12, 14))
				.add(createMovie(8, "Live and let die", 23.976, 161.800, true, 1973, 6, 27))
				.add(createMovie(9, "The man with the golden gun", 23.976, 97.600, true, 1974, 12, 19))
				.add(createMovie(10, "The spy who loved me", 23.976, 185.400, true, 1977, 7, 7))
				.add(createMovie(11, "Moonraker", 23.976, 210.300, true, 1979, 6, 26))
				.add(createMovie(12, "For your eyes only", 23.976, 195.300, true, 1981, 6, 24))
				.add(createMovie(13, "Octopussy", 23.976, 187.500, true, 1983, 6, 6))
				.add(createMovie(14, "A view to a kill", 23.976, 152.400, true, 1985, 5, 22))
				.add(createMovie(15, "The living daylights", 23.976, 191.200, true, 1987, 6, 27))
				.build();
	}
}