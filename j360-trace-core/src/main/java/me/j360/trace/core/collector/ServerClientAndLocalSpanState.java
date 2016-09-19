package me.j360.trace.core.collector;

/**
 * Combines server and client span state.
 * 
 * @author kristof
 */
public interface ServerClientAndLocalSpanState extends ServerSpanState, ClientSpanState, LocalSpanState {

}
