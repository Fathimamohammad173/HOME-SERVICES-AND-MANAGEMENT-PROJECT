import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class WorkerConsoleApp {

    //static final String WORKER_ACCOUNTS_FILE = "data/worker_accounts.json";
    static final String WORKER_ACCOUNTS_FILE = "data/worker_accounts.json";
    static final String BOOKINGS_FILE = "data/bookings.json";

    static Scanner sc = new Scanner(System.in);
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static String currentWorker = null;
    static String workerService = null;

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n👷 Welcome to Urban Club - Worker Panel 👷");
            System.out.println("1. Login");
            System.out.println("0. Register");
            System.out.print("👉 Enter your choice: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    if (login()) {
                        showMenu();
                    }
                    break;
                case "0":
                    register();
                    break;
                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }

    private static boolean login() {
        System.out.print("👤 Enter Worker Email: ");
        String username = sc.nextLine().trim();

        List<Worker> accounts = readWorkers();
        Worker foundWorker = accounts.stream()
                .filter(w -> w.username.equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);

        if (foundWorker == null) {
            System.out.println("❌ Username not found. Please register first.");
            return false;
        }

        System.out.print("🔐 Enter Password: ");
        String password = sc.nextLine();

        if (!foundWorker.password.equals(password)) {
            System.out.println("❌ Incorrect password.");
            return false;
        }

        currentWorker = foundWorker.username;
        workerService = foundWorker.service;
        foundWorker.isAvailable = true;
        writeWorkers(accounts);

        System.out.println("✅ Login successful! Welcome, " + foundWorker.name);
        System.out.println("🔧 Your Service: " + workerService);
        return true;
    }



    private static void register() {
        System.out.println("\n📝 Register New Worker");

        System.out.print("👤 Enter Full Name: ");
        String name = sc.nextLine().trim();

        System.out.print("📧 Enter Username (Email): ");
        String username = sc.nextLine().trim();

        if (!username.endsWith("@urbanclub.com")) {
            System.out.println("❌ Username must end with @urbanclub.com");
            return;
        }

        List<Worker> accounts = readWorkers();
        for (Worker w : accounts) {
            if (username.equalsIgnoreCase(w.username)) {
                System.out.println("❌ Worker already exists.");
                return;
            }
        }

        System.out.print("🔐 Enter Password: ");
        String password = sc.nextLine();

        // ✅ Load available services
        List<String> services = loadServices();
        if (services.isEmpty()) {
            System.out.println("❌ No services found in services.json.");
            return;
        }

        System.out.println("\n🔧 Available Services:");
        for (int i = 0; i < services.size(); i++) {
            System.out.println((i + 1) + ". " + services.get(i));
        }

        System.out.print("👉 Choose service by number: ");
        String choice = sc.nextLine().trim();
        int serviceIndex;
        try {
            serviceIndex = Integer.parseInt(choice) - 1;
            if (serviceIndex < 0 || serviceIndex >= services.size()) {
                System.out.println("❌ Invalid service choice.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Please enter a valid number.");
            return;
        }

        String selectedService = services.get(serviceIndex);

        // 🔢 Auto-generate unique workerId
        String workerId = "W" + String.format("%03d", accounts.size() + 1);

        Worker newWorker = new Worker(workerId, name, username, password, selectedService, true);
        accounts.add(newWorker);
        writeWorkers(accounts);

        System.out.println("✅ Registration successful!");
        System.out.println("🆔 Your Worker ID: " + workerId);
        System.out.println("👷 Registered Name : " + name);
    }



    private static List<String> loadServices() {
        try {
            File file = new File("data/services.json");
            if (!file.exists()) {
                System.out.println("⚠️ services.json file not found.");
                return new ArrayList<>();
            }

            Reader reader = new FileReader(file);
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> services = gson.fromJson(reader, listType);
            reader.close();

            return services != null ? services : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("❌ Error reading services: " + e.getMessage());
            return new ArrayList<>();
        }
    }



    private static void showMenu() {
        while (true) {
            System.out.println("\n📋 Worker Menu");
            System.out.println("1. View Unconfirmed Bookings (Confirm Now)");
            System.out.println("2. View Confirmed Bookings");
            System.out.println("3. View All Bookings");
            System.out.println("4. Mark Booking as Completed");
            System.out.println("5. Search Bookings by Username");
            System.out.println("0. Logout");
            System.out.print("👉 Choose option: ");
            String option = sc.nextLine();

            switch (option) {
                case "1":
                    confirmUnconfirmedBookings();
                    break;
                case "2":
                    viewBookings("confirmed");
                    break;
                case "3":
                    viewBookings(null);
                    break;
                case "4":
                    markBookingCompleted();
                    break;
                case "5":
                    searchByUsername();
                    break;
                case "0":
                    List<Worker> accounts = readWorkers();
                    for (Worker w : accounts) {
                        if (w.username.equals(currentWorker)) {
                            w.isAvailable = false; // ❌ Set availability to false on logout
                            break;
                        }
                    }
                    writeWorkers(accounts); // 💾 Save changes

                    System.out.println("\n✅ You have successfully logged out.");
                    System.out.println("🙏 Thank you for using Urban Club. Have a great day!");
                    System.exit(0); // 🚪 Exit the app safely
                    break;

                default:
                    System.out.println("❌ Invalid option.");
            }
        }
    }

    private static void confirmUnconfirmedBookings() {
        List<Map<String, String>> bookings = readJsonList(BOOKINGS_FILE);
        List<Map<String, String>> unconfirmedForWorker = new ArrayList<>();

        for (Map<String, String> booking : bookings) {
            String status = booking.getOrDefault("status", "").trim().toLowerCase();
            String worker = booking.getOrDefault("worker", "").trim();
            String service = booking.getOrDefault("service", "").trim();

            if (status.equals("pending") &&
                    worker.equalsIgnoreCase(currentWorker) &&
                    service.equalsIgnoreCase(workerService)) {
                unconfirmedForWorker.add(booking);
            }
        }

        if (unconfirmedForWorker.isEmpty()) {
            System.out.println("📭 No unconfirmed bookings assigned to you.");
            return;
        }

        System.out.println("📝 Unconfirmed bookings assigned to you:");
        int index = 1;
        for (Map<String, String> b : unconfirmedForWorker) {
            System.out.println("[" + index + "]");
            displayBooking(b);
            index++;
        }

        System.out.print("👉 Enter the number of the booking (1 to confirm) or ( 0 to cancel) it: ");
        String input = sc.nextLine().trim();

        try {
            int choice = Integer.parseInt(input);
            if (choice > 0 && choice <= unconfirmedForWorker.size()) {
                Map<String, String> selected = unconfirmedForWorker.get(choice - 1);
                selected.put("status", "confirmed");
                System.out.println("✅ Booking confirmed for user: " + selected.get("username"));
                writeJsonList(BOOKINGS_FILE, bookings);
            } else if (choice == 0) {
                System.out.print("❓ Enter the number of the booking you want (1 to confirm cancel) or ( 0 to cancel request) it: ");
                String cancelInput = sc.nextLine().trim();
                int cancelChoice = Integer.parseInt(cancelInput);

                if (cancelChoice > 0 && cancelChoice <= unconfirmedForWorker.size()) {
                    Map<String, String> selected = unconfirmedForWorker.get(cancelChoice - 1);
                    selected.put("status", "Canceled");
                    System.out.println("❌ Booking canceled for user: " + selected.get("username"));
                    writeJsonList(BOOKINGS_FILE, bookings);
                } else {
                    System.out.println("❌ Invalid cancellation selection.");
                }
            } else {
                System.out.println("❌ Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
        }
    }





    private static void viewBookings(String statusFilter) {
        List<Map<String, String>> bookings = readJsonList(BOOKINGS_FILE);
        boolean found = false;

        for (Map<String, String> booking : bookings) {
            String service = booking.getOrDefault("service", "").trim();
            String status = booking.getOrDefault("status", "").trim().toLowerCase();
            String worker = booking.getOrDefault("worker", "").trim();

            if (service.equalsIgnoreCase(workerService) && worker.equalsIgnoreCase(currentWorker)) {
                if (statusFilter == null || status.equals(statusFilter.toLowerCase())) {
                    displayBooking(booking);
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("📭 No bookings found for your service with status: " + (statusFilter == null ? "All" : statusFilter));
        }
    }


    private static void markBookingCompleted() {
        List<Map<String, String>> bookings = readJsonList(BOOKINGS_FILE);
        System.out.print("🆔 Enter Booking ID to mark as completed: ");
        String bookingId = sc.nextLine();

        boolean found = false;
        for (Map<String, String> booking : bookings) {
            String status = booking.getOrDefault("status", "").trim().toLowerCase();
            String service = booking.getOrDefault("service", "").trim();
            String worker = booking.getOrDefault("worker", "").trim();

            if (bookingId.equals(booking.get("id")) &&
                    service.equalsIgnoreCase(workerService) &&
                    worker.equalsIgnoreCase(currentWorker) &&
                    status.equals("confirmed")) {
                booking.put("status", "completed");
                found = true;
                break;
            }
        }

        if (found) {
            writeJsonList(BOOKINGS_FILE, bookings);
            System.out.println("✅ Booking marked as completed.");
        } else {
            System.out.println("❌ Booking ID not found, or it’s not in 'confirmed' status, or doesn’t belong to your service.");
        }
    }





    private static void searchByUsername() {
        System.out.print("🔍 Enter username to search: ");
        String user = sc.nextLine();

        List<Map<String, String>> bookings = readJsonList(BOOKINGS_FILE);
        boolean found = false;

        for (Map<String, String> booking : bookings) {
            if (user.equalsIgnoreCase(booking.get("username")) &&
                    workerService.equalsIgnoreCase(booking.get("service"))) {
                displayBooking(booking);
                found = true;
            }
        }

        if (!found) {
            System.out.println("📭 No bookings found for user: " + user + " in your service.");
        }
    }

    private static void displayBooking(Map<String, String> b) {
        System.out.println("-------------------------------");
        System.out.println("🆔 ID       : " + b.get("userId"));
        System.out.println("👤 Username : " + b.get("username"));
        System.out.println("🛠️ Service  : " + b.get("service"));
        System.out.println("📍 Address  : " + b.get("address"));
        System.out.println("📞 Phone    : " + b.get("phone"));
        System.out.println("📌 Status   : " + b.get("status"));
        if (b.containsKey("worker")) {
            System.out.println("👷 Assigned Worker: " + b.get("worker"));
        }
        System.out.println("-------------------------------");
    }

    // ---------------------- JSON Helpers ----------------------

    private static List<Map<String, String>> readJsonList(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                return new ArrayList<>();
            }

            Reader reader = new FileReader(file);
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> data = gson.fromJson(reader, listType);
            reader.close();
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("❌ File read error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void writeJsonList(String filename, List<Map<String, String>> data) {
        try (Writer writer = new FileWriter(filename)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.out.println("❌ File write error: " + e.getMessage());
        }
    }


    private static List<Worker> readWorkers() {
        File file = new File(WORKER_ACCOUNTS_FILE);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                return new ArrayList<>();
            }
            Reader reader = new FileReader(file);
            Type listType = new TypeToken<List<Worker>>() {}.getType();
            List<Worker> data = gson.fromJson(reader, listType);
            reader.close();
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("❌ File read error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void writeWorkers(List<Worker> workers) {
        try (Writer writer = new FileWriter(WORKER_ACCOUNTS_FILE)) {
            gson.toJson(workers, writer);
        } catch (IOException e) {
            System.out.println("❌ File write error: " + e.getMessage());
        }
    }

    // ---------------------- Worker Data Class ----------------------
    static class Worker {
        String workerId;
        String name;
        String username;
        String password;
        String service;
        boolean isAvailable;

        Worker(String workerId, String name, String username, String password, String service, boolean isAvailable) {
            this.workerId = workerId;
            this.name = name;
            this.username = username;
            this.password = password;
            this.service = service;
            this.isAvailable = isAvailable;
        }
    }


}
