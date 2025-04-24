package com.ipanalyzer.app.service.impl;

import com.ipanalyzer.app.service.TestService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author gjd
 */
@Service
@ConditionalOnProperty(value = "fact",havingValue = "a")
public class AServiceImpl implements TestService {
    @Override
    public String test() {
        System.out.println(this.getClass().getName());
        return this.getClass().getName();
    }
}
