package me.j360.trace.collector.core;

/**
 * Combines server and client span state.
 * 
 * @author kristof
 */
public interface ServerClientAndLocalSpanState extends ServerSpanState, ClientSpanState, LocalSpanState {

}
