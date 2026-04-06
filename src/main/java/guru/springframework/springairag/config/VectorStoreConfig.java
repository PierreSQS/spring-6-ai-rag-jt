package guru.springframework.springairag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Modified by Pierrot, 2026-04-06.
 */
@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel, VectorStoreProperties vectorStoreProperties) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // Resolve the target file for persisting / restoring the vector store (JSON format)
        File vectorStoreFile = new File(vectorStoreProperties.getVectorStorePath());

        if (vectorStoreFile.exists()) {
            // JSON snapshot already on disk — deserialize it directly to avoid re-indexing on every startup
            log.debug("Restoring vector store from existing file: {}", vectorStoreFile.getAbsolutePath());
            simpleVectorStore.load(vectorStoreFile);
        } else {
            // No snapshot found — build the vector store from the configured source documents
            log.debug("Loading documents into vector store");

            // Read every resource listed under sfg.aiapp.documentsToLoad via Apache Tika
            // (supports PDF, DOCX, CSV, HTML, and more)
            List<Document> documents = new ArrayList<>();
            for (Resource resource : vectorStoreProperties.getDocumentsToLoad()) {
                log.debug("Processing document: {}", resource.getFilename());
                TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
                documents.addAll(tikaDocumentReader.get());
            }

            // Split the raw documents into smaller token-based chunks for better embedding quality
            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
            List<Document> chunks = tokenTextSplitter.apply(documents);

            // Embed the chunks and populate the in-memory vector store
            simpleVectorStore.add(chunks);

            // Serialize the populated vector store to JSON so subsequent startups can skip re-indexing
            log.debug("Saving vector store to file: {}", vectorStoreFile.getAbsolutePath());
            simpleVectorStore.save(vectorStoreFile);
        }

        return simpleVectorStore;
    }
}
