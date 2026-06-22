package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.LinkedList;

@CheckData(name = "AimNoise", stableKey = "grim.aim.noise", description = "Artificial noise pattern detected in rotation", decay = 0.01)
public class AimNoise extends Check implements RotationCheck {

    private static final int SAMPLE_SIZE = 40;
    private final LinkedList<Double> deltaXHistory = new LinkedList<>();
    private final LinkedList<Double> deltaYHistory = new LinkedList<>();

    public AimNoise(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.vehicleData.wasVehicleSwitch) {
            return;
        }

        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();

        if (deltaXRot < 0.001f && deltaYRot < 0.001f) {
            return;
        }

        deltaXHistory.add((double) deltaXRot);
        deltaYHistory.add((double) deltaYRot);

        if (deltaXHistory.size() > SAMPLE_SIZE) {
            deltaXHistory.poll();
            deltaYHistory.poll();
        }

        if (deltaXHistory.size() < SAMPLE_SIZE) {
            return;
        }

        double meanX = deltaXHistory.stream().mapToDouble(d -> d).average().orElse(0);
        double meanY = deltaYHistory.stream().mapToDouble(d -> d).average().orElse(0);

        double varianceX = deltaXHistory.stream().mapToDouble(d -> Math.pow(d - meanX, 2)).average().orElse(0);
        double varianceY = deltaYHistory.stream().mapToDouble(d -> Math.pow(d - meanY, 2)).average().orElse(0);

        double stdDevX = Math.sqrt(varianceX);
        double stdDevY = Math.sqrt(varianceY);

        int zeroCrossingsX = 0;
        int zeroCrossingsY = 0;
        Double prevX = null;
        Double prevY = null;

        for (double d : deltaXHistory) {
            double dev = d - meanX;
            if (prevX != null && Math.signum(dev) != Math.signum(prevX) && Math.abs(dev) > stdDevX * 0.5) {
                zeroCrossingsX++;
            }
            prevX = dev;
        }

        for (double d : deltaYHistory) {
            double dev = d - meanY;
            if (prevY != null && Math.signum(dev) != Math.signum(prevY) && Math.abs(dev) > stdDevY * 0.5) {
                zeroCrossingsY++;
            }
            prevY = dev;
        }

        double crossingRateX = (double) zeroCrossingsX / SAMPLE_SIZE;
        double crossingRateY = (double) zeroCrossingsY / SAMPLE_SIZE;

        if (crossingRateX > 0.35 && crossingRateY > 0.35 && stdDevX < 0.5 && stdDevY < 0.5) {
            flag("crossX=" + String.format("%.2f", crossingRateX) + " crossY=" + String.format("%.2f", crossingRateY) + " stdX=" + String.format("%.4f", stdDevX) + " stdY=" + String.format("%.4f", stdDevY));
        }
    }
}
