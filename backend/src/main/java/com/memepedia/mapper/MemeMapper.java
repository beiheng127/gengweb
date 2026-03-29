package com.memepedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.memepedia.entity.Meme;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemeMapper extends BaseMapper<Meme> {
    IPage<Meme> selectMemePageWithUser(
            Page<Meme> page,
            @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("tagId") Long tagId
    );
}
