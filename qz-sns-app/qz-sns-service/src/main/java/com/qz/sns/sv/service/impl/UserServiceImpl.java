package com.qz.sns.sv.service.impl;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.common.enums.ResultCode;
import com.qz.sns.common.utils.JwtUtil;
import com.qz.sns.common.utils.BeanUtils;
import com.qz.sns.common.utils.RegexUtil;
import com.qz.sns.common.utils.VerificationCodeGenerator;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.User;
import com.qz.sns.model.vo.LoginVO;
import com.qz.sns.model.vo.UploadImageVO;
import com.qz.sns.model.vo.UserVO;
import com.qz.sns.sv.config.props.JwtProperties;
import com.qz.sns.sv.exception.GlobalException;
import com.qz.sns.sv.mapper.UserMapper;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import com.qz.sns.sv.util.EmailUtil;
import com.qz.sns.sv.util.SMSUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.qz.sns.common.constant.UserConstant.*;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@Service
@RequiredArgsConstructor//功能：自动注入
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private static final String REDIS_EMAIL_CODE = "EMAIL_CODE:";
    private final FileService fileService;
    private final SMSUtils smsUtils;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;  //功能：密码加密
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private com.qz.sns.sv.util.TencentCaptchaUtil tencentCaptchaUtil;

    @Override
    public Result<String> resetPassword(ResetPasswordRequest request) {
        // 检查用户是否存在
        User user = this.findByEmail(request.getEmail());
        if (user == null) {
            return ResultUtils.error(ResultCode.PROGRAM_ERROR, "用户不存在");
        }

        // 验证验证码
        String codeInRedis = redisTemplate.opsForValue().get(REDIS_EMAIL_CODE+request.getEmail());
        if (codeInRedis == null || !codeInRedis.equals(request.getVerifyCode())) {
            return ResultUtils.error(ResultCode.CAPTCHA_ERROR, "验证码错误或已过期");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedTime(LocalDateTime.now());
        boolean updated = this.updateById(user);
        if (!updated) {
            return ResultUtils.error(ResultCode.PROGRAM_ERROR, "密码重置失败");
        }

        // 删除Redis中的验证码
        redisTemplate.delete(REDIS_EMAIL_CODE+request.getEmail());

        return ResultUtils.success("密码重置成功");
    }
    @Override
    public User findByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return getOne(queryWrapper);
    }

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return getOne(queryWrapper);
    }



    @Override
    public Result<String> sendEmailVerifyCode(String email, String ticket, String randstr, jakarta.servlet.http.HttpServletRequest request) {
        System.out.println("开始发送邮箱验证码，邮箱：" + email + "，ticket：" + ticket + "，randstr：" + randstr);
        
        // 1. 校验邮箱格式
        if (!RegexUtil.isEmail(email)) {
            return ResultUtils.error(ResultCode.XSS_PARAM_ERROR, "邮箱格式不正确");
        }
        
        // 2. 校验腾讯云验证码票据
        if (ticket != null && !ticket.isEmpty() && randstr != null && !randstr.isEmpty()) {
            System.out.println("开始校验腾讯云验证码票据");
            String userIp = com.qz.sns.sv.util.TencentCaptchaUtil.getClientIp(request);
            System.out.println("用户IP：" + userIp);
            
            boolean captchaValid = tencentCaptchaUtil.verifyCaptcha(ticket, randstr, userIp);
            System.out.println("验证码校验结果：" + captchaValid);
            
            if (!captchaValid) {
                System.out.println("腾讯云验证码校验失败");
                return ResultUtils.error(ResultCode.CAPTCHA_ERROR, "人机验证失败，请重试");
            }
            System.out.println("腾讯云验证码校验成功");
        } else {
            System.out.println("未提供验证码票据，跳过人机验证");
        }

        // 3. 生成6位验证码
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String verifyCode = String.valueOf(code);
        System.out.println("生成的邮箱验证码：" + verifyCode);

        // 4. 发送邮件
        System.out.println("开始发送邮件");
        boolean sendResult = emailUtil.sendMail(
                email,
                "亲子悦读 - 验证码",
                "亲子阅读提醒您，您的验证码为: " + verifyCode + "，请在5分钟内使用。"
        );
        System.out.println("邮件发送结果：" + sendResult);

        if (sendResult) {
            // 5. 存入Redis，有效期5分钟
            redisTemplate.opsForValue().set(REDIS_EMAIL_CODE + email, verifyCode, 5, TimeUnit.MINUTES);
            System.out.println("验证码已存入Redis");
            return ResultUtils.success("验证码已发送");
        } else {
            System.out.println("邮件发送失败");
            return ResultUtils.error(ResultCode.PROGRAM_ERROR, "验证码发送失败");
        }
    }

    @Override
    public LoginVO registerByEmail(RegisterByEmailDTO dto) {
        // 1. 校验邮箱格式
        if (!RegexUtil.isEmail(dto.getEmail())) {
            throw new GlobalException("邮箱格式不正确");
        }

        // 2. 检查验证码是否正确
        String code = redisTemplate.opsForValue().get(REDIS_EMAIL_CODE + dto.getEmail());
        if (!dto.getVerificationCode().equals(code)) {
            throw new GlobalException("验证码错误或过期");
        }

        // 3. 检查是否已经注册
        if (this.findByEmail(dto.getEmail()) != null) {
            throw new GlobalException("该邮箱已注册");
        }

        // 4. 注册，设置默认信息
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole()); // 默认角色
        user.setAvatar("http://114.55.233.139:9000/imtest/%E6%B5%8B%E8%AF%95%E5%A5%B6%E9%BE%99.gif"); // 默认头像
        user.setAvatarThumb("http://114.55.233.139:9000/imtest/%E6%B5%8B%E8%AF%95%E5%A5%B6%E9%BE%99.gif");
        user.setStatus(1); // 正常状态
        user.setUsername(generateRandomUsername(dto.getEmail())); // 生成随机用户名
        user.setNickname(user.getUsername()); // 昵称与用户名相同
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 默认密码

        this.save(user); // 保存用户信息

        // 重新查询完整用户信息
        user = this.findByEmail(dto.getEmail());

        // 5. 生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        if (session != null) {
            session.setUserId(user.getId());
        }
        session.setTerminal(dto.getTerminal());
        session.setUserType(user.getUserType());

        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getAccessTokenExpireIn(),
                jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(),
                jwtProperties.getRefreshTokenSecret());

        // 6. 删除Redis中的验证码
        redisTemplate.delete(REDIS_EMAIL_CODE + dto.getEmail());

        // 7. 返回登录信息
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        System.out.println("开始密码登录，用户名/手机号：" + dto.getUserName());
        
        // 1. 判断输入的是用户名还是手机号，并查找用户
        User user = null;
        if (RegexUtil.isPhone(dto.getUserName())) {
            // 如果是手机号格式，按手机号查找
            System.out.println("检测到手机号格式，按手机号查找用户");
            user = this.findUserByPhone(dto.getUserName());
        } else {
            // 否则按用户名查找
            System.out.println("按用户名查找用户");
            user = this.findUserByUserName(dto.getUserName());
        }
        
        // 2. 检查用户是否存在
        if (Objects.isNull(user)) {
            System.out.println("用户不存在");
            throw new GlobalException("用户不存在");
        }
        
        System.out.println("找到用户：" + user.getUsername() + "，手机号：" + user.getPhone());

        // 3. 检查账号状态
        if (user.getStatus() == 0) {
            System.out.println("账号已被封禁");
            throw new GlobalException("您的账号已被管理员封禁,请联系客服!");
        }

        // 4. 验证密码
        System.out.println("开始验证密码");
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            System.out.println("密码错误");
            throw new GlobalException("密码错误");
        }
        
        System.out.println("密码验证成功，准备生成token");

        // 5. 生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        session.setUserId(user.getId());
        session.setTerminal(dto.getTerminal());
        session.setUserType(user.getUserType());

        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getAccessTokenExpireIn(),
                jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(),
                jwtProperties.getRefreshTokenSecret());

        System.out.println("Token生成成功，返回登录信息");
        
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public LoginVO loginByPhone(PhoneLoginDTO dto) {
        // 1. 校验手机号格式
        if (!RegexUtil.isPhone(dto.getPhone())) {
            throw new GlobalException("手机号格式不正确");
        }

        // 2. 检查验证码是否正确
        String code = redisTemplate.opsForValue().get(REDIS_SMS_CODE + dto.getPhone());
        if (!dto.getVerificationCode().equals(code)) {
            throw new GlobalException("验证码错误或过期");
        }

        // 3. 获取用户
        User user = this.findUserByPhone(dto.getPhone());
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new GlobalException("您的账号已被管理员封禁,请联系客服!");
        }

        // 4. 生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        session.setUserId(user.getId());
        session.setTerminal(dto.getTerminal());
        session.setUserType(user.getUserType());

        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getAccessTokenExpireIn(),
                jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(),
                jwtProperties.getRefreshTokenSecret());

        // 5. 删除Redis中的验证码
        redisTemplate.delete(REDIS_SMS_CODE + dto.getPhone());

        // 6. 返回登录信息
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public LoginVO loginByEmail(EmailLoginDTO dto) {
        // 1. 校验邮箱格式
        if (!RegexUtil.isEmail(dto.getEmail())) {
            throw new GlobalException("邮箱格式不正确");
        }

        // 2. 检查验证码是否正确
        String code = redisTemplate.opsForValue().get(REDIS_EMAIL_CODE + dto.getEmail());
        if (!dto.getVerificationCode().equals(code)) {
            throw new GlobalException("验证码错误或过期");
        }

        // 3. 获取用户
        User user = this.findByEmail(dto.getEmail());
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new GlobalException("您的账号已被管理员封禁,请联系客服!");
        }

        // 4. 生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        session.setUserId(user.getId());
        session.setTerminal(dto.getTerminal());
        session.setUserType(user.getUserType());

        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getAccessTokenExpireIn(),
                jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(),
                jwtProperties.getRefreshTokenSecret());

        // 5. 删除Redis中的验证码
        redisTemplate.delete(REDIS_EMAIL_CODE + dto.getEmail());

        // 6. 返回登录信息
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        vo.setRole(user.getRole());
        return vo;
    }

    /**
     * 生成随机用户名
     */

    @Override
    public UserDTO getCurrentUser() {
        // 1. 获取当前用户ID
        Long userId = SessionContext.getSession().getUserId();

        User user = userMapper.selectById(userId);

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        return userDTO;
    }

    @Override
    public UserVO findUserById(Long userId) {
        User user = getById(userId);
        return BeanUtils.copyProperties(user, UserVO.class);
    }



    @Override
    public LoginVO refreshToken(String refreshToken) {
        //验证 token
        if (!JwtUtil.checkSign(refreshToken, jwtProperties.getRefreshTokenSecret())) {
            throw new GlobalException("您的登录信息已过期，请重新登录");
        }
        String strJson = JwtUtil.getInfo(refreshToken);
        Long userId = JwtUtil.getUserId(refreshToken);//功能：获取用户id，用于查询用户信息验证token是否伪造
        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
//        if (user.getIsBanned()) {
//            String tip = String.format("您的账号因'%s'被管理员封禁,请联系客服!",user.getReason());
//            throw new GlobalException(tip);
//        }
        String accessToken =
                JwtUtil.sign(userId, strJson, jwtProperties.getAccessTokenExpireIn(), jwtProperties.getAccessTokenSecret());
        String newRefreshToken = JwtUtil.sign(userId, strJson, jwtProperties.getRefreshTokenExpireIn(),
                jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }
    @Override
    public User findUserByUserName(String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getUsername, username);
        return this.getOne(queryWrapper);
    }
    @Override
    @Transactional
    public String sendVerificationCode(String phone, String ticket, String randstr, jakarta.servlet.http.HttpServletRequest request) throws Exception {
        System.out.println("开始发送短信验证码，手机号：" + phone + "，ticket：" + ticket + "，randstr：" + randstr);
        
        // 1. 校验手机号格式
        if (!RegexUtil.isPhone(phone)) {
            System.out.println("手机号格式不正确：" + phone);
            throw new GlobalException("手机号格式不正确");
        }
        
        // 2. 校验腾讯云验证码票据
        if (ticket != null && !ticket.isEmpty() && randstr != null && !randstr.isEmpty()) {
            System.out.println("开始校验腾讯云验证码票据");
            String userIp = com.qz.sns.sv.util.TencentCaptchaUtil.getClientIp(request);
            System.out.println("用户IP：" + userIp);
            
            boolean captchaValid = tencentCaptchaUtil.verifyCaptcha(ticket, randstr, userIp);
            System.out.println("验证码校验结果：" + captchaValid);
            
            if (!captchaValid) {
                System.out.println("腾讯云验证码校验失败");
                throw new GlobalException("人机验证失败，请重试");
            }
            System.out.println("腾讯云验证码校验成功");
        } else {
            System.out.println("未提供验证码票据，跳过人机验证");
        }
        
        // 3. 检查是否在限制时间内
        if (Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_SMS_LIMIT+phone))) {
            System.out.println("发送过于频繁，手机号：" + phone);
            throw new GlobalException("发送过于频繁，请稍后再试");
        }
        
        // 4. 生成验证码
        String verificationCode = VerificationCodeGenerator.generateNumericVerificationCode();
        System.out.println("生成的短信验证码：" + verificationCode);
        
        // 5. 存入redis
        redisTemplate.opsForValue().set(REDIS_SMS_CODE+phone,verificationCode,SMS_CODE_EXPIRE_TIME, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(REDIS_SMS_LIMIT+phone, "1", SMS_CODE_RESEND_LIMIT, TimeUnit.SECONDS);
        System.out.println("验证码已存入Redis");
        
        // 6. 发送短信
        Map<String, Object> param = new HashMap<>();
        param.put("code",verificationCode);
        param.put("time",5);
        
        System.out.println("开始发送短信");
        if(smsUtils.sendMessage(phone,param)){
            System.out.println("短信发送成功");
            return "发送成功";
        }
        System.out.println("短信发送失败");
        return "发送失败";
    }
    @Transactional
    @Override
    public LoginVO registerByPhone(RgisterByPhoneDTO dto) {
        if (!RegexUtil.isPhone(dto.getPhone())) {
            // 2. 不符合，返回错误信息
            throw new GlobalException("手机号格式不正确");
        }
        // 2. 检查验证码是否正确
        String code = redisTemplate.opsForValue().get(REDIS_SMS_CODE+dto.getPhone());
        if (!dto.getVerificationCode().equals(code)) {
            throw new GlobalException("验证码错误或过期");
        }
        // 3. 检查是否已经注册
        if (this.findUserByPhone(dto.getPhone()) != null) {
            throw new GlobalException("该手机号已注册");
        }
        // 4. 注册。设置默认信息
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getPhone() + "@temp.com"); // 使用手机号作为临时邮箱
        user.setRole(dto.getRole() != null ? dto.getRole() : 1);
        user.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : "http://114.55.233.139:9000/imtest/%E6%B5%8B%E8%AF%95%E5%A5%B6%E9%BE%99.gif");
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        
        // 设置用户兴趣爱好
        log.info("设置用户兴趣爱好：{}", dto.getInterests());
        user.setInterests(dto.getInterests());
        
        // 保存用户信息
        log.info("即将保存的用户信息：{}", user);
        this.save(user);
        user = this.findUserByPhone(dto.getPhone());
        // 生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);//功能：将user对象转换为session对象
        if (session != null) {
            session.setUserId(user.getId());
        }
        session.setTerminal(dto.getTerminal());
        String strJson = JSON.toJSONString(session);//功能：将对象转换为json字符串，方便存储
        String accessToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getAccessTokenExpireIn(),//三个参数：用户id，json字符串-把用户终端信息放入token中，过期时间
                jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtil.sign(user.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(),
                jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }

    @Override
    public User findUserByPhone(String phone) {
        // 3. 检查是否已经注册-用构造器
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getPhone, phone);
        return this.getOne(queryWrapper);
    }
    public String generateRandomUsername(String phone) {
        return "qzuser" + phone.substring(phone.length() - 4) + new Random().nextInt(1000);
    }

    /**
     * 创建新用户
     * 执行用户名和手机号唯一性验证，密码加密处理，并设置默认状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserCreateDTO createDTO) {
        log.info("开始创建用户，用户名：{}", createDTO.getUsername());

        // 验证用户名唯一性
        if (isUsernameExists(createDTO.getUsername(), null)) {
            throw new RuntimeException("用户名已存在");
        }

       //验证邮箱唯一性（如果提供了邮箱）
        if (StringUtils.hasText(createDTO.getEmail()) &&
                isEmailExists(createDTO.getEmail(), null)) {
            throw new RuntimeException("邮箱已存在");
        }

        // 验证手机号唯一性（如果提供了手机号）
        if (StringUtils.hasText(createDTO.getPhone()) &&
                isPhoneExists(createDTO.getPhone(), null)) {
            throw new RuntimeException("手机号已存在");
        }

        // 构建用户实体
        User user = new User();
        BeanUtils.copyProperties(createDTO, user);

        // 密码加密
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        // 设置默认状态
        user.setStatus(1); // 正常状态

        // 保存到数据库
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("用户创建失败");
        }

        log.info("用户创建成功，用户ID：{}", user.getId());

        // 返回时不包含密码
        user.setPassword(null);
        return user;
    }

    /**
     * 根据ID获取用户详情
     * 自动过滤被禁用的用户，返回结果不包含密码信息
     */
    @Override
    public User getUserById(Long id) {
        log.info("查询用户详情，用户ID：{}", id);

        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("status", 1); // 只查询正常状态的用户

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 清除密码信息
        user.setPassword(null);
        return user;
    }

    /**
     * 更新用户信息
     * 支持部分字段更新，自动处理唯一性验证和密码加密
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(UserUpdateDTO updateDTO) {
        log.info("开始更新用户信息，用户ID：{}", updateDTO.getId());

        // 验证用户是否存在且未被禁用
        User existingUser = getUserById(updateDTO.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 验证用户名唯一性（如果要更新用户名）
        if (StringUtils.hasText(updateDTO.getUsername()) &&
                !updateDTO.getUsername().equals(existingUser.getUsername()) &&
                isUsernameExists(updateDTO.getUsername(), updateDTO.getId())) {
            throw new RuntimeException("用户名已存在");
        }

        // 验证手机号唯一性（如果要更新手机号）
        if (StringUtils.hasText(updateDTO.getPhone()) &&
                !updateDTO.getPhone().equals(existingUser.getPhone()) &&
                isPhoneExists(updateDTO.getPhone(), updateDTO.getId())) {
            throw new RuntimeException("手机号已存在");
        }

        // 构建更新实体
        User updateUser = new User();
        BeanUtils.copyProperties(updateDTO, updateUser);

        // 如果要更新密码，进行加密处理
        if (StringUtils.hasText(updateDTO.getPassword())) {
            updateUser.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }



        // 执行更新操作
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", updateDTO.getId())
                .eq("status", 1); // 确保只更新正常状态的用户

        int result = userMapper.update(updateUser, updateWrapper);
        if (result <= 0) {
            throw new RuntimeException("用户更新失败");
        }

        log.info("用户更新成功，用户ID：{}", updateDTO.getId());

        // 返回更新后的用户信息
        return getUserById(updateDTO.getId());
    }

    /**
     * 逻辑删除用户
     * 通过设置status字段为0实现软删除，保留数据完整性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        log.info("开始删除用户，用户ID：{}", id);

        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 验证用户是否存在
        User user = getUserById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在或已被删除");
        }

        // 执行逻辑删除
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .eq("status", 1)
                .set("status", 0)
                .set("updated_time", LocalDateTime.now());

        int result = userMapper.update(null, updateWrapper);
        boolean success = result > 0;

        if (success) {
            log.info("用户删除成功，用户ID：{}", id);
        } else {
            log.error("用户删除失败，用户ID：{}", id);
        }

        return success;
    }

    /**
     * 更新用户状态（封禁/解封）
     * 支持批量状态管理，用于用户权限控制
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long id, Integer status) {
        log.info("更新用户状态，用户ID：{}，目标状态：{}", id, status);

        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }

        // 验证用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新状态
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("status", status)
                .set("updated_time", LocalDateTime.now());

        int result = userMapper.update(null, updateWrapper);
        boolean success = result > 0;

        if (success) {
            String statusText = status == 1 ? "正常" : "禁用";
            log.info("用户状态更新成功，用户ID：{}，状态：{}", id, statusText);
        }

        return success;
    }

    /**
     * 分页查询用户列表
     * 支持多条件组合查询，自动过滤被禁用用户
     */
    @Override
    public PageDTO<User> getUserPage(UserQueryDTO queryDTO) {
        log.info("分页查询用户列表，查询条件：{}", queryDTO);

        // 构建分页对象
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 执行分页查询
        IPage<User> userPage = userMapper.selectUserPage(page, queryDTO);

        // 清除密码信息
        userPage.getRecords().forEach(user -> user.setPassword(null));

        // 构建返回结果
        PageDTO<User> result = new PageDTO<>();
        result.setRecords(userPage.getRecords());
        result.setTotal(userPage.getTotal());
        result.setCurrent((int) userPage.getCurrent());
        result.setSize((int) userPage.getSize());

        log.info("用户列表查询完成，共查询到{}条记录", result.getTotal());
        return result;
    }

    /**
     * 根据用户名查询用户
     */
    @Override
    public User getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }

        User user = userMapper.selectByUsername(username);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 根据手机号查询用户
     */
    @Override
    public User getUserByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }

        User user = userMapper.selectByPhone(phone);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 检查用户名是否已存在
     */
    @Override
    public boolean isUsernameExists(String username, Long excludeId) {
        if (!StringUtils.hasText(username)) {
            return false;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);

        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }

        return userMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查手机号是否已存在
     */
    @Override
    public boolean isPhoneExists(String phone, Long excludeId) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);

        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }

        return userMapper.selectCount(queryWrapper) > 0;
    }

    //检查邮箱是否已存在
    @Override
    public boolean isEmailExists(String email, Long excludeId) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);

        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }

        return userMapper.selectCount(queryWrapper) > 0;
    }
    /**
     * 更新用户头像
     * 支持上传新头像并删除旧头像，返回新头像URL
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateAvatar(Long userId, MultipartFile updateAvatarDTO) {
        log.info("更新用户头像，用户ID：{}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (updateAvatarDTO == null || updateAvatarDTO.isEmpty()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "请上传头像文件");
        }

        // 获取当前用户的旧头像URL
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }

        String oldAvatarUrl = currentUser.getAvatar(); // 旧头像URL

        // 删除旧头像（如果存在）
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            boolean deleteSuccess = fileService.deleteFile(oldAvatarUrl);
            if (!deleteSuccess) {
                log.warn("旧头像删除失败，用户ID：{}，旧头像URL：{}", userId, oldAvatarUrl);
            }
        }

        // 上传新头像
        String newAvatarUrl = null;
        try {
            UploadImageVO uploadResult = fileService.uploadImage(updateAvatarDTO); // 调用您已有的上传图片方法
            newAvatarUrl = uploadResult.getOriginUrl(); // 获取新头像URL
        } catch (Exception e) {
            log.error("上传新头像失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "上传新头像失败：" + e.getMessage());
        }

        // 更新数据库中用户的头像URL
        currentUser.setAvatar(newAvatarUrl); // 更新新头像URL
        int updateResult = userMapper.updateById(currentUser);
        if (updateResult <= 0) {
            log.error("更新用户头像失败，用户ID：{}", userId);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "更新用户头像失败");
        }

        log.info("用户头像更新成功，用户ID：{}，新头像URL：{}", userId, newAvatarUrl);
        return newAvatarUrl; // 返回新头像URL
    }

}
