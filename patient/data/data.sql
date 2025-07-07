DROP DATABASE IF EXISTS medilabosolutions_patient;

CREATE DATABASE IF NOT EXISTS medilabosolutions_patient;

USE medilabosolutions_patient;

CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(1) NOT NULL,
    address VARCHAR(255) NULL,
    phone_number VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO
    patients (
        firstname,
        lastname,
        birth_date,
        gender,
        address,
        phone_number,
        created_at,
        updated_at
    )
VALUES (
        'TestNone',
        'Test',
        '1966-12-31',
        'F',
        '1 Brookside St',
        '100-222-3333',
        NOW(),
        NOW()
    ),
    (
        'TestBorderline',
        'Test',
        '1945-06-24',
        'M',
        '2 High St',
        '200-333-4444',
        NOW(),
        NOW()
    ),
    (
        'TestInDanger',
        'Test',
        '2004-06-18',
        'M',
        '3 Club Road',
        '300-444-5555',
        NOW(),
        NOW()
    ),
    (
        'TestEarlyOnset',
        'Test',
        '2002-06-28',
        'F',
        '4 Valley Dr',
        '400-555-6666',
        NOW(),
        NOW()
    );