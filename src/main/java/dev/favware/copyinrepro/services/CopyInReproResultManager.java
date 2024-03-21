package dev.favware.copyinrepro.services;

import dev.favware.copyinrepro.modals.Result;
import dev.favware.copyinrepro.repositories.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
class CopyInReproResultManager {
	private final Repository repository;

	Mono<Result> storeResult(long referenceId, String configuredId) {
		log.info("storeResult :: referenceId={}, configuredId={}", referenceId, configuredId);

		return Mono.empty()
				.then(repository.store(referenceId))
				.map(count -> Result.builder().count(count).build());
	}
}
