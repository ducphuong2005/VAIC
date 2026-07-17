import{get,post}from'./apiClient.js';
export const getMiniGames=()=>get('/minigames');export const getMiniGame=id=>get(`/minigames/${encodeURIComponent(id)}`);export const startMiniGame=id=>post(`/minigames/${encodeURIComponent(id)}/start`);
