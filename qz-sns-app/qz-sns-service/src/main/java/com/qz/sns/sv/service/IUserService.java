package com.qz.sns.sv.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.User;
import com.qz.sns.model.vo.LoginVO;
import com.qz.sns.model.vo.UserVO;
import com.qz.sns.sv.result.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
public interface IUserService extends IService<User> {

    UserVO findUserById(Long userId);
    /**
     * 用户登录
     *
     * @param dto 登录dto
     * @return 登录token
     */
    LoginVO login(LoginDTO dto);

    /**
     * 用refreshToken换取新 token
     *
     * @param refreshToken 刷新token
     * @return 登录token
     */
    LoginVO refreshToken(String refreshToken);


    User findUserByUserName(String username);
    //手机号发送验证码
    String sendVerificationCode(String phone, String ticket, String randstr, jakarta.servlet.http.HttpServletRequest request) throws Exception;
    //用户通过手机号注册
    LoginVO registerByPhone(RgisterByPhoneDTO dto);

    //通过手机号查询用户是否存在
    User findUserByPhone(String phone);
    /**
     * 获取当前登录用户信息
     * @return 用户DTO
     */
    UserDTO getCurrentUser();

    /**
     * 后台创建新用户
     *
     * @param createDTO 用户创建请求数据
     * @return 创建成功的用户信息
     */
    User createUser(UserCreateDTO createDTO);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    User getUserById(Long id);

    /**
     * 更新用户信息
     *
     * @param updateDTO 用户更新请求数据
     * @return 更新后的用户信息
     */
    User updateUser(UserUpdateDTO updateDTO);

    /**
     * 删除用户（逻辑删除，设置status为0）
     *
     * @param id 用户ID
     * @return 删除是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 封禁/解封用户
     *
     * @param id 用户ID
     * @param status 状态值：1-正常，0-禁用
     * @return 操作是否成功
     */
    boolean updateUserStatus(Long id, Integer status);

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件和分页参数
     * @return 分页结果
     */
    PageDTO<User> getUserPage(UserQueryDTO queryDTO);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User getUserByPhone(String phone);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isUsernameExists(String username, Long excludeId);

    /**
     * 检查手机号是否已存在
     *
     * @param phone 手机号
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */

    boolean isPhoneExists(String phone, Long excludeId);



    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱地址
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isEmailExists(String email, Long excludeId);
    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param updateAvatarDTO 更新头像请求数据
     * @return 更新后的头像URL
     */

    String updateAvatar(Long userId, MultipartFile updateAvatarDTO) throws Exception;

    Result<String> sendEmailVerifyCode(String email, String ticket, String randstr, jakarta.servlet.http.HttpServletRequest request);
    LoginVO registerByEmail(RegisterByEmailDTO dto);
    LoginVO loginByEmail(EmailLoginDTO dto);

    /**
     * 手机号验证码登录
     * @param dto 登录参数
     * @return 登录结果
     */
    LoginVO loginByPhone(PhoneLoginDTO dto);
    User findByEmail(String email);
    User findByUsername(String username);
    Result<String> resetPassword(ResetPasswordRequest request);
}
