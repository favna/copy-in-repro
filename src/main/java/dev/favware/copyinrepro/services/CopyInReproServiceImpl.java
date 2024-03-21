package dev.favware.copyinrepro.services;

import dev.favware.copyinrepro.modals.ReferenceEntity;
import dev.favware.copyinrepro.modals.Result;
import dev.favware.copyinrepro.repositories.ReferenceRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static dev.favware.copyinrepro.repositories.common.Transactions.TRANSACTION_MANAGER;
import static reactor.core.publisher.Mono.defer;

@Slf4j
@Service
@RequiredArgsConstructor
public class CopyInReproServiceImpl implements CopyInReproService {
	private final CopyInReproResultManager copyInReproResultManager;
	private final ReferenceRepository referenceRepository;

	@Override
	@Transactional(TRANSACTION_MANAGER)
	public Mono<Result> store(@NonNull String configuredId) {
		log.info("store :: configuredId={}", configuredId);

		return referenceRepository.findBy(configuredId)
				.flatMap(existing -> Mono.error(new IllegalArgumentException(
						"Reference already exists: " + existing)))
				.then(defer(() -> create(configuredId)))
				.flatMap(this::extract);
	}

	private Mono<ReferenceEntity> create(String configuredId) {
		log.info("create :: configuredId={}", configuredId);
		var reference = ReferenceEntity.builder()
				.configuredId(configuredId)
				.build();

		return referenceRepository.save(reference);
	}

	private Mono<Result> extract(ReferenceEntity reference) {
		log.info("extract :: reference={}", reference);
		return copyInReproResultManager.storeResult(reference.getId(), reference.getConfiguredId());
	}
}
