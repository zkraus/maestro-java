package net.orpiske.mpt.common.writers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * A writer class for performance rate data. This data is saved to a compressed file in the format
 * {role}-rate.gz
 */
public final class RateWriter implements AutoCloseable {
    private static final int MICROS_PART_LENGTH = "000\"".length();
    private static final String DATE_FORMAT_PATTERN = "\"yyyy-MM-dd HH:mm:ss.SSS";
    private static final char SEPARATOR = ',';
    private static final int ESTIMATED_LINE_LENGTH = (DATE_FORMAT_PATTERN + SEPARATOR + DATE_FORMAT_PATTERN + '\n').length() + MICROS_PART_LENGTH * 2;
    //a more modern approach using DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.systemDefault())
    //could be considered but in the current state produces much more garbage
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    private final OutputStream outputStream;
    private final Date date = new Date();
    private final StringBuffer lineBuilder = new StringBuffer(ESTIMATED_LINE_LENGTH);
    private final byte[] writeBuffer = new byte[ESTIMATED_LINE_LENGTH];
    private final FieldPosition fullFieldPosition = new FieldPosition(DateFormat.FULL);

    public RateWriter(final File reportFolder, boolean sender, boolean compressed) throws IOException {
        final String role = sender ? "sender" : "receiver";
        final String fileName = role + (compressed ? "d-rate.csv.gz" : "d-rate.csv");
        final File reportFile = new File(reportFolder, fileName);
        final FileOutputStream fileStream = new FileOutputStream(reportFile);
        if (compressed) {
            outputStream = new GZIPOutputStream(fileStream);
        } else {
            outputStream = fileStream;
        }
        //header depend on being sender/receiver
        final String firstTimeColumn;
        final String secondTimeColumn;
        if (sender) {
            firstTimeColumn = "etd";
            secondTimeColumn = "atd";
        } else {
            firstTimeColumn = "eta";
            secondTimeColumn = "ata";
        }
        final int encodedSize = encodeAscii(lineBuilder.append(firstTimeColumn).append(SEPARATOR).append(secondTimeColumn).append('\n'), writeBuffer);
        outputStream.write(writeBuffer, 0, encodedSize);
    }

    public void write(long startTimeStampEpochMicros, long endTimeStampEpochMicros) {
        final int encodedSize = encodeAscii(appendOn(startTimeStampEpochMicros, endTimeStampEpochMicros).append('\n'), writeBuffer);
        try {
            outputStream.write(writeBuffer, 0, encodedSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int digitOf(long microseconds) {
        final int digits;
        if (microseconds < 10) {
            digits = 1;
        } else if (microseconds < 100) {
            digits = 2;
        } else {
            assert microseconds < 1000;
            digits = 3;
        }
        return digits;
    }

    private static StringBuffer appendDateMicros(long micros, StringBuffer buffer) {
        assert micros >= 0;
        if (micros == 0) {
            buffer.append("000");
            return buffer;
        }
        final int digits = digitOf(micros);
        final int zerosPrefix = 3 - digits;
        switch (zerosPrefix) {
            case 1:
                buffer.append("0");
                break;
            case 2:
                buffer.append("00");
                break;
        }
        buffer.append(micros);
        return buffer;
    }

    private StringBuffer appendOn(long startTimeStampEpochMicros, long endTimeStampEpochMicros) {
        final long startMillis = startTimeStampEpochMicros / 1000L;
        final long startRemainingMicros = startTimeStampEpochMicros % 1000L;
        final long endMillis = endTimeStampEpochMicros / 1000L;
        final long endRemainingMicros = endTimeStampEpochMicros % 1000L;
        lineBuilder.setLength(0);
        date.setTime(startMillis);
        dateFormat.format(date, lineBuilder, fullFieldPosition);
        appendDateMicros(startRemainingMicros, lineBuilder).append('"').append(SEPARATOR);
        date.setTime(endMillis);
        dateFormat.format(date, lineBuilder, fullFieldPosition);
        appendDateMicros(endRemainingMicros, lineBuilder).append('"');
        return lineBuilder;
    }

    private static int encodeAscii(StringBuffer buffer, byte[] encodedBuffer) {
        final int bufferLength = buffer.length();
        for (int i = 0; i < bufferLength; i++) {
            final char c = buffer.charAt(i);
            byte b = (byte) c;
            if (b < 0) {
                b = '?';
            }
            encodedBuffer[i] = b;
        }
        return bufferLength;
    }

    public void close() {
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
