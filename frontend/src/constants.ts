export const taskTypeIcons = new Map<string, string>();
taskTypeIcons.set('DEVELOPMENT', '👨🏻‍💻');
taskTypeIcons.set('PROCESS_MAINTENANCE', '🗣️');
taskTypeIcons.set('TRAINING_AND_DEVELOPMENT', '🏋️‍♀️');
taskTypeIcons.set('CLUBS_AND_EVENTS', '🎭');
taskTypeIcons.set('ORGANIZING_ACTIVITIES', '🥇');
taskTypeIcons.set('INTERVIEWS', '🤝');
taskTypeIcons.set('ABSENCES', '🤒');

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
