package com.neko.ripple;

import com.neko.ripple.calculate.RippleCalculator;
import com.neko.ripple.pojo.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication
public class RippleAlgorithmApp {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Map<String, Integer> ripple = RippleCalculator.ripple(new ArrayList<User>() {{
            add(User.builder().job("worker").age(18).build());
            add(User.builder().job("worker").age(30).build());
            add(User.builder().job("worker").age(40).build());
        }}, "sum", "job");

        System.out.println(ripple);
    }

}
