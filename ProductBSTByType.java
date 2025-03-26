import java.util.ArrayList;

public class ProductBSTByType {
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
        int cmp = p.getType().compareToIgnoreCase(root.data.getType());
        if (cmp < 0) {
            root.left = insertRec(root.left, p);
        } else if (cmp > 0) {
            root.right = insertRec(root.right, p);
        } else {
            // If type is the same, we can decide how to handle.
            // For simplicity, compare by name to keep tree stable:
            int nameCmp = p.getName().compareToIgnoreCase(root.data.getName());
            if (nameCmp < 0) {
                root.left = insertRec(root.left, p);
            } else if (nameCmp > 0) {
                root.right = insertRec(root.right, p);
            }
            // else ignore duplicates
        }
        return root;
    }

    public Product search(String type) {
        // This is tricky because we can have multiple products of the same type.
        // We'll just return the *first* match we find for demonstration.
        return searchRec(root, type);
    }

    private Product searchRec(Node root, String type) {
        if (root == null) return null;
        int cmp = type.compareToIgnoreCase(root.data.getType());
        if (cmp == 0) {
            // found a matching type
            return root.data;
        } else if (cmp < 0) {
            return searchRec(root.left, type);
        } else {
            return searchRec(root.right, type);
        }
    }

    // remove requires both type + name to identify the exact product
    public void remove(String type, String name) {
        root = removeRec(root, type, name);
    }

    private Node removeRec(Node root, String type, String name) {
        if (root == null) return null;
        int cmp = type.compareToIgnoreCase(root.data.getType());
        if (cmp < 0) {
            root.left = removeRec(root.left, type, name);
        } else if (cmp > 0) {
            root.right = removeRec(root.right, type, name);
        } else {
            // same type, now compare name
            int nameCmp = name.compareToIgnoreCase(root.data.getName());
            if (nameCmp < 0) {
                root.left = removeRec(root.left, type, name);
            } else if (nameCmp > 0) {
                root.right = removeRec(root.right, type, name);
            } else {
                // found node to remove
                if (root.left == null) return root.right;
                if (root.right == null) return root.left;
                // both children
                Node min = findMin(root.right);
                root.data = min.data;
                root.right = removeRec(root.right, min.data.getType(), min.data.getName());
            }
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
