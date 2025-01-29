package org.efrei.rag.AI;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AssistatResourceService {

    @SystemMessage(fromResource = "/prompts/system.st")
    String chat(String UserMessage);

}
