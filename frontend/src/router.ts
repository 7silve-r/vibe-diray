import { createRouter, createWebHistory } from 'vue-router';
export default createRouter({history:createWebHistory(),scrollBehavior:()=>({top:0}),routes:[
{path:'/',component:()=>import('./views/HomeView.vue')},
{path:'/account',component:()=>import('./views/AccountView.vue')},
{path:'/music/:kind?/:id?',component:()=>import('./views/MusicView.vue')},
{path:'/diary',component:()=>import('./views/DiaryView.vue')},
{path:'/diary/write/:id?',component:()=>import('./views/EditorView.vue')},
{path:'/diary/:id',component:()=>import('./views/ArticleView.vue')},
{path:'/:pathMatch(.*)*',redirect:'/'}]});
