package com.qz.sns.web.controller;

import com.alibaba.fastjson.JSON;
import com.qz.sns.common.utils.BeanUtils;
import com.qz.sns.common.utils.JwtUtil;
import com.qz.sns.model.dto.AdminRegisterDTO;
import com.qz.sns.model.entity.User;
import com.qz.sns.model.vo.LoginVO;
import com.qz.sns.sv.config.props.JwtProperties;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.sv.session.UserSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 管理员控制器
 * </p>
 *
 * @author AI Assistant
 * @since 2025-10-23
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理", description = "管理员相关接口")
public class AdminController {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    /**
     * 管理员注册
     */
    @PostMapping("/register")
    @Operation(summary = "管理员注册", description = "创建新的管理员账号并自动登录")
    public Result<LoginVO> register(@Valid @RequestBody AdminRegisterDTO dto) {
        log.info("管理员注册请求：{}", dto);

        try {
            // 1. 检查用户名是否已存在
            if (userService.isUsernameExists(dto.getUsername(), null)) {
                return ResultUtils.error(400, "用户名已存在");
            }

            // 2. 检查手机号是否已存在
            if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
                if (userService.isPhoneExists(dto.getPhone(), null)) {
                    return ResultUtils.error(400, "手机号已被使用");
                }
            }

            // 3. 检查邮箱是否已存在
            if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
                if (userService.isEmailExists(dto.getEmail(), null)) {
                    return ResultUtils.error(400, "邮箱已被使用");
                }
            }

            // 4. 创建管理员账号
            User admin = new User();
            admin.setUsername(dto.getUsername());
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
            admin.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
            admin.setPhone(dto.getPhone());
            
            // 处理邮箱：如果为空，使用用户名或手机号生成默认邮箱
            if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                admin.setEmail(dto.getEmail());
            } else if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
                admin.setEmail(dto.getPhone() + "@admin.temp.com"); // 使用手机号生成临时邮箱
            } else {
                admin.setEmail(dto.getUsername() + "@admin.temp.com"); // 使用用户名生成临时邮箱
            }
            
            admin.setUserType(dto.getUserType() != null ? dto.getUserType() : 2); // 2表示管理员
            admin.setRole(dto.getRole() != null ? dto.getRole() : 0); // 管理员角色设为0
            admin.setStatus(1); // 正常状态
            admin.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : "http://114.55.233.139:9000/imtest/%E6%B5%8B%E8%AF%95%E5%A5%B6%E9%BE%99.gif"); // 默认头像

            userService.save(admin);
            log.info("管理员注册成功：{}", admin.getUsername());

            // 5. 重新查询完整用户信息
            User savedAdmin = userService.findUserByUserName(dto.getUsername());
            
            // 6. 生成token，自动登录
            UserSession session = BeanUtils.copyProperties(savedAdmin, UserSession.class);
            if (session != null) {
                session.setUserId(savedAdmin.getId());
            }
            session.setTerminal(1); // 默认终端为1
            session.setUserType(savedAdmin.getUserType());

            String strJson = JSON.toJSONString(session);
            String accessToken = JwtUtil.sign(savedAdmin.getId(), strJson, jwtProperties.getAccessTokenExpireIn(),
                    jwtProperties.getAccessTokenSecret());
            String refreshToken = JwtUtil.sign(savedAdmin.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(),
                    jwtProperties.getRefreshTokenSecret());

            LoginVO loginVO = new LoginVO();
            loginVO.setAccessToken(accessToken);
            loginVO.setRefreshToken(refreshToken);
            loginVO.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
            loginVO.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());

            log.info("管理员自动登录成功，用户ID：{}", savedAdmin.getId());
            return ResultUtils.success(loginVO);
        } catch (Exception e) {
            log.error("管理员注册失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "注册失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已被使用")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        log.info("检查用户名是否可用：{}", username);
        
        if (username == null || username.trim().isEmpty()) {
            return ResultUtils.error(400, "用户名不能为空");
        }

        boolean exists = userService.isUsernameExists(username, null);
        return ResultUtils.success(!exists); // 返回true表示可用，false表示已被使用
    }

    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已被使用")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        log.info("检查手机号是否可用：{}", phone);
        
        if (phone == null || phone.trim().isEmpty()) {
            return ResultUtils.error(400, "手机号不能为空");
        }

        boolean exists = userService.isPhoneExists(phone, null);
        return ResultUtils.success(!exists); // 返回true表示可用，false表示已被使用
    }
}

