package demo;

import com.neko233.ripple.RippleX;
import com.neko233.ripple.config.MeasureConfig;
import com.neko233.ripple.constant.AggregateType;
import org.junit.jupiter.api.Test;
import pojo.Cat;
import pojo.CatStatistics;

import java.util.ArrayList;
import java.util.List;

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

        /**
         * dimension > measure
         */
        List<CatStatistics> ripple = RippleX.builder()
                .data(cats)
                .dimensionColumnNames("type")
                .excludeColumnNames("id")
                .measureConfig(MeasureConfig.builder()
                        .set("type", AggregateType.COUNT, "count")
                )
                .returnType(CatStatistics.class)
                .build();


        ripple.forEach(System.out::println);

    }

}
