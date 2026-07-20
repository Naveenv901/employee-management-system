import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";
import { useEffect } from "react";
import { FaUser, FaLock, FaEye, FaEyeSlash } from "react-icons/fa";
import "react-toastify/dist/ReactToastify.css";
import "./styles/Login.css";

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const role = sessionStorage.getItem("userRole");
        if (role === "ADMIN") {
            navigate("/biodata-list", { replace: true });
        } else if (role === "USER") {
            navigate("/biodata-view", { replace: true });
        }
    }, [navigate]);

    const handleLogin = async (e) => {
        e.preventDefault();

        if (!email || !password) {
            toast.error("Please enter email and password");
            return;
        }

        try {
            const res = await axios.post(
                "http://localhost:8080/api/login",
                { email, password },
                { withCredentials: true }
            );

            const { role, email: userEmail } = res.data;

            // Save session info
            sessionStorage.setItem("userEmail", userEmail);
            sessionStorage.setItem("userRole", role);

            toast.success(`Login successful as ${role}`);

            // Redirect based on role
            setTimeout(() => {
                if (role === "ADMIN") {
                    navigate("/biodata-list");
                } else {
                    navigate("/biodata-view");
                }
            }, 1500);

        } catch (err) {
            if (err.response && err.response.status === 401) {
                toast.error("Invalid email or password ❌");
            } else {
                toast.error("Something went wrong ⚠️");
            }

        }
    };

    return (
        <div className="login-wrapper">
            <ToastContainer />

            <form className="login-form" onSubmit={handleLogin}>
                <h2>Login</h2>

                <div className="input-group">
                    <FaUser className="icon" />
                    <input
                        type="email"
                        placeholder="Enter email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>

                <div className="input-group">
                    <FaLock className="icon" />
                    <input
                        type={showPassword ? "text" : "password"}
                        placeholder="Enter password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <span
                        className="toggle"
                        onClick={() => setShowPassword(!showPassword)}
                    >
                        {showPassword ? <FaEyeSlash /> : <FaEye />}
                    </span>
                </div>

                <button type="submit">Login</button>

            </form>
        </div>
    );
}

export default Login;