import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import './style.css';
import { tilt } from './directives/tilt';
const app = createApp(App);
app.directive('tilt', tilt);
app.use(createPinia()).use(router).mount('#app');
