package dev.favware.copyinrepro.modals;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;

@Value
@Builder
public class ReferenceEntity {
	@Id
	@With
	Long id;
	String configuredId;
}
