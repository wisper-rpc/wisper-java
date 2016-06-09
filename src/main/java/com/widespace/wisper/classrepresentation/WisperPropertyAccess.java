package com.widespace.wisper.classrepresentation;

/**
 * Enumeration describing what modes of access is available for the property.
 */
public enum WisperPropertyAccess
{
    /**
     * The property can be both written and read. (events will be listened to and sent)
     */
    READ_WRITE,
    /**
     * The property can only be read (events will be sent but not listened to)
     */
    READ_ONLY,
    /**
     * The property can only be written to (no events will be sent)
     */
    WRITE_ONLY
}
