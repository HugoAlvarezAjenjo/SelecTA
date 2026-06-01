package es.hugoalvarezajenjo.selecta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI selectaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SelecTA API")
                        .description("API REST para la plataforma de selección de asignaturas optativas de la UPM. " +
                                "Permite gestionar ratings, recursos, votos, carpetas, tags y listas de matrícula.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hugo Alvarez Ajenjo")
                                .url("https://github.com/HugoAlvarezAjenjo/SelecTA")))
                .servers(List.of(
                        new Server().url("/").description("Local")
                ));
    }
}
