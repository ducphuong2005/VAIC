import { get } from './apiClient.js';

export const getActivityHistory = () => get('/activity-history');
