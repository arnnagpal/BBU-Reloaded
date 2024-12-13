package me.imoltres.bbu.utils.general

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.world.Position
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace


/**
 * Useful utility methods relating to blocks
 */
class BlockUtils {

    companion object {
        val faces = arrayOf(
                BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST,
                BlockFace.UP,
                BlockFace.DOWN
        )

        /**
         * Retrieve the faces touching the certain block within a set of faces
         *
         * @param faces list of faces to check
         * @param block block to look around
         * @return set of blocks around the inputted block in terms of the faces provided
         */
        @JvmStatic
        fun getFacesTouching(faces: Array<BlockFace>, block: Block): Set<Block> {
            val facesTouching = HashSet<Block>()
            for (face in faces) {
                val b = block.getRelative(face)
                if (b.isSolid || b.isLiquid) {
                    facesTouching.add(b)
                }
            }

            return facesTouching
        }

        /**
         * Checks if the original liquid is going to cause a generation
         * in cobble within the surrounding faces
         *
         * @param faces list of faces to check
         * @param originalType original block's material (water / lava)
         * @param toBlock the block that the original fluid is flowing towards
         *
         * @return set of blocks around the inputted block in terms of the faces provided
         */
        fun generatesCobble(faces: Array<BlockFace>, originalType: Material, toBlock: Block): Boolean {
            val mirrorType = if (originalType == Material.WATER) Material.LAVA else Material.WATER
            var gensCobble = false
            var cobbleBlock: Block? = null

            for (face in faces) {
                val r = toBlock.getRelative(face, 1)
                if (r.type == mirrorType) {
                    gensCobble = true
                    cobbleBlock = r
                    break
                }
            }

            if (gensCobble) {
                for (face in faces) {
                    val beacon = cobbleBlock!!.getRelative(face)
                    if (BBU.getInstance().teamController.getTeam(beacon) != null) {
                        return true
                    }
                }
            }
            return false
        }

    }

}