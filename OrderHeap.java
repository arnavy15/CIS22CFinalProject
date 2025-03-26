import java.util.ArrayList;

public class OrderHeap {
    private ArrayList<Order> heap;

    public OrderHeap() {
        heap = new ArrayList<>();
    }

    public void copyFrom(OrderHeap other) {
        this.heap.clear();
        this.heap.addAll(other.heap);
    }

    public void insert(Order o) {
        heap.add(o);
        siftUp(heap.size() - 1);
    }

    public Order remove() {
        if (heap.isEmpty()) return null;
        Order root = heap.get(0);
        Order last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return root;
    }

    public Order peek() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    private void siftUp(int idx) {
        while (idx > 0) {
            int parent = (idx - 1) / 2;
            if (heap.get(idx).getPriority() > heap.get(parent).getPriority()) {
                swap(idx, parent);
                idx = parent;
            } else {
                break;
            }
        }
    }

    private void siftDown(int idx) {
        int left, right, largest;
        while (true) {
            left = 2 * idx + 1;
            right = 2 * idx + 2;
            largest = idx;

            if (left < heap.size() && heap.get(left).getPriority() > heap.get(largest).getPriority()) {
                largest = left;
            }
            if (right < heap.size() && heap.get(right).getPriority() > heap.get(largest).getPriority()) {
                largest = right;
            }

            if (largest != idx) {
                swap(idx, largest);
                idx = largest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        Order temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
