import { useState } from 'react'
import './Accordion.css'

type Item = { question: string, answer: string }

export default function Accordion({ items }: { items: Item[] }) {
    const [openIndex, setOpenIndex] = useState<number | null>(null) // Номер раскрытого пункта или null (ничего не раскрыто)

    return (
        <div className="accordion">
            {items.map((item, i) => {
                const isOpen = openIndex === i
                return (
                    <div className={`accordion_item${isOpen ? ' active' : ''}`} key={i}>
                        <button className="accordion_header" aria-expanded={isOpen}
                            onClick={() => setOpenIndex(isOpen ? null : i)}>
                            {item.question}<span className="accordion_icon">+</span>
                        </button>
                        <div className="accordion_body">
                            <div className="accordion_content">
                                <p>{item.answer}</p>
                            </div>
                        </div>
                    </div>
                )
            })}
        </div>
    )
}
