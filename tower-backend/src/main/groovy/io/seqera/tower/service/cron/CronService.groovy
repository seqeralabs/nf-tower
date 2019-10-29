package io.seqera.tower.service.cron
/**
 * Defines basic cron service to execute tasks at scheduled time
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
interface CronService {

    /**
     * Starts the service
     */
    void start()

    /**
     * Stops the service
     */
    void stop()

}
