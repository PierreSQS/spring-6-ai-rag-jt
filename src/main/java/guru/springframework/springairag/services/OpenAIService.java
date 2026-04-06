package guru.springframework.springairag.services;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;

/**
 * Service contract for sending questions to the OpenAI chat model and receiving answers.
 * Programming to the interface allows swapping the underlying AI provider without
 * touching the controller or any other caller.
 * <p>
 * Created by Pierrot, 2026-04-06.
 */
public interface OpenAIService {

    /**
     * Sends the given question to the AI model and returns the generated answer.
     *
     * @param question the user's question
     * @return the AI-generated answer
     */
    Answer getAnswer(Question question);
}