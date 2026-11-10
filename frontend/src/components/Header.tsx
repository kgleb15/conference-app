import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material'
import { getPerson, logout } from '../api/auth'

const ROLE_LABELS = { PARTICIPANT: 'участник', ORGANIZER: 'организатор', EXPERT: 'эксперт' }

export default function Header() {
    const person = getPerson()

    return (
        <AppBar position="static">
            <Toolbar>
                <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" component="a" href="/"
                        sx={{ color: 'inherit', textDecoration: 'none', fontWeight: 700, letterSpacing: '0.5px' }}>
                        Конкурс НИРС ВолгГТУ
                    </Typography>
                </Box>

                {person ? (<>
                    <Typography sx={{ mr: 2 }}>{person.fullName} ({ROLE_LABELS[person.role]})</Typography>
                    <Button color="inherit" onClick={() => { logout(); window.location.href = '/' }}>
                        Выйти
                    </Button>
                </>) : (<>
                    <Button color="inherit" href="/login">Вход</Button>
                    <Button color="inherit" href="/register">Регистрация</Button>
                </>)}
            </Toolbar>
        </AppBar>
    )
}
