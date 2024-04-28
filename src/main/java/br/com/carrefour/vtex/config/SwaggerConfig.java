package br.com.carrefour.vtex.config;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;


@Configuration
@EnableSwagger2
//@Profile({"dev", "act", "hml", "prd"})
public class SwaggerConfig {

	@Value("${project.version}")
	private String version;

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false)
				.select()
				.apis(RequestHandlerSelectors.basePackage("br.com.carrefour.vtex"))
				.paths(PathSelectors.any())
				.build()
				.securityContexts(Lists.newArrayList(securityContext()))
				.securitySchemes(apiKey())
				.forCodeGeneration(true)
				.apiInfo(apiInfo());
//				.tags(new Tag("Microserviço 01", ""));
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("Carrefour - Catalagos")
				.description("API de acesso ao sistema VTEX.")
				.version(this.version)
				.license("Apache License Version 2.0")
				.licenseUrl("https://www.apache.com/licenses/LICENSE-2.0")
				.build();
	}

	@Bean
	SecurityContext securityContext() {
		return SecurityContext.builder()
				.securityReferences(defaultAuth())
//				.forPaths(PathSelectors.any())
				.build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Lists.newArrayList(
				new SecurityReference("JWT",  authorizationScopes));
	}

	private List<SecurityScheme> apiKey() {

		ApiKey key1 = new ApiKey("JWT", HttpHeaders.AUTHORIZATION, ApiKeyVehicle.HEADER.getValue());

		return Lists.newArrayList(key1);
	}
}
