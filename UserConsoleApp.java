import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserConsoleApp {

    static final String USER_FILE = "data/users.json";
    static final String WORKER_FILE = "data/worker_accounts.json";
    static final String SERVICE_FILE = "data/services.json";
    static final String BOOKING_FILE = "data/bookings.json";

    static Scanner scanner = new Scanner(System.in);
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();


    static void migrateOldUsers() {
        List<User> users = loadUsers();
        boolean changed = false;

        for (User user : users) {
            if (user.name == null || user.name.trim().isEmpty()) {
                user.name = user.username.split("@")[0]; // Default name = username prefix
                changed = true;
            }
        }

        if (changed) {
            writeUsers(users);
            System.out.println("✅ Migration complete. Missing names added.");
        } else {
            System.out.println("✅ All users already have names.");
        }
    }


    public static void main(String[] args) {


        ensureFileExists(USER_FILE);
        ensureFileExists(WORKER_FILE);
        ensureFileExists(SERVICE_FILE);
        ensureFileExists(BOOKING_FILE);

        while (true) {
            System.out.println("\n🌟 Welcome to Urban Club 🌟");
            System.out.println("1. Login");
            System.out.println("0. Register");
            System.out.print("👉 Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("❌ Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear input
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 1 && login()) {
                userDashboard();
            } else if (choice == 0) {
                registerUser();
            } else {
                System.out.println("❌ Invalid choice.");
            }
        }
    }

    static User loggedInUser = null; // Add this at top instead of `String currentUser`

    static boolean login() {
        System.out.print("👤 Enter Email: ");
        String username = scanner.nextLine().trim();

        List<User> users = loadUsers();
        Optional<User> userOpt = users.stream()
                .filter(u -> u.username.equalsIgnoreCase(username))
                .findFirst();

        if (!userOpt.isPresent()) {
            System.out.println("❌ Username not found. Please register first.");
            return false;
        }

        System.out.print("🔐 Enter Password: ");
        String password = scanner.nextLine();

        if (userOpt.get().password.equals(password)) {
            loggedInUser = userOpt.get();
            System.out.println("✅ Login successful!");
            System.out.println("👋 Welcome, " + loggedInUser.name + "!");
            return true;
        } else {
            System.out.println("❌ Incorrect password.");
            return false;
        }
    }





    static void registerUser() {
        System.out.println("\n📝 Register New User");

        System.out.print("👤 Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("📧 Enter Username (Email): ");
        String username = scanner.nextLine().trim();

        if (!(username.endsWith("@gmail.com") || username.endsWith("@urbanclub.com"))) {
            System.out.println("❌ Invalid email. Must end with @gmail.com or @urbanclub.com");
            return;
        }

        List<User> users = loadUsers();
        for (User u : users) {
            if (u.username.equalsIgnoreCase(username)) {
                System.out.println("❌ Username already taken. Try another one.");
                return;
            }
        }

        System.out.print("🔐 Enter Password: ");
        String password = scanner.nextLine().trim();

        System.out.print("📍 Enter Address: ");
        String address = scanner.nextLine().trim();

        System.out.print("📞 Enter Phone Number: ");
        String phone = scanner.nextLine().trim();

        // Generate a unique user ID like USR1001
        String userId = "USR" + (1000 + users.size() + 1);

        users.add(new User(userId, name, username, password, address, phone));
        writeUsers(users);

        System.out.println("✅ Registration successful! Your User ID is: " + userId);
        System.out.println("🔐 You can now login.");
    }





    static void userDashboard() {
        while (true) {
            System.out.println("\n📋 1. Book Service\n📂 2. View My Bookings\n🚪 0. Logout");
            System.out.print("👉 Choose option: ");

            if (!scanner.hasNextInt()) {
                System.out.println("❌ Invalid input. Enter a number.");
                scanner.nextLine();
                continue;
            }

            int option = scanner.nextInt();
            scanner.nextLine();

            if (option == 1) {
                bookService();
            } else if (option == 2) {
                viewBookings();
            } else if (option == 0) {
            loggedInUser = null;
            System.out.println("👋 Logged out.");
            System.out.println("\n🙏 Thank you for using Urban Club. Have a great day!");
            System.exit(0);
            } else {
                System.out.println("❌ Invalid option.");
            }
        }
    }


    static void bookService() {
        List<Booking> bookings = readJson(BOOKING_FILE, new TypeToken<List<Booking>>() {}.getType());
        List<User> users = loadUsers();

        User user = loggedInUser;



        long count = bookings.stream()
                .filter(b -> b.username.equals(user.username))
                .count();

        if (count >= 3) {
            System.out.println("❗ You have already booked 3 services.");
            return;
        }

        List<String> services = readJson(SERVICE_FILE, new TypeToken<List<String>>() {}.getType());


        if (services.isEmpty()) {
            System.out.println("⚠️ No services available.");
            return;
        }

        System.out.println("\n🛠️ Available Services:");
        for (int i = 0; i < services.size(); i++) {
            System.out.println((i + 1) + ". " + services.get(i));
        }

        System.out.print("👉 Choose a service: ");
        int index = scanner.nextInt() - 1;
        scanner.nextLine();

        if (index < 0 || index >= services.size()) {
            System.out.println("❌ Invalid service selection.");
            return;
        }

        String selectedService = services.get(index);

        List<Worker> workers = readJson(WORKER_FILE, new TypeToken<List<Worker>>() {}.getType());

        Optional<Worker> availableWorker = workers.stream()
                .filter(w -> w.isAvailable && w.service.equalsIgnoreCase(selectedService))
                .findFirst();

        if (availableWorker.isPresent()) {
            Worker assigned = availableWorker.get();
            assigned.isAvailable = false;

            // ✨ Include full user details
            Map<String, String> bookingMap = new LinkedHashMap<>();
            bookingMap.put("userId", user.userId);
            bookingMap.put("username", user.username);
            bookingMap.put("address", user.address);
            bookingMap.put("phone", user.phone);
            bookingMap.put("service", selectedService);
            bookingMap.put("worker", assigned.username);
            bookingMap.put("status", "Pending");
            bookingMap.put("timestamp", String.valueOf(System.currentTimeMillis()));


            Booking booking = new Booking(
                    user.userId,
                    user.username,
                    user.address,
                    user.phone,
                    selectedService,
                    assigned.username,
                    "Pending",
                    System.currentTimeMillis()
            );
            bookings.add(booking);
            writeJson(BOOKING_FILE, bookings);
            writeJson(WORKER_FILE, workers);

            System.out.println("📨 Booking sent to " + assigned.username + ". Waiting for confirmation...");

            // Auto-cancel logic remains same...
            new Timer().schedule(new TimerTask() {
                public void run() {
                    List<Booking> updatedBookings = readJson(BOOKING_FILE, new TypeToken<List<Booking>>() {}.getType());

                    for (Booking b : updatedBookings) {
                        if (b.username.equals(user.username) &&
                                b.timestamp == booking.timestamp &&
                                b.status.equals("Pending")) {

                            b.status = "Auto-Cancelled";
                            assigned.isAvailable = true;

                            writeJson(BOOKING_FILE, updatedBookings);

                            List<Worker> updatedWorkers = readJson(WORKER_FILE, new TypeToken<List<Worker>>() {}.getType());
                            for (Worker w : updatedWorkers) {
                                if (w.username.equals(assigned.username)) {
                                    w.isAvailable = true;
                                    break;
                                }
                            }
                            writeJson(WORKER_FILE, updatedWorkers);

                            System.out.println("⏰ Booking auto-cancelled due to no response.");
                            break;
                        }
                    }
                }
            }, 2 * 60 * 1000); // 2 minutes

        } else {
            System.out.println("😔 No available workers for " + selectedService + " currently.");
        }
    }



    static void viewBookings() {
        if (loggedInUser == null) {
            System.out.println("❌ Not logged in.");
            return;
        }

        List<Booking> bookings = readJson(BOOKING_FILE, new TypeToken<List<Booking>>() {}.getType());
        boolean found = false;
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        for (Booking b : bookings) {
            if (b.username.equalsIgnoreCase(loggedInUser.username)) {
                found = true;
                String dt = formatter.format(new Date(b.timestamp));
                System.out.println("\n🔹 Service: " + b.service +
                        "\n👷 Worker: " + b.worker +
                        "\n📌 Status: " + formatStatus(b.status) +
                        "\n📅 Date & Time: " + dt);
            }
        }

        if (!found) {
            System.out.println("❌ No bookings found.");
        }
    }

    static String formatStatus(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "🟡 Pending";
            case "confirmed":
                return "🟢 Confirmed";
            case "canceled":
                return "🔴 Canceled";
            case "auto-cancelled":
                return "⚫ Auto-Cancelled";
            default:
                return status;
        }
    }


    static void ensureFileExists(String path) {
        File file = new File(path);
        try {
            File parent = file.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                System.err.println("❌ Failed to create directory: " + parent);
            }

            if (!file.exists()) {
                if (file.createNewFile()) {
                    try (Writer writer = new FileWriter(file)) {
                        writer.write("[]");
                    }
                } else {
                    System.err.println("❌ Failed to create file: " + path);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error ensuring file: " + e.getMessage());
        }
    }

    static List<User> loadUsers() {
        Type userListType = new TypeToken<List<User>>() {}.getType();
        return readJson(USER_FILE, userListType);
    }

    static void writeUsers(List<User> users) {
        writeJson(USER_FILE, users);
    }

    static <T> List<T> readJson(String filePath, Type type) {
        File file = new File(filePath);
        try (Reader reader = new FileReader(file)) {
            List<T> list = gson.fromJson(reader, type);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    static <T> void writeJson(String filePath, List<T> data) {
        File file = new File(filePath);
        try {
            File parent = file.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                System.err.println("❌ Failed to create directory for: " + filePath);
            }
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            System.out.println("❌ File write error: " + e.getMessage());
        }
    }

    // Data Classes
    static class User {
        String userId;
        String name;
        String username;
        String password;
        String address;
        String phone;

        User(String userId, String name, String username, String password, String address, String phone) {
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.password = password;
            this.address = address;
            this.phone = phone;
        }
    }



    static class Worker {
        String username;
        String password;
        String service;
        boolean isAvailable;

        Worker(String username, String password, String service, boolean isAvailable) {
            this.username = username;
            this.password = password;
            this.service = service;
            this.isAvailable = isAvailable;
        }
    }


    static class Booking {
        String userId;
        String username;
        String address;
        String phone;
        String service;
        String worker;
        String status;
        long timestamp;

        Booking(String userId, String username, String address, String phone,
                String service, String worker, String status, long timestamp) {
            this.userId = userId;
            this.username = username;
            this.address = address;
            this.phone = phone;
            this.service = service;
            this.worker = worker;
            this.status = status;
            this.timestamp = timestamp;
        }
    }

}
