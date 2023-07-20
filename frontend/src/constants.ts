interface TaskTypeMeta {
    [key: string]: TaskTypeMetaItem
}

interface TaskTypeMetaItem {
    description: string;
    icon: string;
    bgColor: string;
    textColor: string;
}

export const taskTypeMeta: TaskTypeMeta = {
    DEVELOPMENT: {
        description: 'Разработка',
        icon: '👨🏻‍💻',
        bgColor: 'rgb(223, 96, 104)',
        textColor: 'rgb(255, 223, 223)'
    },
    PROCESS_MAINTENANCE: {
        description: 'Обслуживание процесса',
        icon: '🎙️',
        bgColor: 'rgb(96, 174, 223)',
        textColor: 'rgb(250, 253, 255)'
    },
    TRAINING_AND_DEVELOPMENT: {
        description: 'Обучение и развитие',
        icon: '🏋️‍♀️',
        bgColor: 'rgb(101, 223, 96)',
        textColor: '#ffffff'
    },
    CLUBS_AND_EVENTS: {
        description: 'Клубы и мероприятия',
        icon: '🎭',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    ORGANIZING_ACTIVITIES: {
        description: 'Орг. деятельность',
        icon: '🪪',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    INTERVIEWS: {
        description: 'Собеседования',
        icon: '🤝',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    ABSENCES: {
        description: 'Отсутствия',
        icon: '🤒',
        bgColor: 'rgb(61, 42, 42)',
        textColor: '#ffffff'
    }
};

export function formatTime(minutes: number): string {
    if (minutes < 60) {
        return `${minutes}м`;
    }

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (remainingMinutes === 0) {
        return `${hours}ч`;
    } else if (remainingMinutes % 10 === 0) {
        return `${hours}ч ${remainingMinutes}м`;
    } else {
        return `${hours}ч ${remainingMinutes}м`;
    }
}

export function convertTimeToMinutes(timeString: string): number {
    const trimmedString = timeString.trim();

    const hoursMatch = trimmedString.match(/(\d+)\s*[чh]/);
    const minutesMatch = trimmedString.match(/(\d+)\s*[мm]/);

    if (hoursMatch || minutesMatch) {
        return (hoursMatch && (60 * parseInt(hoursMatch[1], 10)) || 0) + (minutesMatch && parseInt(minutesMatch[1], 10) || 0);
    } else if (!isNaN(parseInt(trimmedString, 10))) {
        const numericValue = parseInt(trimmedString, 10);
        if (numericValue < 9) {
            return numericValue * 60; // Часы
        } else {
            return numericValue; // Минуты
        }
    }

    throw new Error("Некорректный формат строки времени");
}