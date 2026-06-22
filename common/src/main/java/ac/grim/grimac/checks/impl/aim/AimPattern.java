package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.LinkedList;

@CheckData(name = "AimPattern", stableKey = "grim.aim.pattern", description = "Repeated rotation pattern detected", decay = 0.01)
public class AimPattern extends Check implements RotationCheck {

    private static final int PATTERN_WINDOW = 20;
    private final LinkedList<Float> deltaXWindow = new LinkedList<>();
    private final LinkedList<Float> deltaYWindow = new LinkedList<>();
    private final LinkedList<Float> deltaXPrev = new LinkedList<>();
    private final LinkedList<Float> deltaYPrev = new LinkedList<>();

    public AimPattern(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.vehicleData.wasVehicleSwitch) {
            return;
        }

        float deltaX = rotationUpdate.getDeltaXRot();
        float deltaY = rotationUpdate.getDeltaYRot();

        deltaXWindow.add(deltaX);
        deltaYWindow.add(deltaY);

        if (deltaXWindow.size() > PATTERN_WINDOW) {
            deltaXWindow.poll();
            deltaYWindow.poll();
        }

        if (deltaXWindow.size() < PATTERN_WINDOW * 2) {
            deltaXPrev.add(deltaX);
            deltaYPrev.add(deltaY);
            if (deltaXPrev.size() > PATTERN_WINDOW) {
                deltaXPrev.poll();
                deltaYPrev.poll();
            }
            return;
        }

        if (deltaXPrev.size() >= PATTERN_WINDOW) {
            int matches = 0;
            for (int i = 0; i < PATTERN_WINDOW; i++) {
                float currX = deltaXWindow.get(i);
                float prevX = deltaXPrev.get(i);
                float currY = deltaYWindow.get(i);
                float prevY = deltaYPrev.get(i);
                if (Math.abs(currX - prevX) < 1e-4 && Math.abs(currY - prevY) < 1e-4) {
                    matches++;
                }
            }

            if (matches > PATTERN_WINDOW * 0.7) {
                flag("matches=" + matches + "/" + PATTERN_WINDOW);
            }
        }

        deltaXPrev.add(deltaX);
        deltaYPrev.add(deltaY);
        if (deltaXPrev.size() > PATTERN_WINDOW) {
            deltaXPrev.poll();
            deltaYPrev.poll();
        }
    }
}
