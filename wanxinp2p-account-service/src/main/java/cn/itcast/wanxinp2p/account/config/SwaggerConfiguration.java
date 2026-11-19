package cn.itcast.wanxinp2p.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "swagger", value = {"enable"}, havingValue = "true")
public class SwaggerConfiguration {

    @Bean
    public OpenAPI buildOpenAPI() {
        Contact contact = new Contact();
        return new OpenAPI()
                .info(new Info()
                        .title("万信金融P2P平台-统一账户API文档")
                        .description("包含统一账户api")
                        .contact(contact)
                        .version("1.0.0"));
    }

    @Bean
    public GroupedOpenApi accountApi() {
        return GroupedOpenApi.builder()
                .group("account")
                .packagesToScan("cn.itcast.wanxinp2p.account.controller")
                .build();
    }
}
