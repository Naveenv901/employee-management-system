import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "./styles/ViewBiodata.css"; // External CSS file

function ViewBiodata() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        // Fetch the logged-in user's biodata
        axios.get("http://localhost:8080/api/user/profile", { withCredentials: true })
            .then(res => {
                setUser(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setError("Failed to fetch biodata. Please login again.");
                setLoading(false);
            });
    }, []);

    const handleLogout = () => {
        axios.post("http://localhost:8080/api/logout", {}, { withCredentials: true })
            .then(() => {
                sessionStorage.clear();
                navigate("/login");
            })
            .catch(err => {
                console.error(err);
                setError("Logout failed. Please try again.");
            });
    };

    if (loading) {
        return <div className="view-wrapper"><p>Loading...</p></div>;
    }

    if (error) {
        return <div className="view-wrapper"><p className="error">{error}</p></div>;
    }

    return (
        <div className="view-wrapper">
            <div className="header">
                <h2>My Biodata</h2>
                <button className="logout-btn" onClick={handleLogout}>Logout</button>
            </div>

            <div className="biodata-card">
                <p><strong>Name:</strong> {user.name}</p>
                <p><strong>Email:</strong> {user.email}</p>
                <p><strong>Phone:</strong> {user.phone}</p>
                <p><strong>Date of Birth:</strong> {user.dob}</p>
                <p><strong>Age:</strong> {user.age}</p>
                <p><strong>Gender:</strong> {user.gender}</p>
                <p><strong>Address:</strong> {user.address}, {user.city}, {user.state}, {user.country}</p>
                <p><strong>Skills:</strong> {user.skills?.join(", ")}</p>
                <p><strong>Role:</strong> {user.role}</p>
            </div>
        </div>
    );
}

export default ViewBiodata;