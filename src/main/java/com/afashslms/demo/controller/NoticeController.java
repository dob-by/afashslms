package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Notice;
import com.afashslms.demo.repository.NoticeRepository;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.Timestamp;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "notices/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('MID_ADMIN', 'TOP_ADMIN')")
    public String newNoticeForm(Model model) {
        model.addAttribute("notice", new Notice());
        return "notices/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MID_ADMIN', 'TOP_ADMIN')")
    public String createNotice(@ModelAttribute Notice notice, @AuthenticationPrincipal CustomUserDetails userDetails) {
        notice.setAuthor(userDetails.getUser());
        notice.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        noticeService.saveNotice(notice);
        return "redirect:/notices";
    }

    @GetMapping("/{id}")
    public String viewNotice(@PathVariable Long id, Model model, HttpServletRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지가 없습니다. id=" + id));
        model.addAttribute("notice", notice);

        // CSRF 토큰 직접 추가
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");

        return "notices/detail"; // -> templates/notices/detail.html
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('MID_ADMIN', 'TOP_ADMIN')")
    public String editNoticeForm(@PathVariable Long id, Model model) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지가 없습니다. id=" + id));
        model.addAttribute("notice", notice);
        return "notices/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('MID_ADMIN', 'TOP_ADMIN')")
    public String updateNotice(@PathVariable Long id, @ModelAttribute Notice updatedNotice) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지가 없습니다. id=" + id));

        notice.setTitle(updatedNotice.getTitle());
        notice.setContent(updatedNotice.getContent());
        noticeService.saveNotice(notice);  // 업데이트 시간 갱신 로직도 포함되면 좋음

        return "redirect:/notices/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('MID_ADMIN', 'TOP_ADMIN')")
    public String deleteNotice(@PathVariable Long id) {
        noticeRepository.deleteById(id);
        return "redirect:/notices";
    }

}
