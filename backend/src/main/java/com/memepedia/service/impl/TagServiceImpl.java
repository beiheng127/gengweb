package com.memepedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.memepedia.entity.Tag;
import com.memepedia.mapper.TagMapper;
import com.memepedia.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}
