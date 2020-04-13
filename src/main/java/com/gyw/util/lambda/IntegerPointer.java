package com.gyw.util.lambda;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Integer pointer
 * @author guyw
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntegerPointer {

    private int value = 0;

    /**
     * copy constructor
     * @param integerPointer
     */
    public IntegerPointer(IntegerPointer integerPointer) {
        this.value = integerPointer.getValue();
    }

    /**
     * increment by one
     */
    public void incr() {
        value++;
    }

    /**
     * decrement by one
     */
    public void decr() {
        value--;
    }

    /**
     * increment
     * @param increment increment
     */
    public void incrBy(int increment) {
        value += increment;
    }

    /**
     * decrement
     * @param decrement decrement
     */
    public void decrBy(int decrement) {
        value -= decrement;
    }

    /**
     * difference of IntegerPointer pointers
     */
    public int diff(IntegerPointer integerPointer) {
        return this.value - integerPointer.getValue();
    }
}
