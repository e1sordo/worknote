listenForThemeChanges();

import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import VCalendar from 'v-calendar';
import 'v-calendar/style.css';
import { createApp } from 'vue';
import VueApexCharts from 'vue3-apexcharts';
import App from './App.vue';
import router from './router';

createApp(App)
    .use(router)
    .use(VCalendar, {})
    .use(VueApexCharts)
    .mount('#app')

import "bootstrap/dist/js/bootstrap.js"; 
import { listenForThemeChanges } from '@/theme';

