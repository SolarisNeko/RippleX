package pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author SolarisNeko
 * @date 2022-02-22
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Comparable{

    Integer id;
    String name;
    Integer age;
    String job;
    Double salary;

    @Override
    public int compareTo(Object o) {
        if (o instanceof User) {
            User otherUser = (User) o;
            return this.getSalary().compareTo(otherUser.getSalary());
        }
        return 0;
    }
}
