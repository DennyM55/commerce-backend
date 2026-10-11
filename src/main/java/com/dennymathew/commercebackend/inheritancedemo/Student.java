package com.dennymathew.commercebackend.inheritancedemo;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Student extends User {

    private String college;
}
