export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
  totpCode?: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
  user: UserAuth;
}

export interface UserAuth {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  role: string;
  twoFactorEnabled: boolean;
}
