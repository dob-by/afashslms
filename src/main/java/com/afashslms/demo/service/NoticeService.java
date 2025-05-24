package com.afashslms.demo.service;

import com.afashslms.demo.domain.Notice;
import com.afashslms.demo.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    public void saveNotice(Notice notice) {
        notice.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        noticeRepository.save(notice);
    }

    public Page<Notice> getNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    public Page<Notice> searchNotices(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return noticeRepository.findAll(pageable);
        }
        return noticeRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }
}
