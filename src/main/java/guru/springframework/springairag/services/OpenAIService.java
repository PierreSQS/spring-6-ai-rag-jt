package guru.springframework.springairag.services;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;

/**
 * Created by Pierrot, on 27-02-2025.
 */
public interface OpenAIService {

    Answer getAnswer(Question question);
}
