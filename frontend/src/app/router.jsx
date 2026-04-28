import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import AppShell from '../components/layout/AppShell.jsx';
import { useAuth } from '../hooks/useAuth.js';
import HomePage from '../pages/HomePage/HomePage.jsx';
import LoginPage from '../pages/LoginPage/LoginPage.jsx';
import RegisterPage from '../pages/RegisterPage/RegisterPage.jsx';
import DoctorListPage from '../pages/DoctorListPage.jsx';
import DoctorDetailPage from '../pages/DoctorDetailPage.jsx';
import BookAppointmentPage from '../pages/BookAppointmentPage.jsx';
import AppointmentDetailPage from '../pages/AppointmentDetailPage.jsx';
import MyAppointmentsPage from '../pages/MyAppointmentsPage.jsx';
import ProfilePage from '../pages/ProfilePage.jsx';
import NotificationPage from '../pages/NotificationPage.jsx';
import DoctorSchedulePage from '../pages/DoctorSchedulePage.jsx';
import DoctorAppointmentDashboardPage from '../pages/DoctorAppointmentDashboardPage.jsx';
import DoctorLeavePage from '../pages/DoctorLeavePage.jsx';
import AdminLeavePage from '../pages/AdminLeavePage.jsx';
import RouteFallbackPage from '../pages/RouteFallbackPage.jsx';

function ProtectedLayout() {
  const { session } = useAuth();

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  return (
    <AppShell>
      <Outlet />
    </AppShell>
  );
}

function normalizeRole(role) {
  return role?.replace(/^ROLE_/, '').toUpperCase();
}

function RoleGate({ allowedRoles, children }) {
  const { session } = useAuth();
  const role = normalizeRole(session?.role);

  if (!allowedRoles.includes(role)) {
    return <Navigate to="/doctors" replace />;
  }

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
    element: <ProtectedLayout />,
    errorElement: <RouteFallbackPage />,
    children: [
      {
        path: '/doctors',
        element: <DoctorListPage />,
      },
      {
        path: '/doctors/:doctorId',
        element: <DoctorDetailPage />,
      },
      {
        path: '/appointments/book',
        element: <BookAppointmentPage />,
      },
      {
        path: '/appointments',
        element: <MyAppointmentsPage />,
      },
      {
        path: '/appointments/:appointmentId',
        element: <AppointmentDetailPage />,
      },
      {
        path: '/profile',
        element: <ProfilePage />,
      },
      {
        path: '/notifications',
        element: <NotificationPage />,
      },
      {
        path: '/doctor/schedules',
        element: (
          <RoleGate allowedRoles={['DOCTOR', 'ADMIN']}>
            <DoctorSchedulePage />
          </RoleGate>
        ),
      },
      {
        path: '/doctor/appointments',
        element: (
          <RoleGate allowedRoles={['DOCTOR', 'ADMIN']}>
            <DoctorAppointmentDashboardPage />
          </RoleGate>
        ),
      },
      {
        path: '/doctor/leaves',
        element: (
          <RoleGate allowedRoles={['DOCTOR', 'ADMIN']}>
            <DoctorLeavePage />
          </RoleGate>
        ),
      },
      {
        path: '/admin/leaves',
        element: (
          <RoleGate allowedRoles={['ADMIN']}>
            <AdminLeavePage />
          </RoleGate>
        ),
      },
      {
        path: '*',
        element: <RouteFallbackPage />,
      },
    ],
  },
]);
