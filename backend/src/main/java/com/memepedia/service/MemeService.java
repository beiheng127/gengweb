package com.memepedia.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.memepedia.entity.Meme;

public interface MemeService extends IService<Meme> {
    IPage<Meme> getMemePage(Page<Meme> page, Integer status, String keyword, Long tagId);
    Meme getMemeDetail(Long id);
    void incrementViewCount(Long id);
    void submitForAudit(Long memeId);
    void auditMeme(Long memeId, Integer auditStatus, String rejectReason, Long auditorId);
}
