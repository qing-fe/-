package com.huang.redblack;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class Randomtest {
    @Test
    public void testRandom() {
        Random r=new Random();
        for (int i = 0; i < 256; i++) {
            System.out.print(r.nextInt(10));
        }
    }
}
