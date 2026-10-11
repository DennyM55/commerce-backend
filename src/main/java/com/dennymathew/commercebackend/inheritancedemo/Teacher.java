
package com.dennymathew.commercebackend.inheritancedemo;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Teacher extends User {

    private Double salary;
}
