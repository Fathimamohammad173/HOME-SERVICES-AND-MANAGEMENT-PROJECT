import java.sql.*;
import java.time.LocalDateTime;

public class BookingInsertTest {
    public static void main(String[] args) {
        String url = "jdbc:ucanaccess://D:/UrbanClub/UrbanClubDB.accdb"; // adjust path if needed
        try (Connection conn = DriverManager.getConnection(url)) {
            String email = "kiran@urbanclub.com";
            String service = "Fitness Trainer";
            int workerId = 1; // make sure this exists in your Workers table
            String slot = "Morning";
            LocalDateTime dateTime = LocalDateTime.now();

            saveBooking(conn, email, service, dateTime, slot, workerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveBooking(Connection conn, String email, String service, LocalDateTime dateTime, String slot, int workerId) throws SQLException {
        String query = "INSERT INTO Bookings (user_id, service_id, worker_id, time_slot, confirmation, booking_date) " +
                "VALUES ((SELECT id FROM Users WHERE email = ?), " +
                "(SELECT id FROM Services WHERE services = ?), ?, ?, 'No', ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, service);
            ps.setInt(3, workerId);
            ps.setString(4, slot);
            ps.setDate(5, java.sql.Date.valueOf(dateTime.toLocalDate()));

            System.out.println("✅ DEBUG INSERT:");
            System.out.println("  📧 user_email = " + email);
            System.out.println("  🛠️ service = " + service);
            System.out.println("  👷 workerId = " + workerId);
            System.out.println("  ⏰ slot = " + slot);
            System.out.println("  📅 date = " + dateTime.toLocalDate());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("🎉 Booking inserted successfully!");
            } else {
                System.out.println("❌ Failed to insert booking.");
            }
        }
    }
}
