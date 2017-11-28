package net.orpiske.mpt.maestro;

import net.orpiske.mpt.common.client.MaestroReceiver;
import net.orpiske.mpt.common.duration.EpochClocks;
import net.orpiske.mpt.common.duration.EpochMicroClock;
import net.orpiske.mpt.common.exceptions.MaestroException;
import net.orpiske.mpt.maestro.client.MaestroClient;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.notes.InternalError;
import net.orpiske.mpt.maestro.notes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MaestroReceiverClient extends MaestroClient implements MaestroReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MaestroReceiverClient.class);

    private final EpochMicroClock epochMicroClock;
    private String clientName;
    private String host;
    private String id;

    public MaestroReceiverClient(String url, final String clientName, final String host, final String id) throws MaestroException {
        super(url);

        this.clientName = clientName;
        this.host = host;
        this.id = id;
        //it is supposed to be used just by one thread
        this.epochMicroClock = EpochClocks.exclusiveMicro();
    }

    public void replyOk() {
        logger.trace("Sending the OK response from {}", this.toString());
        OkResponse okResponse = new OkResponse();

        okResponse.setName(clientName + "@" + host);
        okResponse.setId(id);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, okResponse);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response {}", e.getMessage(), e);
        }
    }

    public void replyInternalError() {
        logger.trace("Sending the internal error response from {}", this.toString());
        InternalError errResponse = new InternalError();

        errResponse.setName(clientName + "@" + host);
        errResponse.setId(id);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, errResponse);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response: {}", e.getMessage(), e);
        }
    }

    public void pingResponse(long sec, long uSec) {
        logger.trace("Creation seconds.micro: {}.{}", sec, uSec);

        final long creationEpochMicros = TimeUnit.SECONDS.toMicros(sec) + uSec;
        final long nowMicros = epochMicroClock.microTime();
        final long elapsedMicros = nowMicros - creationEpochMicros;

        logger.trace("Elapsed: {}", elapsedMicros);
        PingResponse response = new PingResponse();

        response.setElapsed(TimeUnit.MICROSECONDS.toMillis(elapsedMicros));
        response.setName(clientName + "@" + host);
        response.setId(id);

        super.publish(MaestroTopics.MAESTRO_TOPIC, response);
    }


    @Override
    public void notifySuccess(String message) {
        logger.trace("Sending the test success notification from {}", this.toString());
        TestSuccessfulNotification notification = new TestSuccessfulNotification();

        notification.setName(clientName + "@" + host);
        notification.setId(id);

        notification.setMessage(message);

        try {
            super.publish(MaestroTopics.NOTIFICATION_TOPIC, notification, 0, true);
        } catch (Exception e) {
            logger.error("Unable to publish the success notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyFailure(String message) {
        logger.trace("Sending the test success notification from {}", this.toString());
        TestFailedNotification notification = new TestFailedNotification();

        notification.setName(clientName + "@" + host);
        notification.setId(id);

        notification.setMessage(message);

        try {
            super.publish(MaestroTopics.NOTIFICATION_TOPIC, notification, 0, true);
        } catch (Exception e) {
            logger.error("Unable to publish the failure notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void abnormalDisconnect() {
        // TODO: handle abnormal disconnect and the LWT message
    }
}
