import { get } from './apiClient.js';

const query = params => {
  const search = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  const text = search.toString();
  return text ? `?${text}` : '';
};

export const getCareers = (params = {}) => get(`/careers${query(params)}`, { auth: false });
export const searchCareers = (q, params = {}) => get(`/careers/search${query({ ...params, q })}`, { auth: false });
export const getCareerClusters = () => get('/career-clusters', { auth: false });
