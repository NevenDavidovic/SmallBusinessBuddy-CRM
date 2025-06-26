-- ORGANIZATION
CREATE TABLE IF NOT EXISTS organization (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    IBAN TEXT NOT NULL,
    street_name TEXT,
    street_num TEXT,
    postal_code TEXT,
    city TEXT,
    email TEXT,
    image BLOB,
    phone_num TEXT,
    created_at TEXT,
    updated_at TEXT
);

-- CONTACTS
CREATE TABLE IF NOT EXISTS contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT,
    last_name TEXT,
    street_name TEXT,
    street_num TEXT,
    postal_code TEXT,
    city TEXT,
    email TEXT,
    phone_num TEXT,
    is_member INTEGER DEFAULT 0,
    member_since TEXT,
    member_until TEXT,
    created_at TEXT,
    updated_at TEXT
);

-- PAYMENTS
CREATE TABLE IF NOT EXISTS payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    organization_id INTEGER,
    contact_id INTEGER,
    model_payment TEXT,
    call_to_receiver TEXT,
    sifra TEXT,
    payment_desc TEXT,
    amount REAL,
    created_at TEXT,
    updated_at TEXT,
    FOREIGN KEY (organization_id) REFERENCES organization(id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

-- CHILDREN
CREATE TABLE IF NOT EXISTS children (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT,
    last_name TEXT,
    birth_date TEXT,
    age INTEGER,
    gender TEXT,
    is_member INTEGER DEFAULT 0,
    member_since TEXT,
    member_until TEXT,
    note TEXT,
    created_at TEXT,
    updated_at TEXT
);

-- GUARDIANS
CREATE TABLE IF NOT EXISTS guardians (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contact_id INTEGER,
    children_id INTEGER,
    created_at TEXT,
    updated_at TEXT,
    FOREIGN KEY (contact_id) REFERENCES contacts(id),
    FOREIGN KEY (children_id) REFERENCES children(id)
);

-- TEACHERS
CREATE TABLE IF NOT EXISTS teachers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT,
    last_name TEXT,
    email TEXT,
    phone_num TEXT,
    created_at TEXT,
    updated_at TEXT
);

-- WORKSHOPS
CREATE TABLE IF NOT EXISTS workshops (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    from_date TEXT,
    to_date TEXT,
    created_at TEXT,
    updated_at TEXT
);

-- WORKSHOPS PARTICIPANTS
CREATE TABLE IF NOT EXISTS workshops_participants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    workshop_id INTEGER,
    participant_id INTEGER,
    participant_type TEXT,
    payment_status TEXT,
    notes TEXT,
    created_at TEXT,
    updated_at TEXT,
    teacher_id INTEGER,
    FOREIGN KEY (workshop_id) REFERENCES workshops(id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);
