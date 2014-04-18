package co.kuznetsov.util;

/**
 * Highly efficient fixed size int stack. It doesn't create any garbage
 */
public class SimpleStack {
    private int arr[];
    int top = -1;

    public SimpleStack(int capacity) {
        arr = new int[capacity];
    }

    public void push(int pushedElement) {
        if (top < arr.length - 1) {
            top++;
            arr[top] = pushedElement;
        } else {
            throw new RuntimeException("Stack overflow: increase capacity!");
        }
    }

    public int pop() {
        if (top >= 0) {
            int res = arr[top];
            top--;
            return res;
        } else {
            throw new RuntimeException("Stack is empty");
        }
    }

    public boolean empty() {
        return top < 0;
    }

    public void clear() {
        top = -1;
    }
}

