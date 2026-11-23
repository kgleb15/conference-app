import { useEffect, useState } from 'react'
import { fetchDirections } from '../api/api'
import { getPerson } from '../api/auth'
import './DirectionLabel.css'

/* Подпись с направлением текущего пользователя */
export default function DirectionLabel() {
    const [name, setName] = useState<string>("Направление")
    const directionId = getPerson()?.directionId ?? "0"

    useEffect(() => {
        fetchDirections().then((list) => setName(list.find((d) => d.id === directionId)?.name ?? "Направление"))
    }, [directionId])

    return <p className="page_text direction_value">Направление {directionId}. {name}</p>
}
