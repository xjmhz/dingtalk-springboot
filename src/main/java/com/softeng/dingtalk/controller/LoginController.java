package com.softeng.dingtalk.controller;

import com.softeng.dingtalk.component.DingTalkUtils;
import com.softeng.dingtalk.component.EncryptorComponent;
import com.softeng.dingtalk.entity.User;
import com.softeng.dingtalk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhanyeye
 * @description
 * @date 11/13/2019
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class LoginController {
    //为了防止伪造角色
    private static final String USER_ROLE = "bb63e5f7e0f2ffae845c";
    private static final String AUDITOR_AUTHORITY = "pb53e2f7g0f2hfanp4sx";
    private static final String ADMIN_ROLE = "6983f953b49c88210cb9";

    @Autowired
    DingTalkUtils dingTalkUtils;
    @Autowired
    UserService userService;
    @Autowired
    EncryptorComponent encryptorComponent;


    /**
     * @description 用户登录
     * @param authcode：免登授权码
     * @return java.util.Map
     * @date 9:17 AM 12/11/2019
     **/
    @PostMapping("/login")
    public void login(@RequestBody Map authcode, HttpServletResponse response) {
        String userid = dingTalkUtils.getUserId((String) authcode.get("authcode"));  //根据免登授权码获取userid
        User user = userService.getUser(userid); //去数据库查找用户
        if (user == null) { //如果用户不存在，调用钉钉API获取用户信息，将用户导入数据库
            user = dingTalkUtils.getNewUser(userid);
            userService.addUser(user);
        }
        Map map = Map.of("uid", user.getId(), "authorityid", user.getAuthority());
        // 生成加密token
        String token = encryptorComponent.encrypt(map);
        // 在header创建自定义的权限
        response.setHeader("token",token);
        String role = null;
        if (user.getAuthority() == User.USER_AUTHORITY) {
            role = USER_ROLE;
        } else if (user.getAuthority() == User.AUDITOR_AUTHORITY) {
            role = AUDITOR_AUTHORITY;
        } else {
            role = ADMIN_ROLE;
        }
        log.debug(token);
        response.setHeader("role", role);
        response.setHeader("uid", user.getId() + "");
    }
}
