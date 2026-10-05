
CREATE DATABASE IF NOT EXISTS hallmanagement_db;
USE hallmanagement_db;

DROP TABLE IF EXISTS complaint_responses;
DROP TABLE IF EXISTS complaints;
DROP TABLE IF EXISTS hall_change_requests;
DROP TABLE IF EXISTS student_notice_reads;
DROP TABLE IF EXISTS notices;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS payment_requests;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS hall_fee_structures;
DROP TABLE IF EXISTS student_hall_history;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS provosts;
DROP TABLE IF EXISTS halls;
DROP TABLE IF EXISTS users;

CREATE TABLE halls (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    hall_name       VARCHAR(100)    NOT NULL,
    hall_type       ENUM('MALE','FEMALE') NOT NULL,
    provost_user_id VARCHAR(50)     DEFAULT NULL,
    provost_name    VARCHAR(100)    DEFAULT NULL,
    provost_phone   VARCHAR(20)     DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_hall_name (hall_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE provosts (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    user_id     VARCHAR(50)     NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    phone       VARCHAR(20)     DEFAULT NULL,
    hall_id     INT UNSIGNED    DEFAULT NULL,
    age         INT             DEFAULT 45,
    email       VARCHAR(100)    DEFAULT 'provost@ruet.ac.bd',
    designation VARCHAR(100)    DEFAULT 'Professor & Provost',
    office_room VARCHAR(100)    DEFAULT 'Provost Office, Ground Floor',
    status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_provost_user_id (user_id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rooms (
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    hall_id     INT UNSIGNED NOT NULL,
    room_number INT          NOT NULL,
    total_seats INT          NOT NULL DEFAULT 4,

    PRIMARY KEY (id),
    UNIQUE KEY uq_hall_room (hall_id, room_number),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE seats (
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    room_id     INT UNSIGNED NOT NULL,
    seat_number INT          NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_room_seat (room_id, seat_number),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE students (
    id              INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    user_id         VARCHAR(50)     NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20)     DEFAULT NULL,
    email           VARCHAR(100)    DEFAULT NULL,
    department      VARCHAR(50)     NOT NULL,
    year            VARCHAR(20)     DEFAULT NULL,
    current_hall_id INT UNSIGNED    DEFAULT NULL,
    current_room    INT             DEFAULT NULL,
    current_seat    INT             DEFAULT NULL,
    status          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_student_user_id (user_id),
    UNIQUE KEY uq_active_seat (current_hall_id, current_room, current_seat),
    FOREIGN KEY (current_hall_id) REFERENCES halls(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE student_hall_history (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student_id  INT UNSIGNED    NOT NULL,
    hall_id     INT UNSIGNED    NOT NULL,
    room_number INT             NOT NULL,
    seat_number INT             NOT NULL,
    start_date  DATE            NOT NULL,
    end_date    DATE            DEFAULT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE hall_fee_structures (
    id                 INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    hall_id            INT UNSIGNED    NOT NULL,
    hall_charge        DECIMAL(10,2)   NOT NULL DEFAULT 500.00,
    electricity_charge DECIMAL(10,2)   NOT NULL DEFAULT 100.00,
    water_charge       DECIMAL(10,2)   NOT NULL DEFAULT 50.00,
    maintenance_charge DECIMAL(10,2)   NOT NULL DEFAULT 50.00,
    other_charge       DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_hall_fee (hall_id),
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bills (
    id                 INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student_id         VARCHAR(50)     NOT NULL,
    hall_id            INT UNSIGNED    NOT NULL,
    room_number        INT             NOT NULL,
    seat_number        INT             NOT NULL,
    billing_period     VARCHAR(50)     NOT NULL,
    hall_charge        DECIMAL(10,2)   NOT NULL DEFAULT 500.00,
    electricity_charge DECIMAL(10,2)   NOT NULL DEFAULT 100.00,
    water_charge       DECIMAL(10,2)   NOT NULL DEFAULT 50.00,
    maintenance_charge DECIMAL(10,2)   NOT NULL DEFAULT 50.00,
    other_charge       DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    total_amount       DECIMAL(10,2)   NOT NULL,
    paid_amount        DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    due_amount         DECIMAL(10,2)   NOT NULL,
    status             ENUM('DUE', 'PARTIALLY PAID', 'PAID') NOT NULL DEFAULT 'DUE',
    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_student_bill_period (student_id, billing_period),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payment_requests (
    id               INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    request_code     VARCHAR(20)     NOT NULL,
    student_id       VARCHAR(50)     NOT NULL,
    provost_id       VARCHAR(50)     NOT NULL,
    bill_id          INT UNSIGNED    NOT NULL,
    amount           DECIMAL(10,2)   NOT NULL,
    request_date     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status           ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(255)    DEFAULT NULL,
    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_request_code (request_code),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (provost_id) REFERENCES provosts(user_id) ON DELETE CASCADE,
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
    id                 INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    payment_code       VARCHAR(20)     NOT NULL,
    payment_request_id INT UNSIGNED    DEFAULT NULL,
    student_id         VARCHAR(50)     NOT NULL,
    provost_id         VARCHAR(50)     NOT NULL,
    bill_id            INT UNSIGNED    NOT NULL,
    amount             DECIMAL(10,2)   NOT NULL,
    payment_date       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status             ENUM('APPROVED') NOT NULL DEFAULT 'APPROVED',

    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_code (payment_code),
    FOREIGN KEY (payment_request_id) REFERENCES payment_requests(id) ON DELETE SET NULL,
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (provost_id) REFERENCES provosts(user_id) ON DELETE CASCADE,
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notices (
    id           INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    title        VARCHAR(255)    NOT NULL,
    content      TEXT            NOT NULL,
    posted_date  DATE            NOT NULL,
    posted_by    VARCHAR(50)     NOT NULL,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    FOREIGN KEY (posted_by) REFERENCES provosts(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE student_notice_reads (
    id         INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(50)     NOT NULL,
    notice_id  INT UNSIGNED    NOT NULL,
    read_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_student_notice (student_id, notice_id),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE hall_change_requests (
    id                 INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    request_code       VARCHAR(20)     NOT NULL,
    student_id         VARCHAR(50)     NOT NULL,
    current_hall_id    INT UNSIGNED    NOT NULL,
    current_room       INT             NOT NULL,
    current_seat       INT             NOT NULL,
    requested_hall_id  INT UNSIGNED    NOT NULL,
    requested_room     INT             NOT NULL,
    requested_seat     INT             NOT NULL,
    request_date       DATE            NOT NULL,
    status             ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    processed_by       VARCHAR(50)     DEFAULT NULL,
    processed_date     TIMESTAMP       NULL DEFAULT NULL,
    rejection_reason   TEXT            DEFAULT NULL,
    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_change_request_code (request_code),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (current_hall_id) REFERENCES halls(id) ON DELETE CASCADE,
    FOREIGN KEY (requested_hall_id) REFERENCES halls(id) ON DELETE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES provosts(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE complaints (
    id           INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student_id   VARCHAR(50)     NOT NULL,
    provost_id   VARCHAR(50)     NOT NULL,
    title        VARCHAR(200)    NOT NULL,
    message      TEXT            NOT NULL,
    submitted_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    processed_at TIMESTAMP       NULL DEFAULT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (provost_id) REFERENCES provosts(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE complaint_responses (
    id               INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    complaint_id     INT UNSIGNED    NOT NULL,
    provost_id       VARCHAR(50)     NOT NULL,
    response_message TEXT            NOT NULL,
    responded_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_complaint_response (complaint_id),
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (provost_id) REFERENCES provosts(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO halls (id, hall_name, hall_type, provost_user_id, provost_name, provost_phone) VALUES
(1, 'Shahid Lt. Selim Hall', 'MALE', 'provost001', 'Prof. Dr. M. Rahman', '01711223344'),
(2, 'Shahid Shahidul Islam Hall', 'MALE', 'provost002', 'Prof. Dr. A. K. Azad', '01722334455'),
(3, 'Shahid Abdul Hamid Hall', 'MALE', 'provost003', 'Prof. Dr. M. S. Islam', '01733445566'),
(4, 'Tin Shed Hall (Extension)', 'MALE', 'provost001', 'Prof. Dr. M. Rahman', '01711223344'),
(5, 'Nawab Foyzunnessa Chowdhurani Hall', 'FEMALE', 'provost004', 'Prof. Dr. N. Sultana', '01744556677'),
(6, 'Shahid President Ziaur Rahman Hall', 'MALE', 'provost005', 'Prof. Dr. K. M. Hossain', '01755667788'),
(7, 'Sher-e-Bangla A K Fazlul Huq Hall', 'MALE', 'provost006', 'Prof. Dr. T. Ahmed', '01766778899'),
(8, 'Male Hall-1', 'MALE', 'provost007', 'Prof. Dr. S. K. Roy', '01777889900'),
(9, 'Male Hall-2', 'MALE', 'provost008', 'Prof. Dr. M. Alam', '01788990011'),
(10, 'Male Hall-3', 'MALE', 'provost009', 'Prof. Dr. B. K. Das', '01799001122'),
(11, 'Female Male Hall-1', 'FEMALE', 'provost010', 'Prof. Dr. F. Yasmin', '01811223300'),
(12, 'Female Male Hall-2', 'FEMALE', 'provost011', 'Prof. Dr. S. Parvin', '01822334411');

INSERT INTO provosts (user_id, password, name, phone, hall_id, age, email, designation, office_room, status) VALUES
('provost001', 'provost123', 'Prof. Dr. M. Rahman', '01711223344', 1, 48, 'saniulsami@gmail.com', 'Professor & Provost', 'Room 101, Selim Hall', 'ACTIVE'),
('provost002', 'provost123', 'Prof. Dr. A. K. Azad', '01722334455', 2, 52, 'saniulsami@gmail.com', 'Professor & Provost', 'Room 102, Shahidul Hall', 'ACTIVE'),
('provost003', 'provost123', 'Prof. Dr. M. S. Islam', '01733445566', 3, 50, 'saniulsami@gmail.com', 'Professor & Provost', 'Room 101, Hamid Hall', 'ACTIVE');

INSERT INTO students (user_id, password, name, phone, email, department, year, current_hall_id, current_room, current_seat, status) VALUES
('2403058', '1111', 'Shamiul Islam', '01921602024', 'shamiulislam39999@gmail.com', 'ECE', '1st Year', 1, 101, 1, 'ACTIVE');

INSERT INTO rooms (hall_id, room_number, total_seats) VALUES
(1, 101, 4), (1, 102, 4), (1, 103, 4), (1, 104, 4),
(1, 201, 4), (1, 202, 4), (1, 203, 4), (1, 204, 4),
(1, 301, 4), (1, 302, 4), (1, 303, 4), (1, 304, 4), (1, 305, 4),
(2, 101, 4), (2, 102, 4), (2, 103, 4), (2, 201, 4), (2, 202, 4), (2, 301, 4), (2, 302, 4),
(3, 101, 4), (3, 102, 4), (3, 201, 4), (3, 202, 4), (3, 204, 4), (3, 301, 4),
(4, 101, 4), (4, 102, 4), (4, 103, 4), (4, 104, 4),
(5, 101, 4), (5, 102, 4), (5, 201, 4), (5, 202, 4), (5, 301, 4),
(6, 101, 4), (6, 102, 4), (6, 201, 4),
(7, 101, 4), (7, 102, 4), (7, 201, 4),
(8, 101, 4), (8, 102, 4),
(9, 101, 4), (9, 102, 4),
(10, 101, 4), (10, 102, 4),
(11, 101, 4), (11, 102, 4),
(12, 101, 4), (12, 102, 4);

INSERT INTO seats (room_id, seat_number)
SELECT r.id, s.n
FROM rooms r
CROSS JOIN (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
) s
ORDER BY r.id, s.n;

INSERT INTO student_hall_history (student_id, hall_id, room_number, seat_number, start_date, end_date) VALUES
(1, 3, 204, 2, '2024-01-10', '2025-02-01'),
(1, 2, 301, 1, '2025-02-02', '2026-01-15'),
(1, 1, 101, 1, '2026-01-16', NULL),
(2, 3, 204, 2, '2023-01-10', NULL),
(3, 1, 101, 3, '2025-01-10', NULL),
(4, 1, 101, 4, '2025-01-10', NULL);

INSERT INTO hall_fee_structures (hall_id, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge) VALUES
(1,  500.00, 100.00, 50.00, 50.00, 0.00),
(2,  500.00, 100.00, 50.00, 50.00, 0.00),
(3,  450.00, 100.00, 50.00, 50.00, 0.00),
(4,  400.00,  80.00, 40.00, 40.00, 0.00),
(5,  550.00, 120.00, 60.00, 50.00, 0.00),
(6,  500.00, 100.00, 50.00, 50.00, 0.00),
(7,  500.00, 100.00, 50.00, 50.00, 0.00),
(8,  500.00, 100.00, 50.00, 50.00, 0.00),
(9,  500.00, 100.00, 50.00, 50.00, 0.00),
(10, 500.00, 100.00, 50.00, 50.00, 0.00),
(11, 500.00, 100.00, 50.00, 50.00, 0.00),
(12, 500.00, 100.00, 50.00, 50.00, 0.00);

INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) VALUES
('2403001', 1, 101, 1, 'August 2026', 500.00, 100.00, 50.00, 50.00, 0.00, 700.00, 0.00, 700.00, 'DUE'),
('2403015', 1, 101, 3, 'August 2026', 500.00, 100.00, 50.00, 50.00, 0.00, 700.00, 200.00, 500.00, 'PARTIALLY PAID'),
('2403020', 1, 101, 4, 'August 2026', 500.00, 100.00, 50.00, 50.00, 0.00, 700.00, 700.00, 0.00, 'PAID');

INSERT INTO notices (title, content, posted_date, posted_by) VALUES
('Hall Seat Allocation 2026', 'Seat allocation results for the upcoming academic session have been published on the hall notice board and online portal. Selected resident students are advised to report to the provost office for document verification.', '2026-09-02', 'provost001'),
('Monthly Hall & Dining Bill Notice', 'All residential students are instructed to clear their monthly hall charges, electricity and maintenance dues by the 15th of this month. Please submit the physical payment to the provost office and confirm your payment request.', '2026-09-01', 'provost001'),
('General Hall Meeting', 'A general meeting with all hall residents will be held this Friday after Maghrib prayer in the central auditorium. Discussions will focus on dining facility improvements and hall cleanliness.', '2026-08-29', 'provost001'),
('Hall Maintenance & Cleanliness Drive', 'A collective cleanliness drive will take place this Saturday across all blocks. All students are requested to cooperate with the hall staff and maintain hygiene standards in common corridors and washrooms.', '2026-08-25', 'provost001');

INSERT INTO hall_change_requests (request_code, student_id, current_hall_id, current_room, current_seat, requested_hall_id, requested_room, requested_seat, request_date, status) VALUES
('HCR-001', '2403020', 1, 101, 4, 3, 204, 3, '2026-09-02', 'PENDING');
