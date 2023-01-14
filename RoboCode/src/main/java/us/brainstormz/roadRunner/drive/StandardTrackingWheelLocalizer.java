//X Correction (mean avg of distances, see LRR) x =  3.0263
//calc (this EQ is designed for Gnu Octave) (dist1+dist2+dist3: f = 50; f /49.41997375943151 + f/49.6002709249511 + f/49.67584577487027; ans/3
//dist2: ans + 37/ 37.37377747309522
//dist3: ans + / 37.44794675168949

//(same) yCorrectionCoefficient = -1.0142
//calc (this EQ is designed for Gnu Octave) (dist1+dist2+dist3/3): f = 50; f/-49.40326614704213 + f/-49.148253710294895 + f/-49.34904516339453; ans/3


package us.brainstormz.roadRunner.drive;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.ThreeTrackingWheelLocalizer;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import us.brainstormz.roadRunner.util.Encoder;

import java.util.Arrays;
import java.util.List;

/*
 * Sample tracking wheel localizer implementation assuming the standard configuration:
 *
 *    /--------------\
 *    |     ____     |
 *    |     ----     |
 *    | ||        || |
 *    | ||        || |
 *    |              |
 *    |              |
 *    \--------------/
 *
 * 6 3/8 = 6.375
 * 12 5/16 /2= 6.15625
 *
 * forward offset = 0.21875
 */
@Config
public class StandardTrackingWheelLocalizer extends ThreeTrackingWheelLocalizer {
    public static double TICKS_PER_REV = 8192;
    public static double WHEEL_RADIUS = 1.3779527559/2; // in
    public static double GEAR_RATIO = 1; // output (wheel) speed / input (encoder) speed
    public static double X_MULTIPLIER = -1.0088;//-1.8417; // Multiplier in the X direction
    public static double Y_MULTIPLIER = -1.0142;//0.058967; // Multiplier in the Y direction (central odom)

    public static double LATERAL_DISTANCE = 6.3;//7.81;//10.5; // in; distance between the left and right wheels
    public static double FORWARD_OFFSET = 0.21875; // in; offset of the lateral wheel

    private Encoder leftEncoder, rightEncoder, frontEncoder;

    public StandardTrackingWheelLocalizer(HardwareMap hardwareMap) {
        super(Arrays.asList(
                new Pose2d(0, LATERAL_DISTANCE / 2, 0), // left
                new Pose2d(0, -LATERAL_DISTANCE / 2, 0), // right
                new Pose2d(FORWARD_OFFSET, 0, Math.toRadians(90)) // front
        ));

        leftEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "leftLift"));
        rightEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "rEncoder"));
        frontEncoder = new Encoder(hardwareMap.get(DcMotorEx.class, "cEncoder"));

        // TODO: reverse any encoders using Encoder.setDirection(Encoder.Direction.REVERSE)
        rightEncoder.setDirection(Encoder.Direction.REVERSE);
    }

    public static double encoderTicksToInches(double ticks) {
        return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV;
    }

    @NonNull
    @Override
    public List<Double> getWheelPositions() {
        return Arrays.asList(
                encoderTicksToInches(leftEncoder.getCurrentPosition() * X_MULTIPLIER),
                encoderTicksToInches(rightEncoder.getCurrentPosition() * X_MULTIPLIER),
                encoderTicksToInches(frontEncoder.getCurrentPosition() * Y_MULTIPLIER)
        );
    }

    @NonNull
    @Override
    public List<Double> getWheelVelocities() {
        // TODO: If your encoder velocity can exceed 32767 counts / second (such as the REV Through Bore and other
        //  competing magnetic encoders), change Encoder.getRawVelocity() to Encoder.getCorrectedVelocity() to enable a
        //  compensation method

        return Arrays.asList(
                encoderTicksToInches(leftEncoder.getCorrectedVelocity()),
                encoderTicksToInches(rightEncoder.getCorrectedVelocity()),
                encoderTicksToInches(frontEncoder.getCorrectedVelocity())
        );
    }
}
