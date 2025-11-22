package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.Constants.Companion.AUTO_BLUE
import org.firstinspires.ftc.teamcode.Constants.Companion.AUTO_RED
import org.firstinspires.ftc.teamcode.Constants.Companion.BANK_VELOCITY
import org.firstinspires.ftc.teamcode.Constants.Companion.FAR_VELOCITY
import org.firstinspires.ftc.teamcode.Constants.Companion.MAX_VELOCITY
import org.firstinspires.ftc.teamcode.Constants.Companion.TELEOP
import org.firstinspires.ftc.teamcode.Constants.Companion.TIMEOUT_MS
import org.firstinspires.ftc.teamcode.Constants.Companion.WHEELS_INCHES_TO_TICKS
import kotlin.math.abs

@TeleOp
class ExperimentalAuto : LinearOpMode() {
    private val autoLaunchTimer = ElapsedTime()
    private val autoDriveTimer = ElapsedTime()
    private var flywheel: DcMotor? = null
    private var coreHex: DcMotor? = null
    private var servo: CRServo? = null
    private var frontLeft: DcMotor? = null
    private var frontRight: DcMotor? = null
    private var backLeft: DcMotor? = null
    private var backRight: DcMotor? = null
    private var operationSelected: String = TELEOP

    override fun runOpMode() {
        flywheel = hardwareMap.get(DcMotor::class.java, "flywheel")
        coreHex = hardwareMap.get(DcMotor::class.java, "coreHex")
        servo = hardwareMap.get(CRServo::class.java, "servo")
        frontLeft = hardwareMap.get(DcMotor::class.java, "FL")
        frontRight = hardwareMap.get(DcMotor::class.java, "FR")
        backLeft = hardwareMap.get(DcMotor::class.java, "BL")
        backRight = hardwareMap.get(DcMotor::class.java, "BR")

        // Establishing the direction and mode for the motors
        flywheel!!.mode = DcMotor.RunMode.RUN_USING_ENCODER
        flywheel!!.direction = DcMotorSimple.Direction.REVERSE
        coreHex!!.direction = DcMotorSimple.Direction.REVERSE
        //Ensures the servo is active and ready
        servo!!.power = 0.0

        motorSettings()

        //On initialization the Driver Station will prompt for which OpMode should be run - Auto Blue, Auto Red, or TeleOp
        while (opModeInInit()) {
            operationSelected = selectOperation(operationSelected, gamepad1.startWasPressed())
            telemetry.update()
        }
        waitForStart()
        when (operationSelected) {
            AUTO_BLUE -> {
                doAutoBlue()
            }

            AUTO_RED -> {
                doAutoRed()
            }

            else -> {
                doTeleOp()
            }
        }
    }

    /**
     * If the PS/Home button is pressed, the robot will cycle through the OpMode options following the if/else statement here.
     * The telemetry readout to the Driver Station App will update to reflect which is currently selected for when "play" is pressed.
     */
    private fun selectOperation(state: String, cycleNext: Boolean): String {
        var state = state
        if (cycleNext) {
            when (state) {
                TELEOP -> {
                    state = AUTO_BLUE
                }

                AUTO_BLUE -> {
                    state = AUTO_RED
                }

                AUTO_RED -> {
                    state = TELEOP
                }

                else -> {
                    telemetry.addData(
                        "WARNING", "Unknown Operation State Reached - Restart Program"
                    )
                }
            }
        }
        telemetry.addLine("Press Start Button to cycle options")
        telemetry.addData("CURRENT SELECTION", state)
        if (state == AUTO_BLUE || state == AUTO_RED) {
            telemetry.addLine("Please remember to enable the AUTO timer!")
        }
        telemetry.addLine("Press START to start your program")
        return state
    }

    //TeleOp Code
    /**
     * If TeleOp was selected or defaulted to, the following will be active upon pressing "play".
     */
    private fun doTeleOp() {
        if (opModeIsActive()) {
            while (opModeIsActive()) {
                // Calling our methods while the OpMode is running
                mecanumDrive()
                setFlywheelVelocity()
                manualCoreHexAndServoControl()
                telemetry.addData("Flywheel Velocity", (flywheel as DcMotorEx).velocity)
                telemetry.addData("Flywheel Power", flywheel!!.power)
                telemetry.update()
            }
        }
    }

