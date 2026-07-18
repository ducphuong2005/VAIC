import { post } from './apiClient.js';
import { setCurrentUser, setTokens } from '../core/authStore.js';

function persistAuth(response){
  setTokens({accessToken:response?.accessToken,refreshToken:response?.refreshToken});
  setCurrentUser(response?.user||null);
  return response;
}

export async function login(data){
  const response=await post('/auth/login',{email:data.email,password:data.password},{auth:false});
  return persistAuth(response);
}

export async function register(data){
  const response=await post('/auth/register',{email:data.email,password:data.password,fullName:data.fullName},{auth:false});
  return persistAuth(response);
}
