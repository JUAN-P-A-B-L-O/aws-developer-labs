package com.example.awsdeveloperlabs.controller;

import com.example.awsdeveloperlabs.aws.RdsMessage;
import com.example.awsdeveloperlabs.aws.RdsMessageRequest;
import com.example.awsdeveloperlabs.aws.RdsMessageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aws/rds/messages")
public class RdsMessageController {

    private final RdsMessageService messageService;

    public RdsMessageController(RdsMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<RdsMessage> getMessages() {
        return messageService.getMessages();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createMessage(@Valid @RequestBody RdsMessageRequest request) {
        messageService.createMessage(request.message());
    }
}
