package guru.springframework.springairag.services;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * RAG (Retrieval-Augmented Generation) implementation of {@link OpenAIService}.
 * <p>
 * Uses Spring AI's {@link RetrievalAugmentationAdvisor} to handle the full RAG pipeline
 * declaratively: on each call it retrieves the most relevant document chunks from the
 * {@link VectorStore}, augments the user prompt with that context, and forwards the
 * enriched prompt to the LLM — all without manual template management.
 * <p>
 * Created by Pierrot, 2026-04-06. Updated for RetrievalAugmentationAdvisor, 2026-04-07.
 */
@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatClient chatClient;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    /**
     * Wires the {@link ChatClient} with a {@link SimpleLoggerAdvisor} for debug logging,
     * and builds a {@link RetrievalAugmentationAdvisor} backed by the application's
     * {@link VectorStore}.
     *
     * @param chatClientBuilder autoconfigured builder provided by Spring AI
     * @param vectorStore       in-memory vector store loaded with document embeddings at startup
     */
    public OpenAIServiceImpl(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .build())
                .build();
    }

    /**
     * Sends the user's question to the LLM through the RAG pipeline managed by
     * {@link RetrievalAugmentationAdvisor}: the advisor retrieves relevant document
     * chunks from the vector store, augments the prompt with that context, and the
     * model generates a grounded answer.
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