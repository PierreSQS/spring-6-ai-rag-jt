package guru.springframework.springairag.services;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

/**
 * Spring AI-backed implementation of {@link OpenAIService}.
 * <p>
 * Spring AI's {@link ChatClient} abstracts the underlying model provider, so
 * switching from OpenAI to another LLM only requires changing the autoconfiguration
 * on the classpath — no code changes here.
 * <p>
 * {@link SimpleLoggerAdvisor} is registered as a default advisor so that every
 * request/response pair is logged at DEBUG level out-of-the-box.
 * <p>
 * Created by Pierrot, 2026-04-06.
 */
@Service
public class OpenAIServiceImpl implements OpenAIService {

    // ChatClient is the Spring AI fluent API for interacting with the underlying LLM
    private final ChatClient chatClient;

    /**
     * Constructs the service using Spring AI's autoconfigured {@link ChatClient.Builder}.
     * The {@link SimpleLoggerAdvisor} is attached so each prompt/response is logged automatically.
     *
     * @param chatClientBuilder autoconfigured builder provided by Spring AI
     */
    public OpenAIServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor()) // log every prompt & response at DEBUG
                .build();
    }

    /**
     * Builds a {@link Prompt} from the user's question text, sends it to the LLM,
     * and wraps the first result's text in an {@link Answer} record.
     *
     * @param question the user's question
     * @return the AI-generated answer
     */
    @Override
    public Answer getAnswer(Question question) {
        // Wrap the raw question string in a PromptTemplate so Spring AI can apply
        // variable substitution and formatting before sending it to the model
        PromptTemplate promptTemplate = new PromptTemplate(question.question());
        Prompt prompt = promptTemplate.create();

        // Call the model and extract the response
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

        assert response != null;

        // Pull the generated text from the first choice and return it as an Answer record
        return new Answer(response.getResult().getOutput().getText());
    }
}