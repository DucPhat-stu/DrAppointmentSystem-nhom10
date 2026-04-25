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
import ProfilePage from '../pages/ProfilePage.jsx';
import NotificationPage from '../pages/NotificationPage.jsx';
import DoctorSchedulePage from '../pages/DoctorSchedulePage.jsx';
import DoctorAppointmentDashboardPage from '../pages/DoctorAppointmentDashboardPage.jsx';
import DoctorLeavePage from '../pages/DoctorLeavePage.jsx';
import AdminLeavePage from '../pages/AdminLeavePage.jsx';

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

export const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    element: <ProtectedLayout />,
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
        element: <DoctorSchedulePage />,
      },
      {
        path: '/doctor/appointments',
        element: <DoctorAppointmentDashboardPage />,
      },
      {
        path: '/doctor/leaves',
        element: <DoctorLeavePage />,
      },
      {
        path: '/admin/leaves',
        element: <AdminLeavePage />,
      },
    ],
  },
]);
