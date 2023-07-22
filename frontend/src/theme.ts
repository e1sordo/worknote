function updateTheme() {
    const prefersDarkScheme = window.matchMedia("(prefers-color-scheme: dark)").matches;
    const htmlElement = document.documentElement;
    const newTheme = prefersDarkScheme ? "dark" : "light";
    htmlElement.setAttribute('data-bs-theme', newTheme);
}

export function listenForThemeChanges() {
    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    const onThemeChange = () => { updateTheme(); };
    mediaQuery.addEventListener('change', onThemeChange);
    updateTheme();
}
