package com.neko.ripple.demo;

import com.neko.ripple.RippleX;
import com.neko.ripple.constant.AggregateOption;
import com.neko.ripple.pojo.Cat;
import com.neko.ripple.pojo.User;
import com.neko.ripple.pojo.Animal;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Demo {

    public static void main(String[] args) {
        // group by 字段做 distinct 处理, 其余做 aggregate 操作
        List<String> groupColumnList = new ArrayList<String>() {{
//            add("job");
            add("type");
        }};

        List<String> excludeColumnList = new ArrayList<String>() {{
//            add("name");
//            add("id");
        }};

        Map<String, AggregateOption> handleMap = new HashMap<String, AggregateOption>() {{
//            put("id", AggregateOption.MAX);
//            put("age", AggregateOption.SUM);
//            put("salary", AggregateOption.MIN);
            put("name", AggregateOption.COUNT);
        }};

        List<User> users = new ArrayList<User>() {{
            add(User.builder().id(1).name("neko").job("worker").age(18).salary(1000d).build());
            add(User.builder().id(2).name("doge").job("worker").age(30).salary(2000d).build());
            add(User.builder().id(3).name("duck").job("worker").age(40).salary(1000d).build());
        }};
        List<Cat> cats = new ArrayList<Cat>() {{
            add(new Cat(1, "Zzz", "布偶"));
            add(new Cat(2, "小花", "布偶"));
            add(new Cat(3, "哈撒给", "英美"));
        }};

        List<Map<String, Object>> ripple = RippleX.builder()
            .data(cats)
            .schema(Cat.class)
            .aggOperateMap(handleMap)
            .groupColumns(groupColumnList)
            .exclude(excludeColumnList)
            .build();


        System.out.println(ripple);
    }

}
