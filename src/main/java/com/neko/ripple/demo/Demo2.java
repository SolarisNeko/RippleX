package com.neko.ripple.demo;

import com.neko.ripple.RippleX;
import com.neko.ripple.constant.AggregateOption;
import com.neko.ripple.pojo.Cat;
import com.neko.ripple.pojo.User;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class Demo2 {

    public static void main(String[] args) {

        List<User> users = new ArrayList<User>() {{
            add(User.builder().id(1).name("neko").job("worker").age(18).salary(1000d).build());
            add(User.builder().id(2).name("doge").job("worker").age(30).salary(2000d).build());
            add(User.builder().id(3).name("duck").job("worker").age(40).salary(1000d).build());
        }};
        List<Cat> cats = new ArrayList<Cat>() {{
            add(new Cat(1, "Zzz", "布偶", 10000));
            add(new Cat(1, "小花", "布偶", 10000));
            add(new Cat(3, "哈撒给", "英美", 10000));
        }};


//        Map<String, List<Cat>> collect = cats.stream().collect(Collectors.groupingBy(t -> t.getType() + ">" + t.getId()));

        List<Cat> collect = cats.stream().collect(Collectors.groupingBy(t -> t.getType() + ">" + t.getId()))
                .values()
                .stream().map(values -> {
                    return values.stream().reduce((t1, t2) -> {
                        t1.setPrice(t1.getPrice() + t2.getPrice());
                        return t1;
                    }).orElse(null);
                }).collect(Collectors.toList());

        System.out.println(collect);

    }

}
