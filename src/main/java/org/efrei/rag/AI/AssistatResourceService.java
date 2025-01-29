package org.efrei.rag.AI;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.UUID;

@AiService
public interface AssistatResourceService {

    @SystemMessage(fromResource = "/prompts/system.st")
    String chat(String UserMessage);



    @SystemMessage(fromResource = "/prompts/system.st")
    TokenStream chat2(@MemoryId UUID memoryId, @UserMessage String userMessage);

}
