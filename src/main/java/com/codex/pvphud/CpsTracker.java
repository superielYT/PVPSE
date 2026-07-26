package com.codex.pvphud;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

final class CpsTracker {
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean leftWasDown;
    private boolean rightWasDown;

    void tick(MinecraftClient client) {
        if (client.getWindow() == null) {
            return;
        }

        long window = client.getWindow().getHandle();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();

        if (leftDown && !leftWasDown) {
            leftClicks.add(now);
        }
        if (rightDown && !rightWasDown) {
            rightClicks.add(now);
        }

        leftWasDown = leftDown;
        rightWasDown = rightDown;
        prune(leftClicks, now);
        prune(rightClicks, now);
    }

    int leftCps() {
        return leftClicks.size();
    }

    int rightCps() {
        return rightClicks.size();
    }

    private static void prune(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) {
            clicks.removeFirst();
        }
    }
}
