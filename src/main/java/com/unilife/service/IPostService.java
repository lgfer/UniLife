package com.unilife.service;

import com.unilife.dto.Result;
import com.unilife.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IPostService extends IService<Post> {

    Result queryPostById(Long id);

    Result queryHotPost(Integer current);

    Result updateLike(Long id);

    Result queryPostLikes(Long id);

    Result savePost(Post post);

    Result quertPostOfFollow(Long max, Integer offset);
}
