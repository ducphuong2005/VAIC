import { del, get, post } from './apiClient.js';

export const getFavoriteCareers = () => get('/favorites/careers');
export const saveFavoriteCareer = onetCode => post(`/favorites/careers/${encodeURIComponent(onetCode)}`);
export const deleteFavoriteCareer = onetCode => del(`/favorites/careers/${encodeURIComponent(onetCode)}`);
export const getFavoriteCourses = () => get('/favorites/courses');
export const getFavoriteLinks = () => get('/favorites/links');
export const saveFavoriteLink = link => post('/favorites/links', link);
export const deleteFavoriteLink = id => del(`/favorites/links/${encodeURIComponent(id)}`);
