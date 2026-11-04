package dev.engine_room.flywheel.lib.transform;

import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface Rotate<Self extends Rotate<Self>> {
   Self rotate(Quaternionfc var1);

   default Self rotate(AxisAngle4f axisAngle) {
      return this.rotate(new Quaternionf(axisAngle));
   }

   default Self rotate(float radians, float axisX, float axisY, float axisZ) {
      return radians == 0.0F ? this.self() : this.rotate(new Quaternionf().setAngleAxis(radians, axisX, axisY, axisZ));
   }

   default Self rotate(float radians, Axis axis) {
      return radians == 0.0F ? this.self() : this.rotate(axis.rotation(radians));
   }

   default Self rotate(float radians, Vector3fc axis) {
      return this.rotate(radians, axis.x(), axis.y(), axis.z());
   }

   default Self rotate(float radians, Direction axis) {
      return this.rotate(radians, axis.getStepX(), axis.getStepY(), axis.getStepZ());
   }

   default Self rotate(float radians, net.minecraft.core.Direction.Axis axis) {
      return this.rotate(radians, Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE));
   }

   default Self rotateDegrees(float degrees, float axisX, float axisY, float axisZ) {
      return this.rotate((float) (Math.PI / 180.0) * degrees, axisX, axisY, axisZ);
   }

   default Self rotateDegrees(float degrees, Axis axis) {
      return this.rotate((float) (Math.PI / 180.0) * degrees, axis);
   }

   default Self rotateDegrees(float degrees, Vector3fc axis) {
      return this.rotate((float) (Math.PI / 180.0) * degrees, axis);
   }

   default Self rotateDegrees(float degrees, Direction axis) {
      return this.rotate((float) (Math.PI / 180.0) * degrees, axis);
   }

   default Self rotateDegrees(float degrees, net.minecraft.core.Direction.Axis axis) {
      return this.rotate((float) (Math.PI / 180.0) * degrees, axis);
   }

   default Self rotateX(float radians) {
      return this.rotate(radians, Axis.XP);
   }

   default Self rotateY(float radians) {
      return this.rotate(radians, Axis.YP);
   }

   default Self rotateZ(float radians) {
      return this.rotate(radians, Axis.ZP);
   }

   default Self rotateXDegrees(float degrees) {
      return this.rotateX((float) (Math.PI / 180.0) * degrees);
   }

   default Self rotateYDegrees(float degrees) {
      return this.rotateY((float) (Math.PI / 180.0) * degrees);
   }

   default Self rotateZDegrees(float degrees) {
      return this.rotateZ((float) (Math.PI / 180.0) * degrees);
   }

   default Self rotateToFace(Direction facing) {
      return (Self)(switch (facing) {
         case DOWN -> this.rotateXDegrees(-90.0F);
         case UP -> this.rotateXDegrees(90.0F);
         case NORTH -> this.self();
         case SOUTH -> this.rotateYDegrees(180.0F);
         case WEST -> this.rotateYDegrees(90.0F);
         case EAST -> this.rotateYDegrees(270.0F);
         default -> throw new MatchException(null, null);
      });
   }

   default Self rotateTo(float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
      return this.rotate(new Quaternionf().rotationTo(fromX, fromY, fromZ, toX, toY, toZ));
   }

   default Self rotateTo(Vector3fc from, Vector3fc to) {
      return this.rotateTo(from.x(), from.y(), from.z(), to.x(), to.y(), to.z());
   }

   default Self rotateTo(Direction from, Direction to) {
      return this.rotateTo(from.getStepX(), from.getStepY(), from.getStepZ(), to.getStepX(), to.getStepY(), to.getStepZ());
   }

   default Self self() {
      return (Self)this;
   }
}
