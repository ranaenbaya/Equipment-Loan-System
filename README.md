# University Equipment Loan System

This is a console-based equipment loan system developed independently as a
university project. It provides a small catalogue for lab equipment and
supports managing users and their equipment loans.

## Features

- Add, edit, delete and list laptop and camera equipment.
- Register, update and remove student and staff users.
- Borrow available equipment, with suggested loan periods based on user type.
- Return equipment and calculate overdue fees by equipment category.
- View active and overdue loans and generate overdue reminders.
- Save application records to a local Java-serialised `data.ser` file.

## Technologies

- Java 17
- Maven
- JUnit 5
- Java object serialization for local file persistence

## Object-oriented programming

The project demonstrates encapsulation through validated model fields and
service methods, inheritance through `Laptop` and `Camera` extending
`Equipment` and `Student` and `Staff` extending `User`, and polymorphism through
the shared equipment and user types. `Equipment` and `User` are abstract base
classes, while `Category` is an enum for equipment categories and fee rates.

## Project structure

```text
src/
├── main/java/equipmentloan/
│   ├── Main.java
│   ├── core/
│   │   ├── common/
│   │   ├── equipment/
│   │   ├── loan/
│   │   ├── user/
│   │   └── validation/
│   ├── persistence/
│   ├── service/
│   └── ui/
└── test/java/equipmentloan/tests/
    └── LabServiceTest.java
```

## Run the application

Install a Java 17 (or newer) JDK and Maven, then run these commands from the
project root:

```text
mvn clean package
java -cp target/classes equipmentloan.Main
```

Choose **7. Exit** from the main menu to leave the application. Its data is
saved to `data.ser` in the current working directory and loaded on the next
start. The first run starts with an empty catalogue.

## Run the tests

Run the JUnit 5 test suite from the project root:

```text
mvn test
```

## What I learned

Developing this project gave me practice organising a Java application into
model, service, persistence and user-interface responsibilities. I also gained
experience applying object-oriented design, validating user input, enforcing
loan rules, persisting application state, and testing business logic separately
from console interaction.
