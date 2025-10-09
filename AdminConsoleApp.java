import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.file.*;


public class AdminConsoleApp {
    static final String ADMIN_FILE = "data/admins.json";
    static final String USER_FILE = "data/users.json";
    static final String WORKER_FILE = "data/worker_accounts.json";
    static final String BOOKING_FILE = "data/bookings.json";
    static final String SERVICE_FILE = "data/services.json";
    static final Scanner sc = new Scanner(System.in);
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n🛡️ Urban Club - Admin Panel 🛡️");
            System.out.println("1. Login");
            System.out.println("0. Register");
            System.out.print("👉 Enter your choice: ");
            String choice = sc.nextLine();
            if (choice.equals("1")) {
                if (adminLogin()) break;
            } else if (choice.equals("0")) {
                adminRegister();
            } else {
                System.out.println("❌ Invalid choice");
            }
        }

        while (true) {
            System.out.println("\n📋 Admin Menu:");
            System.out.println("1. View All Users");
            System.out.println("2. View All Workers");
            System.out.println("3. View All Bookings");
            System.out.println("4. View All Services");
            System.out.println("5. Add New Service");
            System.out.println("6. View Stats");
            System.out.println("0. Exit");
            System.out.print("👉 Choose option: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1" :
                        viewAllUsers();
                        break;
                case "2" :
                    viewAllWorkers();
                    break;
                case "3" :
                    viewAll(BOOKING_FILE, "Bookings");
                    break;
                case "4" :
                    viewAll(SERVICE_FILE, "Services");
                    break;
                case "5" :
                    addService();
                    break;
                case "6" :
                    showStats();
                    break;
                case "0" :
                {
                    System.out.println("👋 Exiting Admin Panel. Goodbye!");
                    return;
                }
                default :
                    System.out.println("❌ Invalid choice");
                    break;
            }
        }
    }

    // ============ Admin Auth ============
    static boolean adminLogin() {
        List<Map<String, String>> admins = readJsonList(ADMIN_FILE, new TypeToken<List<Map<String, String>>>() {}.getType());

        System.out.print("👤 Enter Admin email: ");
        String username = sc.nextLine().trim();

        if (!username.endsWith("@urbanclub.com")) {
            System.out.println("❌ Email must end with @urbanclub.com");
            return false;
        }

        // Check if exists
        Map<String, String> matchedAdmin = null;
        for (Map<String, String> admin : admins) {
            if (admin.get("username").equals(username)) {
                matchedAdmin = admin;
                break;
            }
        }

        if (matchedAdmin == null) {
            System.out.println("❌ No such username found. Please register first.");
            return false;
        }

        // Ask password only if username is found
        System.out.print("🔐 Enter Password: ");
        String password = sc.nextLine().trim();

        if (matchedAdmin.get("password").equals(password)) {
            System.out.println("✅ Login Successful! Welcome " + matchedAdmin.get("name"));
            return true;
        } else {
            System.out.println("❌ Incorrect password.");
            return false;
        }
    }




    static void adminRegister() {
        List<Map<String, String>> admins = readJsonList(ADMIN_FILE, new TypeToken<List<Map<String, String>>>() {}.getType());

        System.out.print("🧑 Enter Admin Name: ");
        String name = sc.nextLine().trim();

        System.out.print("👤 Enter Admin email: ");
        String username = sc.nextLine().trim();

        if (!username.endsWith("@urbanclub.com")) {
            System.out.println("❌ Username must end with @urbanclub.com");
            return;
        }

        boolean exists = admins.stream().anyMatch(admin -> admin.get("username").equals(username));
        if (exists) {
            System.out.println("❌ This username is already registered.");
            return;
        }

        System.out.print("🔐 Create Password: ");
        String password = sc.nextLine().trim();

        Map<String, String> newAdmin = new HashMap<>();
        newAdmin.put("name", name);
        newAdmin.put("username", username);
        newAdmin.put("password", password);

        admins.add(newAdmin);
        writeJsonList(ADMIN_FILE, admins);

        System.out.println("✅ Admin registered successfully.");
    }



    // ============ Service Methods ============
    static void addService() {
        List<String> services = readJsonList(SERVICE_FILE, new TypeToken<List<String>>() {}.getType());

        System.out.print("➕ Enter new service name: ");
        String name = sc.nextLine().trim();

        if (services.contains(name)) {
            System.out.println("⚠️ Service already exists.");
            return;
        }

        services.add(name);
        writeJsonList(SERVICE_FILE, services);
        System.out.println("✅ Service added successfully.");
    }

    // 📋 Show only usernames of all registered users
    static void viewAllUsers() {
        System.out.println("\n👥 Registered Users:");
        List<Map<String, Object>> users = readJsonList(USER_FILE, new TypeToken<List<Map<String, Object>>>() {}.getType());

        if (users.isEmpty()) {
            System.out.println("🚫 No users found.");
            return;
        }

        System.out.printf("%-10s %-20s %-30s\n", "User ID", "Name", "Username");
        System.out.println(repeat("-", 60));

        for (Map<String, Object> user : users) {
            String userId = String.valueOf(user.get("userId")); // Get userId from users.json
            String name = String.valueOf(user.get("name"));
            String username = String.valueOf(user.get("username"));

            System.out.printf("%-10s %-20s %-30s\n", userId, name, username);
        }
    }



    // 🔧 Show workers with username, service, and availability only
    static void viewAllWorkers() {
        List<Map<String, Object>> workers = readJsonList(WORKER_FILE, new TypeToken<List<Map<String, Object>>>() {}.getType());

        if (workers.isEmpty()) {
            System.out.println("\n🚫 No workers found.");
            return;
        }

        // Auto-assign workerId if missing
        boolean updated = false;
        for (int i = 0; i < workers.size(); i++) {
            Map<String, Object> w = workers.get(i);
            if (!w.containsKey("workerId")) {
                w.put("workerId", "W" + (i + 1));
                updated = true;
            }
        }
        if (updated) {
            writeJsonList(WORKER_FILE, workers);
        }

        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) workers.size() / pageSize);
        int currentPage = 1;

        while (true) {
            int start = (currentPage - 1) * pageSize;
            int end = Math.min(start + pageSize, workers.size());

            System.out.printf("\n👷 All Workers (Page %d/%d):\n", currentPage, totalPages);
            System.out.printf("%-10s %-20s %-25s %-15s %-10s\n", "ID", "Name", "Username", "Service", "Available");
            System.out.println(repeat("-", 80));

            for (int i = start; i < end; i++) {
                Map<String, Object> w = workers.get(i);
                String id = String.valueOf(w.getOrDefault("workerId", "N/A"));
                String username = String.valueOf(w.getOrDefault("username", "N/A"));
                String service = String.valueOf(w.getOrDefault("service", "N/A"));
                boolean isAvailable = Boolean.parseBoolean(String.valueOf(w.getOrDefault("isAvailable", false)));
                String icon = isAvailable ? "✅" : "❌";

                // 🧠 Auto-generate name from username
                String name = "N/A";
                if (!"N/A".equals(username) && username.contains("@")) {
                    String part = username.split("@")[0];
                    name = part.substring(0, 1).toUpperCase() + part.substring(1);
                }

                System.out.printf("%-10s %-20s %-25s %-15s %-10s\n", id, name, username, service, icon);
            }

            System.out.println("\n📦 Options: [n] next | [p] previous | [e] export | [q] quit");
            System.out.print("👉 Choose: ");
            String action = sc.nextLine().trim().toLowerCase();

            if (action.equals("n") && currentPage < totalPages) {
                currentPage++;
            } else if (action.equals("p") && currentPage > 1) {
                currentPage--;
            } else if (action.equals("e")) {
                exportWorkersToFile(workers);
            } else if (action.equals("q")) {
                break;
            }
        }
    }



    // ✅ Helper method for repeat (since Java 8 doesn’t have String.repeat)
    static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }



    static void exportWorkersToFile(List<Map<String, Object>> workers) {
        String fileName = "workers_export.txt";
        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.printf("%-20s %-20s %-15s\n", "Username", "Service", "Available");
            writer.println("=".repeat(55));
            for (Map<String, Object> worker : workers) {
                String username = String.valueOf(worker.get("username"));
                String service = String.valueOf(worker.get("service"));
                boolean isAvailable = Boolean.parseBoolean(String.valueOf(worker.get("isAvailable")));
                String icon = isAvailable ? "✅" : "❌";
                writer.printf("%-20s %-20s %-15s\n", username, service, icon);
            }
            System.out.println("📁 Workers exported to " + fileName + " successfully!");
        } catch (IOException e) {
            System.out.println("❌ Failed to export workers: " + e.getMessage());
        }
    }





    // ============ Stats ============
    static void showStats() {
        List<Map<String, Object>> workers = readJsonList(WORKER_FILE, new TypeToken<List<Map<String, Object>>>(){}.getType());
        List<Map<String, Object>> bookings = readJsonList(BOOKING_FILE, new TypeToken<List<Map<String, Object>>>(){}.getType());

        long availableWorkers = workers.stream().filter(w -> Boolean.parseBoolean(String.valueOf(w.get("isAvailable")))).count();
        long pendingBookings = bookings.stream().filter(b -> "Pending".equalsIgnoreCase(String.valueOf(b.get("status")))).count();

        System.out.println("\n📊 Urban Club Stats:");
        System.out.println("✅ Available Workers: " + availableWorkers);
        System.out.println("⏳ Pending Bookings : " + pendingBookings);
        System.out.println("📦 Total Bookings   : " + bookings.size());
        System.out.println("👷 Total Workers    : " + workers.size());

        // 🧾 Optional: Show Names
        System.out.println("\n👥 Worker Summary:");
        for (Map<String, Object> w : workers) {
            String name = String.valueOf(w.getOrDefault("name", "N/A"));
            String service = String.valueOf(w.getOrDefault("service", "N/A"));
            boolean available = Boolean.parseBoolean(String.valueOf(w.getOrDefault("isAvailable", false)));
            System.out.printf("   - %s (%s) %s\n", name, service, available ? "✅ Available" : "❌ Busy");
        }

        System.out.println("\n📦 Booking Summary:");
        for (Map<String, Object> b : bookings) {
            String username = String.valueOf(b.getOrDefault("username", "N/A"));
            String service = String.valueOf(b.getOrDefault("service", "N/A"));
            String status = String.valueOf(b.getOrDefault("status", "N/A"));
            System.out.printf("   - %s booked %s [%s]\n", username, service, status);
        }
    }


    // ============ Common View ============
    static void viewAll(String file, String label) {
        System.out.println("\n📄 All " + label + ":");

        if (file.equals(SERVICE_FILE)) {
            List<String> services = readJsonList(file, new TypeToken<List<String>>(){}.getType());
            for (String service : services) {
                System.out.println("🛠️ " + service);
            }

        } else if (file.equals(BOOKING_FILE)) {
            List<Map<String, Object>> bookings = readJsonList(file, new TypeToken<List<Map<String, Object>>>(){}.getType());

            if (bookings.isEmpty()) {
                System.out.println("🚫 No bookings found.");
                return;
            }

            // Header
            System.out.printf("%-10s %-25s %-12s %-12s %-12s %-25s %-18s %-20s\n",
                    "User ID", "Username", "Address", "Phone", "Service", "Worker", "Status", "Time");
            System.out.println("-".repeat(140));

            for (Map<String, Object> booking : bookings) {
                String userId = String.valueOf(booking.get("userId"));
                String username = String.valueOf(booking.get("username"));
                String address = String.valueOf(booking.get("address"));
                String phone = String.valueOf(booking.get("phone"));
                String service = String.valueOf(booking.get("service"));
                String worker = String.valueOf(booking.get("worker"));
                String status = String.valueOf(booking.get("status"));
                long timestamp = Long.parseLong(String.valueOf(booking.get("timestamp")));
                String time = formatTimestamp(timestamp);

                System.out.printf("%-10s %-25s %-12s %-12s %-12s %-25s %-18s %-20s\n",
                        userId, username, address, phone, service, worker, status, time);
            }

        } else {
            List<Map<String, Object>> list = readJsonList(file, new TypeToken<List<Map<String, Object>>>(){}.getType());
            for (Map<String, Object> item : list) {
                System.out.println(item);
            }
        }
    }

    static String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }


    // ============ JSON Read/Write ============
    public static <T> List<T> readJsonList(String filePath, Type typeOfT) {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            Gson gson = new Gson();
            return gson.fromJson(reader, typeOfT);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    static <T> void writeJsonList(String fileName, List<T> list) {
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            System.err.println("❌ Error writing " + fileName);
        }
    }
}
