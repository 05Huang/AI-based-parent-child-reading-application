package com.qz.sns.web.controller;

import com.qz.sns.model.dto.RgisterByPhoneDTO;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.service.IUserService;
import com.qz.sns.model.dto.LoginDTO;
import com.qz.sns.model.vo.LoginVO;
import com.qz.sns.sv.result.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "注册登录")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final IUserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        System.out.println(dto);
        LoginVO vo = userService.login(dto);
        return ResultUtils.success(vo);
    }

    @PutMapping("/refreshToken")
    @Operation(summary = "刷新token", description = "用refreshtoken换取新的token")
    public Result refreshToken(@RequestHeader("refreshToken") String refreshToken) {
        LoginVO vo = userService.refreshToken(refreshToken);
        return ResultUtils.success(vo);
    }

    @PutMapping("/registerByPhone")
    @Operation(summary = "用户注册", description = "用户通过手机号注册")
    public Result registerByPhone(@Valid @RequestBody RgisterByPhoneDTO dto) {
        LoginVO vo = userService.registerByPhone(dto);
        return ResultUtils.success(vo);
    }



}
