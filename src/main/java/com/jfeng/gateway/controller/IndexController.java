package com.jfeng.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@CrossOrigin("*")
public class IndexController {
    /**
     * 首次加载所有可以展示的协议
     *
     * @param model
     * @return
     */
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("allProtocols", null);
        return "index";
    }
}
