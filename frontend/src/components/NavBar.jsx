import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function NavBar() {
  const { isAuthenticated, username, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">CryptoX</Link>
      </div>
      <div className="navbar-links">
        <Link to="/market">Market</Link>
        {isAuthenticated ? (
          <>
            <Link to="/dashboard">Wallet</Link>
            <Link to="/trade">Trade</Link>
            <Link to="/send">Send</Link>
            <Link to="/history">History</Link>
            <span className="navbar-user">Hi, {username}</span>
            <button className="btn btn-secondary" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register" className="btn btn-primary btn-small">
              Sign up
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}
