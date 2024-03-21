package dev.favware.copyinrepro.repositories;

import dev.favware.copyinrepro.modals.ReferenceEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReferenceRepository extends R2dbcRepository<ReferenceEntity, Long> {

	Mono<ReferenceEntity> findByConfiguredId(String configuredId);

	default Mono<ReferenceEntity> findBy(String configuredId) {
		return findByConfiguredId(configuredId);
	}
}
