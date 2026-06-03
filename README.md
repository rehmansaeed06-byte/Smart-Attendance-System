#  Smart Attendance System

A JavaFX-based Attendance and Quiz Management System that allows teachers to manage attendance, conduct MCQ quizzes, and monitor student participation in real time over both Local Area Networks (LAN) and the Internet using Tailscale.

##  Features

* Teacher Authentication
* Student Authentication
* Attendance Management
* Real-Time MCQ Quiz System
* Live Question Broadcasting
* Automatic Answer Collection
* Quiz Timer Support
* SQLite Database Integration
* Multi-Client Architecture
* LAN Connectivity
* Tailscale Cloud Connectivity
* Windows Executable Distribution
* Custom Java Runtime Included

##  Getting Started

### Prerequisites

* Windows 10/11
* Java 21 (for development)
* JavaFX 21
* SQLite JDBC Driver
* Tailscale (for cloud connectivity)

### Clone Repository

```bash
git clone https://github.com/rehmansaeed06-byte/Smart-Attendance-System.git
cd Smart-Attendance-System
```

##  Development Setup

Configure JavaFX VM Options:

```text
--module-path "C:\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml
```

Open the project in IntelliJ IDEA and run the desired application.

##  Cloud Connection (Tailscale)

To connect students and teachers over the Internet:

1. Install Tailscale on all devices.
2. Log in to the same Tailnet.
3. Start the Server Application.
4. Click "Start Server".
5. Copy the displayed Tailscale IP address.
6. Enter the IP address in Teacher and Student applications.
7. Connect normally.

No port forwarding is required.

##  Local Network Usage

1. Start the Server Application.
2. Click "Start Server".
3. Copy the displayed local IP address.
4. Enter the IP address in Teacher and Student applications.
5. Connect.

##  Project Structure

```text
src/            Application Source Code
lib/            External Libraries
users/          User Data
xml/            Configuration Files
mcqs.csv        Sample MCQ Data
```

##  Technologies Used

* Java
* JavaFX
* SQLite
* JDBC
* Tailscale
* Launch4j
* Inno Setup

## 📸 Screenshots

Add screenshots of:

* Server GUI
* Teacher Dashboard
* Student Interface
* Quiz Window

##  Releases

Pre-built Windows releases are available in the Releases section.

No separate Java installation is required for release builds.

##  Developer

BroCode

##  License

This project is provided for educational and learning purposes.
