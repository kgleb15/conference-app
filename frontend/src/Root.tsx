import { Navigate, Route, Routes } from 'react-router'
import { GuestRoute, RoleRoute } from './components/Guards'
import Layout from './components/Layout'

import HomePage from '../src/pages/HomePage'
import LoginPage from '../src/pages/LoginPage'
import RegisterPage from '../src/pages/RegisterPage'
import SubmitPage from '../src/pages/SubmitPage'
import MyApplicationsPage from '../src/pages/MyApplicationsPage'
import ExpertPage from '../src/pages/ExpertPage'
import AddExpertPage from '../src/pages//AddExpertPage'
import AddOrganizerPage from './pages/AddOrganizerPage'
import ModerationPage from './pages/ModerationPage'

import './styles/App.css'

export default function Root() {
    return (
        <Routes>
            <Route element={<Layout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/login" element={<GuestRoute><LoginPage /></GuestRoute>} />
                <Route path="/register" element={<GuestRoute><RegisterPage /></GuestRoute>} />

                <Route path="/my" element={<RoleRoute role="PARTICIPANT"><MyApplicationsPage /></RoleRoute>} />
                <Route path="/submit" element={<RoleRoute role="PARTICIPANT"><SubmitPage /></RoleRoute>} />

                <Route path="/moderation" element={<RoleRoute role="ORGANIZER"><ModerationPage /></RoleRoute>} />
                <Route path="/add-expert" element={<RoleRoute role="ORGANIZER"><AddExpertPage /></RoleRoute>} />
                <Route path="/add-organizer" element={<RoleRoute role="ORGANIZER"><AddOrganizerPage /></RoleRoute>} />

                <Route path="/review" element={<RoleRoute role="EXPERT"><ExpertPage /></RoleRoute>} />

                <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
        </Routes>
    )
}
