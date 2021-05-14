import Vue from "vue"
import VueRouter from "vue-router"
import SecKillList from "../views/SecKillList.vue"
import SecKillGoods from "../views/SecKillGoods.vue"
const originalReplace = VueRouter.prototype.replace;
VueRouter.prototype.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => err);
};


Vue.use(VueRouter)

const routes = [
	{
		path: '/',
		name:'SecKillList',
		component: SecKillList
	},
	{
		path: '/SecKillGoods/:gid',
		name:'SecKillGoods',
		component: SecKillGoods,
		props:true
	}
]




const router = new VueRouter({
	mode: 'history',
	base: process.env.BASE_URL,
	routes
})

export default router
