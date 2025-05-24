package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Notice;
import com.afashslms.demo.repository.NoticeRepository;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        Page<Notice> noticePage = noticeService.searchNotices(keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        model.addAttribute("notices", noticePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", noticePage.getTotalPages());
        model.addAttribute("totalItems", noticePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("keyword", keyword); // 💡 검색어 유지용

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
    public String createNotice(@ModelAttribute Notice notice,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam("file") MultipartFile file) throws IOException {

        notice.setAuthor(userDetails.getUser());
        notice.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        if (!file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String storedFileName = uuid + "_" + originalFileName;

            // 실제 저장 경로 (루트 기준)
            String uploadDir = System.getProperty("user.dir") + "/upload-dir/notices";
            Path uploadPath = Paths.get(uploadDir);

            // 경로 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 저장
            file.transferTo(uploadPath.resolve(storedFileName).toFile());

            // 엔티티에 파일 정보 저장
            notice.setOriginalFileName(originalFileName);
            notice.setStoredFileName(storedFileName);
        }

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
