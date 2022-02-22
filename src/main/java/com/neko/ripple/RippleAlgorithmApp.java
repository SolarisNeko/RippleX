package com.neko.ripple;

import com.neko.ripple.calculate.RippleCalculator;
import com.neko.ripple.pojo.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class RippleAlgorithmApp {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        List<String> groupColumnList = new ArrayList<String>() {{
            add("name");
        }};
        List<String> exceptColumnList = new ArrayList<String>() {{
            add("job");
        }};

        Map<String, Object> ripple = RippleCalculator.ripple(new ArrayList<User>() {{
            add(User.builder().name("neko").job("worker").age(18).build());
            add(User.builder().name("doge").job("worker").age(30).build());
            add(User.builder().name("duck").job("worker").age(40).build());
        }}, "sum", groupColumnList, exceptColumnList);


        System.out.println(ripple);
    }

}
