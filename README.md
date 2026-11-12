# CollegeAdministrationSystem

## Setup
1. Run `schema.sql` in MySQL.
2. Copy `db.properties.sample` to `db.properties` in the project root and edit the credentials.
3. Put the MySQL Connector/J jar at `lib/mysql-connector-j.jar` (or fix the path in `.classpath`).
4. Run `collegeadmin.main.MainFrame`.

## Modules
Student, Faculty, Course (add, assign faculty, register students), Examination, Attendance (record, report, shortage).
