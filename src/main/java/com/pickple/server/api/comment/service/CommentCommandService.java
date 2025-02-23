package com.pickple.server.api.comment.service;

import com.pickple.server.api.comment.domain.Comment;
import com.pickple.server.api.comment.dto.request.CommentCreateRequest;
import com.pickple.server.api.comment.repository.CommentRepository;
import com.pickple.server.api.notice.domain.Notice;
import com.pickple.server.api.notice.repository.NoticeRepository;
import com.pickple.server.api.user.domain.User;
import com.pickple.server.api.user.repository.UserRepository;
import com.pickple.server.global.exception.CustomException;
import com.pickple.server.global.response.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public void createComment(Long userId,
                              Long noticeId,
                              CommentCreateRequest commentCreateRequest) {
        User user = userRepository.findUserByIdOrThrow(userId);
        Notice notice = noticeRepository.findNoticeByIdOrThrow(noticeId);

        Comment comment = Comment.builder()
                .notice(notice)
                .commenter(user)
                .commentContent(commentCreateRequest.commentContent())
                .build();

        commentRepository.save(comment);
    }

    public void deleteComment(Long userId, Long noticeId, Long commentId) {
        Notice notice = noticeRepository.findNoticeByIdOrThrow(noticeId);
        Comment comment = commentRepository.findCommentByIdOrThrow(commentId);

        //댓글 작성자가 사용자 본인인 경우 or 사용자가 해당 모임의 호스트인 경우 삭제 가능
        if (comment.getCommenter().getId().equals(userId) || userId.equals(
                notice.getMoim().getHost().getUser().getId())) {
            commentRepository.delete(comment);
        } else {
            throw new CustomException(ErrorCode.NOT_AUTHOR);
        }
    }
}
