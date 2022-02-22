package com.neko.ripple.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LuoHaoJun
 * @date 2022-02-22
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    Integer id;
    String name;
    Integer age;
    String job;

}
