package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.dto.UserQueryDTO;
import com.qz.sns.model.entity.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    /**
     * 分页查询用户列表（排除被禁用的用户）
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<User> selectUserPage(Page<User> page, @Param("query") UserQueryDTO queryDTO);

    /**
     * 根据用户名查询用户（排除被禁用的用户）
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户（排除被禁用的用户）
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据微信ID查询用户（排除被禁用的用户）
     *
     * @param wechatId 微信openid
     * @return 用户信息
     */
    User selectByWechatId(@Param("wechatId") String wechatId);

    /**
     * 获取用户基本信息
     */
    @Select("SELECT id, nickname, avatar, avatar_thumb FROM user WHERE id = #{userId}")
    Map<String, Object> getUserBasicInfo(@Param("userId") Long userId);


}
