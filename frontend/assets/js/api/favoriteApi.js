import { del, get, post } from './apiClient.js';

export const getFavoriteCareers = () => get('/favorites/careers');
export const saveFavoriteCareer = onetCode => post(`/favorites/careers/${encodeURIComponent(onetCode)}`);
export const deleteFavoriteCareer = onetCode => del(`/favorites/careers/${encodeURIComponent(onetCode)}`);
export const getFavoriteCourses = () => get('/favorites/courses');
