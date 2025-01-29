package org.efrei.rag.AI;

import dev.langchain4j.service.SystemMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssistantResourceController {

    private final AssistatResourceService assistatResourceService;

    public AssistantResourceController(AssistatResourceService assistatResourceService) {
        this.assistatResourceService = assistatResourceService;
    }

    @PostMapping("/assistant/chat")
    public String chat(@RequestBody String message) {
        return assistatResourceService.chat(message);
    }

}
