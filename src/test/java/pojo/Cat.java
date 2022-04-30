package pojo;

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
public class Cat extends Animal {

    public Cat(Integer id, String name, String type, Integer price) {
        super(id, name);
        this.type = type;
        this.price = price;
    }

    public Cat(Integer id, String name, String type) {
        super(id, name);
        this.type = type;
    }

    String type;

    Integer price;

}