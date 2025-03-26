public class OrderLinkedList {
    private class Node {
        Order data;
        Node next;

        Node(Order o) {
            data = o;
        }
    }

    private Node head;

    public void insert(Order o) {
        Node n = new Node(o);
        n.next = head;
        head = n;
    }

    public void displayByUsername(String username) {
        Node current = head;
        boolean foundAny = false;
        while (current != null) {
            if (current.data.getUsername().equals(username)) {
                System.out.println(current.data);
                foundAny = true;
            }
            current = current.next;
        }
        if (!foundAny) {
            System.out.println("No shipped orders found for user " + username);
        }
    }
}
