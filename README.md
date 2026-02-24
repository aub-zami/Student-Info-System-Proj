# Student Information System (SIS)
**Developed by: Obrenah Mendoza** 

## 📌 Project Overview
A Java-based Student Information System designed to manage Student, Program, and College records. This project uses a **File System** approach with CSV persistence, mimicking the data integrity and relationship constraints of a **Relational Model**.

## 🚀 Key Features
* **Referential Integrity**: Logic layer ensures Students cannot be added to non-existent Programs.
* **Cascading Nullification**: Deleting a Program or College automatically updates dependent records to "NULL" to prevent orphaned data.
* **Robust Validation**: Implements Regex gatekeepers for Student IDs (`XXXX-NNNN`) and Year Levels (numeric only).
* **Data Sanitization**: Custom `formatName` logic handles multi-word names like "Dela Cruz" and "San Jose".
* **Actions Interface**: A streamlined UI replacing mass selection with a focused "Actions" button for better UX.

## 🛠️ Technical Logic
The system is divided into a **GUI layer** (`Main.java`) and a **Logic layer** (`Logic.java`). 
* **Persistence**: Data is saved in `student.csv`, `program.csv`, and `college.csv`.
* **Search & Sort**: Features dynamic searching and a "Default Sort" reset to maintain original CSV order.
