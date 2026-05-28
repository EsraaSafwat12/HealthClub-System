# 🏋️ Power Gym — Health Club Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

> A full-featured desktop application for managing gyms and fitness clubs — built with **Java 21 + JavaFX**.

</div>

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Tech Stack](#️-tech-stack)
- [How to Run](#️-how-to-run)
- [Project Structure](#-project-structure)
- [OOP Concepts](#-oop-concepts-used)
- [Data Storage](#-data-storage)
- [Team Members](#-team-members)

---

## 🧾 About

**Power Gym** is a comprehensive gym management system that handles everything from member registration to billing, coaching, attendance tracking, and advanced features like frozen memberships, family packages, and digital contracts.

Designed with a clean, modern UI that supports **15 languages** including Arabic and English, with full **Dark / Light Mode** support.

---

## ✨ Features

### 👑 Admin Panel

| Feature | Description |
|---------|-------------|
| 📊 Dashboard | Live overview of members, revenue, and activity with charts |
| 👥 Manage Users | Add, edit, delete, search, and lock/unlock accounts |
| 🔗 Assign Members | Assign members to coaches |
| 💳 Billing | Create, search, and track invoices — mark as paid |
| 🔄 Renewals | Renew member subscriptions with expiry alerts |
| 📄 Reports | Generate PDF reports for bills and coaches |
| 🔔 Notifications | Expiry alerts for members nearing subscription end |
| 📅 Attendance | PIN-based daily check-in/check-out tracking |
| 📦 Inventory | Manage stock with low-quantity alerts and sales tracking |
| 💰 Salaries | Manage and track coach salaries |
| 🔐 Login History | View all login attempts with timestamps |
| 🎟️ Coupons | Create discount coupons |
| ❄️ Freeze Membership | Freeze/unfreeze member subscriptions |
| 👨‍👩‍👧 Family Packages | Group memberships for families |
| 🤝 Referrals | Track and reward member referrals |
| 💳 Split Payments | Split bills into installments |
| 📝 Digital Contracts | Generate and manage member contracts |
| 📱 WhatsApp | Send workout plans via WhatsApp |
| 💪 Body Progress | Track member body measurements over time |

### 🏃 Coach Panel

| Feature | Description |
|---------|-------------|
| 👥 My Members | View assigned members with status (Active/Expired/Warning) |
| 📅 Add Schedule | Create training sessions for members |
| 📋 View Schedules | View all upcoming sessions |
| 💬 Messages | Send messages to members + view inbox from members |
| 🏋️ Workout Plans | Create detailed plans with exercises, export to TXT/WhatsApp |
| 📊 Attendance | Track member attendance |
| ⚙️ Update Info | Update personal information and password |

### 👤 Member Panel

| Feature | Description |
|---------|-------------|
| 🏠 Dashboard | Personal overview with subscription status and stats |
| 📅 Subscription | View status (Active/Warning/Expired) + self-renewal |
| 🏋️ My Schedules | View all assigned training sessions |
| 💬 Messages | Receive messages from coach + send messages to coach |
| 💳 My Bills | View billing history with paid/unpaid summary |
| 📋 My Plan | View workout plan + export to WhatsApp |
| ⚙️ Update Info | Update personal information and password |

---

## 🌍 Multi-Language Support (15 Languages)

| 🇬🇧 English | 🇸🇦 العربية | 🇫🇷 Français |
|------------|------------|-------------|
| 🇪🇸 Español | 🇩🇪 Deutsch | 🇮🇹 Italiano |
| 🇧🇷 Português | 🇹🇷 Türkçe | 🇨🇳 中文 |
| 🇯🇵 日本語 | 🇰🇷 한국어 | 🇷🇺 Русский |
| 🇮🇳 हिन्दी | 🇮🇷 فارسی | 🇵🇰 اردو |

---

## 🎨 UI Features

- 🌙 Dark / ☀️ Light Mode toggle
- ✨ Animated Splash Screen
- 💾 Auto-save every 5 minutes
- ⏱️ Session timeout with auto-lock (15 min inactivity)
- 📄 PDF report generation (Apache PDFBox)
- 🔐 PIN-based attendance security
- 📱 WhatsApp integration for workout plans

---

## 🛠️ Tech Stack

| Tool | Version |
|------|---------|
| Java | 21 |
| JavaFX | 21 |
| Apache PDFBox | 2.0.27 |
| Maven | Build tool |
| IDE | NetBeans |

---

## ▶️ How to Run

### Requirements

- Java 21+
- Maven

### Steps

```bash
# Clone the repo
git clone https://github.com/EsraaSafwat12/HealthClub-System.git

# Install the core library first
cd HealthClub-System/healthclubsystem
mvn install

# Run the GUI application
cd ../HealthClubGUI
mvn javafx:run
```

> ⚠️ You must run `mvn install` on `healthclubsystem` first before running `HealthClubGUI`.

---

## 📁 Project Structure

```
HealthClub-System/
│
├── HealthClubGUI/                          # JavaFX GUI Application
│   ├── src/main/java/com/mycompany/healthclubgui/
│   │   ├── LoginScreen.java                # Entry point + authentication
│   │   ├── AdminScreen.java                # Admin dashboard (12 sections)
│   │   ├── CoachScreen.java                # Coach panel
│   │   ├── MemberScreen.java               # Member panel
│   │   ├── SplashScreen.java               # Animated intro screen
│   │   ├── AppState.java                   # Global app state
│   │   ├── AttendanceManager.java          # Attendance logic
│   │   ├── WorkoutPlanScreen.java          # Workout plan builder
│   │   ├── InventoryScreen.java            # Inventory management
│   │   ├── SalaryScreen.java               # Salary management
│   │   ├── ChartsPanel.java                # Revenue & attendance charts
│   │   ├── ContractPdfGenerator.java       # PDF contract generation
│   │   └── TranslationService.java         # Multi-language engine
│   └── pom.xml
│
└── healthclubsystem/                       # Core Java Library
    ├── src/main/java/com/mycompany/healthclubsystem/
    │   ├── User.java                       # Abstract base class
    │   ├── Admin.java                      # Admin role
    │   ├── Coach.java                      # Coach role
    │   ├── Member.java                     # Member role
    │   ├── FileManager.java                # All file I/O operations
    │   ├── Bill.java                       # Billing model
    │   ├── Message.java                    # Messaging model
    │   ├── Schedule.java                   # Schedule model
    │   ├── WorkoutPlan.java                # Workout plan model
    │   ├── InventoryItem.java              # Inventory model
    │   ├── Salary.java                     # Salary model
    │   ├── Attendance.java                 # Attendance model
    │   └── Reportable.java                 # Interface for PDF reports
    └── pom.xml
```

---

## 🧠 OOP Concepts Used

| Concept | Implementation |
|---------|---------------|
| **Inheritance** | `Admin`, `Coach`, `Member` all extend `User` |
| **Encapsulation** | All fields are `private` with getters/setters |
| **Abstraction** | `User` is an abstract class with abstract methods |
| **Polymorphism** | Each class overrides `toCSV()` and `generateReport()` |
| **Interface** | `Reportable` interface implemented by all user types |
| **Aggregation** | `Member` has-a `Coach` — they exist independently |
| **Association** | `Schedule` and `Message` reference `Coach` and `Member` |
| **Composition** | System owns all `ArrayList` collections |

---

## 💾 Data Storage

All data is stored locally in `.txt` files (CSV format):

| File | Contents |
|------|----------|
| `users.txt` | All registered users |
| `bills.txt` | Billing records |
| `messages.txt` | Coach-member messages |
| `schedules.txt` | Training sessions |
| `attendance.txt` | Daily check-in/out log |
| `inventory.txt` | Stock items |
| `salaries.txt` | Coach salary records |
| `workout_plans.txt` | Member workout plans |
| `pins.txt` | Attendance PINs |
| `login_history.txt` | Login activity log |

> 💡 Auto-backup is created daily in a `backup_YYYY-MM-DD/` folder.

---

## 👥 Team Members

| # | Name |
|---|------|
| 1 | 👩‍💻 **Esraa Safwat** |
| 2 | 👩‍💻 **Marina Bassem** |
| 3 | 👩‍💻 **Arwa Mohamed** |
| 4 | 👩‍💻 **Alaa Abdelrahman** |
| 5 | 👩‍💻 **Maria Victor** |
| 6 | 👨‍💻 **Salah Ahmed** |

---

<div align="center">
Made with ❤️ by the Power Gym Team
</div>
