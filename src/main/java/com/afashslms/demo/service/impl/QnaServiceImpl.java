package com.afashslms.demo.service.impl;

import com.afashslms.demo.domain.Qna;
import com.afashslms.demo.repository.QnaRepository;
import com.afashslms.demo.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaServiceImpl implements QnaService {

    private final QnaRepository qnaRepository;

    @Override
    public List<Qna> findAll() {
        return qnaRepository.findAll();
    }

    @Override
    public Qna findById(Long id) {
        return qnaRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("QnA not found. id=" + id));
    }

    @Override
    public Qna save(Qna qna) {
        return qnaRepository.save(qna);
    }

    @Override
    public Qna update(Long id, Qna qna) {
        Qna existing = findById(id);
        existing.setTitle(qna.getTitle());
        existing.setContent(qna.getContent());
        existing.setAuthor(qna.getAuthor());
        return qnaRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        qnaRepository.deleteById(id);
    }
}