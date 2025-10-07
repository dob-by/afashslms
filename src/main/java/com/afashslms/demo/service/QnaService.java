package com.afashslms.demo.service;

import com.afashslms.demo.domain.Qna;

import java.util.List;

public interface QnaService {
    List<Qna> findAll();
    Qna findById(Long id);
    Qna save(Qna qna);
    Qna update(Long id, Qna qna);
    void delete(Long id);
}