    /**
     * Controls for the drivetrain. The robot uses a split stick style arcade drive.
     * Forward and back is on the left stick. Turning is on the right stick.
     */
    private fun mecanumDrive() {
        val leftFrontPower: Float
        val rightFrontPower: Float
        val leftBackPower: Float
        val rightBackPower: Float

        // Determining movement based on gamepad inputs
        val forwardBack: Float = -gamepad1.left_stick_y
        val strafe: Float = gamepad1.left_stick_x
        val turn: Float = gamepad1.right_stick_x
        leftFrontPower = forwardBack + strafe + turn
        rightFrontPower = (forwardBack - strafe) - turn
        leftBackPower = (forwardBack - strafe) + turn
        rightBackPower = (forwardBack + strafe) - turn
        // Setting Motor Power
        frontLeft!!.power = leftFrontPower.toDouble()
        frontRight!!.power = rightFrontPower.toDouble()
        backLeft!!.power = leftBackPower.toDouble()
        backRight!!.power = rightBackPower.toDouble()
    }

    /**
     * Manual control for the Core Hex powered feeder and the agitator servo in the hopper
     */
    private fun manualCoreHexAndServoControl() {
        // Manual control for the Core Hex intake
        if (gamepad1.a) {
            coreHex!!.power = 0.5
        } else if (gamepad1.y) {
            coreHex!!.power = -0.5
        }
        // Manual control for the hopper's servo
        if (gamepad1.dpad_left) {
            servo!!.power = 1.0
        } else if (gamepad1.dpad_right) {
            servo!!.power = -1.0
        }
    }

    /**
     * This if/else statement contains the controls for the flywheel, both manual and auto.
     * Circle and Square will spin up ONLY the flywheel to the target velocity set.
     * The bumpers will activate the flywheel, Core Hex feeder, and servo to cycle a series of balls.
     */
    private fun setFlywheelVelocity() {
        if (gamepad1.back) {
            flywheel!!.power = -0.5
        } else if (gamepad1.left_bumper) {
            farPowerAuto()
        } else if (gamepad1.right_bumper) {
            bankShotAuto()
        } else if (gamepad1.b) {
            (flywheel as DcMotorEx).velocity = BANK_VELOCITY.toDouble()
        } else if (gamepad1.x) {
            (flywheel as DcMotorEx).velocity = MAX_VELOCITY.toDouble()
        } else {
            (flywheel as DcMotorEx).velocity = 0.0
            coreHex!!.power = 0.0
            // The check below is in place to prevent stuttering with the servo. It checks if the servo is under manual control!
            if (!gamepad1.dpad_right && !gamepad1.dpad_left) {
                servo!!.power = 0.0
            }
        }
    }

    //Automatic Flywheel controls used in Auto and TeleOp
    /**
     * The bank shot or near velocity is intended for launching balls touching or a few inches from the goal.
     * When running this function, the flywheel will spin up and the Core Hex will wait before balls can be fed.
     * The servo will spin until the bumper is released.
     */
    private fun bankShotAuto() {
        (flywheel as DcMotorEx).velocity = BANK_VELOCITY.toDouble()
        servo!!.power = -1.0
        if ((flywheel as DcMotorEx).velocity >= BANK_VELOCITY - 100) {
            coreHex!!.power = 1.0
        } else {
            coreHex!!.power = 0.0
        }
    }

    /**
     * The far power velocity is intended for launching balls a few feet from the goal. It may require adjusting the deflector.
     * When running this function, the flywheel will spin up and the Core Hex will wait before balls can be fed.
     * The servo will spin until the bumper is released.
     */
    private fun farPowerAuto() {
        (flywheel as DcMotorEx).velocity = FAR_VELOCITY.toDouble()
        servo!!.power = -1.0
        if ((flywheel as DcMotorEx).velocity >= FAR_VELOCITY - 100) {
            coreHex!!.power = 1.0
        } else {
            coreHex!!.power = 0.0
        }
    }

