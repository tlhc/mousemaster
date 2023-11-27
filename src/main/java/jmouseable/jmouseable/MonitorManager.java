package jmouseable.jmouseable;

import java.util.Set;

public class MonitorManager implements MousePositionListener {

    private int mouseX;
    private int mouseY;

    public Monitor activeMonitor() {
        return nearestMonitorContaining(mouseX, mouseY);
    }

    public Monitor nearestMonitorContaining(int pointX, int pointY) {
        // Mouse position can be 0 -4 even when there is only one monitor.
        Set<Monitor> monitors = monitors();
        if (monitors.isEmpty())
            throw new IllegalStateException("No monitors found");
        for (Monitor monitor : monitors) {
            if (RectUtil.rectContains(monitor.x(), monitor.y(), monitor.width(),
                    monitor.height(), pointX, pointY))
                return monitor;
        }
        double minDistance = Double.MAX_VALUE;
        Monitor nearestMonitor = null;
        for (Monitor monitor : monitors) {
            double distance = RectUtil.rectEdgeDistanceTo(monitor.x(), monitor.y(),
                    monitor.width(), monitor.height(), pointX, pointY);
            if (distance < minDistance) {
                minDistance = distance;
                nearestMonitor = monitor;
            }
        }
        return nearestMonitor;
    }

    public Set<Monitor> monitors() {
        return WindowsMonitor.findMonitors();
    }

    @Override
    public void mouseMoved(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

}
