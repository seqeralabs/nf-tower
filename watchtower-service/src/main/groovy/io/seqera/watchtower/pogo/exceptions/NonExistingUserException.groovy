package io.seqera.watchtower.pogo.exceptions

class NonExistingUserException extends RuntimeException {

    NonExistingUserException(String message) {
        super(message)
    }

}
