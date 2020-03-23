package com.octo.tools.crud.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {
	
    @RequestMapping("/admin")
    public String index() {
        return "admin/index.html";
    }
}