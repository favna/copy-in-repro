package dev.favware.copyinrepro.services;

import dev.favware.copyinrepro.modals.Result;
import reactor.core.publisher.Mono;

public interface CopyInReproService {
	Mono<Result> store(String configuredId);
}
