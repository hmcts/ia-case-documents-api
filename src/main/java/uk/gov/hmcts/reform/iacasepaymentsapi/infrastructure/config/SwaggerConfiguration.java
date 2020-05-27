package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("ia-case-payments-api")
            .globalOperationParameters(getGlobalOperationParameters())
            .useDefaultResponseMessages(false)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
            .paths(PathSelectors.any())
            .build();
    }


    private List<Parameter> getGlobalOperationParameters() {
        return Arrays.asList(
            new ParameterBuilder()
                .name("Authorization")
                .description("User authorization header")
                .required(true)
                .parameterType("header")
                .modelRef(new ModelRef("string"))
                .build(),
            new ParameterBuilder()
                .name("ServiceAuthorization")
                .description("Service authorization header")
                .required(true)
                .parameterType("header")
                .modelRef(new ModelRef("string"))
                .build());
    }


}
