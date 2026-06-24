import sqlite3

from flask import Flask, request, jsonify

app = Flask(__name__)



@app.post("/login")
def login():
    data = request.json
    username = data["username"]
    password = data["password"]

    conn = sqlite3.connect("../database/expense_manager.db")
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    cursor.execute(
        "SELECT * FROM users WHERE username = ?",
        (username,)
    )

    user = cursor.fetchone()

    conn.close()

    if user and user["password"] == password:
        return jsonify({
            "success": True,
            "user": dict(user)
        }), 200

    return jsonify({
        "success": False,
        "message": "Invalid credentials"
    }), 401

if __name__ == "__main__":
    app.run()