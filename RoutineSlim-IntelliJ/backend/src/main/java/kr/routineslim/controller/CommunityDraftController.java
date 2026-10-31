package kr.routineslim.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import kr.routineslim.dto.PostDraftRequest;
import kr.routineslim.security.SessionUser;
import kr.routineslim.service.CommunityDraftService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community/draft")
public class CommunityDraftController {

    private final CommunityDraftService drafts;

    public CommunityDraftController(
            CommunityDraftService drafts) {
        this.drafts = drafts;
    }

    @GetMapping
    public Map<String, Object> get(HttpSession session) {
        var draft =
                drafts.get(SessionUser.require(session));

        Map<String, Object> response = new HashMap<>();
        response.put("draft", draft);

        return response;
    }

    @PutMapping
    public PostDraftRequest save(
            @Valid @RequestBody PostDraftRequest value,
            HttpSession session) {

        return drafts.save(
                SessionUser.require(session), value);
    }

    @DeleteMapping
    public void delete(HttpSession session) {
        drafts.delete(SessionUser.require(session));
    }
}