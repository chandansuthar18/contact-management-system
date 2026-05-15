
CREATE DATABASE ContactDB;
GO

USE ContactDB;
GO

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

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);

CREATE INDEX idx_contacts_user_id    ON contacts(user_id);

CREATE INDEX idx_contacts_first_name ON contacts(user_id, first_name);
CREATE INDEX idx_contacts_last_name  ON contacts(user_id, last_name);

-- Join queries for emails and phones
CREATE INDEX idx_emails_contact_id   ON contact_emails(contact_id);
CREATE INDEX idx_phones_contact_id   ON contact_phones(contact_id);
GO

INSERT INTO users (first_name, last_name, email, password)
VALUES ('Test', 'User', 'test@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

DECLARE @uid BIGINT = SCOPE_IDENTITY();

INSERT INTO contacts (user_id, first_name, last_name, title)
VALUES
    (@uid, 'Sarah',   'Connor',  'Software Engineer'),
    (@uid, 'Bruce',   'Wayne',   'CEO'),
    (@uid, 'Diana',   'Prince',  'Ambassador'),
    (@uid, 'Tony',    'Stark',   'CTO'),
    (@uid, 'Natasha', 'Romanoff','Director');

INSERT INTO contact_emails (contact_id, label, email)
SELECT id, 'work',     'sarah@corp.com'   FROM contacts WHERE first_name='Sarah' AND user_id=@uid
UNION ALL
SELECT id, 'personal', 'sarah@gmail.com'  FROM contacts WHERE first_name='Sarah' AND user_id=@uid;

-- Phone for Sarah Connor
INSERT INTO contact_phones (contact_id, label, phone)
SELECT id, 'work', '+15550001' FROM contacts WHERE first_name='Sarah' AND user_id=@uid;
GO

PRINT 'ContactDB schema created successfully!';
