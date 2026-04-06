package guru.springframework.springairag.controllers;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import guru.springframework.springairag.services.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes a single endpoint for asking questions to the AI model.
 * <p>
 * {@link RequiredArgsConstructor} (Lombok) generates a constructor for the final
 * {@link OpenAIService} field, which Spring uses to inject the implementation at startup.
 * <p>
 * Created by Pierrot, 2026-04-06.
 */
@RequiredArgsConstructor
@RestController
public class QuestionController {

    // Injected via Lombok-generated constructor — decoupled from the concrete implementation
    private final OpenAIService openAIService;

    /**
     * Accepts a JSON body containing the user's question, forwards it to the AI service,
     * and returns the generated answer as JSON.
     *
     * <p>Example request body:
     * <pre>{ "question": "What movies did Tom Hanks star in?" }</pre>
     *
     * <p>Example response body:
     * <pre>{ "answer": "Tom Hanks starred in Forrest Gump, Cast Away, ..." }</pre>
     *
     * @param question deserialized from the POST request body
     * @return the AI-generated answer serialized as JSON
     */
    @PostMapping("/ask")
    public Answer askQuestion(@RequestBody Question question) {
        return openAIService.getAnswer(question);
    }
}