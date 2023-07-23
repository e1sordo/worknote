export function convertTimeToMinutes(timeString: string): number {
    const trimmedString = timeString.trim();

    const hoursMatch = trimmedString.match(/(\d+)\s*[чh]/);
    const minutesMatch = trimmedString.match(/(\d+)\s*[мm]/);

    if (hoursMatch || minutesMatch) {
        return (hoursMatch && (60 * parseInt(hoursMatch[1], 10)) || 0) + (minutesMatch && parseInt(minutesMatch[1], 10) || 0);
    } else if (!isNaN(parseInt(trimmedString, 10))) {
        const numericValue = parseInt(trimmedString, 10);
        if (numericValue < 9) {
            return numericValue * 60; // Hours
        } else {
            return numericValue; // Minutes
        }
    }

    throw new Error("Incorrect format of time string");
}
