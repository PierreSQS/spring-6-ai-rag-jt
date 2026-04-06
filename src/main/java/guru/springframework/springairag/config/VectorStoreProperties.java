package guru.springframework.springairag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * Modified by Pierrot, 2026-04-06.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sfg.aiapp")
public class VectorStoreProperties {

    private String vectorStorePath;

    // List of Spring Resources (classpath:, file:, etc.) to load and vectorize at startup
    private List<Resource> documentsToLoad;

}
