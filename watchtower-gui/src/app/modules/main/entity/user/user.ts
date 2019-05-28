import {UserData} from "./user-data";

export class User {

  data: UserData;

  constructor(userData: UserData) {
    this.data = userData;
  }

  get avatar(): string {
    return (this.data.avatar || '/assets/avatar_placeholder.png');
  }

  generateCopy(): User {
    let userDataCopy: UserData = JSON.parse(JSON.stringify(this.data));

    return new User(userDataCopy)
  }

}
