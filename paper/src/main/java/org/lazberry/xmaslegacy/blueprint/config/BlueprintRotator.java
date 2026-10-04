package org.lazberry.xmaslegacy.blueprint.config;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.blueprint.RelativeBlock;
import org.lazberry.xmaslegacy.utils.Axiom;

@Slf4j
public class BlueprintRotator {

	public static BluePrint getRotatedBlueprint(BluePrint original, float playerYaw) {
		float snapped = Axiom.snapDegrees(playerYaw);
		if (snapped == 0f) return original.copy();

		AffineTransform transform = new AffineTransform().rotateY(-snapped);
		StructureRotation rotation = getStructureRotation(snapped);

		BluePrint rotated = new BluePrint(original.getStructureName());

		for (RelativeBlock block : original.getBlocks()) {
			BlockVector3 vec = BlockVector3.at(block.getX(), block.getY(), block.getZ());
			BlockVector3 rotatedVec = transform.apply(vec.toVector3()).toBlockPoint();

			BlockData blockData = block.getBlockData().clone();
			blockData.rotate(rotation);

			rotated.getBlocks().add(new RelativeBlock(
					rotatedVec.x(),
					rotatedVec.y(),
					rotatedVec.z(),
					block.getMaterial(),
					blockData
			));

			rotated.getNeededMaterial().merge(block.getMaterial(), 1, Integer::sum);
			rotated.getMaterialTypes().add(block.getMaterial());
		}

		return rotated;
	}

	private static org.bukkit.block.structure.StructureRotation getStructureRotation(float snappedAngle) {
		if (snappedAngle == 90f) return org.bukkit.block.structure.StructureRotation.CLOCKWISE_90;
		if (snappedAngle == 180f) return org.bukkit.block.structure.StructureRotation.CLOCKWISE_180;
		if (snappedAngle == -90f || snappedAngle == 270f) return org.bukkit.block.structure.StructureRotation.COUNTERCLOCKWISE_90;
		return org.bukkit.block.structure.StructureRotation.NONE;
	}
}
