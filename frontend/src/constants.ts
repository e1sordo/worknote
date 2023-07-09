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
        description: '',
        icon: '👨🏻‍💻',
        bgColor: 'rgb(223, 96, 104)',
        textColor: 'rgb(255, 223, 223)'
    },
    PROCESS_MAINTENANCE: {
        description: '',
        icon: '🎙️',
        bgColor: 'rgb(96, 174, 223)',
        textColor: 'rgb(223, 240, 255)'
    },
    TRAINING_AND_DEVELOPMENT: {
        description: '',
        icon: '🏋️‍♀️',
        bgColor: 'rgb(101, 223, 96)',
        textColor: '#ffffff'
    },
    CLUBS_AND_EVENTS: {
        description: '',
        icon: '🎭',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    ORGANIZING_ACTIVITIES: {
        description: '',
        icon: '🥇',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    INTERVIEWS: {
        description: '',
        icon: '🤝',
        bgColor: 'rgb(188, 96, 223)',
        textColor: '#ffffff'
    },
    ABSENCES: {
        description: '',
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
  
    if (trimmedString.includes("ч") || trimmedString.includes("м")) {
      // Пример строки: "2ч 15м"
      const [hours, minutes] = trimmedString.split(/[чм ]+/);
  
      const hoursValue = parseInt(hours, 10);
      const minutesValue = parseInt(minutes, 10);
  
      return hoursValue * 60 + minutesValue;
    } else if (!isNaN(parseInt(trimmedString, 10))) {
      // Пример строки: "30"
      const numericValue = parseInt(trimmedString, 10);
  
      if (numericValue < 5) {
        return numericValue * 60; // Часы
      } else {
        return numericValue; // Минуты
      }
    }
  
    throw new Error("Некорректный формат строки времени");
  }