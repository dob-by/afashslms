package com.afashslms.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {
    @GetMapping("/board")
    public String board() {
        return "board"; // board.html로 연결
    }
}
