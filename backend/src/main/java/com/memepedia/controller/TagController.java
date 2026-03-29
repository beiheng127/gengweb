package com.memepedia.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.memepedia.common.Result;
import com.memepedia.entity.Tag;
import com.memepedia.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
@CrossOrigin
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/list")
    public Result<List<Tag>> list(@RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Tag::getType, type);
        }
        wrapper.orderByDesc(Tag::getUseCount);
        return Result.success(tagService.list(wrapper));
    }

    @PostMapping
    public Result<Tag> create(@RequestBody Tag tag) {
        tag.setUseCount(0);
        tagService.save(tag);
        return Result.success(tag);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return Result.success();
    }
}
