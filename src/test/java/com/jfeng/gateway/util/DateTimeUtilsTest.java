package com.jfeng.gateway.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateTimeUtilsTest extends TestCase {


    @Test
    public void test1(){
        System.out.println(ZonedDateTime.now().toEpochSecond());
        System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        System.out.println(System.currentTimeMillis());
    }
}