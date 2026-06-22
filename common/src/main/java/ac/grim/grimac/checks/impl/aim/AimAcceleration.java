package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimAcceleration", stableKey = "grim.aim.acceleration", description = "Suspicious rotation acceleration pattern", decay = 0.01)
public class AimAcceleration extends Check implements RotationCheck {

    private float lastDeltaX;
    private float lastDeltaY;
    private double lastAccelX;
    private double lastAccelY;
    private int consistentTicks;

    public AimAcceleration(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.vehicleData.wasVehicleSwitch) {
            consistentTicks = 0;
            return;
        }

        float deltaX = rotationUpdate.getDeltaXRot();
        float deltaY = rotationUpdate.getDeltaYRot();

        if (Math.abs(deltaX) < 0.01f && Math.abs(deltaY) < 0.01f) {
            consistentTicks = 0;
            lastDeltaX = deltaX;
            lastDeltaY = deltaY;
            return;
        }

        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);

        double accelX = absDeltaX - Math.abs(lastDeltaX);
        double accelY = absDeltaY - Math.abs(lastDeltaY);

        boolean accelerating = Math.abs(accelX) < 0.01 && Math.abs(accelY) < 0.01 && absDeltaX > 0.1f && absDeltaY > 0.1f
                && Math.abs(lastAccelX) < 0.01 && Math.abs(lastAccelY) < 0.01;

        if (accelerating) {
            consistentTicks++;
            if (consistentTicks > 3) {
                flag("ticks=" + consistentTicks + " deltaX=" + absDeltaX + " deltaY=" + absDeltaY + " accelX=" + accelX + " accelY=" + accelY);
            }
        } else {
            consistentTicks = Math.max(0, consistentTicks - 1);
        }

        lastDeltaX = deltaX;
        lastDeltaY = deltaY;
        lastAccelX = accelX;
        lastAccelY = accelY;
    }
}
