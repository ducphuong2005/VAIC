import { get, post } from './apiClient.js';

export const getChatSessions = () => get('/chat/sessions');
export const getChatSession = sessionId => get(`/chat/sessions/${encodeURIComponent(sessionId)}`);
export const createChatSession = title => post('/chat/sessions', { title });
export const sendChatMessage = (sessionId, content) =>
  post(`/chat/sessions/${encodeURIComponent(sessionId)}/messages`, { content });
