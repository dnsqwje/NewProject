package kr.routineslim.service;

import java.util.ArrayList;
import java.util.List;
import kr.routineslim.dto.CommentRequest;
import kr.routineslim.dto.PostRequest;
import kr.routineslim.entity.CommunityComment;
import kr.routineslim.entity.CommunityLike;
import kr.routineslim.entity.CommunityPost;
import kr.routineslim.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityPostService {

  private final NotificationService notifications;
  private final CommunityDraftService drafts;
  private final CommunityReportRepository reports;
  private final CommunityPostRepository posts;
  private final CommunityCommentRepository comments;
  private final CommunityLikeRepository likes;

  public CommunityPostService(
          CommunityPostRepository posts,
          CommunityCommentRepository comments,
          CommunityLikeRepository likes,
          CommunityReportRepository reports,
          NotificationService notifications,
          CommunityDraftService drafts) {

    this.posts = posts;
    this.comments = comments;
    this.likes = likes;
    this.reports = reports;
    this.notifications = notifications;
    this.drafts = drafts;
  }

  private void media(
          CommunityPost post, PostRequest request) {

    if (request.html() != null) {
      post.html = request.html();
    }

    if (request.images() != null) {
      post.images = new ArrayList<>(request.images());
    }

    if (request.tags() != null) {
      post.tags = new ArrayList<>(request.tags());
    }
  }

  public List<CommunityPost> list() {
    return posts.findAllByOrderByCreatedAtDesc();
  }

  public CommunityPost get(Long id) {
    return posts.findById(id)
            .orElseThrow(
                    () ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND));
  }

  @Transactional
  public int recordView(Long id) {
    if (posts.incrementViews(id) == 0) {
      throw new ResponseStatusException(
              HttpStatus.NOT_FOUND);
    }

    return get(id).viewCount;
  }

  @Transactional
  public CommunityPost create(
          String user, PostRequest request) {

    var post =
            new CommunityPost(
                    user,
                    request.title().trim(),
                    request.category().trim(),
                    request.content().trim());

    media(post, request);

    // 게시와 임시글 삭제를 같은 트랜잭션에서 처리합니다.
    drafts.delete(user);

    return posts.save(post);
  }

  @Transactional
  public CommunityPost update(
          String user,
          Long id,
          PostRequest request) {

    var post =
            posts.findByIdAndAuthorId(id, user)
                    .orElseThrow(
                            () ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND));

    post.title = request.title().trim();
    post.category = request.category().trim();

    if (!post.content.equals(request.content().trim())
            && request.html() == null) {
      post.html = null;
    }

    post.content = request.content().trim();

    media(post, request);

    return posts.save(post);
  }

  @Transactional
  public void delete(String user, Long id) {
    var post =
            posts.findByIdAndAuthorId(id, user)
                    .orElseThrow(
                            () ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND));

    comments.deleteByPostId(id);
    likes.deleteByPostId(id);
    reports.deleteByPostId(id);
    posts.delete(post);
  }

  public List<CommunityComment> comments(Long postId) {
    get(postId);

    return comments.findByPostIdOrderByCreatedAtAsc(postId);
  }

  @Transactional
  public CommunityComment comment(
          String user,
          Long postId,
          CommentRequest request) {

    var post = get(postId);

    var comment =
            comments.saveAndFlush(
                    new CommunityComment(
                            postId,
                            user,
                            request.content().trim()));

    if (!post.authorId.equals(user)) {
      notifications.publish(
              post.authorId,
              "comment:" + comment.id,
              "내 게시글에 댓글이 달렸어요.",
              post.title,
              "community",
              String.valueOf(postId));
    }

    return comment;
  }

  @Transactional
  public boolean toggleLike(
          String user, Long postId) {

    get(postId);

    if (likes.existsByPostIdAndUserId(postId, user)) {
      likes.deleteByPostIdAndUserId(postId, user);
      return false;
    }

    likes.save(new CommunityLike(postId, user));
    return true;
  }

  @Transactional
  public CommunityComment updateComment(
          String user,
          Long postId,
          Long commentId,
          CommentRequest request) {

    get(postId);

    var comment =
            comments.findById(commentId)
                    .filter(
                            value ->
                                    value.postId.equals(postId)
                                            && value.authorId.equals(user))
                    .orElseThrow(
                            () ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND));

    comment.content = request.content().trim();

    return comments.save(comment);
  }

  @Transactional
  public void deleteComment(
          String user,
          Long postId,
          Long commentId) {

    get(postId);

    var comment =
            comments.findById(commentId)
                    .filter(
                            value ->
                                    value.postId.equals(postId)
                                            && value.authorId.equals(user))
                    .orElseThrow(
                            () ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND));

    comments.delete(comment);
  }

  public boolean isLiked(
          String user, Long postId) {

    get(postId);

    return likes.existsByPostIdAndUserId(postId, user);
  }

  public long likeCount(Long postId) {
    get(postId);

    return likes.countByPostId(postId);
  }
}