package guru.springframework.springairag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

/**
 * Created by Pierrot, on 27-02-2024.
 */
@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel, VectorStoreProperties vectorStoreProperties) {
        log.info("### the default embedding model is: {} ###", OpenAiApi.DEFAULT_EMBEDDING_MODEL);
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        File vectorStoreFile = new File(vectorStoreProperties.getVectorStorePath());

        if (vectorStoreFile.exists()) {
            simpleVectorStore.load(vectorStoreFile);
        } else {
            vectorStoreProperties.getDocumentsToLoad().forEach(document -> {
                log.info("Loading documents into vector store: {}", document.getFilename());
                TikaDocumentReader documentReader = new TikaDocumentReader(document);
                List<Document> documents = documentReader.get();
                TextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocuments = textSplitter.apply(documents);
                simpleVectorStore.add(splitDocuments);
            });
            simpleVectorStore.save(vectorStoreFile);
        }

        return simpleVectorStore;
    }
}
