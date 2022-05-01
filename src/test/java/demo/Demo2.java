package demo;

import org.junit.jupiter.api.Test;
import pojo.Cat;
import pojo.User;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Demo2 {

    @Test
    public void test() {

        List<User> users = new ArrayList<User>() {{
            add(User.builder().id(1).name("neko").job("worker").age(18).salary(1000d).build());
            add(User.builder().id(2).name("doge").job("worker").age(30).salary(2000d).build());
            add(User.builder().id(3).name("duck").job("worker").age(40).salary(1000d).build());
        }};
        List<Cat> cats = new ArrayList<Cat>() {{
            add(Cat.builder()
                    .name("Zzz")
                    .type("布偶")
                    .price(1000)
                    .build());
            add(Cat.builder()
                    .name("小花")
                    .type("布偶")
                    .price(1000)
                    .build());
            add(Cat.builder()
                    .name("哈撒给")
                    .type("布偶")
                    .price(1000)
                    .build());
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
