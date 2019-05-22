export interface User {
  username: string;
  email: string;
  authToken: string;
  roles: string[];
  accessToken: string;

  firstName: string;
  lastName: string;
  organisation: string;
  description: string;
  avatar: string;
}
