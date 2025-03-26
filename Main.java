import java.util.*;
import java.io.*;

public class Main {
    private static Scanner sc = new Scanner(System.in);

    // Data structures
    private static HashTableUser userTable = new HashTableUser(20); // for Customers, Employees, Managers
    private static ProductBSTByName bstByName = new ProductBSTByName();
    private static ProductBSTByType bstByType = new ProductBSTByType();
    private static OrderHeap orderHeap = new OrderHeap();

    // LinkedLists for shipped/unshipped orders (per user or global)
    // We'll store shipped orders globally, but you could also store them per user if needed
    private static OrderLinkedList shippedOrders = new OrderLinkedList();

    // In-memory list of all orders (for searching by ID, or you can store them in a separate DS)
    private static ArrayList<Order> allOrders = new ArrayList<>();

    public static void main(String[] args) {
        // 1) Load data from files
        loadUsers("customers.txt", "employees.txt", "managers.txt");
        loadProducts("products.txt");
        loadOrders("orders.txt");

        boolean exitProgram = false;
        while (!exitProgram) {
            System.out.println("\nWelcome to the Musical Instrument Store!");
            System.out.println("1) Login as Customer");
            System.out.println("2) Create New Customer Account");
            System.out.println("3) Login as Employee");
            System.out.println("4) Login as Manager");
            System.out.println("5) Login as Guest");
            System.out.println("6) Exit");
            System.out.print("Choose an option: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                // ignore, will loop
            }

            switch (choice) {
                case 1:
                    handleCustomerLogin();
                    break;
                case 2:
                    createNewCustomer();
                    break;
                case 3:
                    handleEmployeeLogin();
                    break;
                case 4:
                    handleManagerLogin();
                    break;
                case 5:
                    handleGuestSession();
                    break;
                case 6:
                    exitProgram = true;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
                    break;
            }
        }

        // 5) On exit, write updated data to files
        saveUsers("customers.txt", "employees.txt", "managers.txt");
        saveProducts("products.txt");
        saveOrders("orders.txt");

        System.out.println("Thank you for using the Musical Instrument Store system!");
    }

