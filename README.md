## Urban Club: Home Services and Management System

**Urban Club** is a comprehensive, console-based application designed to manage home services efficiently, connecting users, service workers, and administrators through distinct, secure panels.

### 🌟 Features

This system provides a full suite of management tools across three different user roles:

#### 👤 User Panel

  * **Registration & Login:** Securely register as a new user or log in with existing credentials.
  * **Book Service:** Easily book a desired home service.
  * **View My Bookings:** Track and review the status of all past and pending service bookings.

    ![WhatsApp Image 2025-10-24 at 21 27 23_ccdd0786](https://github.com/user-attachments/assets/34c7f967-a82a-43ae-bcc8-9045f9be5a54)


#### 👷 Worker Panel

  * **Registration & Login:** Workers can register and log in, specifying their primary **Service Type** (e.g., Cleaning, Painting, Plumbing).
  * **Availability:** Automatically sets availability to `true` upon login.
  * **Booking Management:**
      * **View Unconfirmed Bookings:** See new service requests and **confirm** them.
      * **View Confirmed Bookings:** Review bookings they have accepted.
      * **View All Bookings:** A complete list of all bookings assigned to the worker.
      * **Mark Booking as Completed:** Update the status of a finished job.
      * **Search Bookings by Username:** Quickly find specific bookings.
   
        ![WhatsApp Image 2025-10-24 at 21 27 24_ad8aace2](https://github.com/user-attachments/assets/3bf69d19-a4c1-4502-828d-bd1c534014bd)


#### 🛡️ Admin Panel

The administrator has ultimate control and oversight over the entire system.

  * **Registration & Login:** Secure registration and login for the administrator.
  * **System Oversight:**
      * **View All Users**
      * **View All Workers**
      * **View All Bookings**
      * **View All Services**
  * **Service Management:**
      * **Add New Service:** Expand the range of services offered by the club.
  * **Statistics:**
      * **View Stats:** Access high-level system metrics and performance data.
   
        ![WhatsApp Image 2025-10-24 at 21 27 24_88cf6a41](https://github.com/user-attachments/assets/254c53f9-16c6-4bfd-9b3d-14d0c1c0531a)


-----

### 🛠️ Technology Stack

  * **Language:** **Java** (specifically for console application development).
  * **Database:** **JSON (JavaScript Object Notation)** files for data storage (persisting User, Worker, Booking, and Service data).
  * **Data Handling:** Likely using a library like **Gson** or **Jackson** for mapping Java objects to JSON files.
  * **Environment:** Console/Terminal-based application.

-----

### 🚀 Installation and Setup

To get a local copy up and running, you will need the **Java Development Kit (JDK)** installed on your system.

1.  **Clone the repository:**
    ```bash
    git clone [Your GitHub Repo URL Here]
    ```
2.  **Navigate to the project directory:**
    ```bash
    cd Urban_Club-master
    ```
3.  **Compile the Java files (if not using a build tool like Maven/Gradle):**
    ```bash
    # This is a generic example. The actual command depends on your file structure.
    javac *.java
    ```
4.  **Run the application:**
    ```bash
    java Main
    ```

### 🤝 Getting Started

1.  Start the application.
2.  **Admin Setup:** Register an Admin user first (Choice **0** on the Admin Panel).
3.  **Service Setup:** Log in as Admin and use option **5. Add New Service** to define the services workers can register for (e.g., "Cleaning", "Plumbing").
4.  **Worker Registration:** Register and log in a Worker, selecting one of the defined service types.
5.  **User Interaction:** Register a new User and **Book Service**.
6.  **Worker Action:** Log in as the Worker and **Confirm** the new booking.
7.  **Admin Oversight:** Log in as Admin to **View All Bookings** and system **Stats**.
