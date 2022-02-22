package com.neko.ripple;

import com.neko.ripple.calculate.RippleX;
import com.neko.ripple.constant.AggregateOption;
import com.neko.ripple.pojo.User;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class RippleAlgorithmApp {

    public static void main(String[] args) {
        // group by 字段做 distinct 处理, 其余做 aggregate 操作
        List<String> groupColumnList = new ArrayList<String>() {{
            add("name");
        }};
        List<String> exceptColumnList = new ArrayList<String>() {{
            add("job");
            add("id");
        }};
        Map<String, AggregateOption> handleMap = new HashMap<String, AggregateOption>() {{
            put("id", AggregateOption.MAX);
            put("age", AggregateOption.SUM);
        }};

        ArrayList<User> users = new ArrayList<User>() {{
            add(User.builder().id(1).name("neko").job("worker").age(18).build());
            add(User.builder().id(2).name("doge").job("worker").age(30).build());
            add(User.builder().id(3).name("duck").job("worker").age(40).build());
        }};

        List<Map<String, Object>> ripple = RippleX.builder()
            .data(users)
            .aggOperateMap(handleMap)
            .groupColumns(groupColumnList)
            .except(exceptColumnList)
            .build();


        System.out.println(ripple);
    }

}
