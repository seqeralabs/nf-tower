package watchtower.service.pogo.exceptions

class WorkflowNotExistsException extends RuntimeException {

    WorkflowNotExistsException() {
        super("Workflow doesn't exist")
    }

}
