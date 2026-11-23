import { AppBar, Box, Button, IconButton, Toolbar, Typography } from '@mui/material'
import { getPerson, logout } from '../../api/auth.ts'
import { Link } from 'react-router'
import './Header.css'

const ROLE_LABELS = { PARTICIPANT: 'участник', ORGANIZER: 'организатор', EXPERT: 'эксперт' }

export default function Header({ onToggleSidebar }: { onToggleSidebar?: () => void }) {
    const person = getPerson()

    return (
        <AppBar position="static" className="header_bar">
            <Toolbar>
                {onToggleSidebar && (
                    <IconButton color="inherit" edge="start" sx={{ mr: 1 }} onClick={onToggleSidebar}>
                        <img src="/menu.png" alt="Меню" width={24} height={24} />
                    </IconButton>
                )}

                <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" component={Link} to="/" className="header_title"
                        sx={{ color: 'inherit', textDecoration: 'none', fontWeight: 700, letterSpacing: '0.5px' }}>
                        Конкурс НИРС ВолгГТУ
                    </Typography>
                </Box>

                {person ? (<>
                    <Typography className="header_user" sx={{ mr: 2 }}>{person.fullName} ({ROLE_LABELS[person.role]})</Typography>
                    <Button color="inherit" onClick={() => { logout(); window.location.href = '/' }}>
                        Выйти
                    </Button>
                </>) : (<>
                    <Button color="inherit" component={Link} to="/login">Вход</Button>
                    <Button color="inherit" component={Link} to="/register">Регистрация</Button>
                </>)}
            </Toolbar>
        </AppBar>
    )
}
