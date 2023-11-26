package jmouseable.jmouseable;

public sealed interface Command {

    record SwitchMode(String modeName) implements Command {}

    record StartMoveUp() implements Command {}
    record StartMoveDown() implements Command {}
    record StartMoveLeft() implements Command {}
    record StartMoveRight() implements Command {}

    record StopMoveUp() implements Command {}
    record StopMoveDown() implements Command {}
    record StopMoveLeft() implements Command {}
    record StopMoveRight() implements Command {}

    record PressLeft() implements Command {}
    record PressMiddle() implements Command {}
    record PressRight() implements Command {}

    record ReleaseLeft() implements Command {}
    record ReleaseMiddle() implements Command {}
    record ReleaseRight() implements Command {}

    record StartWheelUp() implements Command {}
    record StartWheelDown() implements Command {}
    record StartWheelLeft() implements Command {}
    record StartWheelRight() implements Command {}

    record StopWheelUp() implements Command {}
    record StopWheelDown() implements Command {}
    record StopWheelLeft() implements Command {}
    record StopWheelRight() implements Command {}

    record SnapUp() implements Command {}
    record SnapDown() implements Command {}
    record SnapLeft() implements Command {}
    record SnapRight() implements Command {}

    record KeepGridTop() implements Command {}
    record KeepGridBottom() implements Command {}
    record KeepGridLeft() implements Command {}
    record KeepGridRight() implements Command {}

}
