import i18n from '@/i18n';

interface TaskTypeMeta {
    [key: string]: TaskTypeMetaItem
}

interface TaskTypeMetaItem {
    icon: string;
    bgColor: string;
    textColor: string;
}

export const taskTypeMeta: TaskTypeMeta = {
    DEVELOPMENT: {
        icon: '👨🏻‍💻',
        bgColor: 'rgb(223, 96, 104)',
        textColor: 'rgb(255, 223, 223)'
    },
    PROCESS_MAINTENANCE: {
        icon: '🎙️',
        bgColor: 'rgb(96, 174, 223)',
        textColor: 'rgb(250, 253, 255)'
    },
    TRAINING_AND_DEVELOPMENT: {
        icon: '🏋️‍♀️',
        bgColor: 'rgb(101, 223, 96)',
        textColor: '#ffffff'
    },
    CLUBS_AND_EVENTS: {
        icon: '🎭',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    ORGANIZING_ACTIVITIES: {
        icon: '🖨',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    INTERVIEWS: {
        icon: '🤝',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    ABSENCES: {
        icon: '🤒',
        bgColor: 'rgb(61, 42, 42)',
        textColor: '#ffffff'
    },
    DUTY: {
        icon: '🛡️',
        bgColor: 'rgba(97, 60, 5, 1)',
        textColor: 'rgba(255, 255, 255, 1)'
    }
};

export function formatTime(minutes: number): string {
    const { t } = i18n.global;

    const h = t("time.shortHour");
    const m = t("time.shortMinute");

    if (minutes < 60) {
        return `${minutes}${m}`;
    }

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (remainingMinutes === 0) {
        return `${hours}${h}`;
    } else if (remainingMinutes % 10 === 0) {
        return `${hours}${h} ${remainingMinutes}${m}`;
    } else {
        return `${hours}${h} ${remainingMinutes}${m}`;
    }
}
