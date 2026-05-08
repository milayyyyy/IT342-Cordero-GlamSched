import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Login from './features/auth/Login';
import Register from './features/auth/Register';
import Dashboard from './features/dashboard/Dashboard';
import PrivateRoute from './shared/PrivateRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/dashboard/:tab" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/booking" element={<Navigate to="/dashboard/booknow" replace />} />
        <Route path="/payment" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/artist/:artistId" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
