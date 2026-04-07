package guru.springframework.springairag.services;

import guru.springframework.springairag.model.Answer;
import guru.springframework.springairag.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) implementation of {@link OpenAIService}.
 * <p>
 * Instead of forwarding the raw user question directly to the LLM, this service
 * first retrieves the most relevant document chunks from the {@link VectorStore}
 * and injects them into the structured RAG prompt template
 * ({@code rag-prompt-template.st}) before calling the model.
 * <p>
 * This approach grounds the model's answer in the actual content of the loaded
 * documents, reducing hallucinations and improving factual accuracy.
 * <p>
 * Spring AI's {@link ChatClient} abstracts the underlying model provider, so
 * switching from OpenAI to another LLM only requires changing the autoconfiguration
 * on the classpath — no code changes here.
 * <p>
 * Created by Pierrot, 2026-04-06. Updated for RAG, 2026-04-07.
 */
@Service
public class OpenAIServiceImpl implements OpenAIService {

    // ChatClient is the Spring AI fluent API for interacting with the underlying LLM
    private final ChatClient chatClient;

    // The in-memory vector store populated at startup from the configured source documents.
    // We query it at request time to find chunks semantically similar to the user's question.
    private final VectorStore vectorStore;

    // The StringTemplate (.st) resource that defines the RAG prompt structure.
    // It contains two placeholders: {input} for the user question and {documents}
    // for the retrieved context chunks.
    private final Resource ragPromptTemplateResource;

    /**
     * Constructs the service using Spring AI's autoconfigured {@link ChatClient.Builder},
     * the application-managed {@link VectorStore}, and the RAG prompt template resource.
     *
     * @param chatClientBuilder        autoconfigured builder provided by Spring AI
     * @param vectorStore              in-memory vector store loaded with document embeddings at startup
     * @param ragPromptTemplateResource the StringTemplate file that defines the RAG prompt structure
     */
    public OpenAIServiceImpl(ChatClient.Builder chatClientBuilder,
                             VectorStore vectorStore,
                             @Value("classpath:/templates/rag-prompt-template.st")
                             Resource ragPromptTemplateResource) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor()) // log every prompt & response at DEBUG
                .build();
        this.vectorStore = vectorStore;
        this.ragPromptTemplateResource = ragPromptTemplateResource;
    }

    /**
     * Implements the RAG pipeline:
     * <ol>
     *   <li>Retrieve the top-k document chunks from the {@link VectorStore} whose
     *       embeddings are most similar to the user's question.</li>
     *   <li>Concatenate the text content of those chunks into a single {@code documents}
     *       string (chunks separated by blank lines for readability).</li>
     *   <li>Load the RAG prompt template and substitute the {@code {input}} placeholder
     *       with the user's question and the {@code {documents}} placeholder with the
     *       retrieved context.</li>
     *   <li>Send the fully-rendered prompt to the LLM and return its answer.</li>
     * </ol>
     *
     * @param question the user's question
     * @return the AI-generated answer grounded in the retrieved document content
     */
    @Override
    public Answer getAnswer(Question question) {
        // Step 1 — Semantic retrieval: find document chunks most relevant to the user's question.
        // VectorStore.similaritySearch() embeds the query on-the-fly and returns the nearest chunks.
        List<Document> similarDocuments = vectorStore.similaritySearch(question.question());

        // Step 2 — Aggregate the text content of every retrieved chunk into a single string.
        // Each chunk's content is separated by a blank line to preserve readability for the LLM.
        String documents = similarDocuments.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        // Step 3 — Build the RAG prompt using the external StringTemplate resource.
        // The template defines two named placeholders:
        //   {input}     — the user's original question
        //   {documents} — the aggregated text retrieved from the vector store
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplateResource);
        Prompt prompt = promptTemplate.create(Map.of(
                "input", question.question(),   // populate {input} with the user's question
                "documents", documents          // populate {documents} with the retrieved context
        ));

        // Step 4 — Send the fully-rendered, context-enriched prompt to the LLM.
        // Spring AI maps the model's plain-text response directly into an Answer record.
        return chatClient
                .prompt(prompt)
                .call()
                .entity(Answer.class);
    }
}