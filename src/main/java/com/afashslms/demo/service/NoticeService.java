package com.afashslms.demo.service;

import com.afashslms.demo.domain.Notice;
import com.afashslms.demo.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
