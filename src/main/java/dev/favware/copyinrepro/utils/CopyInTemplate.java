package dev.favware.copyinrepro.utils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Wrapped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

/**
 * Helper component for inserting data in batch using COPY IN.
 */
@Slf4j
@RequiredArgsConstructor
public class CopyInTemplate {

	private static final String DELIMITER = "|";

	private static final String CSV_DELIMITER = ";";

	private static final String ROW_DELIMITER = "\n";

	private static final String NULL_VALUE = "<<null>>";

	private static final String ESCAPE = "\\";

	private static final Joiner COLUMN_JOINER = Joiner.on(",");

	private final ConnectionFactory connectionFactory;

	/**
	 * Copy data into the columns of the given table.
	 *
	 * <p>
	 * For timestamps only {@link ZonedDateTime} and {@link Instant} are supported, representing the time in UTC.
	 * Before insert these timestamps are converted to ISO-8601 format using
	 * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} and {@link DateTimeFormatter#ISO_INSTANT} respectively
	 * </p>
	 *
	 * @param tableName Name of the table to copy data into.
	 * @param columnNames Names of the columns to copy data into.
	 * @param rows Individual rows that can be written/staged, representing the values for the supplied columns.
	 * @return The number of rows copied wrapped in a {@link Mono}.
	 */
	public Mono<Long> copyIn(final String tableName, final List<String> columnNames, final Flux<List<?>> rows) {
		final Flux<String> frameStrings = rows.map(this::rowToString);
		final Flux<ByteBuf> frames = frameStrings.doOnNext(row -> log.info("copyIn :: row={}", row)).map(row -> byteArrayToByteBuf(row.getBytes(UTF_8)));

		return Mono.usingWhen(
						connectionFactory.create(),
						c -> ((Wrapped<PostgresqlConnection>) c)
								.unwrap()
								.copyIn(copyInSql(tableName, columnNames))
								.fromMany(frames.map(Mono::just))
								.build(),
						Connection::close
				)
				.switchIfEmpty(Mono.error(new RuntimeException("No connection found")));
	}

	/**
	 * Copy a CSV into the greenplum database.
	 *
	 * <p>
	 * For timestamps only {@link ZonedDateTime} and {@link Instant} are supported, representing the time in UTC.
	 * Before insert these timestamps are converted to ISO-8601 format using
	 * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} and {@link DateTimeFormatter#ISO_INSTANT} respectively
	 * </p>
	 *
	 * @param tableName Name of the table to copy data into.
	 * @param columnNames Names of the columns to copy data into.
	 * @param dataBuffers The CSV data buffer that contains the data to be copied.
	 * @return The number of rows copied wrapped in a {@link Mono}.
	 */
	public Mono<Long> copyInCsv(final String tableName, final List<String> columnNames, final Flux<DataBuffer> dataBuffers) {
		final Flux<ByteBuf> byteBuffers = dataBuffers.map(this::dataBufferToByteBuf);

		return Mono.usingWhen(
						connectionFactory.create(),
						c -> ((Wrapped<PostgresqlConnection>) c)
								.unwrap()
								.copyIn(copyInCsvSql(tableName, columnNames))
								.fromMany(byteBuffers.map(Mono::just))
								.build(),
						Connection::close
				)
				.switchIfEmpty(Mono.error(new RuntimeException("No connection found")));
	}

	@VisibleForTesting
	String valueToString(Object value) {
		if (value instanceof Temporal || value instanceof Date) {
			return temporalToCopyValue(value);
		}

		if (value instanceof final String stringValue) {
			value = stringValueToCopyValue(stringValue);
		}

		return Objects.toString(value, NULL_VALUE);
	}

	/**
	 * Converts a Spring {@link DataBuffer} to a netty {@link ByteBuf}
	 *
	 * @param dataBuffer The {@link DataBuffer} to convert
	 * @return The converted {@link ByteBuf}
	 */
	private ByteBuf dataBufferToByteBuf(DataBuffer dataBuffer) {
		// First convert the DataBuffer to a ByteBuffer
		ByteBuffer destinationByteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
		dataBuffer.toByteBuffer(destinationByteBuffer);

		// Then convert the ByteBuffer to a ByteBuf
		ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(dataBuffer.readableByteCount());
		byteBuf.writeBytes(destinationByteBuffer);
		return byteBuf;
	}

	/**
	 * Converts a native {@link byte[]} to a {@link ByteBuf}.
	 *
	 * @param byteArray The {@link byte[]} to convert
	 * @return The converted {@link ByteBuf}
	 */
	private ByteBuf byteArrayToByteBuf(byte[] byteArray) {
		ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
		ByteBuf byteBuf = allocator.buffer(byteArray.length);
		byteBuf.writeBytes(byteArray);
		return byteBuf;
	}

	private String rowToString(final List<?> rowValues) {
		final var row = rowValues.stream()
				.map(this::valueToString)
				.collect(joining(DELIMITER));

		log.trace("appendRow :: row={}", row);

		return row + ROW_DELIMITER;
	}

	private String temporalToCopyValue(final Object value) {
		if (value instanceof final ZonedDateTime zonedDateTime) {
			// We cannot use DateTimeFormatter.ISO_ZONED_DATE_TIME because it adds the timezone between
			// [square brackets] which is not supported by PostgreSQL
			final var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSXXX");
			return zonedDateTime.format(formatter);
		} else if (value instanceof final Instant instant) {
			return DateTimeFormatter.ISO_INSTANT.format(instant);
		} else {
			throw new IllegalArgumentException(
					"Only ZonedDateTime and Instant supported for dates/timestamps, was: " +
					value.getClass().getName());
		}
	}

	private String stringValueToCopyValue(final String value) {
		return value
				.replace(ESCAPE, ESCAPE + ESCAPE)// Escape escape chars (\'s)
				.replace("|", ESCAPE + "|") // Escape |'s
				.replace("\n", ESCAPE + "n"); // Escape enters
	}

	private String copyInSql(final String tableName, final List<String> columnNames) {
		return copyInCsvSql(tableName, columnNames, bld -> bld
				.append("FROM STDIN WITH")
				.append("  DELIMITER '" + DELIMITER + "'")
				.append("  NULL '" + NULL_VALUE + "'")
				.append("  ESCAPE '" + ESCAPE + "'")
		);
	}

	private String copyInCsvSql(final String tableName, final List<String> columnNames) {
		return copyInCsvSql(tableName, columnNames, bld -> bld
				.append("FROM STDIN WITH CSV HEADER")
				.append("  DELIMITER '" + CSV_DELIMITER + "'")
				.append("  ESCAPE '" + ESCAPE + "'")
		);
	}

	private String copyInCsvSql(final String tableName, final List<String> columnNames, final Consumer<StringBuilder> consumer) {
		final var bld = new StringBuilder();

		bld.append("COPY ")
				.append(tableName)
				.append(" (")
				.append(COLUMN_JOINER.join(columnNames))
				.append(") ");

		consumer.accept(bld);

		final var statement = bld.toString();
		log.debug("copyInSql :: statement={}", statement);
		return statement;
	}

}
