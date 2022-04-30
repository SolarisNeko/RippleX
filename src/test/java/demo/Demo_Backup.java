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
        // group by 字段做 distinct 处理, 其余做 aggregate 操作
        List<String> groupColumnList = new ArrayList<String>() {{
//            add("job");
            add("type");
        }};

        List<String> excludeColumnList = new ArrayList<String>() {{
//            add("name");
//            add("id");
        }};

        Map<String, AggregateType> handleMap = new HashMap<String, AggregateType>() {{
//            put("id", AggregateOption.MAX);
//            put("age", AggregateOption.SUM);
//            put("salary", AggregateOption.MIN);
            put("name", AggregateType.COUNT);
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
                .data(users)
                .returnType(User.class)
                .aggregateRelationMap(handleMap)
                .groupColumnNames(groupColumnList)
                .excludeColumnNames(excludeColumnList)
                .build();


        System.out.println(ripple);
    }

}
