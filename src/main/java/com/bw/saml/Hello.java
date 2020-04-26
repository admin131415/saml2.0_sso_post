package com.bw.saml;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Hello {
    @RequestMapping("/index")
    public String index(){
        System.out.println(123);
        return "index";
    }
}
