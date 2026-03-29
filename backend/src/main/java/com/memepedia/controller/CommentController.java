package com.memepedia.controller;

import cn.hutool.jwt.JWT;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.memepedia.common.Result;
import com.memepedia.entity.Comment;
import com.memepedia.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping
    public Result<Comment> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> params) {
        Long memeId = params.get("memeId") == null ? null : Long.valueOf(params.get("memeId").toString());
        String content = params.get("content") == null ? null : params.get("content").toString();

        // 从JWT中解析userId
        Long userId = parseUserIdFromToken(authorization);
        if (userId == null) {
            // 未登录时使用请求体中的userId（兼容旧逻辑），或返回错误
            Object userIdObj = params.get("userId");
            if (userIdObj != null) {
                userId = Long.valueOf(userIdObj.toString());
            } else {
                return Result.error(401, "请先登录再评论");
            }
        }

        Comment comment = commentService.createComment(memeId, userId, content);
        return Result.success(comment);
    }

    @GetMapping("/list/{memeId}")
    public Result<List<Comment>> list(@PathVariable Long memeId) {
        return Result.success(commentService.listByMemeId(memeId));
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        LambdaUpdateWrapper<Comment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Comment::getId, id);
        wrapper.setSql("like_count = like_count + 1");
        commentService.update(wrapper);
        return Result.success();
    }

    private Long parseUserIdFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authorization.substring(7);
            JWT jwt = JWT.of(token).setKey(jwtSecret.getBytes(StandardCharsets.UTF_8));
            if (jwt.verify()) {
                Object userId = jwt.getPayload("userId");
                if (userId != null) {
                    return Long.valueOf(userId.toString());
                }
            }
        } catch (Exception e) {
            // token解析失败
        }
        return null;
    }
}
