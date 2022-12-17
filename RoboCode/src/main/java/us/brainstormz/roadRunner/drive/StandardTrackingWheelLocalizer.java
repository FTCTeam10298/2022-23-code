
//X (mean avg of distances, see LRR) x =  -2.8417
//dist1: 50/ -29.662793399551123
//dist2: 50/ -20.15522013354858
//dist3: 50/ -11.47110710175644

//(same) y = 0.058967
//dist1:  50/ 845.9061912983248
//dist2: ans + 50/ 858.2465632298844
//dist3: ans + 50/ 840.5327050224676

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
    public static double X_MULTIPLIER = -2.8417; // Multiplier in the X direction
    public static double Y_MULTIPLIER = 0.058967; // Multiplier in the X direction

    public static double LATERAL_DISTANCE = 10.5; // in; distance between the left and right wheels
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
        leftEncoder.setDirection(Encoder.Direction.REVERSE);
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
