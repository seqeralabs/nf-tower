import {UserData} from "./user-data";

export class User {

  data: UserData;

  constructor(userData: UserData) {
    this.data = userData;
  }

  get avatar(): string {
    return (this.data.avatar || '/assets/avatar_placeholder.png');
  }

  get nameToDisplay(): string {
    return (this.data.firstName && this.data.lastName) ? `${this.data.firstName} ${this.data.lastName}` : this.data.userName;
  }

  generateCopy(): User {
    let userDataCopy: UserData = JSON.parse(JSON.stringify(this.data));

    return new User(userDataCopy)
  }

}
