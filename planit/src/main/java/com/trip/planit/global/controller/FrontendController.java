package com.trip.planit.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    @RequestMapping(value = { "/", "/splash", "/register", "/login", "/mypage", "/**/{path:[^\\.]*}" })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}

