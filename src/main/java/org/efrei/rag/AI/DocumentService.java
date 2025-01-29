package org.efrei.rag.AI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.efrei.rag.entities.MyUser;
import org.efrei.rag.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private static final String SYSTEM_MESSAGE_PROMPT = """
    Assistant helps the Library company customers with support questions regarding terms of service, privacy policy, and questions about support requests.
    Be brief in your answers.
    Answer ONLY with the facts listed in the list of sources below.
    If there isn't enough information below, say you don't know.
    Do not generate answers that don't use the sources below.
    If asking a clarifying question to the user would help, ask the question.
    For tabular information return it as an html table.
    Do not return markdown format.
    If the question is not in English, answer in the language used in the question.
    """;


    private final UserRepository userRepository;

    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatLanguageModel;

    public DocumentService(UserRepository userRepository, InMemoryEmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel, ChatLanguageModel chatLanguageModel) {
        this.userRepository = userRepository;
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.chatLanguageModel = chatLanguageModel;
    }

    @EventListener
    public void loadDocumentDataToEmbeddingStoreOnStartup(ApplicationStartedEvent event) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<MyUser> userPage = userRepository.findAll(pageable);
        if (userPage.hasContent()) {
            String documentsAsJson = convertListToJson(userPage.getContent());

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(new DocumentByLineSplitter(1000, 200))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(new dev.langchain4j.data.document.Document(documentsAsJson));
        }
    }

    public String convertListToJson(List<MyUser> users) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convert List<Vet> to JSON string
            StringBuilder jsonArray = new StringBuilder();
            for (MyUser user : users) {
                String jsonElement = objectMapper.writeValueAsString(user);
                jsonArray.append(jsonElement).append("\n"); // For use of the
                // DocumentByLineSplitter
            }
            return jsonArray.toString();
        }
        catch (JsonProcessingException e) {
            log.error("Problems encountered when generating JSON from the documents list", e);
            return null;
        }
    }

    @SuppressWarnings("removal")
    public String chat(String request) {
        Embedding embeddedQuestion = embeddingModel.embed(request).content();
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(embeddedQuestion, 3);
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(SystemMessage.from(SYSTEM_MESSAGE_PROMPT));
        String userMessage = request + "\n\nSources:\n";
        for (EmbeddingMatch<TextSegment> textSegmentEmbeddingMatch : relevant) {
            userMessage += textSegmentEmbeddingMatch.embedded().text() + "\n";
        }
        chatMessages.add(UserMessage.from(userMessage));

        // Invoke the LLM
        log.info("### Invoke the LLM");
        Response<AiMessage> response = chatLanguageModel.generate(chatMessages);
        return response.content().text();
    }
}