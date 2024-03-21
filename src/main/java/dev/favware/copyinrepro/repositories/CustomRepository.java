package dev.favware.copyinrepro.repositories;

import reactor.core.publisher.Mono;

public interface CustomRepository {
	/**
	 * Store the given query result in the configured table
	 * @param id The id of the row to store it at
	 * @return The amount of changed rows
	 */
	Mono<Long> store(long id);
}
