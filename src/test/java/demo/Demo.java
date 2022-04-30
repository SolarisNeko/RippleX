package demo;

import com.neko233.ripple.RippleX;
import com.neko233.ripple.constant.AggregateType;
import pojo.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Demo {

    // group by 字段做 distinct 处理, 其余做 aggregate 操作
    public static void main(String[] args) {


        /**
         * 聚合配置
         */
        Map<String, AggregateType> aggregateRelationMap = new HashMap<String, AggregateType>() {{
            put("age", AggregateType.SUM);
            put("salary", AggregateType.SUM);
        }};

        /**
         * Data
         */
        // group by a, b, c, d | json -> a, b |
        List<User> dataList = new ArrayList<User>() {{
            add(User.builder().id(1).name("neko").job("worker").age(18).salary(1000d).build());
            add(User.builder().id(2).name("doge").job("worker").age(30).salary(2000d).build());
            add(User.builder().id(3).name("duck").job("worker").age(40).salary(1000d).build());
            add(User.builder().id(3).name("boss").job("boss").age(66).salary(666666d).build());
        }};

        // Ripple 水波
        // TODO ORM test
        List<User> ripple = RippleX.builder()
                .data(dataList)
                .groupColumnNames("job")
                .excludeColumnNames("id")
                .aggregateRelationMap(aggregateRelationMap)
                .returnType(User.class)
                .build();

        ripple.forEach(System.out::println);
    }

}
