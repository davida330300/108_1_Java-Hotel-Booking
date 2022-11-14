Online Hotel Booking System
===

```c
online_hotel_booking_system
│
├── Vendor_pkg
│   │
│   ├── App_Ubuntu
│   │   ├── Hotel Booking App.jar*
│   │   └── ClientConfig.txt
│   │
│   ├── App_Windows
│   │   ├── Hotel Booking App.jar*
│   │   └── ClientConfig.txt
│   │
│   └── Management_System
│       ├── data
│       │   └── hotel.json
│       ├── ManagementSystem.jar**
│       └── ServerConfig.txt
│
│   * Double click to execute
│   ** Return "java -jar ManagementSystem.jar" in terminal to execute
│
└── Developer_pkg
    │
    ├── Client
    │   ├── images
    │   │   └── some images for GUI
    │   /*Launch*/
    │   ├── runClient.sh
    │   ├── ClientConfig.txt
    │   ├── Client.java
    │   /*IPC API*/
    │   ├── Packet.java
    │   ├── Query.java
    │   ├── QueryType.java
    │   ├── Response.java
    │   ├── HotelInfo.java
    │   /*GUI*/
    │   ├── GUIThread.java
    │   ├── CancelReservation.java
    │   ├── CheckReservation.java
    │   ├── CreateAccount.java
    │   ├── DatePopup.java
    │   ├── Menu.java
    │   ├── ModifyReservation.java
    │   ├── OrderHotel.java
    │   ├── ResultPopUp.java
    │   ├── SearchHotel.java
    │   ├── ShowSearchHotel.java
    │   ├── SignIn.java
    │   └── Welcome.java
    │
    └── Server
        ├── data
        │   /*Sample data*/
        │   └── hotel.json
        ├── lib
        │   /*Prerequisite library*/
        │   ├── json-simple-1.1.1.jar
        │   └── sqlite-jdbc-3.23.1.jar
        /*Launch*/
        ├── runServer.sh
        ├── ServerConfig.txt
        ├── Server.java
        /*IPC API*/
        ├── Query.java
        ├── QueryType.java
        ├── Response.java
        ├── HotelInfo.java
        /*DBMS*/
        ├── HotelManager.java
        ├── HotelDB.java
        ├── MemberDB.java
        └── ReservationDB.java
```
