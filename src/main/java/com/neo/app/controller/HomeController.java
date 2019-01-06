package com.neo.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 应用控制器
 * Created by 杨颖 on 2018/12/1.
 */
@Controller
public class HomeController {

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "用户已登录");
        return "index";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("message", "管理员界面");
        return "index";
    }

    @RequestMapping("/403")
    public String error(Model model) {
        model.addAttribute("message", "没有权限访问");
        return "index";
    }

    @RequestMapping("/logout")
    public String logout() {
        return "login";
    }
}
