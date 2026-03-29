package com.memepedia.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.memepedia.common.Result;
import com.memepedia.entity.Meme;
import com.memepedia.service.MemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/meme")
@CrossOrigin
public class MemeController {

    @Autowired
    private MemeService memeService;

    @GetMapping("/list")
    public Result<IPage<Meme>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId) {
        Page<Meme> page = new Page<>(current, size);
        IPage<Meme> result = memeService.getMemePage(page, status, keyword, tagId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Meme> detail(@PathVariable Long id) {
        Meme meme = memeService.getMemeDetail(id);
        memeService.incrementViewCount(id);
        return Result.success(meme);
    }

    @PostMapping
    public Result<Meme> create(@RequestBody Meme meme) {
        meme.setViewCount(0);
        meme.setLikeCount(0);
        meme.setDislikeCount(0);
        meme.setCurrentVersion(1);
        meme.setStatus(0);
        memeService.save(meme);
        return Result.success(meme);
    }

    @PutMapping("/{id}")
    public Result<Meme> update(@PathVariable Long id, @RequestBody Meme meme) {
        meme.setId(id);
        memeService.updateById(meme);
        return Result.success(meme);
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        memeService.submitForAudit(id);
        return Result.success();
    }

    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer auditStatus = (Integer) params.get("status");
        String rejectReason = (String) params.get("rejectReason");
        Long auditorId = (Long) params.get("auditorId");
        memeService.auditMeme(id, auditStatus, rejectReason, auditorId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        memeService.removeById(id);
        return Result.success();
    }

    /**
     * 点赞
     */
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        LambdaUpdateWrapper<Meme> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Meme::getId, id).setSql("like_count = like_count + 1");
        memeService.update(wrapper);
        return Result.success();
    }

    /**
     * 点踩
     */
    @PostMapping("/{id}/dislike")
    public Result<Void> dislike(@PathVariable Long id) {
        LambdaUpdateWrapper<Meme> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Meme::getId, id).setSql("dislike_count = dislike_count + 1");
        memeService.update(wrapper);
        return Result.success();
    }

    /**
     * 取消点赞
     */
    @PostMapping("/{id}/unlike")
    public Result<Void> unlike(@PathVariable Long id) {
        LambdaUpdateWrapper<Meme> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Meme::getId, id).setSql("like_count = GREATEST(like_count - 1, 0)");
        memeService.update(wrapper);
        return Result.success();
    }

    /**
     * 取消点踩
     */
    @PostMapping("/{id}/undislike")
    public Result<Void> undislike(@PathVariable Long id) {
        LambdaUpdateWrapper<Meme> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Meme::getId, id).setSql("dislike_count = GREATEST(dislike_count - 1, 0)");
        memeService.update(wrapper);
        return Result.success();
    }
}
