import sqlite3

from flask import Flask, request, jsonify
import logging
logging.basicConfig(
    filename="server.log",
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

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
    logging.info(f"Login request received for {username}")
    conn = get_db()
    cursor = conn.cursor()

    cursor.execute(
        "SELECT * FROM users WHERE username = ?",
        (username,)
    )

    user = cursor.fetchone()

    conn.close()

    if not user:
        logging.warning(f"User {username} not found")
        return jsonify({
            "success": False,
            "message": "User does not exist"
        }), 404

    if user["password"] != password:
        logging.warning(f"Incorrect password for {username}")
        return jsonify({
            "success": False,
            "message": "Incorrect password"
        }), 401
    

    logging.info(f"User {username} authenticated")
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

        logging.info(f"Creating new user: {username}")
        return jsonify({
            "success": True,
            "user": dict(user)
        }), 201

    except sqlite3.IntegrityError:
        conn.close()
        logging.warning(f"Username {username} already taken")
        return jsonify({
            "success": False,
            "message": "Username is taken"
        }), 400


@app.post("/expense")
def add_expense():
    data = request.json
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO expenses(user_id, amount, category, description, date) VALUES (?, ?, ?, ?, ?)",
            (
                data["user_id"],
                data["amount"],
                data["category"],
                data["description"],
                data["date"]
            )
        )

        expense_id = cursor.lastrowid

        cursor.execute(
            "INSERT INTO approvals(expense_id, status) VALUES (?, ?)",
            (expense_id, "pending")
        )

        conn.commit()
        logging.info(
            f"Expense created for user {data['user_id']} amount ${data['amount']}"
        )
        return jsonify({
            "message": "Expense added successfully",
            "expense_id": expense_id
        }), 201

    except Exception as e:
        conn.rollback()
        logging.error(f"Expense creation failed: {str(e)}")
        return jsonify({
            "message": "Failed to add expense",
            "error": str(e)
        }), 400

@app.get("/expenses/<int:user_id>")
def get_expenses(user_id):
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("""
            SELECT * FROM expenses
            JOIN approvals ON approvals.expense_id = expenses.id
            WHERE expenses.user_id = ?
        """, (user_id,))
        expenses = [dict(row) for row in cursor.fetchall()]
        logging.info(f"User {user_id} viewed all expenses")
        return jsonify(expenses), 200

    except Exception as e:
        logging.error(f"Could not retrieve expenses: {str(e)}")
        return jsonify({
            "message": "Could not retrieve expenses",
            "error": str(e)
        }), 500

    finally:
        conn.close()

@app.get("/expenses/<int:user_id>/pending")
def get_pending_expenses(user_id):
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("""SELECT * FROM expenses JOIN approvals ON 
            approvals.expense_id = expenses.id WHERE expenses.user_id = ? AND approvals.status = ? """, 
            (user_id, "pending"))
        expenses = [dict(row) for row in cursor.fetchall()]
        logging.info(f"User {user_id} viewed pending expenses")
        return jsonify(expenses), 200

    except Exception as e:
        logging.error(f"Could not retrieve expenses: {str(e)}")
        return jsonify({
            "message": "Could not retrieve expenses",
            "error": str(e)
        }), 500

    finally:
        conn.close()

@app.get("/expenses/<int:user_id>/history")
def get_expense_history(user_id):
    conn = get_db()
    cursor = conn.cursor()
    try:
        cursor.execute("""SELECT * FROM expenses JOIN approvals ON 
                approvals.expense_id = expenses.id WHERE expenses.user_id = ? 
                AND (approvals.status = ? OR approvals.status = ?)""", 
            (user_id, "approved", "denied"))
        expenses = [dict(row) for row in cursor.fetchall()]
        logging.info(f"User {user_id} retrieved expense history")
        return jsonify(expenses), 200

    except Exception as e:
        logging.error(f"Could not retrieve expenses: {str(e)}")
        return jsonify({
            "message": "Could not retrieve expenses",
            "error": str(e)
        }), 500

    finally:
        conn.close()


@app.delete("/expense/<int:expense_id>")
def delete_expense(expense_id):
    conn = get_db()
    cursor = conn.cursor()

    try:
        cursor.execute(
            "DELETE FROM approvals WHERE expense_id = ?",
            (expense_id,)
        )
        cursor.execute(
            "DELETE FROM expenses WHERE id = ?",
            (expense_id,)
        )
        if cursor.rowcount == 0:
            conn.rollback()
            logging.error(f"Delete failed for expense {expense_id}: {str(e)}")
            return jsonify({
                "message": "Expense not found"
            }), 404
        conn.commit()
        logging.info(f"Expense {expense_id} deleted")
        return jsonify({
            "message": f"Expense {expense_id} deleted successfully"
        }), 200
    except Exception as e:
        conn.rollback()
        logging.error(f"Delete failed for expense {expense_id}: {str(e)}")
        return jsonify({
            "message": "Delete failed",
            "error": str(e)
        }), 500

    finally:
        conn.close()

@app.put("/expense/<int:expense_id>")
def edit_expense(expense_id):
    data = request.json

    allowed_fields = ["amount", "category", "description", "date"]
    updates = {k: v for k, v in data.items() if k in allowed_fields}
    if not updates:
        return jsonify({
            "message": "No valid fields provided"
        }), 400

    conn = get_db()
    cursor = conn.cursor()

    try:
        set_clause = ", ".join([f"{field} = ?" for field in updates.keys()])
        values = list(updates.values())
        values.append(expense_id)

        cursor.execute(
            f"UPDATE expenses SET {set_clause} WHERE id = ?",
            values
        )

        if cursor.rowcount == 0:
            conn.rollback()
            logging.error(f"Update failed for expense {expense_id}: Expense not found")
            return jsonify({
                "message": "Expense not found"
            }), 404

        conn.commit()
        logging.info(
            f"Expense {expense_id} updated. Fields changed: {list(updates.keys())}"
        )
        return jsonify({
            "message": f"Expense {expense_id} updated successfully"
        }), 200

    except Exception as e:
        conn.rollback()
        logging.error(f"Update failed for expense {expense_id}: {str(e)}")
        return jsonify({
            "message": "Update failed",
            "error": str(e)
        }), 500

    finally:
        conn.close()


if __name__ == "__main__":
    app.run()