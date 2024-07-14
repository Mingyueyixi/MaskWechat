package com.lu.wxmask.clazz;

import org.junit.Test;

import java.lang.reflect.Array;

public class ClazzTest {

    @Test
    public void getStringArrayClazz() {
        String[] strings = new String[]{"1", "2", "3"};
        Object hh = Array.newInstance(String.class, 8);
        assert strings.getClass().equals(hh.getClass());
        assert "[Ljava.lang.String;".equals(hh.getClass().getName());
    }

    @Test
    public void getObjectArrayClazz() {
        Object[] objs = new Object[]{};
        System.out.println(objs.getClass().getName());
    }
}
