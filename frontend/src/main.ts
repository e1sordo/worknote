import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css'
import VCalendar from 'v-calendar';
import 'v-calendar/style.css';
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

createApp(App)
    .use(router)
    .use(VCalendar, {})
    .mount('#app')

import "bootstrap/dist/js/bootstrap.js";
