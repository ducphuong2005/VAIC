import { get, post } from './apiClient.js';

export const getLatestRecommendations = () => get('/recommendations/latest');
export const generateRecommendations = (payload = {}) => post('/recommendations/generate', payload);
export const getRecommendation = id => get(`/recommendations/${encodeURIComponent(id)}`);
export const getSkillGaps = id => get(`/recommendations/${encodeURIComponent(id)}/skill-gaps`);
