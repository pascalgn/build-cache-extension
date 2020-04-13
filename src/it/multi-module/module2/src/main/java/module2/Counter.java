package module2;

import module1.Calculator;

public class Counter {
    private int value;

    public int getValue() {
        return value;
    }

    public void increment() {
        this.value = Calculator.add(this.value, 1);
    }
}
