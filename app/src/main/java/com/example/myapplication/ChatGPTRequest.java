package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class ChatGPTRequest {
    private String model;
    private List<Message> messages;

    public ChatGPTRequest(String userMessage) {
        this.model = "gpt-4";
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", userMessage));
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
