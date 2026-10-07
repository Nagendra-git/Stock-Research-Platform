package com.nagendra.platform.dto.position;

import com.nagendra.platform.models.Position;

public class PositionResult {
  private final boolean ok;
  private final Position position;
  private final String message;

  private PositionResult(boolean ok, Position position, String message) {
    this.ok = ok;
    this.position = position;
    this.message = message;
  }

  public static PositionResult accepted(Position position) {
    return new PositionResult(true, position, null);
  }

  public static PositionResult rejected(String message) {
    return new PositionResult(false, null, message);
  }

  public boolean isOk() {
    return ok;
  }

  public Position getPosition() {
    return position;
  }

  public String getMessage() {
    return message;
  }
}
