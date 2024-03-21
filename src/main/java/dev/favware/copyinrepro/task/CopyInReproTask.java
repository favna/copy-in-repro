package dev.favware.copyinrepro.task;

import com.google.common.base.Stopwatch;
import dev.favware.copyinrepro.services.CopyInReproService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyInReproTask implements CommandLineRunner {

	private final CopyInReproService copyInReproService;

	@Value("${configuredId}")
	private final String configuredId;

	@Override
	public void run(final String... args) {
		var stopwatch = Stopwatch.createStarted();

		log.info("run :: Running task");

		executeService(configuredId);

		log.info("run :: Completed task in {} millis.", stopwatch.elapsed(MILLISECONDS));
	}

	private void executeService(String configuredId) {
		log.info("executeService :: Executing service");

		Mono.empty()
				.then(copyInReproService.store(configuredId))
				.doOnNext(results -> log.info("executeService :: Stored data: {}", results))
				.block();
	}
}
