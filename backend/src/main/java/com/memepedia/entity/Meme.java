package com.memepedia.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("meme")
public class Meme {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private LocalDateTime originTime;

    private LocalDateTime boomTime;

    private String content;

    private Integer mediaType;

    private String mediaUrl;

    private String audioUrl;

    private Integer viewCount;

    private Integer likeCount;

    private Integer dislikeCount;

    private Integer currentVersion;

    private Integer status;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String createUserName;

    @TableField(exist = false)
    private java.util.List<Tag> tags;
}
