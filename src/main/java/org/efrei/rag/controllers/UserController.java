package org.efrei.rag.controllers;


import org.efrei.rag.AI.AssistatResourceService;
import org.efrei.rag.AI.DocumentService;
import org.efrei.rag.entities.MyUser;
import org.efrei.rag.repositories.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

@RestController
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    private final UserRepository userRepository;
    private final AssistatResourceService documentAiService;
    private final DocumentService documentService;

    public UserController(UserRepository userRepository, AssistatResourceService documentAiService, DocumentService documentService) {
        this.userRepository = userRepository;
        this.documentAiService = documentAiService;
        this.documentService = documentService;
    }

    @GetMapping("/return-text/{text}")
    public String returnText(@PathVariable String text) {
        return "you wrote : " + text;
    }

    @PostMapping("create-user")
    public String createUser(@RequestBody MyUser user) {
        userRepository.save(user);
        return "User created";
    }

    @GetMapping("get-user/{id}")
    public MyUser getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @GetMapping("/users")
    public List<MyUser> getUsers() {
        return userRepository.findAll();
    }


    @PostMapping("/documents/chat2/{user}")
    public String chat2(@RequestBody String query) throws InterruptedException {
        String result = documentService.chat(query);

        return result;
    }

    private static void sendMessage(SseEmitter emitter, String message) throws IOException {
        String token = message
                // Hack line break problem when using Server Sent Events (SSE)
                .replace("\n", "<br>")
                // Escape JSON quotes
                .replace("\"", "\\\"");
        emitter.send("{\"t\": \"" + token + "\"}");
    }

    @PostMapping("/documents/chat/{user}")
    public String chat(@PathVariable UUID user, @RequestBody String query) throws InterruptedException {
        SseEmitter emitter = new SseEmitter();
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicReference<String> myMessage = new AtomicReference<>();
        nonBlockingService.execute(() -> documentAiService.chat2(user, query)
                .onNext(message -> {
                    try {
                        sendMessage(emitter, message);
                        myMessage.set(message);
                    }
                    catch (IOException e) {
                        log.error("Error while writing next token", e);
                        emitter.completeWithError(e);
                    }
                })
                .onComplete(token -> {
                    emitter.complete();
                    completed.set(true);
                })
                .onError(error -> {
                    log.error("Unexpected chat error", error);
                    try {
                        sendMessage(emitter, error.getMessage());
                    }
                    catch (IOException e) {
                        log.error("Error while writing next token", e);
                    }
                    emitter.completeWithError(error);
                })
                .start());
        while (!completed.get()) {
            Thread.sleep(1000);
        }
        return myMessage.get();
    }
}
