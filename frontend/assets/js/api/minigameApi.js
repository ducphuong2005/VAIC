import{get,post}from'./apiClient.js';
export const getMiniGames=()=>get('/minigames',{auth:false});export const getMiniGame=id=>get(`/minigames/${encodeURIComponent(id)}`,{auth:false});export const startMiniGame=id=>post(`/minigames/${encodeURIComponent(id)}/start`);
export const recordMiniGameActions=(sessionId,actions)=>post(`/minigame-sessions/${encodeURIComponent(sessionId)}/actions`,{actions});
export const completeMiniGame=(sessionId,rawMetrics,resultSummary)=>post(`/minigame-sessions/${encodeURIComponent(sessionId)}/complete`,{rawMetrics,resultSummary});
