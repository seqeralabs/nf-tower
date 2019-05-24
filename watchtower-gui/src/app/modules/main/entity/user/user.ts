import {UserData} from "./user-data";

export class User {

  data: UserData;

  constructor(userData: UserData) {
    this.data = userData;
  }

  get avatar(): string {
    return (this.data.avatar || 'https://upload.wikimedia.org/wikipedia/commons/7/7c/Profile_avatar_placeholder_large.png');
  }

}
