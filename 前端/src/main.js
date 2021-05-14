import Vue from 'vue'
import VueAxios from "vue-axios"
import axios from "axios"
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './App.vue'
import router  from "./router/index.js"

import utils from "./util/util.js"

Vue.use(ElementUI)
Vue.use(VueAxios,axios);

Vue.prototype.utils = utils

new Vue({
  el: '#app',
  render: h => h(App),
	router 
})