import { Navigate } from "react-router-dom";

function ProtectedRoute({ children, role }) {
    const userRole = sessionStorage.getItem("userRole");

    if (!userRole) {
        return <Navigate to="/login" replace />;
    }

    if (role && userRole !== role) {
        return <Navigate to="/" replace />;
    }

    return children;
}

export default ProtectedRoute;