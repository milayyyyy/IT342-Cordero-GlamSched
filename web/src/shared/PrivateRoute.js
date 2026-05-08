import { Navigate } from 'react-router-dom';

// Redirects to /login if the user is not authenticated
function PrivateRoute({ children }) {
  const userId = localStorage.getItem('userId');
  return userId ? children : <Navigate to="/login" replace />;
}

export default PrivateRoute;
