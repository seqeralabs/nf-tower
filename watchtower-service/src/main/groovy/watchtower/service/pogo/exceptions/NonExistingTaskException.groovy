package watchtower.service.pogo.exceptions

class NonExistingTaskException extends RuntimeException {

    NonExistingTaskException(String message) {
        super(message)
    }

}
