package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimReaction", stableKey = "grim.aim.reaction", description = "Impossibly fast rotation reaction to target", decay = 0.01)
public class AimReaction extends Check implements RotationCheck {

    private float lastDeltaX;
    private float lastDeltaY;
    private int suddenChangeCount;

    public AimReaction(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.vehicleData.wasVehicleSwitch) {
            suddenChangeCount = 0;
            return;
        }

        float deltaX = rotationUpdate.getDeltaXRot();
        float deltaY = rotationUpdate.getDeltaYRot();

        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);
        float absLastX = Math.abs(lastDeltaX);
        float absLastY = Math.abs(lastDeltaY);

        boolean suddenChange = false;

        if (absLastX > 0.5f && absDeltaX < 0.05f && absLastY > 0.5f && absDeltaY < 0.05f) {
            suddenChange = true;
        }

        if (!suddenChange && absLastX < 0.05f && absDeltaX > 1.5f && absLastY < 0.05f && absDeltaY > 1.5f) {
            suddenChange = true;
        }

        if (suddenChange) {
            suddenChangeCount++;
            if (suddenChangeCount > 2) {
                flag("count=" + suddenChangeCount + " prevX=" + absLastX + " curX=" + absDeltaX + " prevY=" + absLastY + " curY=" + absDeltaY);
            }
        } else {
            suddenChangeCount = Math.max(0, suddenChangeCount - 1);
        }

        lastDeltaX = deltaX;
        lastDeltaY = deltaY;
    }
}