    // --------------------------
    // Loading / Saving Data
    // --------------------------
    private static void loadUsers(String customersFile, String employeesFile, String managersFile) {
        // Load customers
        try (BufferedReader br = new BufferedReader(new FileReader(customersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                // Format: username,password,firstName,lastName
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Customer c = new Customer(parts[0], parts[1], parts[2], parts[3]);
                    userTable.insert(c.getUsername(), c);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load customers: " + e.getMessage());
        }

        // Load employees
        try (BufferedReader br = new BufferedReader(new FileReader(employeesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                // Format: username,password,firstName,lastName
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Employee emp = new Employee(parts[0], parts[1], parts[2], parts[3]);
                    userTable.insert(emp.getUsername(), emp);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load employees: " + e.getMessage());
        }

        // Load managers
        try (BufferedReader br = new BufferedReader(new FileReader(managersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                // Format: username,password,firstName,lastName
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Manager mgr = new Manager(parts[0], parts[1], parts[2], parts[3]);
                    userTable.insert(mgr.getUsername(), mgr);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load managers: " + e.getMessage());
        }
    }

    private static void loadProducts(String productsFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(productsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                // Format: instrumentName, instrumentType, price, brand, quantityInStock, description
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String name = parts[0];
                    String type = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    String brand = parts[3];
                    int qty = Integer.parseInt(parts[4]);
                    String desc = parts[5];

                    Product p = new Product(name, type, price, brand, qty, desc);
                    bstByName.insert(p);
                    bstByType.insert(p);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load products: " + e.getMessage());
        }
    }

    private static void loadOrders(String ordersFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(ordersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                // Format: orderID,username,productName,shippingMethod
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int orderID = Integer.parseInt(parts[0]);
                    String username = parts[1];
                    String productName = parts[2];
                    String shippingStr = parts[3];

                    Order.ShippingMethod method = Order.ShippingMethod.valueOf(shippingStr.toUpperCase());
                    Order o = new Order(orderID, username, productName, method);
                    allOrders.add(o);
                    // Put it in the heap as unshipped
                    orderHeap.insert(o);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load orders: " + e.getMessage());
        }
    }

    private static void saveUsers(String customersFile, String employeesFile, String managersFile) {
        // For simplicity, we will iterate through the entire hash table
        // and separate out customers, employees, managers based on object type
        ArrayList<Customer> customers = new ArrayList<>();
        ArrayList<Employee> employees = new ArrayList<>();
        ArrayList<Manager> managers = new ArrayList<>();

        // Gather from hash table
        ArrayList<User> allUsers = userTable.getAllUsers();
        for (User u : allUsers) {
            if (u instanceof Customer) {
                customers.add((Customer) u);
            } else if (u instanceof Employee) {
                employees.add((Employee) u);
            } else if (u instanceof Manager) {
                managers.add((Manager) u);
            }
        }

        // Save customers
        try (PrintWriter pw = new PrintWriter(new FileWriter(customersFile))) {
            pw.println("# Format: username,password,firstName,lastName");
            for (Customer c : customers) {
                pw.println(c.getUsername() + "," + c.getPassword() + ","
                        + c.getFirstName() + "," + c.getLastName());
            }
        } catch (IOException e) {
            System.out.println("Error saving customers: " + e.getMessage());
        }

        // Save employees
        try (PrintWriter pw = new PrintWriter(new FileWriter(employeesFile))) {
            pw.println("# Format: username,password,firstName,lastName");
            for (Employee emp : employees) {
                pw.println(emp.getUsername() + "," + emp.getPassword() + ","
                        + emp.getFirstName() + "," + emp.getLastName());
            }
        } catch (IOException e) {
            System.out.println("Error saving employees: " + e.getMessage());
        }

        // Save managers
        try (PrintWriter pw = new PrintWriter(new FileWriter(managersFile))) {
            pw.println("# Format: username,password,firstName,lastName");
            for (Manager mgr : managers) {
                pw.println(mgr.getUsername() + "," + mgr.getPassword() + ","
                        + mgr.getFirstName() + "," + mgr.getLastName());
            }
        } catch (IOException e) {
            System.out.println("Error saving managers: " + e.getMessage());
        }
    }

    private static void saveProducts(String productsFile) {
        // We'll do an in-order traversal by name to ensure we don't get duplicates
        ArrayList<Product> products = new ArrayList<>();
        bstByName.inOrder(products);

        try (PrintWriter pw = new PrintWriter(new FileWriter(productsFile))) {
            pw.println("# Format: instrumentName, instrumentType, price, brand, quantityInStock, description");
            for (Product p : products) {
                pw.println(p.getName() + "," + p.getType() + "," + p.getPrice() + ","
                        + p.getBrand() + "," + p.getQuantityInStock() + "," + p.getDescription());
            }
        } catch (IOException e) {
            System.out.println("Error saving products: " + e.getMessage());
        }
    }

    private static void saveOrders(String ordersFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ordersFile))) {
            pw.println("# Format: orderID,username,productName,shippingMethod");
            for (Order o : allOrders) {
                pw.println(o.getOrderID() + "," + o.getUsername() + "," + o.getProductName() + "," + o.getShippingMethod());
            }
        } catch (IOException e) {
            System.out.println("Error saving orders: " + e.getMessage());
        }
    }

    // --------------------------
    // Menu Handling
    // --------------------------
    private static void handleCustomerLogin() {
        System.out.print("Enter username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        User u = userTable.search(user);
        if (u != null && u instanceof Customer && u.getPassword().equals(pass)) {
            System.out.println("Login successful. Welcome, " + ((Customer) u).getFirstName() + "!");
            customerMenu((Customer) u);
        } else {
            System.out.println("Invalid credentials or not a customer.");
        }
    }

    private static void createNewCustomer() {
        System.out.print("Choose a username: ");
        String user = sc.nextLine();
        if (userTable.search(user) != null) {
            System.out.println("That username is already taken!");
            return;
        }
        System.out.print("Choose a password: ");
        String pass = sc.nextLine();
        System.out.print("First name: ");
        String fName = sc.nextLine();
        System.out.print("Last name: ");
        String lName = sc.nextLine();

        Customer c = new Customer(user, pass, fName, lName);
        userTable.insert(c.getUsername(), c);
        System.out.println("Account created successfully!");
    }

    private static void handleEmployeeLogin() {
        System.out.print("Enter username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        User u = userTable.search(user);
        if (u != null && u instanceof Employee && u.getPassword().equals(pass)) {
            System.out.println("Login successful. Welcome, " + ((Employee) u).getFirstName() + "!");
            employeeMenu((Employee) u);
        } else {
            System.out.println("Invalid credentials or not an employee.");
        }
    }

    private static void handleManagerLogin() {
        System.out.print("Enter username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        User u = userTable.search(user);
        if (u != null && u instanceof Manager && u.getPassword().equals(pass)) {
            System.out.println("Login successful. Welcome, " + ((Manager) u).getFirstName() + "!");
            managerMenu((Manager) u);
        } else {
            System.out.println("Invalid credentials or not a manager.");
        }
    }

    private static void handleGuestSession() {
        System.out.println("Welcome, Guest! You have limited access. You can search/list products, but cannot order.");
        guestMenu();
    }

    // --------------------------
    // Customer Menu
    // --------------------------
    private static void customerMenu(Customer c) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n-- Customer Menu --");
            System.out.println("1) Search for a product by Name (Primary Key)");
            System.out.println("2) Search for a product by Type (Secondary Key)");
            System.out.println("3) List all products (by Name)");
            System.out.println("4) List all products (by Type)");
            System.out.println("5) Place an Order");
            System.out.println("6) View Purchases (Shipped)");
            System.out.println("7) View Purchases (Unshipped)");
            System.out.println("8) Quit and Save");

            System.out.print("Choose an option: ");
            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                // ignore
            }

            switch (choice) {
                case 1:
                    searchProductByName();
                    break;
                case 2:
                    searchProductByType();
                    break;
                case 3:
                    listProductsByName();
                    break;
                case 4:
                    listProductsByType();
                    break;
                case 5:
                    placeOrder(c);
                    break;
                case 6:
                    viewShippedOrders(c);
                    break;
                case 7:
                    viewUnshippedOrders(c);
                    break;
                case 8:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    // --------------------------
    // Employee Menu
    // --------------------------
    private static void employeeMenu(Employee e) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n-- Employee Menu --");
            System.out.println("1) Search for an Order by Order ID");
            System.out.println("2) Search for an Order by Customer Name");
            System.out.println("3) View Order with Highest Priority");
            System.out.println("4) View All Orders Sorted by Priority");
            System.out.println("5) Ship an Order");
            System.out.println("6) Quit and Save");

            System.out.print("Choose an option: ");
            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException ex) {
                // ignore
            }

            switch (choice) {
                case 1:
                    searchOrderByID();
                    break;
                case 2:
                    searchOrderByCustomerName();
                    break;
                case 3:
                    viewHighestPriorityOrder();
                    break;
                case 4:
                    viewAllOrdersByPriority();
                    break;
                case 5:
                    shipAnOrder();
                    break;
                case 6:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    // --------------------------
    // Manager Menu
    // --------------------------
    private static void managerMenu(Manager m) {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n-- Manager Menu --");
            System.out.println("1) (Manager) Update Products - Add New Product");
            System.out.println("2) (Manager) Update Existing Product (price, description, or stock)");
            System.out.println("3) (Manager) Remove a Product");
            System.out.println("4) [Also has Employee Options]");
            System.out.println("5) Quit and Save");

            System.out.print("Choose an option: ");
            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException ex) {
                // ignore
            }

            switch (choice) {
                case 1:
                    addNewProduct();
                    break;
                case 2:
                    updateExistingProduct();
                    break;
                case 3:
                    removeProduct();
                    break;
                case 4:
                    // Jump to employee menu for manager
                    employeeMenu(m);
                    break;
                case 5:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    // --------------------------
    // Guest Menu
    // --------------------------
    private static void guestMenu() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n-- Guest Menu --");
            System.out.println("1) Search for a product by Name");
            System.out.println("2) Search for a product by Type");
            System.out.println("3) List all products by Name");
            System.out.println("4) List all products by Type");
            System.out.println("5) Quit");

            System.out.print("Choose an option: ");
            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException ex) {
                // ignore
            }

            switch (choice) {
                case 1:
                    searchProductByName();
                    break;
                case 2:
                    searchProductByType();
                    break;
                case 3:
                    listProductsByName();
                    break;
                case 4:
                    listProductsByType();
                    break;
                case 5:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    // --------------------------
    // Customer Actions
    // --------------------------
    private static void searchProductByName() {
        System.out.print("Enter product name: ");
        String name = sc.nextLine();
        Product found = bstByName.search(name);
        if (found != null) {
            System.out.println(found);
        } else {
            System.out.println("Product not found.");
        }
    }

    private static void searchProductByType() {
        System.out.print("Enter product type: ");
        String type = sc.nextLine();
        Product found = bstByType.search(type);
        if (found != null) {
            System.out.println(found);
        } else {
            System.out.println("Product not found by that type.");
        }
    }

    private static void listProductsByName() {
        ArrayList<Product> products = new ArrayList<>();
        bstByName.inOrder(products);

        // Print header
        System.out.println("\nPRODUCTS SORTED BY NAME");
        System.out.printf("%-20s %-15s %-10s %-15s %-10s %-30s\n",
                "NAME", "TYPE", "PRICE", "BRAND", "QTY", "DESCRIPTION");
        System.out.println("-----------------------------------------------------------------------------------------------");

        // Print each product in a formatted row
        for (Product p : products) {
            System.out.printf("%-20s %-15s $%-9.2f %-15s %-10d %-30s\n",
                    p.getName(),
                    p.getType(),
                    p.getPrice(),
                    p.getBrand(),
                    p.getQuantityInStock(),
                    p.getDescription());
        }
    }

    private static void listProductsByType() {
        ArrayList<Product> products = new ArrayList<>();
        bstByType.inOrder(products);

        // Print header
        System.out.println("\nPRODUCTS SORTED BY TYPE");
        System.out.printf("%-20s %-15s %-10s %-15s %-10s %-30s\n",
                "NAME", "TYPE", "PRICE", "BRAND", "QTY", "DESCRIPTION");
        System.out.println("-----------------------------------------------------------------------------------------------");

        // Print each product in a formatted row
        for (Product p : products) {
            System.out.printf("%-20s %-15s $%-9.2f %-15s %-10d %-30s\n",
                    p.getName(),
                    p.getType(),
                    p.getPrice(),
                    p.getBrand(),
                    p.getQuantityInStock(),
                    p.getDescription());
        }
    }


    private static void placeOrder(Customer c) {
        System.out.print("Enter product name to order: ");
        String productName = sc.nextLine();
        // Check if product exists
        Product p = bstByName.search(productName);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }
        System.out.println("Shipping methods available:");
        System.out.println("1) OVERNIGHT");
        System.out.println("2) RUSH");
        System.out.println("3) STANDARD");
        System.out.print("Choose: ");
        int smChoice = -1;
        try {
            smChoice = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            // ignore
        }

        Order.ShippingMethod method;
        switch (smChoice) {
            case 1: method = Order.ShippingMethod.OVERNIGHT; break;
            case 2: method = Order.ShippingMethod.RUSH; break;
            default: method = Order.ShippingMethod.STANDARD; break;
        }

        // Generate a new order ID
        int newOrderID = 1000 + allOrders.size() + 1;
        Order newOrder = new Order(newOrderID, c.getUsername(), productName, method);
        allOrders.add(newOrder);
        orderHeap.insert(newOrder);
        System.out.println("Order placed! Your order ID is: " + newOrderID);
    }

    private static void viewShippedOrders(Customer c) {
        // We can filter the shippedOrders list by this customer's username
        shippedOrders.displayByUsername(c.getUsername());
    }

    private static void viewUnshippedOrders(Customer c) {
        // Filter from the orderHeap or from allOrders where shipping is not done
        // Easiest: check allOrders for orders that are not shipped yet and match username
        System.out.println("Unshipped Orders for " + c.getUsername() + ":");
        for (Order o : allOrders) {
            if (o.getUsername().equals(c.getUsername()) && !o.isShipped()) {
                System.out.println(o);
            }
        }
    }

    // --------------------------
    // Employee Actions
    // --------------------------
    private static void searchOrderByID() {
        System.out.print("Enter Order ID: ");
        int id = -1;
        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        Order found = null;
        for (Order o : allOrders) {
            if (o.getOrderID() == id) {
                found = o;
                break;
            }
        }

        if (found != null) {
            System.out.println(found);
        } else {
            System.out.println("Order not found.");
        }
    }

    private static void searchOrderByCustomerName() {
        System.out.print("Enter Customer First Name: ");
        String fName = sc.nextLine();
        System.out.print("Enter Customer Last Name: ");
        String lName = sc.nextLine();

        // We can look up the user by searching in userTable
        // Then check orders for that username
        // For simplicity, let's just brute force across all orders
        boolean anyFound = false;
        for (Order o : allOrders) {
            User u = userTable.search(o.getUsername());
            if (u != null && u instanceof Customer) {
                Customer c = (Customer) u;
                if (c.getFirstName().equalsIgnoreCase(fName) && c.getLastName().equalsIgnoreCase(lName)) {
                    System.out.println(o);
                    anyFound = true;
                }
            }
        }
        if (!anyFound) {
            System.out.println("No orders found for that customer name.");
        }
    }

    private static void viewHighestPriorityOrder() {
        Order highest = orderHeap.peek();
        if (highest != null) {
            System.out.println("Highest Priority Order: " + highest);
        } else {
            System.out.println("No orders in the heap.");
        }
    }

    private static void viewAllOrdersByPriority() {
        // We can copy the heap, pop all to display in sorted order, then re-insert
        OrderHeap tempHeap = new OrderHeap();
        // Copy current
        tempHeap.copyFrom(orderHeap);

        System.out.println("All orders sorted by priority (highest first):");
        while (!tempHeap.isEmpty()) {
            Order o = tempHeap.remove();
            System.out.println(o);
        }
    }

    private static void shipAnOrder() {
        // Remove from heap, mark as shipped, and add to shipped linked list
        Order o = orderHeap.remove();
        if (o == null) {
            System.out.println("No unshipped orders to ship.");
            return;
        }
        o.setShipped(true);
        shippedOrders.insert(o);
        System.out.println("Order " + o.getOrderID() + " has been shipped.");
    }

    private static void addNewProduct() {
        System.out.print("Enter new product name (primary key): ");
        String name = sc.nextLine();
        if (bstByName.search(name) != null) {
            System.out.println("A product with that name already exists!");
            return;
        }
        System.out.print("Enter product type (secondary key): ");
        String type = sc.nextLine();
        System.out.print("Enter price: ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("Enter brand: ");
        String brand = sc.nextLine();
        System.out.print("Enter quantity in stock: ");
        int qty = Integer.parseInt(sc.nextLine());
        System.out.print("Enter description: ");
        String desc = sc.nextLine();

        Product p = new Product(name, type, price, brand, qty, desc);
        bstByName.insert(p);
        bstByType.insert(p);
        System.out.println("Product added successfully.");
    }

    private static void updateExistingProduct() {
        System.out.print("Enter product name to update: ");
        String name = sc.nextLine();
        Product p = bstByName.search(name);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }
        System.out.println("What would you like to update?");
        System.out.println("1) Price");
        System.out.println("2) Description");
        System.out.println("3) Add more to Stock");
        int choice = Integer.parseInt(sc.nextLine());
        switch (choice) {
            case 1:
                System.out.print("Enter new price: ");
                double newPrice = Double.parseDouble(sc.nextLine());
                p.setPrice(newPrice);
                break;
            case 2:
                System.out.print("Enter new description: ");
                String newDesc = sc.nextLine();
                p.setDescription(newDesc);
                break;
            case 3:
                System.out.print("How many to add to stock? ");
                int addQty = Integer.parseInt(sc.nextLine());
                p.setQuantityInStock(p.getQuantityInStock() + addQty);
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        System.out.println("Product updated successfully.");
    }

    private static void removeProduct() {
        System.out.print("Enter product name to remove: ");
        String name = sc.nextLine();
        Product p = bstByName.search(name);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }
        bstByName.remove(name);
        // Also remove from type BST
        bstByType.remove(p.getType(), name);
        System.out.println("Product removed successfully.");
    }
}
