const keys={access:'careerCompass.accessToken',refresh:'careerCompass.refreshToken',user:'careerCompass.user'};
const read=k=>{try{return localStorage.getItem(k)}catch{return null}};
const write=(k,v)=>{try{v==null?localStorage.removeItem(k):localStorage.setItem(k,v)}catch{}};
export const getAccessToken=()=>read(keys.access);
export const getRefreshToken=()=>read(keys.refresh);
export function setTokens({accessToken,refreshToken}={}){write(keys.access,accessToken||null);if(refreshToken!==undefined)write(keys.refresh,refreshToken||null)}
export function clearTokens(){write(keys.access,null);write(keys.refresh,null);write(keys.user,null)}
export const getCurrentUser=()=>{try{return JSON.parse(read(keys.user))}catch{return null}};
export const setCurrentUser=user=>write(keys.user,user?JSON.stringify(user):null);
export const isAuthenticated=()=>Boolean(getAccessToken());
