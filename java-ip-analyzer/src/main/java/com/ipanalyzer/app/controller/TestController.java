package com.ipanalyzer.app.controller;

import com.ipanalyzer.app.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gjd
 */
@RequestMapping(value = "/test")
@RestController
public class TestController {
    @Autowired
    TestService testService;

    @GetMapping(value = "/test2")
    public String run(){
        return testService.test();
    }
}
