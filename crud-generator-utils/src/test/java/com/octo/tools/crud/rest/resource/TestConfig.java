package com.octo.tools.crud.rest.resource;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.hazelcast.config.Config;
import com.octo.tools.crud.rest.resource.repository.MockRestResourceMapperService;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.octo.tools.crud.rest.resource.repository")
@EntityScan({"com.octo.tools.crud.rest.resource.model"})	
@ComponentScan(basePackages = {"com.octo.tools.crud.admin", "com.octo.tools.crud.doc", 
	"com.octo.tools.audit", "com.octo.tools.crud.filter",
	"com.octo.tools.crud.cache",
	"com.octo.tools.crud.rest.resource"}, 
	excludeFilters =  @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=RestResourceMapperService.class)
)
public class TestConfig {

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	RestResourceMapperService restMapper() {
		return new MockRestResourceMapperService();
	}
	
	@Bean
    public Config hazelCastConfig() {
		return new Config();
	}
}
