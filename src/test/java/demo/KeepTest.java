package demo;

import com.neko233.ripple.RippleX;
import com.neko233.ripple.config.MeasureConfig;
import com.neko233.ripple.constant.AggregateType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pojo.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeepTest {

    // group by 字段做 distinct 处理, 其余做 aggregate 操作
    @Test
    public void test() {
        /**
         * Data
         */
        List<User> dataList = new ArrayList<User>() {{
            add(User.builder().id(1).name("neko").job("worker").age(10).salary(1000d).build());
            add(User.builder().id(2).name("doge").job("worker").age(20).salary(2000d).build());
            add(User.builder().id(3).name("doge").job("worker").age(30).salary(1000d).build());
            add(User.builder().id(4).name("boss").job("boss").age(40).salary(666666d).build());
        }};


        // Ripple 水波
        List<User> build = RippleX.builder()
                .data(dataList)
                .dimensionColumnNames("name")
//                .excludeColumnNames("id")
                .measureConfig(MeasureConfig.builder()
                        .set("id", AggregateType.KEEP_FIRST)
                        .set("name", AggregateType.KEEP_FIRST)
                        // Sum
                        .set("age", AggregateType.SUM, "age")
                )
                .returnType(User.class)
                .build();
        List<User> ripple = build.stream()
                .sorted(User::compareTo)
                .collect(Collectors.toList());

        Assertions.assertEquals(40, ripple.get(0).getAge());
        Assertions.assertEquals(50, ripple.get(1).getAge());
        Assertions.assertEquals(10, ripple.get(2).getAge());
    }

}
