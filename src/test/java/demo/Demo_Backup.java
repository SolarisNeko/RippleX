package demo;

import com.neko233.ripple.RippleX;
import com.neko233.ripple.constant.AggregateType;
import org.junit.jupiter.api.Test;
import pojo.Cat;
import pojo.User;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Demo_Backup {

    @Test
    public void test() {

        List<Cat> cats = new ArrayList<Cat>() {{
            add(Cat.builder()
                    .id(1)
                    .name("Zzz")
                    .type("布偶")
                    .build());
            add(Cat.builder()
                    .id(1)
                    .name("小花")
                    .type("布偶")
                    .build());
            add(Cat.builder()
                    .id(1)
                    .name("halo")
                    .type("英美")
                    .build());
            add(Cat.builder()
                    .id(1)
                    .name("Zzz")
                    .type("布偶")
                    .build());
        }};

        List<Cat> ripple = RippleX.builder()
                .data(cats)
                .groupColumnNames("type")
                .excludeColumnNames("id")
                .aggregateRelationMap(new HashMap<String, AggregateType>() {{
                    put("name", AggregateType.COUNT);
                }})
                .returnType(Cat.class)
                .build();


        ripple.forEach(System.out::println);

    }

}
