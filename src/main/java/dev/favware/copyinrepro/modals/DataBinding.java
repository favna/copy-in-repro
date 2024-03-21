package dev.favware.copyinrepro.modals;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class DataBinding {
	Map<String, Data> bindings;
}
