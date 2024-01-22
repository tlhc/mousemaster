package mousemaster;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class MouseController implements ModeListener, MousePositionListener {

    private Mouse mouse;
    private Wheel wheel;
    private double moveDuration;
    private double deltaDistanceX, deltaDistanceY;
    // Forward means right or down.
    private final Deque<Boolean> xMoveForwardStack = new ArrayDeque<>();
    private final Deque<Boolean> yMoveForwardStack = new ArrayDeque<>();
    private boolean leftPressing, middlePressing, rightPressing;
    private double wheelDuration;
    private final Deque<Boolean> xWheelForwardStack = new ArrayDeque<>();
    private final Deque<Boolean> yWheelForwardStack = new ArrayDeque<>();

    private int mouseX, mouseY;
    private boolean jumping;
    private double jumpDuration;
    private int jumpX, jumpY;
    private int jumpBeginX, jumpBeginY;
    private int jumpEndX, jumpEndY;

    public void reset() {
        moveDuration = 0;
        deltaDistanceX = deltaDistanceY = 0;
        xMoveForwardStack.clear();
        yMoveForwardStack.clear();
        leftPressing = middlePressing = rightPressing = false;
        wheelDuration = 0;
        xWheelForwardStack.clear();
        yWheelForwardStack.clear();
        jumping = false;
        jumpDuration = 0;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    boolean moving() {
        return !xMoveForwardStack.isEmpty() || !yMoveForwardStack.isEmpty();
    }

    boolean pressing() {
        return leftPressing || middlePressing || rightPressing;
    }

    boolean wheeling() {
        return !xWheelForwardStack.isEmpty() || !yWheelForwardStack.isEmpty();
    }

    public void update(double delta) {
        if (moving()) {
            moveDuration += delta;
            double moveVelocity = Math.min(mouse.maxVelocity(), mouse.initialVelocity() +
                                                                mouse.acceleration() *
                                                                Math.pow(moveDuration,
                                                                        1));
            boolean deltaBigEnough;
            if (!xMoveForwardStack.isEmpty() && !yMoveForwardStack.isEmpty()) {
                deltaDistanceX += moveVelocity * delta / Math.sqrt(2);
                deltaDistanceY += moveVelocity * delta / Math.sqrt(2);
                deltaBigEnough = deltaDistanceX >= 1 && deltaDistanceY >= 1;
            }
            else if (!xMoveForwardStack.isEmpty()) {
                deltaDistanceX += moveVelocity * delta;
                deltaDistanceY = 0;
                deltaBigEnough = deltaDistanceX >= 1;
            }
            else {
                deltaDistanceX = 0;
                deltaDistanceY += moveVelocity * delta;
                deltaBigEnough = deltaDistanceY >= 1;
            }
            if (deltaBigEnough && !jumping) {
                WindowsMouse.moveBy(
                        !xMoveForwardStack.isEmpty() && xMoveForwardStack.peek(),
                        deltaDistanceX,
                        !yMoveForwardStack.isEmpty() && yMoveForwardStack.peek(),
                        deltaDistanceY);
                deltaDistanceX = deltaDistanceY = 0;
            }
        }
        if (jumping) {
            jumpDuration += delta;
            double jumpVelocity = mouse.smoothJumpVelocity(); // Pixels per second.
            double jumpTotalDuration =
                    Math.hypot(jumpEndX - jumpBeginX, jumpEndY - jumpBeginY) /
                    jumpVelocity;
            double t = Math.min(1, jumpDuration / jumpTotalDuration);
            // Smooth.
            // t = t * t * (3 - 2 * t);
            // Smoother (Ken Perlin).
            t = t * t * t * (t * (t * 6 - 15) + 10);
            int nextJumpX = (int) (jumpBeginX + (jumpEndX - jumpBeginX) * t);
            int nextJumpY = (int) (jumpBeginY + (jumpEndY - jumpBeginY) * t);
            // Merge the user movement in.
            int movingDeltaX = xMoveForwardStack.isEmpty() ? 0 :
                    (int) (deltaDistanceX * (xMoveForwardStack.peek() ? 1 : -1));
            deltaDistanceX -= (int) deltaDistanceX;
            nextJumpX += movingDeltaX;
            jumpEndX += movingDeltaX;
            int movingDeltaY = yMoveForwardStack.isEmpty() ? 0 :
                    (int) (deltaDistanceY * (yMoveForwardStack.peek() ? 1 : -1));
            deltaDistanceY -= (int) deltaDistanceY;
            nextJumpY += movingDeltaY;
            jumpEndY += movingDeltaY;
            if (nextJumpX != jumpX || nextJumpY != jumpY) {
                WindowsMouse.moveTo(nextJumpX, nextJumpY);
                jumpX = nextJumpX;
                jumpY = nextJumpY;
            }
        }
        if (wheeling()) {
            wheelDuration += delta;
            double wheelVelocity = Math.min(wheel.maxVelocity(),
                    wheel.initialVelocity() + wheel.acceleration() * wheelDuration);
            double deltaDistance = wheelVelocity * delta;
            if (!xWheelForwardStack.isEmpty())
                WindowsMouse.wheelHorizontallyBy(xWheelForwardStack.peek(), deltaDistance);
            if (!yWheelForwardStack.isEmpty())
                WindowsMouse.wheelVerticallyBy(yWheelForwardStack.peek(), deltaDistance);
        }
    }

    public void startMoveUp() {
        if (!yMoveForwardStack.isEmpty() && yMoveForwardStack.contains(false))
            return;
        yMoveForwardStack.push(false);
    }

    public void startMoveDown() {
        if (!yMoveForwardStack.isEmpty() && yMoveForwardStack.contains(true))
            return;
        yMoveForwardStack.push(true);
    }

    public void startMoveLeft() {
        if (!xMoveForwardStack.isEmpty() && xMoveForwardStack.contains(false))
            return;
        xMoveForwardStack.push(false);
    }

    public void startMoveRight() {
        if (!xMoveForwardStack.isEmpty() && xMoveForwardStack.contains(true))
            return;
        xMoveForwardStack.push(true);
    }

    public void stopMoveUp() {
        removeFirst(yMoveForwardStack, false);
        if (yMoveForwardStack.isEmpty() || yMoveForwardStack.peek() != false)
            deltaDistanceY = 0;
        if (xMoveForwardStack.isEmpty() && yMoveForwardStack.isEmpty())
            moveDuration = 0;
    }

    private static void removeFirst(Deque<Boolean> moveForward, boolean forward) {
        Iterator<Boolean> iterator = moveForward.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == forward) {
                iterator.remove();
                break;
            }
        }
    }

    public void stopMoveDown() {
        removeFirst(yMoveForwardStack, true);
        if (yMoveForwardStack.isEmpty() || yMoveForwardStack.peek() != true)
            deltaDistanceY = 0;
        if (xMoveForwardStack.isEmpty() && yMoveForwardStack.isEmpty())
            moveDuration = 0;
    }

    public void stopMoveLeft() {
        removeFirst(xMoveForwardStack, false);
        if (xMoveForwardStack.isEmpty() || xMoveForwardStack.peek() != false)
            deltaDistanceX = 0;
        if (xMoveForwardStack.isEmpty() && yMoveForwardStack.isEmpty())
            moveDuration = 0;
    }

    public void stopMoveRight() {
        removeFirst(xMoveForwardStack, true);
        if (xMoveForwardStack.isEmpty() || xMoveForwardStack.peek() != true)
            deltaDistanceX = 0;
        if (xMoveForwardStack.isEmpty() && yMoveForwardStack.isEmpty())
            moveDuration = 0;
    }

    public void clickLeft() {
        if (!leftPressing)
            WindowsMouse.pressLeft();
        WindowsMouse.releaseLeft();
    }

    public void clickMiddle() {
        if (!middlePressing)
            WindowsMouse.pressMiddle();
        WindowsMouse.releaseMiddle();
    }

    public void clickRight() {
        if (!rightPressing)
            WindowsMouse.pressRight();
        WindowsMouse.releaseRight();
    }

    public void pressLeft() {
        if (leftPressing)
            return;
        releaseAll();
        leftPressing = true;
        WindowsMouse.pressLeft();
    }

    public void pressMiddle() {
        if (middlePressing)
            return;
        releaseAll();
        middlePressing = true;
        WindowsMouse.pressMiddle();
    }

    public void pressRight() {
        if (rightPressing)
            return;
        releaseAll();
        rightPressing = true;
        WindowsMouse.pressRight();
    }

    public void releaseLeft() {
        if (leftPressing)
            WindowsMouse.releaseLeft();
        leftPressing = false;
    }

    public void releaseMiddle() {
        if (middlePressing)
            WindowsMouse.releaseMiddle();
        middlePressing = false;
    }

    public void releaseRight() {
        if (rightPressing)
            WindowsMouse.releaseRight();
        rightPressing = false;
    }

    private void releaseAll() {
        releaseLeft();
        releaseMiddle();
        releaseRight();
    }

    public void toggleLeft() {
        if (leftPressing)
            releaseLeft();
        else
            pressLeft();
    }

    public void toggleMiddle() {
        if (middlePressing)
            releaseMiddle();
        else
            pressMiddle();
    }

    public void toggleRight() {
        if (rightPressing)
            releaseRight();
        else
            pressRight();
    }

    public void startWheelUp() {
        if (!yWheelForwardStack.isEmpty() && yWheelForwardStack.peek() == false)
            return;
        yWheelForwardStack.push(false);
    }

    public void startWheelDown() {
        if (!yWheelForwardStack.isEmpty() && yWheelForwardStack.peek() == true)
            return;
        yWheelForwardStack.push(true);
    }

    public void startWheelLeft() {
        if (!xWheelForwardStack.isEmpty() && xWheelForwardStack.peek() == false)
            return;
        xWheelForwardStack.push(false);
    }

    public void startWheelRight() {
        if (!xWheelForwardStack.isEmpty() && xWheelForwardStack.peek() == true)
            return;
        xWheelForwardStack.push(true);
    }

    public void stopWheelUp() {
        removeFirst(yWheelForwardStack, false);
        if (xWheelForwardStack.isEmpty() && yWheelForwardStack.isEmpty())
            wheelDuration = 0;
    }

    public void stopWheelDown() {
        removeFirst(yWheelForwardStack, true);
        if (xWheelForwardStack.isEmpty() && yWheelForwardStack.isEmpty())
            wheelDuration = 0;
    }

    public void stopWheelLeft() {
        removeFirst(xWheelForwardStack, false);
        if (xWheelForwardStack.isEmpty() && yWheelForwardStack.isEmpty())
            wheelDuration = 0;
    }

    public void stopWheelRight() {
        removeFirst(xWheelForwardStack, true);
        if (xWheelForwardStack.isEmpty() && yWheelForwardStack.isEmpty())
            wheelDuration = 0;
    }

    public void showCursor() {
        WindowsMouse.showCursor();
    }

    public void hideCursor() {
        WindowsMouse.hideCursor();
    }

    public void moveTo(int x, int y) {
        if (x == mouseX && y == mouseY)
            return;
        if (!mouse.smoothJumpEnabled()) {
            WindowsMouse.moveTo(x, y);
            return;
        }
        if (jumping && x == jumpEndX && y == jumpEndY)
            return;
        jumpX = jumpBeginX = mouseX;
        jumpY = jumpBeginY = mouseY;
        jumping = true;
        jumpEndX = x;
        jumpEndY = y;
        jumpDuration = 0;
    }

    public boolean jumping() {
        return jumping;
    }

    public int jumpEndX() {
        return jumpEndX;
    }

    public int jumpEndY() {
        return jumpEndY;
    }

    @Override
    public void modeChanged(Mode newMode) {
        setMouse(newMode.mouse());
        setWheel(newMode.wheel());
        if (jumping && !mouse.smoothJumpEnabled()) {
            jumping = false;
            jumpDuration = 0;
            WindowsMouse.moveTo(jumpEndX, jumpEndY);
        }
    }

    @Override
    public void modeTimedOut() {
        // No op.
    }

    @Override
    public void mouseMoved(int x, int y) {
        mouseX = x;
        mouseY = y;
        if (mouseX == jumpEndX && mouseY == jumpEndY) {
            jumping = false;
            jumpDuration = 0;
        }
    }

}
