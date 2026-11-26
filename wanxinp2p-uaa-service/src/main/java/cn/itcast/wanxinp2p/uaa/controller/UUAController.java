package cn.itcast.wanxinp2p.uaa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UUAController {

    @GetMapping("/hi")
    public String hi() {
        return "hi";
    }
}
