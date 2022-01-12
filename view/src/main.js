import {createApp} from 'vue'
import App from './App.vue'
import router from "./router";
import store from "./store";
import PrimeVue from "primevue/config";
import 'ant-design-vue/dist/antd.css'
import Message from 'primevue/message'

var app = createApp(App);
app.use(router)
    .use(store)
    .use(PrimeVue)
    .use(Message)
    .component('Dialog', Dialog)
    .use(antd)
    .mount('#app')
