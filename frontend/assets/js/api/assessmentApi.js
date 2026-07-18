import { get, post } from './apiClient.js';

export const getAssessments = () => get('/assessments');
export const getAssessment = id => get(`/assessments/${encodeURIComponent(id)}`);
export const startAssessment = id => post(`/assessments/${encodeURIComponent(id)}/start`);
