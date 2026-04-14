package dev.forint.deafmute.modules.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/hello")
    public String hello() {
        return "hello campus lost found";
    }
}