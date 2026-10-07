package com.nagendra.platform.service;

public interface UpstoxMarketDataManager {

  /** Starts the Upstox market data WebSocket connection. Safe to call multiple times. */
  void start();

  /** Stops the Upstox market data WebSocket connection. */
  void stop();

  /** Returns whether the WebSocket is currently connected. */
  boolean isConnected();

  /** Returns the number of currently subscribed instruments. */
  int getSubscribedInstrumentCount();

  /**
   * Reloads the instruments from MongoDB and updates the WebSocket subscriptions without
   * reconnecting.
   */
  void refreshSubscriptions();
}
