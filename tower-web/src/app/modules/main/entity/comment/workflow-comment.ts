import {WorkflowCommentAuthor} from "./workflow-comment-author";
import {FormatterUtil} from "../../util/formatter-util";

export class WorkflowComment {
  author: WorkflowCommentAuthor;
  dateCreated: Date;
  id: number;
  text: string;
  userId: number;
  workflowId: string;
  lastUpdated?: Date;
  deleted?: boolean;
}
