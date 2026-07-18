import { get, post, put } from './apiClient.js';

export const getLearningPaths = () => get('/learning-paths');
export const generateLearningPath = payload => post('/learning-paths/generate', payload);
export const updateLearningStep = (pathId, stepId, payload) =>
  put(`/learning-paths/${encodeURIComponent(pathId)}/steps/${encodeURIComponent(stepId)}/progress`, payload);
