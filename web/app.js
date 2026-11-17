
const API_URL = "/api/students";

let students = [];

// Navigation
document.querySelectorAll(".nav-btn").forEach(button => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".nav-btn").forEach(btn =>
            btn.classList.remove("active")
        );

        document.querySelectorAll(".page").forEach(page =>
            page.classList.remove("active")
        );

        button.classList.add("active");

        const page = button.dataset.page;
        document.getElementById(page).classList.add("active");

        if (page === "students" || page === "dashboard") {
            loadStudents();
        }
    });
});

// Load students from Java backend
async function loadStudents() {
    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Unable to load students");
        }

        students = await response.json();

        updateDashboard();
        displayStudents(students);

    } catch (error) {
        console.error(error);
        document.getElementById("studentsList").innerHTML =
            "<p>Unable to connect to server. Please start Java backend.</p>";
    }
}

// Safe HTML text
function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}

// Dashboard
function updateDashboard() {
    document.getElementById("totalStudents").textContent =
        students.length;

    document.getElementById("passedStudents").textContent =
        students.filter(s => s.status === "PASS").length;

    document.getElementById("failedStudents").textContent =
        students.filter(s => s.status === "FAIL").length;

    const recent = students.slice(-5).reverse();

    document.getElementById("recentStudents").innerHTML =
        recent.length
            ? recent.map(studentCard).join("")
            : "<p>No students added yet.</p>";
}

// Student card
function studentCard(student) {
    return `
        <div class="student-card">
            <h3>${escapeHTML(student.name)}</h3>
            <p><b>Roll Number:</b> ${escapeHTML(student.rollNumber)}</p>
            <p><b>Department:</b> ${escapeHTML(student.department)}</p>
            <p><b>Semester:</b> ${escapeHTML(student.semester)}</p>
            <p><b>Total Marks:</b> ${student.total}</p>
            <p><b>Percentage:</b> ${student.percentage}%</p>
            <p><b>Grade:</b> ${escapeHTML(student.grade)}</p>
            <p class="${student.status === "PASS" ? "status-pass" : "status-fail"}">
                ${escapeHTML(student.status)}
            </p>
            <button class="submit-btn" onclick="printResult('${encodeURIComponent(student.rollNumber)}')">
                View / Print Result
            </button>
        </div>
    `;
}

// Display all students
function displayStudents(list) {
    const container = document.getElementById("studentsList");

    if (list.length === 0) {
        container.innerHTML = "<p>No students found.</p>";
        return;
    }

    container.innerHTML = list.map(studentCard).join("");
}

// Search students
document.getElementById("searchInput").addEventListener("input", event => {
    const search = event.target.value.toLowerCase();

    const filtered = students.filter(student =>
        student.name.toLowerCase().includes(search) ||
        student.rollNumber.toLowerCase().includes(search)
    );

    displayStudents(filtered);
});

// Add student
document.getElementById("studentForm").addEventListener("submit", async event => {
    event.preventDefault();

    const form = event.target;
    const message = document.getElementById("formMessage");
    const formData = new FormData(form);

    const marks = ["mark0", "mark1", "mark2", "mark3", "mark4"];

    for (const key of marks) {
        const value = Number(formData.get(key));

        if (!Number.isInteger(value) || value < 0 || value > 100) {
            message.textContent = "Marks must be between 0 and 100.";
            message.style.color = "red";
            return;
        }
    }

    const body = new URLSearchParams(formData);

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: body.toString()
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Unable to save student");
        }

        message.textContent = "Student result saved successfully!";
        message.style.color = "green";

        form.reset();
        await loadStudents();

    } catch (error) {
        message.textContent = error.message;
        message.style.color = "red";
    }
});

// View and print result
async function printResult(encodedRoll) {
    const roll = decodeURIComponent(encodedRoll);

    const student = students.find(s => s.rollNumber === roll);

    if (!student) {
        alert("Student not found!");
        return;
    }

    const marksHTML = Object.entries(student.marks)
        .map(([subject, mark]) => `
            <tr>
                <td>${escapeHTML(subject)}</td>
                <td>${mark}</td>
            </tr>
        `).join("");

    const resultWindow = window.open("", "_blank");

    if (!resultWindow) {
        alert("Please allow pop-ups to print the marksheet.");
        return;
    }

    resultWindow.document.write(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>Student Marksheet</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    padding: 35px;
                    color: #222;
                }
                h1, h2, p { text-align: center; }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 25px;
                }
                th, td {
                    border: 1px solid #777;
                    padding: 12px;
                    text-align: center;
                }
                th { background: #e8eefc; }
                .details { margin-top: 25px; }
                button {
                    padding: 10px 20px;
                    margin-top: 25px;
                    cursor: pointer;
                }
                @media print {
                    button { display: none; }
                }
            </style>
        </head>
        <body>
            <h1>STUDENT RESULT MANAGEMENT SYSTEM</h1>
            <h2>STUDENT MARKSHEET</h2>

            <div class="details">
                <p><b>Name:</b> ${escapeHTML(student.name)}</p>
                <p><b>Roll Number:</b> ${escapeHTML(student.rollNumber)}</p>
                <p><b>Department:</b> ${escapeHTML(student.department)}</p>
                <p><b>Semester:</b> ${escapeHTML(student.semester)}</p>
            </div>

            <table>
                <tr><th>Subject</th><th>Marks</th></tr>
                ${marksHTML}
            </table>

            <h3>Total: ${student.total} / 500</h3>
            <h3>Percentage: ${student.percentage}%</h3>
            <h3>Grade: ${escapeHTML(student.grade)}</h3>
            <h3>Result: ${escapeHTML(student.status)}</h3>

            <button onclick="window.print()">Print Marksheet</button>
        </body>
        </html>
    `);

    resultWindow.document.close();
}

// Initial load
loadStudents();
