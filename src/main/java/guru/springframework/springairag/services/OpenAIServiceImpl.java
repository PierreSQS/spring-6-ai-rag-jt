package guru.springframework.springairag.services;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * RAG (Retrieval-Augmented Generation) implementation of {@link OpenAIService}.
 * <p>
 * Uses Spring AI's {@link RetrievalAugmentationAdvisor} to handle the full RAG pipeline
 * declaratively. A custom {@link ContextualQueryAugmenter} is plugged in so that the
 * external template ({@code rag-prompt-template-meta.st}) controls how the retrieved
 * context and the user question are presented to the LLM. The template additionally
 * instructs the model to reformat movie metadata columns (budget, revenue, runtime,
 * credits) into a human-readable layout.
 * <p>
 * Created by Pierrot, 2026-04-06. Updated for custom prompt template, 2026-04-08.
 */
@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatClient chatClient;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    /**
     * Wires the {@link ChatClient} with a {@link SimpleLoggerAdvisor} for debug logging,
     * and builds a {@link RetrievalAugmentationAdvisor} backed by the application's
     * {@link VectorStore}, using a custom {@link ContextualQueryAugmenter} that injects
     * the external prompt template.
     *
     * @param chatClientBuilder  autoconfigured builder provided by Spring AI
     * @param vectorStore        in-memory vector store loaded with document embeddings at startup
     * @param templateResource   classpath resource for {@code rag-prompt-template-meta.st},
     *                           injected via {@code @Value}
     */
    public OpenAIServiceImpl(ChatClient.Builder chatClientBuilder,
                             VectorStore vectorStore,
                             @Value("classpath:templates/rag-prompt-template-meta.st") Resource templateResource) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        // Custom augmenter that replaces the advisor's default template with the external one.
        // ContextualQueryAugmenter fills {query} with the user question and {context} with
        // the retrieved document chunks — matching the placeholders in rag-prompt-template-meta.st.
        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .promptTemplate(new PromptTemplate(templateResource))
                .build();

        this.retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .build())
                // Plug in the custom augmenter so the advisor uses rag-prompt-template-meta.st
                .queryAugmenter(queryAugmenter)
                .build();
    }

    /**
     * Sends the user's question through the RAG pipeline: the {@link RetrievalAugmentationAdvisor}
     * retrieves relevant document chunks, the {@link ContextualQueryAugmenter} fills the custom
     * template, and the model returns a grounded, reformatted answer.
     *
     * @param question the user's question
     * @return the AI-generated answer grounded in the retrieved document content
     */
    @Override
    public Answer getAnswer(Question question) {
        return chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user(question.question())
                .call()
                .entity(Answer.class);
    }
}