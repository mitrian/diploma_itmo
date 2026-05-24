import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { Layout } from './components/Layout'
import { RequireAuth } from './components/RequireAuth'
import { HomePage } from './pages/home/HomePage'
import { LoginPage } from './pages/login/LoginPage'
import { RegisterPage } from './pages/register/RegisterPage'
import { ProfilePage } from './pages/profile/ProfilePage'
import { RoomHistoryPage } from './pages/profile/RoomHistoryPage'
import { RoomEntryPage } from './pages/room-entry/RoomEntryPage'
import { RoomPage } from './pages/room/RoomPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<HomePage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />
            <Route element={<RequireAuth />}>
              <Route path="room/:roomCode" element={<RoomPage />} />
              <Route path="room" element={<RoomEntryPage />} />
              <Route path="profile" element={<ProfilePage />} />
              <Route path="profile/rooms/:code" element={<RoomHistoryPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
