package guru.springframework.springairag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.autoconfigure.openai.OpenAiEmbeddingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringAiRagApplicationTests {

    @Autowired
    OpenAiEmbeddingProperties openAiEmbeddingProperties;

    @Test
    void contextLoads() {

        displayLLMModels(openAiEmbeddingProperties);

    }

    private void displayLLMModels(OpenAiEmbeddingProperties openAiEmbeddingProperties) {

        String modelInUse = openAiEmbeddingProperties.getOptions().getModel();

        System.out.println("The model in use is: " + modelInUse);

        String defaultEmbeddingModel = OpenAiEmbeddingProperties.DEFAULT_EMBEDDING_MODEL;
        System.out.println("The default model is: " + defaultEmbeddingModel);

    }

}
