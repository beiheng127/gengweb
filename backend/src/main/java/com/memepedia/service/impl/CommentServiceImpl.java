package com.memepedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.memepedia.entity.Comment;
import com.memepedia.mapper.CommentMapper;
import com.memepedia.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Override
    public Comment createComment(Long memeId, Long userId, String content) {
        if (memeId == null) {
            throw new IllegalArgumentException("梗 ID 不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户 ID 不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        Comment comment = new Comment();
        comment.setMemeId(memeId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setLikeCount(0);
        this.save(comment);
        return comment;
    }

    @Override
    public List<Comment> listByMemeId(Long memeId) {
        return this.baseMapper.selectCommentListWithUser(memeId);
    }
}
