package cn.itcast.wanxinp2p.consumer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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
                        .title("万信金融P2P平台-用户服务API文档")
                        .description("包含用户服务api")
                        .contact(contact)
                        .version("1.0.0"));
    }
}
