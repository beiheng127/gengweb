package com.memepedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.memepedia.entity.Meme;
import com.memepedia.mapper.MemeMapper;
import com.memepedia.service.MemeService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MemeServiceImpl extends ServiceImpl<MemeMapper, Meme> implements MemeService {

    @Override
    public IPage<Meme> getMemePage(Page<Meme> page, Integer status, String keyword, Long tagId) {
        return this.baseMapper.selectMemePageWithUser(page, status, keyword, tagId);
    }

    @Override
    public Meme getMemeDetail(Long id) {
        return this.getById(id);
    }

    @Override
    @Async
    public void incrementViewCount(Long id) {
        LambdaUpdateWrapper<Meme> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Meme::getId, id);
        wrapper.setSql("view_count = view_count + 1");
        this.update(wrapper);
    }

    @Override
    public void submitForAudit(Long memeId) {
        LambdaUpdateWrapper<Meme> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Meme::getId, memeId);
        wrapper.set(Meme::getStatus, 1);
        this.update(wrapper);
    }

    @Override
    public void auditMeme(Long memeId, Integer auditStatus, String rejectReason, Long auditorId) {
        LambdaUpdateWrapper<Meme> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Meme::getId, memeId);
        wrapper.set(Meme::getStatus, auditStatus);
        this.update(wrapper);
    }
}
