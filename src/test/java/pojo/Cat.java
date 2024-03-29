package pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @title:
 * @description:
 * @author: SolarisNeko
 * @date: 2/23/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cat {

    Integer id;

    String name;

    String type;

    Integer price;

    Integer appearCount;


}
