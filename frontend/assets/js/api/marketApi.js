import { get } from './apiClient.js';

export const getTrendingCareers = (limit = 10) => get(`/market/trending?limit=${encodeURIComponent(limit)}`, { auth: false });
export const getSkillsInDemand = (limit = 20) => get(`/market/skills-in-demand?limit=${encodeURIComponent(limit)}`, { auth: false });
export const getRegions = () => get('/market/regions', { auth: false });
