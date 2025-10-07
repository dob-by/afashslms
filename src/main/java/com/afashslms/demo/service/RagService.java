package com.afashslms.demo.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

@Service
public class RagService {
    private final WebClient webClient;

    public RagService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://127.0.0.1:8000").build();
    }

    public String askRag(String question) {
        return webClient.post()
                .uri("/ask")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(new Question(question))
                .retrieve()
                .bodyToMono(Answer.class)
                .map(Answer::getAnswer)
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn("RAG 서버 응답 지연 또는 연결 실패")
                .block();
    }

    static class Question {
        private String question;
    
        public Question() {} // 기본 생성자 추가
        public Question(String question) { this.question = question; }
    
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }
    
    static class Answer {
        private String answer;
    
        public Answer() {} // 기본 생성자 추가
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    
}