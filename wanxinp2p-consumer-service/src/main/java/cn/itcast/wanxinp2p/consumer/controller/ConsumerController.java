package cn.itcast.wanxinp2p.consumer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "用户服务的Controller", description = "用户服务API")
public class ConsumerController {

    @Operation(summary = "测试hello")
    @GetMapping(path = "/hello")
    public String hello(){
        return "hello";
    }

    @Operation(summary = "测试hi")
    @PostMapping(value = "/hi")
    public String hi(@Parameter(name = "name", description = "姓名", required = true) String name){
        return "hi,"+name;
    }

}
