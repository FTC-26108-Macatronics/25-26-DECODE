package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.Constants.Companion.BANK_VELOCITY
import org.firstinspires.ftc.teamcode.Constants.Companion.MAX_VELOCITY
import org.firstinspires.ftc.teamcode.Constants.Companion.FAR_VELOCITY

@TeleOp
class TeleOp : LinearOpMode() {
    private var flywheel: DcMotor? = null
//    private var coreHex: DcMotor? = null
    private var servo: CRServo? = null
    private var frontLeft: DcMotor? = null
    private var frontRight: DcMotor? = null
    private var backLeft: DcMotor? = null
    private var backRight: DcMotor? = null

    override fun runOpMode() {
        flywheel = hardwareMap.get(DcMotor::class.java, "flywheel")
//        coreHex = hardwareMap.get(DcMotor::class.java, "coreHex")
        servo = hardwareMap.get(CRServo::class.java, "servo")
        frontLeft = hardwareMap.get(DcMotor::class.java, "FL")
        frontRight = hardwareMap.get(DcMotor::class.java, "FR")
        backLeft = hardwareMap.get(DcMotor::class.java, "BL")
        backRight = hardwareMap.get(DcMotor::class.java, "BR")

        // Establishing the direction and mode for the motors
        flywheel!!.mode = DcMotor.RunMode.RUN_USING_ENCODER
        flywheel!!.direction = DcMotorSimple.Direction.REVERSE
//        coreHex!!.direction = DcMotorSimple.Direction.REVERSE
        //Ensures the servo is active and ready
        servo!!.power = 0.0

        // Put initialization blocks here.
        motorSettings()

        waitForStart()
        if (opModeIsActive()) {
            while (opModeIsActive()) {
                // Calling our methods while the OpMode is running

                if (!gamepad1.dpad_right && !gamepad1.dpad_left) {
                    servo!!.power = 0.0
                }
                setFlywheelVelocity()
                manualCoreHexAndServoControl()
                mecanumDrive()
                telemetry.addData("Flywheel Velocity", (flywheel as DcMotorEx).velocity)
                telemetry.addData("Flywheel Power", flywheel!!.power)
                telemetry.update()
            }
        }
    }

    /**
     * Manual control for the Core Hex powered feeder and the agitator servo in the hopper
     */
    private fun manualCoreHexAndServoControl() {
        // Manual control for the Core Hex intake
        /*if (gamepad1.a) {
            coreHex!!.power = 0.5
        } else if (gamepad1.y) {
            coreHex!!.power = -0.5
        }*/
        // Manual control for the hopper's servo
        if (gamepad1.right_bumper) {
            servo!!.power = -2.0
        } else if (gamepad1.left_bumper) {
            servo!!.power = 2.0
        }
    }

    /**
     * This if/else statement contains the controls for the flywheel, both manual and auto.
     * Circle and Square will spin up ONLY the flywheel to the target velocity set.
     * The bumpers will activate the flywheel, Core Hex feeder, and servo to cycle a series of balls.
     */
    private fun setFlywheelVelocity() {
        (flywheel as DcMotorEx).velocity = FAR_VELOCITY.toDouble()
//        if (gamepad1.back) {
//            flywheel!!.power = -0.5
//        } else if (gamepad1.left_bumper) {
//            farPowerAuto()
//        } else if (gamepad1.right_bumper) {
//            bankShotAuto()
//        } else if (gamepad1.b) {
//            (flywheel as DcMotorEx).velocity = BANK_VELOCITY.toDouble()
//        } else if (gamepad1.x) {
//            (flywheel as DcMotorEx).velocity = MAX_VELOCITY.toDouble()
//        } else {
//            (flywheel as DcMotorEx).velocity = 0.0
//            coreHex!!.power = 0.0
//            // The check below is in place to prevent stuttering with the servo. It checks if the servo is under manual control!
//            if (!gamepad1.dpad_right && !gamepad1.dpad_left) {
//                servo!!.power = 0.0
//            }
//        }
    }

    /**
     * The bank shot or near velocity is intended for launching balls touching or a few inches from the goal.
     * When running this function, the flywheel will spin up and the Core Hex will wait before balls can be fed.
     * The servo will spin until the bumper is released.
     */
//    private fun bankShotAuto() {
//        (flywheel as DcMotorEx).velocity = BANK_VELOCITY.toDouble()
//        servo!!.power = -1.0
//        if ((flywheel as DcMotorEx).velocity >= BANK_VELOCITY - 50) {
//            coreHex!!.power = 1.0
//        } else {
//            coreHex!!.power = 0.0
//        }
//    }

    /**
     * The far power velocity is intended for launching balls a few feet from the goal. It may require adjusting the deflector.
     * When running this function, the flywheel will spin up and the Core Hex will wait before balls can be fed.
     * The servo will spin until the bumper is released.
     */
//    private fun farPowerAuto() {
//        (flywheel as DcMotorEx).velocity = FAR_VELOCITY.toDouble()
//        servo!!.power = -1.0
//        if ((flywheel as DcMotorEx).velocity >= FAR_VELOCITY - 100) {
//            coreHex!!.power = 1.0
//        } else {
//            coreHex!!.power = 0.0
//        }
//    }

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
}