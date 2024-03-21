package dev.favware.copyinrepro.modals;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Query {
	List<String> columns;
}
