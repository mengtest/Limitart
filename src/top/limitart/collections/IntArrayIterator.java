package top.limitart.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 数组迭代器
 *
 * @author hank
 * @version 2018/2/12 0012 13:45
 */
public class IntArrayIterator implements Iterator<Integer> {
    private final int[] array;
    private int curIndex = 0;

    public IntArrayIterator(int[] array) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return this.curIndex < array.length;
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException("no more element,please call hasNext() to check");
        }
        return array[this.curIndex++];
    }
}
