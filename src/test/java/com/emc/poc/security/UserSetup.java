package com.emc.poc.security;

import com.emc.poc.Application;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@SpringApplicationConfiguration(classes = Application.class)
public class UserSetup extends AbstractTestNGSpringContextTests {

    @Test
    public void test() {

        assert(true);

    }

}
