package org.lazberry.xmaslegacy.blueprint.config;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.blueprint.RelativeBlock;
import org.lazberry.xmaslegacy.utils.Axiom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Slf4j
public class BlueprintSchematicManager {

	public static boolean saveSchematic(File file, Location pos1, Location pos2, Location origin) {
		try {
			if (file.getParentFile() != null && !file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			var world = BukkitAdapter.adapt(pos1.getWorld());
			var region = new CuboidRegion(world, BukkitAdapter.asBlockVector(pos1), BukkitAdapter.asBlockVector(pos2));
			var clipboard = new BlockArrayClipboard(region);
			clipboard.setOrigin(BukkitAdapter.asBlockVector(origin));

			try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
				ForwardExtentCopy copy = new ForwardExtentCopy(
						editSession, region, clipboard, region.getMinimumPoint()
				);
				Operations.complete(copy);
			}

			ClipboardFormat format = ClipboardFormats.findByFile(file);
			if (format == null) {
				format = BuiltInClipboardFormat.SPONGE_V1_SCHEMATIC;
			}

			try (ClipboardWriter writer = format.getWriter(new FileOutputStream(file))) {
				writer.write(clipboard);
			}
			return true;
		} catch (Exception e) {
			log.error("Failed to save blueprint file.", e);
			return false;
		}
	}

	public static BluePrint loadAndCreateBlueprint(File file, String structureName, float playerYaw) {
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		if (format == null) return null;

		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			Clipboard clipboard = reader.read();

			float snapped = Axiom.snapDegrees(playerYaw);

			AffineTransform transform = new AffineTransform().rotateY(-snapped);
			StructureRotation rotation = getStructureRotation(snapped);

			BluePrint blueprint = new BluePrint(structureName);
			BlockVector3 origin = clipboard.getOrigin();

			for (BlockVector3 vec : clipboard.getRegion()) {
				BlockState state = clipboard.getBlock(vec);
				if (state.getBlockType().getMaterial().isAir()) continue;

				BlockVector3 relVec = vec.subtract(origin);
				BlockVector3 rotatedVec = transform.apply(relVec.toVector3()).toBlockPoint();

				BlockData blockData = BukkitAdapter.adapt(state);
				blockData.rotate(rotation);
				Material material = blockData.getMaterial();

				blueprint.getBlocks().add(new RelativeBlock(
						rotatedVec.x(),
						rotatedVec.y(),
						rotatedVec.z(),
						material,
						blockData
				));

				blueprint.getNeededMaterial().merge(material, 1, Integer::sum);
				blueprint.getMaterialTypes().add(material);
			}

			return blueprint;
		} catch (Exception e) {
			log.error("Failed to load blueprint file.", e);
			return null;
		}
	}

	public static BluePrint getRotatedBlueprint(BluePrint original, float playerYaw) {
		float snapped = Axiom.snapDegrees(playerYaw);
		if (snapped == 0f) return original.copy();

		AffineTransform transform = new AffineTransform().rotateY(-snapped);
		StructureRotation rotation = getStructureRotation(snapped);

		BluePrint rotated = new BluePrint(original.getStructureName());

		for (RelativeBlock block : original.getBlocks()) {
			BlockVector3 vec = BlockVector3.at(block.getX(), block.getY(), block.getZ());
			BlockVector3 rotatedVec = transform.apply(vec.toVector3()).toBlockPoint();

			BlockData blockData = block.getBlockData();
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
