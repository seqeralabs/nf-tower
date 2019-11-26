import {WorkflowCommentAuthor} from "./workflow-comment-author";

export class WorkflowComment {
  author: WorkflowCommentAuthor;
  dateCreated: Date;
  id: number;
  text: string;
  userId: number;
  workflowId: string
  lastUpdated?: Date;
}
