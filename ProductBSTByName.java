import java.util.ArrayList;

public class ProductBSTByName {
    private class Node {
        Product data;
        Node left, right;

        Node(Product p) {
            data = p;
        }
    }

    private Node root;

    public void insert(Product p) {
        root = insertRec(root, p);
    }

    private Node insertRec(Node root, Product p) {
        if (root == null) {
            return new Node(p);
        }
        int cmp = p.getName().compareToIgnoreCase(root.data.getName());
        if (cmp < 0) {
            root.left = insertRec(root.left, p);
        } else if (cmp > 0) {
            root.right = insertRec(root.right, p);
        } else {
            // same name => update or ignore. We'll ignore for simplicity.
        }
        return root;
    }

    public Product search(String name) {
        Node current = root;
        while (current != null) {
            int cmp = name.compareToIgnoreCase(current.data.getName());
            if (cmp == 0) {
                return current.data;
            } else if (cmp < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    public void remove(String name) {
        root = removeRec(root, name);
    }

    private Node removeRec(Node root, String name) {
        if (root == null) return null;
        int cmp = name.compareToIgnoreCase(root.data.getName());
        if (cmp < 0) {
            root.left = removeRec(root.left, name);
        } else if (cmp > 0) {
            root.right = removeRec(root.right, name);
        } else {
            // Found node to remove
            if (root.left == null) return root.right;
            if (root.right == null) return root.left;
            // Both children exist
            // find min in right subtree
            Node min = findMin(root.right);
            root.data = min.data;
            root.right = removeRec(root.right, min.data.getName());
        }
        return root;
    }

    private Node findMin(Node root) {
        while (root.left != null) {
            root = root.left;
        }
        return root;
    }

    public void inOrder(ArrayList<Product> list) {
        inOrderRec(root, list);
    }

    private void inOrderRec(Node root, ArrayList<Product> list) {
        if (root == null) return;
        inOrderRec(root.left, list);
        list.add(root.data);
        inOrderRec(root.right, list);
    }
}
