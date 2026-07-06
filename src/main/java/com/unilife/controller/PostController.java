package com.unilife.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unilife.dto.Result;
import com.unilife.dto.UserDTO;
import com.unilife.entity.Post;
import com.unilife.entity.User;
import com.unilife.service.IPostService;
import com.unilife.service.IUserService;
import com.unilife.utils.SystemConstants;
import com.unilife.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 */
@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private IPostService postService;
    @Resource
    private IUserService userService;

    @PostMapping
    public Result savePost(@RequestBody Post post) {
       return postService.savePost(post);
    }

    @PutMapping("/like/{id}")
    public Result likePost(@PathVariable("id") Long id) {
        // 修改点赞数量
        return postService.updateLike(id);
    }

    @GetMapping("/of/me")
    public Result queryMyPost(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Post> page = postService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Post> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 分页查询
     * @param current
     * @return
     */
    @GetMapping("/hot")
    public Result queryHotPost(@RequestParam(value = "current", defaultValue = "1") Integer current) {
       return postService.queryHotPost(current);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result queryById(@PathVariable("id") Long id){
        return postService.queryPostById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryPostLikes(@PathVariable("id") Long id){
        return postService.queryPostLikes(id);
    }

    @GetMapping("/of/user")
    public Result queryPostByUserId(
            @RequestParam(value = "current",defaultValue = "1") Integer currrent,
            @RequestParam("id") Long id){
        //根据用户查询
        Page<Post> page = postService.query()
                .eq("user_id", id).page(new Page<>(currrent, SystemConstants.MAX_PAGE_SIZE));

        //获取当前页数据
        List<Post> records = page.getRecords();
        return Result.ok(records);
    }
    @GetMapping("/of/follow")
    public Result queryPostOfFollow(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset",defaultValue = "0") Integer offset){
        return postService.quertPostOfFollow(max,offset);

    }
}
