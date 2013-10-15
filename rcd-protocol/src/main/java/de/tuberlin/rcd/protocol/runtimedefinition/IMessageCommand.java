package de.tuberlin.rcd.protocol.runtimedefinition;

import de.tuberlin.rcd.network.Message;

/**
 * Abstract command interface.
 * The communication between client and server (communication-protocol) is based
 * on a distributed command-pattern. Every received message is internally translated to a dedicated
 * command.
 */
public interface IMessageCommand {

    /**
     * Execute a command.
     * @param msg The message object that triggered the command execution.
     */
    public abstract void execute( Message msg );

}