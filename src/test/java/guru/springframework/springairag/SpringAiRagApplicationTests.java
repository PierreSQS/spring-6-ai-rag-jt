package guru.springframework.springairag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.autoconfigure.openai.OpenAiEmbeddingProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringAiRagApplicationTests {

    @Test
    void contextLoads(ApplicationContext appCtx) {

        OpenAiEmbeddingProperties openAIEmbeddingProperties =
                appCtx.getBean(OpenAiEmbeddingProperties.class);

        String theModelInUse = openAIEmbeddingProperties.getOptions().getModel();

        System.out.println("Beans in context: " + appCtx.getBeanDefinitionCount());
        System.out.println("The model in use is: " + theModelInUse);

        assertThat(theModelInUse).isEqualTo("text-embedding-3-small");

    }

}