    //Autonomous Code
    //For autonomous, the robot will launch the pre-loaded 3 balls then back away from the goal, turn, and back up off the launch line.
    /**
     * For autonomous, the robot is using a timer and encoders on the drivetrain to move away from the target.
     * This method contains the math to be used with the inputted distance for the encoders, resets the elapsed timer, and
     * provides a check for it to run so long as the motors are busy and the timer has not run out.
     */
    private fun autoDrive(
        speed: Double, leftDistanceInch: Int, rightDistanceInch: Int
    ) {
        autoDriveTimer.reset()
        frontLeft!!.targetPosition =
            (frontLeft!!.currentPosition + leftDistanceInch * WHEELS_INCHES_TO_TICKS).toInt()
        frontRight!!.targetPosition =
            (frontRight!!.currentPosition + rightDistanceInch * WHEELS_INCHES_TO_TICKS).toInt()
        backLeft!!.targetPosition =
            (backLeft!!.currentPosition + leftDistanceInch * WHEELS_INCHES_TO_TICKS).toInt()
        backRight!!.targetPosition =
            (backRight!!.currentPosition + rightDistanceInch * WHEELS_INCHES_TO_TICKS).toInt()
        frontLeft!!.mode = DcMotor.RunMode.RUN_TO_POSITION
        frontRight!!.mode = DcMotor.RunMode.RUN_TO_POSITION
        backLeft!!.mode = DcMotor.RunMode.RUN_TO_POSITION
        backRight!!.mode = DcMotor.RunMode.RUN_TO_POSITION
        frontLeft!!.power = abs(speed)
        frontRight!!.power = abs(speed)
        backLeft!!.power = abs(speed)
        backRight!!.power = abs(speed)
        while (opModeIsActive() && (frontLeft!!.isBusy || frontRight!!.isBusy || backLeft!!.isBusy || backRight!!.isBusy) && autoDriveTimer.milliseconds() < TIMEOUT_MS) {
            idle()
        }
        frontLeft!!.power = 0.0
        frontRight!!.power = 0.0
        backLeft!!.power = 0.0
        backRight!!.power = 0.0
        frontLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
    }

    /**
     * Blue Alliance Autonomous
     * The robot will fire the pre-loaded balls until the 10 second timer ends.
     * Then it will back away from the goal and off the launch line.
     */
    private fun doAutoBlue() {
        if (opModeIsActive()) {
            telemetry.addData("RUNNING OPMODE", operationSelected)
            telemetry.update()
            // Fire balls
            autoLaunchTimer.reset()
            while (opModeIsActive() && autoLaunchTimer.milliseconds() < 10000) {
                bankShotAuto()
                telemetry.addData("Launcher Countdown", autoLaunchTimer.seconds())
                telemetry.update()
            }
            (flywheel as DcMotorEx).velocity = 0.0
            coreHex!!.power = 0.0
            servo!!.power = 0.0
            // Back Up
            autoDrive(0.5, -12, -12)
            // Turn
            autoDrive(0.5, -8, 8)
            // Drive off Line
            autoDrive(1.0, -50, -50)
        }
    }

    /**
     * Red Alliance Autonomous
     * The robot will fire the pre-loaded balls until the 10 second timer ends.
     * Then it will back away from the goal and off the launch line.
     */
    private fun doAutoRed() {
        if (opModeIsActive()) {
            telemetry.addData("RUNNING OPMODE", operationSelected)
            telemetry.update()
            // Fire balls
            autoLaunchTimer.reset()
            while (opModeIsActive() && autoLaunchTimer.milliseconds() < 10000) {
                bankShotAuto()
                telemetry.addData("Launcher Countdown", autoLaunchTimer.seconds())
                telemetry.update()
            }
            (flywheel as DcMotorEx).velocity = 0.0
            coreHex!!.power = 0.0
            servo!!.power = 0.0
            // Back Up
            autoDrive(0.5, -12, -12)
            // Turn
            autoDrive(0.5, 8, -8)
            // Drive off Line
            autoDrive(1.0, -50, -50)
        }
    }

    private fun motorSettings() {
        frontLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontLeft!!.direction = DcMotorSimple.Direction.FORWARD
        frontRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontRight!!.direction = DcMotorSimple.Direction.REVERSE
        backLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backLeft!!.direction = DcMotorSimple.Direction.FORWARD
        backRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backRight!!.direction = DcMotorSimple.Direction.REVERSE
    }
}