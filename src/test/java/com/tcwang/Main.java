package com.tcwang;

import com.tcwang.interfaces.core.FillUpMappingUtils;
import com.tcwang.entity.Customer;
import com.tcwang.entity.DataFromUser;

import java.util.Date;


/**
 * @author: JKin
 * @createTime: 2023/7/19 13:01
 * @fileDescriptions: You had better view this comment before you use it.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        DataFromUser build = DataFromUser.builder()
                .name("Jkin")
                .age(24)
                .habit("rup")
                .birthday(new Date())
                .joinTime(new Date())
                .id("1023")
                .build();

        long startTime = System.currentTimeMillis();
        //Customer customer = new Customer();
        Customer customer1 = new Customer();
       // Customer customer2 = FillUpMappingUtils.fillUpByObject(build, customer);
        Customer customer3 = FillUpMappingUtils.fillUpByObject(build, customer1);
        //System.out.println(customer2);
        //System.out.println(customer3);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime);

    }
}
