package com.afashslms.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.afashslms.demo.service.RagService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/qna")
public class QnaController {

    private final RagService ragService;

    public QnaController(RagService ragService) {
        this.ragService = ragService;
    }
    // Q&A 메인 페이지 (챗봇 UI)
    @GetMapping({"", "/"})
    public String chatPage(Model model) {
        // layout.html에서 페이지별 스크립트를 주입하기 위한 경로
        model.addAttribute("pageScript", "/js/chat.js");
        return "qna/chat";
    }

    // 진단용 핑 엔드포인트
    @GetMapping("/ping")
    @ResponseBody
    public Map<String, String> ping() {
        Map<String, String> res = new HashMap<>();
        res.put("status", "ok");
        return res;
    }

    // 챗봇 답변 API
    @PostMapping("/chat")
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String question = request.get("question");

        // TODO: LangChain / OpenAI 호출 → 답변 가져오기
        String aiAnswer = "이건 임시 답변입니다: " + question;

        Map<String, String> response = new HashMap<>();
        response.put("answer", aiAnswer);
        return response;
    }

    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String askJson(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        return ragService.askRag(question);
    }

    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String askForm(@RequestParam("question") String question) {
        return ragService.askRag(question);
    }
}