package de.tuberlin.rcd.protocol.message;

/**
 *
 */
public final class MessageFormat {

    /**
     *
     */
    public final class MessageAttribute {

        public static final String MSG_ATTR_MSG_TYPE = "type";

        public static final String MSG_ATTR_TYPE_NAME = "name";

        public static final String MSG_ATTR_CLASS_NAME = "class";

        public static final String MSG_ATTR_FILL_DATA = "fill";

        public static final String MSG_ATTR_OPERATION = "operation";
    }

    /**
     *
     */
    public enum MessageType {

        MSG_TYPE_CREATE_REPLICATED_TYPE,

        MSG_TYPE_DELETE_REPLICATED_TYPE,

        MSG_TYPE_REGISTER_BY_REPLICATED_TYPE,

        MSG_TYPE_UNREGISTER_BY_REPLICATED_TYPE,

        MSG_TYPE_OPERATION_FOR_REPLICATED_TYPE,

        MSG_TYPE_FILL_REPLICATED_TYPE,

        MSG_TYPE_ERROR
    }
}