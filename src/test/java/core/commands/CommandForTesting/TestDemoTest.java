package core.commands.CommandForTesting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestDemoTest {

    @Test
    void add() {
         TestDemo t = new TestDemo();
         int result  = t.add(10,20);
         assertEquals(30,result);
    }

    @Test
    void concatenate() {
        TestDemo t = new TestDemo();
        String result  = t.concatenate("hello","world");
        assertEquals("helloworld",result);
    }
}