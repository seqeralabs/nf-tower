package watchtower.service.pogo.exceptions

class NonExistingWorkflowException extends RuntimeException {

    NonExistingWorkflowException(String message) {
        super(message)
    }

}
