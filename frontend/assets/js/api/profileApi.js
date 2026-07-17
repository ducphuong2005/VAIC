import{get,put}from'./apiClient.js';
export const getProfile=()=>get('/profile');export const updateProfile=data=>put('/profile',data);export const getDimensions=()=>get('/profile/dimensions');export const getProfileCompletion=()=>get('/profile/completion');
