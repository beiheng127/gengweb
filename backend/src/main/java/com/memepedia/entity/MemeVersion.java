package com.memepedia.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("meme_version")
public class MemeVersion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memeId;

    private Integer version;

    private String title;

    private String content;

    private String mediaUrl;

    private String audioUrl;

    private Integer auditStatus;

    private Long auditUserId;

    private String rejectReason;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
