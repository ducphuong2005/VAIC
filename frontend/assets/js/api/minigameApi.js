import{get,post}from'./apiClient.js';
export const getMiniGames=()=>get('/minigames',{auth:false});export const getMiniGame=id=>get(`/minigames/${encodeURIComponent(id)}`,{auth:false});export const startMiniGame=id=>post(`/minigames/${encodeURIComponent(id)}/start`);
