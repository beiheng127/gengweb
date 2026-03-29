package com.memepedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.memepedia.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    List<Comment> selectCommentListWithUser(@Param("memeId") Long memeId);
}
