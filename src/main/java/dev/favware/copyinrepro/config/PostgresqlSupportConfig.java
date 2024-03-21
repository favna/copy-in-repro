package dev.favware.copyinrepro.config;

import dev.favware.copyinrepro.utils.CopyInTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({ CopyInTemplate.class })
class PostgresqlSupportConfig {
}
