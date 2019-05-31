import {SseErrorType} from "./sse-error-type";

export class SseError {

  type: SseErrorType;
  message: string;

  constructor(json: any) {
    this.type = (<any>SseErrorType)[json.type];
    this.message = json.message;
  }


}
