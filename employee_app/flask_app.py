import sqlite3

from flask import Flask, request, jsonify

app = Flask(__name__)

def get_db():
    conn = sqlite3.connect("../database/expense_manager.db")
    conn.row_factory = sqlite3.Row
    return conn

@app.post("/login")
def login():
    data = request.json
    username = data["username"]
    password = data["password"]

    conn = get_db()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT * FROM users WHERE username = ?",
        (username,)
    )

    user = cursor.fetchone()

    conn.close()

    if not user:
        return jsonify({
            "success": False,
            "message": "User does not exist"
        }), 404

    if user["password"] != password:
        return jsonify({
            "success": False,
            "message": "Incorrect password"
        }), 401

    return jsonify({
        "success": True,
        "user": dict(user)
    }), 200



@app.post("/user")
def create_user():
    data = request.json
    username = data["username"]
    password = data["password"]
    role = data["role"]

    conn = get_db()
    cursor = conn.cursor()

    try:
        cursor.execute(
            "INSERT INTO users(username, password, role) VALUES (?, ?, ?)",
            (username, password, role)
        )
        conn.commit()

        cursor.execute(
            "SELECT * FROM users WHERE username = ?",
            (username,)
        )

        user = cursor.fetchone()
        conn.close()

        return jsonify({
            "success": True,
            "user": dict(user)
        }), 201

    except sqlite3.IntegrityError:
        conn.close()
        return jsonify({
            "success": False,
            "message": "Username is taken"
        }), 400

if __name__ == "__main__":
    app.run()