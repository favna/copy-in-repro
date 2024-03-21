package dev.favware.copyinrepro.repositories;

import com.google.common.collect.ImmutableList;
import dev.favware.copyinrepro.modals.Data;
import dev.favware.copyinrepro.modals.DataBinding;
import dev.favware.copyinrepro.modals.Query;
import dev.favware.copyinrepro.utils.CopyInTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CustomRepositoryImpl implements CustomRepository {
	private final CopyInTemplate copyInTemplate;

	@Override
	public Mono<Long> store(final long id) {
		log.debug("repository store :: id={}", id);

		var tableName = "public" + "." + "test";
		var query = Query.builder().columns(List.of("subject", "predicate", "object")).build();

		Flux<DataBinding> queryResults = Flux.just(
				createDataBinding("JohnDoe", "has", "name"),
				createDataBinding("JohnDoe", "isAge", "25", "double"),
				createDataBinding("JaneDoe", "has", "name"),
				createDataBinding("JaneDoe", "isAge", "20", "double"),
				createDataBinding("JohnSmith", "has", "name"),
				createDataBinding("JohnSmith", "isAge", "30", "double")
		);
		final Flux<List<?>> rows = queryResults.map(bindingSet -> toRow(bindingSet, query.getColumns()));

		return copyInTemplate.copyIn(
				tableName,
				columns(query),
				rows
		);
	}

	private List<String> columns(Query query) {
		return ImmutableList.<String>builder()
				.addAll(query.getColumns())
				.build();
	}

	private List<?> toRow(DataBinding dataBinding, List<String> columns) {
		var row = new ArrayList<>(columns.size() + 2);

		columns.forEach(name -> row.add(value(dataBinding, name)));

		return row;
	}

	private Object value(DataBinding dataBinding, String name) {
		var data = dataBinding.getBindings().get(name);

		if (data == null) {
			return null;
		}

		if ("double".equals(data.getDatatype())) {
			return Double.valueOf(data.getValue());
		}

		return data.getValue();
	}

	private DataBinding createDataBinding(String subject, String predicate, String object) {
		return createDataBinding(subject, predicate, object, "string");
	}

	private DataBinding createDataBinding(String subject, String predicate, String object, final String datatype) {
		Map<String, Data> hashMap = new HashMap<>();

		hashMap.put("subject", Data.builder().value(subject).datatype(datatype).build());
		hashMap.put("predicate", Data.builder().value(predicate).datatype(datatype).build());
		hashMap.put("object", Data.builder().value(object).datatype(datatype).build());

		return DataBinding.builder().bindings(hashMap).build();
	}
}
