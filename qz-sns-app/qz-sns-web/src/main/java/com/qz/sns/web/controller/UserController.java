package com.qz.sns.web.controller;

import com.qz.sns.common.enums.UserType;
import com.qz.sns.common.utils.RegexUtil;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.User;
import com.qz.sns.model.vo.LoginVO;
import com.qz.sns.model.vo.UserVO;
import com.qz.sns.sv.annotation.RequirePermission;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@CrossOrigin //允许跨域访问
@RestController//返回json数据--@Controller+@ResponseBody:只用controller会返回页面
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final String REDIS_EMAIL_CODE = "EMAIL_CODE:";
    private final IUserService userService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        System.out.println("重置密码请求：" + request);
        // 2. 检查验证码是否正确
        String code = redisTemplate.opsForValue().get(REDIS_EMAIL_CODE + request.getEmail());
        System.out.println("验证码：" + code);
        if (code == null || !code.equals(request.getVerifyCode())) {
            return ResultUtils.error(400, "验证码错误或已过期");
        }

        // 3. 查找用户
        try {
            userService.resetPassword(request);
            return ResultUtils.success("密码重置成功");
        } catch (Exception e) {
            return ResultUtils.error(500, e.getMessage());
        }
    }


    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-email-code")
    public Result<String> sendEmailCode(@RequestBody EmailVerifyRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResultUtils.error(400, "邮箱不能为空");
        }

        return userService.sendEmailVerifyCode(request.getEmail());
    }

    /**
     * 使用邮箱验证码注册
     */
    @PostMapping("/register-by-email")
    public Result<LoginVO> registerByEmail(@RequestBody RegisterByEmailDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            return ResultUtils.error(400, "邮箱不能为空");
        }

        if (dto.getVerificationCode() == null || dto.getVerificationCode().isEmpty()) {
            return ResultUtils.error(400, "验证码不能为空");
        }

        try {
            LoginVO loginVO = userService.registerByEmail(dto);
            return ResultUtils.success(loginVO);
        } catch (Exception e) {
            return ResultUtils.error(500, e.getMessage());
        }
    }

    /**
     * 使用手机号验证码注册
     */
    @PostMapping("/register-by-phone")
    public Result<LoginVO> registerByPhone(@RequestBody RgisterByPhoneDTO dto) {
        log.info("接收手机号注册请求，请求数据：{}", dto);
        
        if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
            return ResultUtils.error(400, "手机号不能为空");
        }

        if (dto.getVerificationCode() == null || dto.getVerificationCode().isEmpty()) {
            return ResultUtils.error(400, "验证码不能为空");
        }

        // 为email设置一个默认值（使用手机号作为临时邮箱）
        dto.setEmail(dto.getPhone() + "@temp.com");
        
        log.info("处理后的注册数据：{}", dto);

        try {
            LoginVO loginVO = userService.registerByPhone(dto);
            return ResultUtils.success(loginVO);
        } catch (Exception e) {
            log.error("手机号注册失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResultUtils.error(400, "请选择要上传的头像");
        }

        try {
            // TODO: 实现文件上传到MinIO的逻辑
            String avatarUrl = "https://example.com/default-avatar.png"; // 临时返回默认头像
            return ResultUtils.success(avatarUrl);
        } catch (Exception e) {
            log.error("上传头像失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "上传头像失败：" + e.getMessage());
        }
    }

    /**
     * 用户名密码登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        if (dto.getUserName() == null || dto.getUserName().isEmpty()) {
            return ResultUtils.error(400, "用户名不能为空");
        }

        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            return ResultUtils.error(400, "密码不能为空");
        }

        try {
            LoginVO loginVO = userService.login(dto);
            return ResultUtils.success(loginVO);
        } catch (Exception e) {
            return ResultUtils.error(500, e.getMessage());
        }
    }

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login-by-email")
    public Result<LoginVO> loginByEmail(@RequestBody EmailLoginDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            return ResultUtils.error(400, "邮箱不能为空");
        }

        if (dto.getVerificationCode() == null || dto.getVerificationCode().isEmpty()) {
            return ResultUtils.error(400, "验证码不能为空");
        }

        try {
            LoginVO loginVO = userService.loginByEmail(dto);
            return ResultUtils.success(loginVO);
        } catch (Exception e) {
            return ResultUtils.error(500, e.getMessage());
        }
    }

    /**
     * 检查邮箱是否已注册
     */
    @GetMapping("/check-email/{email}")
    public Result<String> checkEmail(@PathVariable String email) {
        if (userService.findByEmail(email) != null) {
            return ResultUtils.error(400, "邮箱已被注册");
        } else {
            return ResultUtils.success("邮箱可以使用");
        }
    }
    @GetMapping("/findbyid")//
    public Result<UserVO> findById(@NotNull @RequestParam Long id){ //测试查询的格式：http://localhost:8888/user/findbyid?id=1
        return ResultUtils.success(userService.findUserById(id));
    }
    //手机号发送验证码接口
    @GetMapping("/smscode")
    public Result<String> sendVerificationCode(@NotNull @RequestParam String phone) throws Exception {
        return ResultUtils.success(userService.sendVerificationCode(phone));
    }
    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser() {
        UserDTO user = userService.getCurrentUser();
        return ResultUtils.success(user);
    }

    /**
     * 创建新用户
     *
     * @param createDTO 用户创建请求数据
     * @return 创建成功的用户信息
     */
    @PostMapping
    public Result<User> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        log.info("接收创建用户请求：{}", createDTO.getUsername());

        try {
            User user = userService.createUser(createDTO);
            return ResultUtils.success(user, "用户创建成功");
        } catch (Exception e) {
            log.error("创建用户失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "创建用户失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        log.info("接收获取用户详情请求，用户ID：{}", id);

        try {
            User user = userService.getUserById(id);
            return ResultUtils.success(user);
        } catch (Exception e) {
            log.error("获取用户详情失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取用户详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     *
     * @param updateDTO 用户更新请求数据
     * @return 更新后的用户信息
     */
    @PutMapping
    public Result<User> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("接收更新用户请求，用户ID：{}", updateDTO.getId());

        try {
            User user = userService.updateUser(updateDTO);
            return ResultUtils.success(user, "用户信息更新成功");
        } catch (Exception e) {
            log.error("更新用户信息失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "更新用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        log.info("接收删除用户请求，用户ID：{}", id);

        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return ResultUtils.success("用户删除成功");
            } else {
                return ResultUtils.error(500, "用户删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户状态（封禁/解封）
     *
     * @param id 用户ID
     * @param status 状态值：1-正常，0-禁用
     * @return 操作结果
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable @NotNull(message = "用户ID不能为空") Long id,
            @RequestParam @NotNull(message = "状态值不能为空") Integer status) {

        log.info("接收更新用户状态请求，用户ID：{}，状态：{}", id, status);

        try {
            boolean success = userService.updateUserStatus(id, status);

            if (success) {
                String statusText = status == 1 ? "启用" : "禁用";
                return ResultUtils.success("用户" + statusText + "成功");
            } else {
                return ResultUtils.error(500, "用户状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "更新用户状态失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件和分页参数
     * @return 分页用户列表
     */
    @RequirePermission(value = {UserType.ADMIN}, description = "查询用户列表")
    @GetMapping("/page")
    public Result<PageDTO<User>> getUserPage(@Valid UserQueryDTO queryDTO) {
        log.info("接收分页查询用户请求，查询条件：{}", queryDTO);

        try {
            PageDTO<User> pageResult = userService.getUserPage(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("分页查询用户失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        log.info("接收根据用户名查询用户请求，用户名：{}", username);

        try {
            User user = userService.getUserByUsername(username);
            if (user != null) {
                return ResultUtils.success(user);
            } else {
                return ResultUtils.error(404, "用户不存在");
            }
        } catch (Exception e) {
            log.error("根据用户名查询用户失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询用户失败：" + e.getMessage());
        }
    }

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @GetMapping("/phone/{phone}")
    public Result<User> getUserByPhone(@PathVariable String phone) {
        log.info("接收根据手机号查询用户请求，手机号：{}", phone);

        try {
            User user = userService.getUserByPhone(phone);
            if (user != null) {
                return ResultUtils.success(user);
            } else {
                return ResultUtils.error(404, "用户不存在");
            }
        } catch (Exception e) {
            log.error("根据手机号查询用户失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询用户失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户名是否可用
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID（可选，用于更新时检查）
     * @return 检查结果
     */
    @GetMapping("/check/username")
    public Result<Boolean> checkUsernameAvailable(
            @RequestParam String username,
            @RequestParam(required = false) Long excludeId) {

        log.info("检查用户名可用性，用户名：{}，排除ID：{}", username, excludeId);

        try {
            boolean exists = userService.isUsernameExists(username, excludeId);
            return ResultUtils.success(!exists, exists ? "用户名已存在" : "用户名可用");
        } catch (Exception e) {
            log.error("检查用户名可用性失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "检查用户名失败：" + e.getMessage());
        }
    }

    /**
     * 检查手机号是否可用
     *
     * @param phone 手机号
     * @param excludeId 排除的用户ID（可选，用于更新时检查）
     * @return 检查结果
     */
    @GetMapping("/check/phone")
    public Result<Boolean> checkPhoneAvailable(
            @RequestParam String phone,
            @RequestParam(required = false) Long excludeId) {

        log.info("检查手机号可用性，手机号：{}，排除ID：{}", phone, excludeId);

        try {
            boolean exists = userService.isPhoneExists(phone, excludeId);
            return ResultUtils.success(!exists, exists ? "手机号已存在" : "手机号可用");
        } catch (Exception e) {
            log.error("检查手机号可用性失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "检查手机号失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 批量删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteUsers(@RequestBody @NotNull(message = "用户ID列表不能为空") Long[] ids) {
        log.info("接收批量删除用户请求，用户ID数量：{}", ids.length);

        try {
            int successCount = 0;
            int failCount = 0;

            for (Long id : ids) {
                try {
                    boolean success = userService.deleteUser(id);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("删除用户失败，用户ID：{}，错误：{}", id, e.getMessage());
                    failCount++;
                }
            }

            String message = String.format("批量删除完成，成功：%d，失败：%d", successCount, failCount);
            log.info(message);

            if (failCount == 0) {
                return ResultUtils.success(message);
            } else {
                return ResultUtils.error(500, message);
            }
        } catch (Exception e) {
            log.error("批量删除用户失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "批量删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新用户状态
     *
     * @param ids 用户ID列表
     * @param status 目标状态
     * @return 批量更新结果
     */
    @PatchMapping("/batch/status")
    public Result<Void> batchUpdateUserStatus(
            @RequestBody @NotNull(message = "用户ID列表不能为空") Long[] ids,
            @RequestParam @NotNull(message = "状态值不能为空") Integer status) {

        log.info("接收批量更新用户状态请求，用户ID数量：{}，目标状态：{}", ids.length, status);

        try {
            int successCount = 0;
            int failCount = 0;

            for (Long id : ids) {
                try {
                    boolean success = userService.updateUserStatus(id, status);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("更新用户状态失败，用户ID：{}，错误：{}", id, e.getMessage());
                    failCount++;
                }
            }

            String statusText = status == 1 ? "启用" : "禁用";
            String message = String.format("批量%s完成，成功：%d，失败：%d", statusText, successCount, failCount);
            log.info(message);

            if (failCount == 0) {
                return ResultUtils.success(message);
            } else {
                return ResultUtils.error(500, message);
            }
        } catch (Exception e) {
            log.error("批量更新用户状态失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "批量更新用户状态失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param updateAvatarDTO 更新头像请求数据
     * @return 新头像URL
     */
    @PatchMapping("/{userId}/avatar")
    public Result<String> updateAvatar(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @Valid @RequestBody MultipartFile updateAvatarDTO) {

        log.info("接收更新头像请求，用户ID：{}", userId);

        try {
            String newAvatarUrl = userService.updateAvatar(userId, updateAvatarDTO);
            return ResultUtils.success(newAvatarUrl, "头像更新成功");
        } catch (Exception e) {
            log.error("更新头像失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "更新头像失败：" + e.getMessage());
        }
    }
}
