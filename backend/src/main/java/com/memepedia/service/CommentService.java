package com.memepedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.memepedia.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    Comment createComment(Long memeId, Long userId, String content);

    List<Comment> listByMemeId(Long memeId);
}
