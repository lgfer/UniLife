package com.unilife.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unilife.dto.Result;
import com.unilife.dto.ScrollResult;
import com.unilife.dto.UserDTO;
import com.unilife.entity.Post;
import com.unilife.entity.Follow;
import com.unilife.entity.User;
import com.unilife.mapper.PostMapper;
import com.unilife.service.IPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unilife.service.IFollowService;
import com.unilife.service.IUserService;
import com.unilife.utils.RedisConstants;
import com.unilife.utils.SystemConstants;
import com.unilife.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.unilife.utils.RedisConstants.POST_LIKED_KEY;
import static com.unilife.utils.RedisConstants.FEED_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Resource
    private IUserService userService;

    @Resource
    private IFollowService followService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryHotPost(Integer current) {
        // 根据用户查询
        Page<Post> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Post> records = page.getRecords();
        // 查询用户
        records.forEach(post -> {
            this.isPostLiked(post);
            this.queryPostUser(post);
        });
        return Result.ok(records);
    }

    @Override
    public Result updateLike(Long id){
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2.判断当前用户有没有点赞
        String key=POST_LIKED_KEY+id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if(score==null) {
            //3.如果未点赞，可以点赞
            //3.1.数据库点赞数+1
            boolean isSuccess = update().setSql("liked=liked+1").eq("id", id).update();
            //3.2.保存用户到redis的set集合  zadd key value score
            if(isSuccess){
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
            }
        }else {
            //4.如果已经点赞，取消点赞
            //4.1.数据库点赞数-1
            boolean isSuccess = update().setSql("liked=liked-1").eq("id", id).update();
            if(isSuccess) {
                //4.2.将用户从set集合中移除
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryPostLikes(Long id) {
        //1.查询top5的点赞用户
        String key=POST_LIKED_KEY+id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if(top5==null||top5.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        //2.解析出其中的id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",",ids);
        //3.根据用户id查询用户
        //SELECT *from tb_user where id IN(5,1) ORDER BY FIELD(id,5,1)
        List<UserDTO> userDTOS = userService.query()
                .in("id",ids).last("order by field(id,"+idStr+")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        //4.返回
        return Result.ok(userDTOS);

    }

    @Override
    public Result savePost(Post post) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        post.setUserId(user.getId());
        // 保存种草帖
        boolean isSuccess = save(post);
        if(!isSuccess){
           return Result.fail("新增帖子失败");
        }
        //查询帖子作者的所有粉丝 select* from tb_follow where follow_user_id=?
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        //推送帖子id给所有粉丝
        for (Follow follow : follows) {
            //获取粉丝id
            Long userId = follow.getUserId();
            //推送
            String key=FEED_KEY+userId;
            stringRedisTemplate.opsForZSet().add(key,post.getId().toString(),System.currentTimeMillis());
        }
        // 返回id
        return Result.ok(post.getId());
    }

    @Override
    public Result quertPostOfFollow(Long max, Integer offset) {
        //1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        //2.查询收件箱  zrevrangebyscore key  min max limit offset count
        String key=FEED_KEY+userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        if(typedTuples==null||typedTuples.isEmpty()){
            return Result.ok();
        }
        //3.解析数据：postId,minTime时间戳),offset
        List<Long> ids=new ArrayList<>(typedTuples.size());
        long minTime=0;
        int os=1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            //3.1.获取id
            ids.add(Long.valueOf( typedTuple.getValue()));
            //3.2.获取分数（时间戳）
            long time=typedTuple.getScore().longValue();
            if(time==minTime){
                os++;
            }else {
                minTime = time;
                os = 1;
            }

        }
        //4.根据id查询post
        String idStr = StrUtil.join(",", ids);
        List<Post> posts = query()
                .in("id", ids).last("order by field(id," + idStr + ")").list();

        for (Post post : posts) {
           //4.1.查询post有关的用户
            queryPostUser(post);
            //4.2查询post是否点赞
            isPostLiked(post);
        }
        //5.封装并返回
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(posts);
        scrollResult.setOffset(os);
        scrollResult.setMinTime(minTime);
        return Result.ok(scrollResult);

    }

    @Override
    public Result  queryPostById(Long id) {
        //1.查询post
        Post post = getById(id);
        //2.查询post关联的对象
        if(post==null){
            return Result.fail("帖子不存在");
        }
        queryPostUser(post);
        //3.查询post是否点赞
        isPostLiked(post);
        return Result.ok(post);
    }

    private void isPostLiked(Post post) {
        //1.获取当前用户
        UserDTO user = UserHolder.getUser();
        if(user==null){
            //用户未登录，无需查询是否点赞
            return;
        }
        Long userId = user.getId();

        //2.判断当前用户有没有点赞
        String key=POST_LIKED_KEY+post.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        post.setIsLike(score!=null);
    }

    private void queryPostUser(Post post) {
        Long userId = post.getUserId();
        User user = userService.getById(userId);
        post.setName(user.getNickName());
        post.setIcon(user.getIcon());
    }

}
