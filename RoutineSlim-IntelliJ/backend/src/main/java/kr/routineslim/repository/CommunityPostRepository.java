package kr.routineslim.repository;

import java.util.List;
import java.util.Optional;
import kr.routineslim.entity.CommunityPost;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository
        extends JpaRepository<CommunityPost, Long> {

  List<CommunityPost> findAllByOrderByCreatedAtDesc();

  Optional<CommunityPost> findByIdAndAuthorId(
          Long id, String authorId);

  List<CommunityPost> findByAuthorId(String authorId);

  @Modifying(
          clearAutomatically = true,
          flushAutomatically = true)
  @Query(
          """
          UPDATE CommunityPost p
          SET p.viewCount = p.viewCount + 1
          WHERE p.id = :id
          """)
  int incrementViews(@Param("id") Long id);
}