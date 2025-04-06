package com.example.airlines.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    @GetMapping(value = {"/{path:^(?!api|static|js|css|media|index\\.html).*}", "/"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
