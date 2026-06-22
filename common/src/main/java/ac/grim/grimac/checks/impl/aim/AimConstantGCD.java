package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import ac.grim.grimac.utils.math.GrimMath;

@CheckData(name = "AimConstantGCD", stableKey = "grim.aim.constant_gcd", description = "Rotation delta perfectly matches GCD multiple with no rounding error", decay = 0.01)
public class AimConstantGCD extends Check implements RotationCheck {

    private double lastRemainderX;
    private double lastRemainderY;

    public AimConstantGCD(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.vehicleData.wasVehicleSwitch
                || player.packetStateData.horseInteractCausedForcedRotation) {
            return;
        }

        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();

        if (deltaXRot < 0.001f || deltaYRot < 0.001f || deltaXRot > 5f || deltaYRot > 5f) {
            return;
        }

        AimProcessor processor = rotationUpdate.getProcessor();
        if (processor == null || processor.modeX == 0 || processor.modeY == 0) {
            return;
        }

        double remainderX = deltaXRot % processor.modeX;
        double remainderY = deltaYRot % processor.modeY;

        double remainderXClosest = Math.min(remainderX, processor.modeX - remainderX);
        double remainderYClosest = Math.min(remainderY, processor.modeY - remainderY);

        if (remainderXClosest < 1e-6 && remainderYClosest < 1e-6) {
            if (lastRemainderX < 1e-6 && lastRemainderY < 1e-6) {
                flag("X=" + deltaXRot + " mode=" + processor.modeX + " remX=" + remainderXClosest + " Y=" + deltaYRot + " remY=" + remainderYClosest);
            }
        }

        lastRemainderX = remainderXClosest;
        lastRemainderY = remainderYClosest;
    }
}
