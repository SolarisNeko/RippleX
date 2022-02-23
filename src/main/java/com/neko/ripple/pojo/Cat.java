package com.neko.ripple.pojo;

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
public class Cat extends Animal {

    public Cat(Integer id, String name, String type) {
        super(id, name);
        this.type = type;
    }

    public Cat(String type) {
        this.type = type;
    }

    String type;

}
