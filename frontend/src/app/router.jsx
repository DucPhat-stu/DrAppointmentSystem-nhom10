import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import AppShell from '../components/layout/AppShell.jsx';
import AdminShell from '../components/layout/AdminShell.jsx';
import { useAuth } from '../hooks/useAuth.js';
import HomePage from '../pages/HomePage/HomePage.jsx';
import LoginPage from '../pages/LoginPage/LoginPage.jsx';
import RegisterPage from '../pages/RegisterPage/RegisterPage.jsx';
import ForgotPasswordPage from '../pages/ForgotPasswordPage.jsx';
import ResetPasswordPage from '../pages/ResetPasswordPage.jsx';
import DoctorListPage from '../pages/DoctorListPage.jsx';
import DoctorDetailPage from '../pages/DoctorDetailPage.jsx';
import BookAppointmentPage from '../pages/BookAppointmentPage.jsx';
import AppointmentDetailPage from '../pages/AppointmentDetailPage.jsx';
import MyAppointmentsPage from '../pages/MyAppointmentsPage.jsx';
import ProfilePage from '../pages/ProfilePage.jsx';
import NotificationPage from '../pages/NotificationPage.jsx';
import ChatbotPage from '../pages/ChatbotPage.jsx';
import TwoFactorPage from '../pages/TwoFactorPage.jsx';
import DoctorSchedulePage from '../pages/DoctorSchedulePage.jsx';
import DoctorAppointmentDashboardPage from '../pages/DoctorAppointmentDashboardPage.jsx';
import DoctorLeavePage from '../pages/DoctorLeavePage.jsx';
import AdminLeavePage from '../pages/AdminLeavePage.jsx';
import AdminPromptPage from '../pages/AdminPromptPage.jsx';
import AdminDashboardPage from '../pages/Admin/AdminDashboardPage.jsx';
import AdminUsersPage from '../pages/Admin/AdminUsersPage.jsx';
import AdminDoctorsPage from '../pages/Admin/AdminDoctorsPage.jsx';
import AdminAppointmentsPage from '../pages/Admin/AdminAppointmentsPage.jsx';
import AdminNotificationsPage from '../pages/Admin/AdminNotificationsPage.jsx';
import RouteFallbackPage from '../pages/RouteFallbackPage.jsx';

function ProtectedLayout() {
  const { session } = useAuth();
  if (!session) return <Navigate to="/login" replace />;
  return <AppShell><Outlet /></AppShell>;
}

function AdminLayout() {
  const { session } = useAuth();
  if (!session) return <Navigate to="/login" replace />;
  const role = session.role?.replace(/^ROLE_/, '').toUpperCase();
  if (role !== 'ADMIN' && role !== 'SUPER_ADMIN') return <Navigate to="/doctors" replace />;
  return <AdminShell><Outlet /></AdminShell>;
}

function normalizeRole(role) {
  return role?.replace(/^ROLE_/, '').toUpperCase();
}

function RoleGate({ allowedRoles, children }) {
  const { session } = useAuth();
  const role = normalizeRole(session?.role);
  if (!allowedRoles.includes(role)) return <Navigate to="/doctors" replace />;
  return children;
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
    errorElement: <RouteFallbackPage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
    errorElement: <RouteFallbackPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
    errorElement: <RouteFallbackPage />,
  },
  {
    path: '/forgot-password',
    element: <ForgotPasswordPage />,
    errorElement: <RouteFallbackPage />,
  },
  {
    path: '/reset-password',
    element: <ResetPasswordPage />,
    errorElement: <RouteFallbackPage />,
  },

  /* ── Admin routes — wrapped in AdminShell (light theme) ── */
  {
    path: '/admin',
    element: <AdminLayout />,
    errorElement: <RouteFallbackPage />,
    children: [
      { index: true,                    element: <AdminDashboardPage /> },
      { path: 'users',                  element: <AdminUsersPage /> },
      { path: 'doctors',                element: <AdminDoctorsPage /> },
      { path: 'appointments',           element: <AdminAppointmentsPage /> },
      { path: 'leaves',                 element: <AdminLeavePage /> },
      { path: 'prompts',                element: <AdminPromptPage /> },
      { path: 'notifications',          element: <AdminNotificationsPage /> },
    ],
  },

  /* ── Standard user routes — wrapped in AppShell (dark theme) ── */
  {
    element: <ProtectedLayout />,
    errorElement: <RouteFallbackPage />,
    children: [
      { path: '/doctors',                       element: <DoctorListPage /> },
      { path: '/doctors/:doctorId',             element: <DoctorDetailPage /> },
      { path: '/appointments/book',             element: <BookAppointmentPage /> },
      { path: '/appointments',                  element: <MyAppointmentsPage /> },
      { path: '/appointments/:appointmentId',   element: <AppointmentDetailPage /> },
      { path: '/profile',                       element: <ProfilePage /> },
      { path: '/notifications',                 element: <NotificationPage /> },
      { path: '/chat',                          element: <ChatbotPage /> },
      { path: '/security/2fa',                  element: <TwoFactorPage /> },
      {
        path: '/doctor/schedules',
        element: <RoleGate allowedRoles={['DOCTOR', 'ADMIN']}><DoctorSchedulePage /></RoleGate>,
      },
      {
        path: '/doctor/appointments',
        element: <RoleGate allowedRoles={['DOCTOR', 'ADMIN']}><DoctorAppointmentDashboardPage /></RoleGate>,
      },
      {
        path: '/doctor/leaves',
        element: <RoleGate allowedRoles={['DOCTOR', 'ADMIN']}><DoctorLeavePage /></RoleGate>,
      },
      { path: '*', element: <RouteFallbackPage /> },
    ],
  },
]);
