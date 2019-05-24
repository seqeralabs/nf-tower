import {UserData} from "./user-data";

export class User {

  data: UserData;

  constructor(userData: UserData) {
    this.data = userData;
  }

  get avatar(): string {
    return (this.data.avatar || `https://ui-avatars.com/api/?name=${this.data.firstName}+${this.data.lastName}&size=200`);
  }

}
