export interface UserData {
  userName: string;
  email: string;
  roles: string[];
  jwtAccessToken: string;
  nfAccessToken: string;

  firstName: string;
  lastName: string;
  organization: string;
  description: string;
  avatar: string;
}
