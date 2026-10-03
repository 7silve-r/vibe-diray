import { createRouter, createWebHistory } from 'vue-router';
export default createRouter({history:createWebHistory(),scrollBehavior:()=>({top:0}),routes:[
{path:'/',component:()=>import('./views/HomeView.vue')},
{path:'/account',component:()=>import('./views/AccountView.vue')},
{path:'/:pathMatch(.*)*',redirect:'/'}]});
