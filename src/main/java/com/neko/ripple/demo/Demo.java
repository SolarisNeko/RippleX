package com.neko.ripple.demo;

import com.neko.ripple.RippleX;
import com.neko.ripple.constant.AggregateOption;
import com.neko.ripple.pojo.User;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Demo {

    // group by 字段做 distinct 处理, 其余做 aggregate 操作
    public static void main(String[] args) {

        List<String> excludeColumnList = new ArrayList<String>() {{
            add("id");
        }};

        List<String> groupColumnList = new ArrayList<String>() {{
            add("job");
        }};

        Map<String, AggregateOption> handleMap = new HashMap<String, AggregateOption>() {{
            put("age", AggregateOption.MIN);
            put("salary", AggregateOption.MAX);
        }};

        // group by a, b, c, d | json -> a, b |
        List<User> users = new ArrayList<User>() {{
            add(User.builder().id(1).name("neko").job("worker").age(18).salary(1000d).build());
            add(User.builder().id(2).name("doge").job("worker").age(30).salary(2000d).build());
            add(User.builder().id(3).name("duck").job("worker").age(40).salary(1000d).build());
        }};

        // Ripple 波纹模块构建
        List<Map<String, Object>> ripple = RippleX.builder()
                .data(users)
                .schema(User.class)
                .aggOperateMap(handleMap)
                .groupColumns(groupColumnList)
                .exclude(excludeColumnList)
                .build();
        System.out.println(ripple);
    }

}
