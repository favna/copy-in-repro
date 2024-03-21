package dev.favware.copyinrepro.repositories;

import dev.favware.copyinrepro.modals.Result;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

@org.springframework.stereotype.Repository
public interface Repository extends R2dbcRepository<Result, Long>, CustomRepository {
}
