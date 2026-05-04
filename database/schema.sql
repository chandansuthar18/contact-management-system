-- ============================================================
-- Contact Management System — SQL Server Database Schema
-- Run this script in SQL Server Management Studio (SSMS)
-- or Azure Data Studio BEFORE starting the Spring Boot app.
-- ===============================================
-- Step 1: Create the database
CREATE DATABASE ContactDB;
GO

USE ContactDB;
GO

-- ── USERS TABLE ──────────────────────────────────────────────
-- Stores registered user accounts.
-- Either email OR phone must be non-null (enforced in application layer).
CREATE TABLE users (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    first_name  NVARCHAR(100)   NOT NULL,
    last_name   NVARCHAR(100)   NULL,
    email       NVARCHAR(255)   NULL,
    phone       NVARCHAR(20)    NULL,
    password    NVARCHAR(255)   NOT NULL,   -- BCrypt hash, never plain text
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    -- Each email must be unique across all users
    CONSTRAINT uq_users_email UNIQUE (email),
    -- Each phone must be unique across all users
    CONSTRAINT uq_users_phone UNIQUE (phone)
);
GO

-- ── CONTACTS TABLE ────────────────────────────────────────────
-- Each contact belongs to exactly one user.
-- Deleting a user cascades to delete all their contacts.
CREATE TABLE contacts (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    first_name  NVARCHAR(100)   NOT NULL,
    last_name   NVARCHAR(100)   NULL,
    title       NVARCHAR(100)   NULL,
    created_at  DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT fk_contacts_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);
GO

-- ── CONTACT_EMAILS TABLE ──────────────────────────────────────
-- A contact can have multiple labeled email addresses.
-- label examples: 'work', 'personal', 'other'
CREATE TABLE contact_emails (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    contact_id  BIGINT          NOT NULL,
    label       NVARCHAR(50)    NOT NULL,
    email       NVARCHAR(255)   NOT NULL,

    CONSTRAINT fk_emails_contact
        FOREIGN KEY (contact_id) REFERENCES contacts(id)
        ON DELETE CASCADE
);
GO

-- ── CONTACT_PHONES TABLE ──────────────────────────────────────
-- A contact can have multiple labeled phone numbers.
-- label examples: 'work', 'home', 'personal', 'other'
CREATE TABLE contact_phones (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    contact_id  BIGINT          NOT NULL,
    label       NVARCHAR(50)    NOT NULL,
    phone       NVARCHAR(20)    NOT NULL,

    CONSTRAINT fk_phones_contact
        FOREIGN KEY (contact_id) REFERENCES contacts(id)
        ON DELETE CASCADE
);
GO

-- ── INDEXES ───────────────────────────────────────────────────
-- Speeds up the most common queries

-- Login queries (find by email or phone)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);

-- "Show all contacts for this user" query
CREATE INDEX idx_contacts_user_id    ON contacts(user_id);

-- "Search contacts by name" query
CREATE INDEX idx_contacts_first_name ON contacts(user_id, first_name);
CREATE INDEX idx_contacts_last_name  ON contacts(user_id, last_name);

-- Join queries for emails and phones
CREATE INDEX idx_emails_contact_id   ON contact_emails(contact_id);
CREATE INDEX idx_phones_contact_id   ON contact_phones(contact_id);
GO

-- ── OPTIONAL: Sample test data ────────────────────────────────
-- Password for test user is: password123  (BCrypt hash below)
INSERT INTO users (first_name, last_name, email, password)
VALUES ('Test', 'User', 'test@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Sample contacts for test user (id=1)
DECLARE @uid BIGINT = SCOPE_IDENTITY();

INSERT INTO contacts (user_id, first_name, last_name, title)
VALUES
    (@uid, 'Sarah',   'Connor',  'Software Engineer'),
    (@uid, 'Bruce',   'Wayne',   'CEO'),
    (@uid, 'Diana',   'Prince',  'Ambassador'),
    (@uid, 'Tony',    'Stark',   'CTO'),
    (@uid, 'Natasha', 'Romanoff','Director');

-- Emails for Sarah Connor (contact id=1 if starting fresh)
INSERT INTO contact_emails (contact_id, label, email)
SELECT id, 'work',     'sarah@corp.com'   FROM contacts WHERE first_name='Sarah' AND user_id=@uid
UNION ALL
SELECT id, 'personal', 'sarah@gmail.com'  FROM contacts WHERE first_name='Sarah' AND user_id=@uid;

-- Phone for Sarah Connor
INSERT INTO contact_phones (contact_id, label, phone)
SELECT id, 'work', '+15550001' FROM contacts WHERE first_name='Sarah' AND user_id=@uid;
GO

PRINT 'ContactDB schema created successfully!';
