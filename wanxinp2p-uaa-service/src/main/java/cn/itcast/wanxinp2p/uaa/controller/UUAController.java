package cn.itcast.wanxinp2p.uaa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UUAController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "authentication_error", required = false) String authenticationError,
                        Model model) {
        model.addAttribute("authentication_error", authenticationError);
        return "login";
    }
}
