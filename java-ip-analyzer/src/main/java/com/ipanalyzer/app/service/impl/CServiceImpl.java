package com.ipanalyzer.app.service.impl;

import com.ipanalyzer.app.service.TestService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @author gjd
 */
@Service
@Primary
@ConditionalOnProperty(value = "30",havingValue = "true")
public class CServiceImpl implements TestService {
    @Override
    public String test() {
        System.out.println(this.getClass().getName());
        return this.getClass().getName();
    }
}
