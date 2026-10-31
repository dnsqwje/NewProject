package kr.routineslim.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import kr.routineslim.dto.PostDraftRequest;
import kr.routineslim.repository.UserDataRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityDraftService {

    private final JdbcTemplate db;
    private final UserDataRepository users;
    private final ObjectMapper json;

    public CommunityDraftService(
            JdbcTemplate db,
            UserDataRepository users,
            ObjectMapper json) {
        this.db = db;
        this.users = users;
        this.json = json;
    }

    public PostDraftRequest get(String user) {
        var values =
                db.queryForList(
                        "SELECT json_value FROM community_drafts WHERE user_id=?",
                        String.class,
                        user);

        if (values.isEmpty()) {
            return null;
        }

        try {
            return json.readValue(values.get(0), PostDraftRequest.class);
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Invalid stored draft", error);
        }
    }

    @Transactional
    public PostDraftRequest save(
            String user, PostDraftRequest value) {

        users.lockUser(user);

        final String payload;

        try {
            payload = json.writeValueAsString(value);
        } catch (java.io.IOException error) {
            throw new IllegalArgumentException("Invalid draft", error);
        }

        var now = Timestamp.from(Instant.now());

        int updated =
                db.update(
                        """
                        UPDATE community_drafts
                        SET json_value=?, updated_at=?
                        WHERE user_id=?
                        """,
                        payload,
                        now,
                        user);

        if (updated == 0) {
            db.update(
                    """
                    INSERT INTO community_drafts
                        (user_id, json_value, updated_at)
                    VALUES (?, ?, ?)
                    """,
                    user,
                    payload,
                    now);
        }

        return value;
    }

    @Transactional
    public void delete(String user) {
        db.update(
                "DELETE FROM community_drafts WHERE user_id=?",
                user);
    }
}