import sqlite3
import sys

try:
    conn = sqlite3.connect(database="chat.db")
except:
    sys.exit("Error connecting to the database. Please check your inputs.")

cur = conn.cursor()

# Users Table
try:
    cur.execute(
        "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, first_name TEXT NOT NULL, last_name TEXT NOT NULL, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL, registration_date TEXT)")
    conn.commit()
    print("users table created successfully.")
except:
    print("Error creating the table. Please check if it already exists.")

cur.execute("SELECT * FROM users")
text = cur.fetchall()
print(text)
conn.close()